package com.cpsc312.finalproject.paintchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";
    private static final int EXTERNAL_STORAGE_WRITE_CODE = 1;

    private DrawView drawView;

    /**
     * onCreate
     * @param savedInstanceState savedInstanceState Bundle (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.post(new Runnable() {
            /**
             * Makes sure that the height and width are given to the Bitmap only once the layout is created
             * Without using a Runnable, they would be 0 at this point
             */
            @Override
            public void run() {
                int height = drawView.getHeight();
                int width = drawView.getWidth();
                int mode = getIntent().getIntExtra("mode", 0);
                String path = getIntent().getStringExtra("file_path");
//                Log.d(TAG, "run: " + mode);
                drawView.init(height, width, mode, path);
            }
        });

        if (!isExternalStorageWritable()) {
            findViewById(R.id.saveMenuItem).setEnabled(false);
            Toast.makeText(this, getResources().getString(R.string.no_save_permissions), Toast.LENGTH_SHORT).show();
        }

        SeekBar brushSizeSeekBar = (SeekBar) findViewById(R.id.brushSizeSeekBar);
        final TextView brushSizeTextView = (TextView) findViewById(R.id.brushSizeTextView);
        brushSizeTextView.setText(Integer.toString(drawView.getBrushSize()));
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * Runs whenever the SeekBar is changed
             * @param seekBar SeekBar View
             * @param i new value of the SeekBar
             * @param b whether or not the change came from the user
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                drawView.setBrushSize(i);
                brushSizeTextView.setText(Integer.toString(i));
            }

            /**
             * Runs whenever the SeekBar is first pressed
             * @param seekBar SeekBar View
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
             * Runs whenever the SeekBar is released
             * @param seekBar SeekBar View
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Initializes the options menu
     * @param menu Menu to be displayed
     * @return true if the menu should be displayed, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_draw, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Runs when a menu item is selected
     * @param item which menu item was selected
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.shareMenuItem:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mmsto:"));
                intent.setType("image/*");
                drawView.saveLastImage(); // ensure the file exists
                intent.putExtra("subject", "New PaintChat Message");
                File file = new File(getFilesDir(), getResources().getString(R.string.last_image_filename));
                Uri uri = FileProvider.getUriForFile(DrawActivity.this, "com.cpsc312.finalproject.paintchat.fileprovider", file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(intent);
                return true;
            case R.id.saveMenuItem:
                return saveToFile();
            case R.id.clearMenuItem:
                drawView.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Runs when the user clicks the undo button
     * @param view undo button
     */
    public void onClickUndo(View view) {
        drawView.undo();
    }

    /**
     * Runs when the user clicks the redo button
     * @param view redo button
     */
    public void onClickRedo(View view) {
        drawView.redo();
    }

    /**
     * Saves a file to external storage, requesting permission when necessary
     * @return true if the image was saved successfully, false otherwise
     */
    private boolean saveToFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, EXTERNAL_STORAGE_WRITE_CODE);
            return false;
        } else {
            // permissions granted by this point
            SaveImageAsyncTask saveImageAsyncTask = new SaveImageAsyncTask();
            saveImageAsyncTask.execute(drawView);
            return true;
        }
    }

    /**
     * Checks whether or not the external storage is writable (ex. full SD card)
     * @return true if the external storage is writable, false otherwise
     */
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Runs when the user selects a color
     * @param view which color ImageView was clicked
     */
    public void onColorClicked(View view) {
        String color = view.getTag().toString();

        switch (color) {
            case "red":
                drawView.setCurrentColor(getResources().getColor(R.color.red));
                break;
            case "orange":
                drawView.setCurrentColor(getResources().getColor(R.color.orange));
                break;
            case "yellow":
                drawView.setCurrentColor(getResources().getColor(R.color.yellow));
                break;
            case "green":
                drawView.setCurrentColor(getResources().getColor(R.color.green));
                break;
            case "blue":
                drawView.setCurrentColor(getResources().getColor(R.color.blue));
                break;
            case "purple":
                drawView.setCurrentColor(getResources().getColor(R.color.purple));
                break;
            case "pink":
                drawView.setCurrentColor(getResources().getColor(R.color.pink));
                break;
            case "brown":
                drawView.setCurrentColor(getResources().getColor(R.color.brown));
                break;
            case "black":
                drawView.setCurrentColor(getResources().getColor(R.color.black));
                break;
            case "gray":
                drawView.setCurrentColor(getResources().getColor(R.color.gray));
                break;
            case "white":
                drawView.setCurrentColor(getResources().getColor(R.color.white));
                break;
            default:
                drawView.setCurrentColor(getResources().getColor(R.color.black));
                break;
        }
    }

    /**
     * AsyncTask which saves images to external storage in the background to reduce UI lag
     */
    private class SaveImageAsyncTask extends AsyncTask<DrawView, Void, Boolean> {
        @Override
        protected Boolean doInBackground(DrawView... drawViews) {
            return drawViews[0].saveToFile();
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            if (isSuccessful) {
                Toast.makeText(DrawActivity.this, getResources().getString(R.string.file_save_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DrawActivity.this, getResources().getString(R.string.file_save_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * AsyncTask which saves images to internal storage in the background to reduce UI lag
     */
    private class SaveLastImageAsyncTask extends AsyncTask<DrawView, Void, Boolean> {
        @Override
        protected Boolean doInBackground(DrawView... drawViews) {
            return drawViews[0].saveLastImage();
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            if (!isSuccessful) {
                Toast.makeText(DrawActivity.this, getResources().getString(R.string.file_save_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Runs when the user responds to a request for permission (API 23 and up)
     * @param requestCode which permission request this is
     * @param permissions permission(s) being requested
     * @param grantResults whether or not the permission(s) were granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_WRITE_CODE) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                saveToFile();
            } else {
                findViewById(R.id.saveMenuItem).setEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.no_save_permissions), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Saves the current image to be the most recent image
     */
    @Override
    protected void onStop() {
        SaveLastImageAsyncTask saveLastImageAsyncTask = new SaveLastImageAsyncTask();
        saveLastImageAsyncTask.execute(drawView);
        super.onStop();
    }
}
