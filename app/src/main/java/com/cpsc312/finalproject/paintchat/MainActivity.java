/**
 * This program runs a paint app, which can import and export images.
 * Final project, CPSC 312
 * Icons from Material.io
 * Would not have been possible without the help from:
 *   - https://android.jlelse.eu/learn-to-create-a-paint-application-for-android-5b16968063f8
 *   - Stack Exchange
 *
 * @author Maxwell Sherman
 *   - DrawView
 *   - Layouts
 *   - Image saving
 *   - Slideshow/documentation
 * @author Andrew Italo
 *   - Image sharing
 *   - Main menu logic
 *   - Using the last image
 *   - Getting the image inside DrawView
 * These were merely areas of focus earlier on in development.
 * A significant portion of the time was spent working together on the same code.
 *
 * @version v1.0
 */
package com.cpsc312.finalproject.paintchat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";
    String mCurrentPhotoPath;

    // ID numbers
    static final int EXTERNAL_STORAGE_READ_CODE = 0;
    static final int CAMERA_CODE = 1;
    static final int REQUEST_CAMERA = 2;
    static final int REQUEST_GALLERY = 3;

    // Mode numbers
    private static final int MODE_BLANK = 0;
    private static final int MODE_LAST = 1;
    private static final int MODE_EXTERNAL = 2;
    private static final int MODE_CAMERA = 3;

    /**
     * onCreate
     * @param savedInstanceState savedInstanceState Bundle (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startNewDrawingButton = (Button) findViewById(R.id.startNewDrawingButton);
        Button resumeLastDrawingButton = (Button) findViewById(R.id.resumeLastDrawingButton);
        Button takeNewPhotoButton = (Button) findViewById(R.id.takeNewPhotoButton);
        Button useExistingPhotoButton = (Button) findViewById(R.id.useExistingPhotoButton);

        startNewDrawingButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickListener for "start new drawing" Button
             * @param view "start new drawing" Button
             */
            @Override
            public void onClick(View view) {
                onStartNewDrawingClicked();
            }
        });

        resumeLastDrawingButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickListener for "resume last drawing" Button
             * @param view "resume last drawing" Button
             */
            @Override
            public void onClick(View view) {
                onResumeLastDrawingClicked();
            }
        });

        takeNewPhotoButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickListener for "take new photo" Button
             * @param view "take new photo" Button
             */
            @Override
            public void onClick(View view) {
                onTakeNewPhotoClicked();
            }
        });

        useExistingPhotoButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickListener for "use existing photo" Button
             * @param view "use existing photo" Button
             */
            @Override
            public void onClick(View view) {
                onExternalPhotoClicked();
            }
        });
    }

    /**
     * Runs when the "start new drawing" Button is clicked
     * Warns the user about overwriting their previous unsaved drawing,
     *     then starts DrawActivity with a blank canvas
     */
    public void onStartNewDrawingClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.new_image_alert_title)
                .setMessage(R.string.new_image_alert_body)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                        intent.putExtra("mode", MODE_BLANK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog newImageDialog = builder.create();
        newImageDialog.show();
    }

    /**
     * Runs when the "resume last drawing" Button is clicked
     * Starts DrawActivity if a previous drawing exists, with this image as the canvas
     */
    private void onResumeLastDrawingClicked() {
        File file = new File(getFilesDir(), getResources().getString(R.string.last_image_filename));
        if (file.exists()) {
            String lastPath = file.getAbsolutePath();
            Intent intent = new Intent(MainActivity.this, DrawActivity.class);
            intent.putExtra("mode", MODE_LAST);
            intent.putExtra("file_path", lastPath);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.file_load_error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Runs when the "take new photo" Button is clicked
     * Warns the user about overwriting their previous unsaved drawing,
     *     then starts the camera, requesting permission as needed.
     * If the camera returns with a picture, then DrawActivity is started with this image as its canvas.
     */
    public void onTakeNewPhotoClicked() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, CAMERA_CODE);
        } else {
            // permission granted by this point
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.new_photo_alert_title)
                    .setMessage(R.string.new_image_alert_body)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    Toast.makeText(MainActivity.this, getString(R.string.file_load_error), Toast.LENGTH_SHORT).show();
                                }
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                            "com.cpsc312.finalproject.paintchat.fileprovider",
                                            photoFile);
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                                }
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog newPhotoDialog = builder.create();
            newPhotoDialog.show();
        }
    }

    /**
     * Runs when the "use existing photo" Button is clicked
     * Warns the user about overwriting their previous unsaved drawing,
     *     then starts the photo picker with an Intent, requesting permission as needed
     * If the user returns with a picture, then DrawActivity is started with this image as its canvas.
     */
    public void onExternalPhotoClicked() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, EXTERNAL_STORAGE_READ_CODE);
        } else {
            // permission granted by this point
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.existing_photo_alert_title)
                    .setMessage(R.string.new_image_alert_body)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, REQUEST_GALLERY);
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog newImageDialog = builder.create();
            newImageDialog.show();
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
        if (requestCode == EXTERNAL_STORAGE_READ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onExternalPhotoClicked();
            } else {
//                findViewById(R.id.useExistingPhotoButton).setEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.no_load_permission), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onTakeNewPhotoClicked();
            } else {
//                findViewById(R.id.takeNewPhotoButton).setEnabled(false);
                Toast.makeText(this, getResources().getString(R.string.no_camera_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Creates an image file from what the camera returns,
     *     which is then moved into the DrawView
     * @return the image
     * @throws IOException if the file could not be created (ex. full SD card)
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_NEW_PHOTO";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, // prefix
                ".jpg",        // suffix
                storageDir     // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Runs when the camera or gallery return with an image (or not)
     * @param requestCode which request started the Activity
     * @param resultCode whether or not they finished successfully
     * @param data data they send back
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK){
            if (requestCode == REQUEST_CAMERA){
                Log.d(TAG, "onActivityResult: mode = " + MODE_CAMERA);
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                intent.putExtra("mode", MODE_CAMERA);
                intent.putExtra("file_path", mCurrentPhotoPath);
                startActivity(intent);
            }
            else if (requestCode == REQUEST_GALLERY && data != null){
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgString = cursor.getString(columnIndex);
                cursor.close();

                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                intent.putExtra("mode", MODE_EXTERNAL);
                intent.putExtra("file_path", imgString);
                startActivity(intent);
            }
        }
    }
}
