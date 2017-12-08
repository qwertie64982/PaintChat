package com.cpsc312.finalproject.paintchat;

        import android.Manifest;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.net.Uri;
        import android.os.AsyncTask;
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
    static final int EXTERNAL_STORAGE_READ_CODE = 0;
    static final int CAMERA_CODE = 1;
    static final int REQUEST_CAMERA = 2;

    private static final int MODE_BLANK = 0;
    private static final int MODE_LAST = 1;
    private static final int MODE_EXTERNAL = 2;
    private static final int MODE_CAMERA = 3;

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
        Button blankImageButton = (Button) findViewById(R.id.startNewImageButton);
        Button resumeImageButton = (Button) findViewById(R.id.resumeLastImageButton);
        Button newPhotoButton = (Button) findViewById(R.id.takeNewPhotoButton);
        Button existingPhotoButton = (Button) findViewById(R.id.useExistingPhotoButton);

        // wire onClickListeners
        blankImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStartNewImageClicked();
            }
        });

        resumeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResumeLastPhotoClicked();
            }
        });

        newPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakeNewPhotoClicked();
            }
        });

        existingPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExternalPhotoClicked();
            }
        });
    }

    public void onStartNewImageClicked() {
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

    public void onExternalPhotoClicked() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, EXTERNAL_STORAGE_READ_CODE);
        } else {
            // permission granted by this point
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.new_image_alert_title)
                    .setMessage(R.string.new_image_alert_body)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                            intent.putExtra("mode", MODE_EXTERNAL);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog newImageDialog = builder.create();
            newImageDialog.show();
        }
    }

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

    private void onResumeLastPhotoClicked() {
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
                intent.putExtra("mode", MODE_CAMERA);
                intent.putExtra("file_path", mCurrentPhotoPath);
                startActivity(intent);
            }
        }
    }

//    private class CameraAsyncTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... voids) {
//            return null;
//        }
//    }
}
