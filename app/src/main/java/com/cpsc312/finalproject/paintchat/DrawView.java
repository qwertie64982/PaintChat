package com.cpsc312.finalproject.paintchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class DrawView extends View {

    // TODO: Instead of using a solid background color, make it a texture

    // Final variables
    private  final int DEFAULT_COLOR = getResources().getColor(R.color.black);
    private  final int DEFAULT_BG_COLOR = getResources().getColor(R.color.white);
    private static final float TOUCH_TOLERANCE = 4;

    // ID numbers
    private static final int MODE_BLANK = 0;
    private static final int MODE_LAST = 1;
    private static final int MODE_EXTERNAL = 2;
    private static final int MODE_CAMERA = 3;

    // Class variables (structure)
    private int mode;
    private Paint paint;
    private Bitmap bitmap;
    private Bitmap imageBitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);
    private ArrayList<FingerPath> paths = new ArrayList<>(); // list of FingerPaths the user made
    private ArrayList<FingerPath> undonePaths = new ArrayList<>(); // list of undos (in case of redo)

    // Class variables (user)
    private int brushSize = 20;
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;

    // Class variables (input)
    private Path path;
    private float x, y;

    /**
     * Default value constructor
     * @param context Activity or Fragment where this View resides
     */
    public DrawView(Context context) {
        super(context);
    }

    /**
     * Explicit value constructor
     * @param context Activity or Fragment where this View resides
     */
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

    /**
     * Initializer method
     * Sets the editing mode, creates the bitmap, and sets the brush color and size
     * @param height height of DrawView
     * @param width width of DrawView
     * @param mode editing mode (ex. whether or not to try to load an image)
     * @param path where to load an image from (ex. internal or external storage)
     */
    public void init(int height, int width, int mode, String path) {
        this.mode = mode;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); // each pixel stored in 4B
        canvas = new Canvas(bitmap);
        if (mode == MODE_EXTERNAL || mode == MODE_CAMERA || mode == MODE_LAST) {
//            imageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rainbow), width, height, false); // debug only
            imageBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path), width, height, false);
            canvas.drawBitmap(imageBitmap, 0, 0, null);
        }

        currentColor = DEFAULT_COLOR;
        strokeWidth = brushSize;
    }

    /**
     * Runs when the DrawView must be redrawn on the screen
     * @param canvas holds the draw calls for the bitmap
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save(); // save where the other paths previously were
//        Log.d(TAG, "onDraw: " + mode);
        if (mode == MODE_BLANK) {
            // overwrite everything with the background color
            this.canvas.drawColor(backgroundColor);
        } else {
            // overwrite everything with the background image
            this.canvas.drawBitmap(imageBitmap, 0, 0, null);
        }

        for (FingerPath fingerPath : paths) { // iterate and draw the new paths
            paint.setColor(fingerPath.color);
            paint.setStrokeWidth(fingerPath.strokeWidth);
            paint.setMaskFilter(null);

            this.canvas.drawPath(fingerPath.path, paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore(); // restore the ones that were already here
    }

    /**
     * Runs whenever the user first touches the screen
     * @param x x coordinate within the DrawView
     * @param y y coordinate within the DrawView
     */
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

    /**
     * Runs whenever the user moves their finger
     * @param x x coordinate within the DrawView
     * @param y y coordinate within the DrawView
     */
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

    /**
     * Runs whenever the user lifts their finger
     */
    private void touchUp() {
        // end drawn line wherever the user lifts the finger
        path.lineTo(this.x, this.y);
    }

    /**
     * Runs whenever the user does something with the touch screen
     * @param event what the user did with the touch screen
     * @return true if the event was handled, false otherwise
     */
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

    /**
     * Undoes the last stroke the user made
     */
    public void undo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.cannot_undo), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Redoes the last stroke the user undid
     */
    public void redo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.cannot_redo), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Changes the brush color
     * @param currentColor color ID (not through R)
     */
    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    /**
     * Saves the image to internal storage
     * Used when the user wants to load their most recent image
     * @return true if the image saved successfully, false otherwise
     */
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

    /**
     * Saves the image to external storage
     * Requests permission if needed
     * @return true if the image saved successfully, false otherwise
     */
    public boolean saveToFile() {
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
            file.createNewFile(); // temporary file

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

    /**
     * Ensures that /Pictures/PaintChat exists - if it does not, it creates it
     * By this point, it is assumed that permission is granted, since this runs within saveToFile()
     */
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

    /**
     * Brush size getter
     * @return brush size
     */
    public int getBrushSize() {
        return brushSize;
    }

    /**
     * Brush size setter
     * @param brushSize
     */
    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
        strokeWidth = this.brushSize;
    }

    /**
     * Clears all the user's strokes
     */
    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        undonePaths.clear();
        invalidate();
    }
}
