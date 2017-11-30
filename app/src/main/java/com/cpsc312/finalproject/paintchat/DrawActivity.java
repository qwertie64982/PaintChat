package com.cpsc312.finalproject.paintchat;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class DrawActivity extends AppCompatActivity {

    // max todo
    // colors
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
                SaveImageAsyncTask saveImageAsyncTask = new SaveImageAsyncTask();
                saveImageAsyncTask.execute(drawView);
                return true;
            case R.id.clearMenuItem:
                drawView.clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
            } else {
                Toast.makeText(DrawActivity.this, "Error: File could not be saved", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
