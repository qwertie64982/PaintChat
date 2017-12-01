package com.cpsc312.finalproject.paintchat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class DrawActivity extends AppCompatActivity {

    private static final int EXTERNAL_STORAGE_REQUEST_CODE = 1;

    // max todo
    // undo
    // erase
    // external storage

    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        drawView = (DrawView) findViewById(R.id.drawView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        drawView.init(metrics);

        if (!isExternalStorageWritable()) {
            findViewById(R.id.saveMenuItem).setEnabled(false);
            Toast.makeText(this, getResources().getString(R.string.no_save_permissions), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_draw, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.saveMenuItem:
                return saveToFile();
            case R.id.clearMenuItem:
                drawView.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean saveToFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        } else {
            // permissions granted by this point
            SaveImageAsyncTask saveImageAsyncTask = new SaveImageAsyncTask();
            saveImageAsyncTask.execute(drawView);
            return true;
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

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
            default:
                drawView.setCurrentColor(getResources().getColor(R.color.black));
                break;
        }
    }

    private class SaveImageAsyncTask extends AsyncTask<DrawView, Void, Boolean> {
        @Override
        protected Boolean doInBackground(DrawView... drawViews) {
            return drawViews[0].saveToFile();
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            super.onPostExecute(isSuccessful);
            if (isSuccessful) {
                Toast.makeText(DrawActivity.this, "Successfully saved file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                saveToFile();
            } else {
                findViewById(R.id.saveMenuItem).setEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.no_save_permissions), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
