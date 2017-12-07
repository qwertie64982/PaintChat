package com.cpsc312.finalproject.paintchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

// Would not have been possible to start this without:
// https://android.jlelse.eu/learn-to-create-a-paint-application-for-android-5b16968063f8

public class DrawView extends AppCompatImageView {

    // TODO: Instead of using a solid background color, make it a texture

    private  final int DEFAULT_COLOR = getResources().getColor(R.color.black);
    private  final int DEFAULT_BG_COLOR = getResources().getColor(R.color.white);
    private static final float TOUCH_TOLERANCE = 4;

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private ArrayList<FingerPath> paths = new ArrayList<>(); // list of FingerPaths the user made
    private ArrayList<FingerPath> undonePaths = new ArrayList<>(); // list of undos (in case of redo)

    private int brushSize = 20;
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;

    private Path path;
    private float x, y;

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true); // smoothes edges of what is being drawn
        paint.setDither(true); // makes colors look better on devices with less colors
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE); // draw strokes, don't fill the objects we draw
        paint.setStrokeJoin(Paint.Join.ROUND); // round corners where stroke segments join
        paint.setStrokeCap(Paint.Cap.ROUND); // draw with a circle shaped "brush"
        paint.setXfermode(null); // we want the default Xfermode
        paint.setAlpha(0xff); // the alpha channel for all colors should be 100% (not transparent)
    }

    public void init(int height, int width) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // each pixel stored in 4B
        canvas = new Canvas(bitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = brushSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save(); // save where the other paths previously were
        this.canvas.drawColor(backgroundColor); // overwrite everything with the background
        // TODO: Draw the background image here?

        for (FingerPath fingerPath : paths) { // iterate and draw the new paths
            paint.setColor(fingerPath.color);
            paint.setStrokeWidth(fingerPath.strokeWidth);
            paint.setMaskFilter(null);

            this.canvas.drawPath(fingerPath.path, paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore(); // restore the ones that were already here
    }

    private void touchStart(float x, float y) {
        // make a new Path object, put it in paths, and start where the user touched
        path = new Path();
        FingerPath fingerPath = new FingerPath(currentColor, strokeWidth, path);
        paths.add(fingerPath);

        path.reset();
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touchMove(float x, float y) {
        // update x and y as the user moves their finger
        float dx = Math.abs(x - this.x); // x difference = abs(new x - old x)
        float dy = Math.abs(y - this.y); // y difference = abs(new y - old y)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // if the finger moves too fast, estimate with a bezier curve
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp() {
        // end drawn line wherever the user lifts the finger
        path.lineTo(this.x, this.y);
    }

    // runs when something happens with the touch screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void undo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.cannot_undo), Toast.LENGTH_SHORT).show();
        }
    }

    public void redo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.cannot_redo), Toast.LENGTH_SHORT).show();
        }
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    // use for later for keeping track of the last image
    public boolean saveLastImage() {
        try {
            // Create file
            File file = new File(getContext().getFilesDir(), getResources().getString(R.string.last_image_filename));
            file.createNewFile();

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            // Convert byte array to file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    public int getBrushSize() {
        return brushSize;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
        strokeWidth = this.brushSize;
    }

    public boolean saveToFile() { // Is it bad to save a file from a View? Maybe, who knows.
        ensureParentDirectory();

        try {
            // Create file
            // DIRECTORY_PICTURES/PaintChat/yyyy-MM-DD-HHmmss.png
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/" + getResources().getString(R.string.app_name) + "/";
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS");
            String filename = df.format(calendar.getTime()) + ".png";

            Log.d(TAG, "saveToFile: " + path + filename);

            File file = new File(path, filename);
            file.createNewFile();

            // Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            // Convert byte array to file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void ensureParentDirectory() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/" + getResources().getString(R.string.app_name) + "/";
        File file = new File(path);
        file.mkdir();

        Log.d(TAG, "ensureParentDirectory: Ensuring directory " + path + " exists");

        if (!file.mkdirs()) { // Check to make sure it made a directory within Pictures
            Log.d(TAG, "ensureParentDirectory: Created new directory");
        }
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        undonePaths.clear();
        invalidate();
    }
}
