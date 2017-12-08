package com.cpsc312.finalproject.paintchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_GALLERY = 2;
    String mCurrentPhotoPath;

    //TODO: Make Buttons Prettier
    /*  TODO: Wire buttons -
        resumeImageButton
        newPhotoButton
        existingPhotoButton
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference buttons
        Button newImageButton = (Button) findViewById(R.id.startNewImageButton);
        Button resumeImageButton = (Button) findViewById(R.id.resumeLastImageButton);
        Button newPhotoButton = (Button) findViewById(R.id.takeNewPhotoButton);
        Button existingPhotoButton = (Button) findViewById(R.id.useExistingPhotoButton);

        // wire onClickListeners
        newImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewImageClicked();
            }
        });
        newPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakeNewPhotoClicked();
            }
        });
    }



    public void onStartNewImageClicked(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.newImageAlertTitle)
                .setMessage(R.string.newImageAlertBody)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                        intent.putExtra("mode", 0);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog newImageDialog = builder.create();
        newImageDialog.show();
    }

    public void onTakeNewPhotoClicked(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.newPhotoAlertTitle)
                .setMessage(R.string.newImageAlertBody)
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
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog newPhotoDialog = builder.create();
        newPhotoDialog.show();
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_NEW_PHOTO";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK){
            if (requestCode == REQUEST_CAMERA){
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                intent.putExtra("mode", 3);
                intent.putExtra("file_path", mCurrentPhotoPath);
                startActivity(intent);
            }
        }
    }
}



