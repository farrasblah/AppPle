package com.example.apel_ppb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GALLERY_PERMISSION = 100;
    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 102;
    private static final int REQUEST_CODE_CAMERA = 103;

    private Uri photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        LinearLayout photoCaptureButton = findViewById(R.id.button_photo_capture);
        LinearLayout galleryButton = findViewById(R.id.button_gallery);
        Button learnMoreButton = findViewById(R.id.button_learn_more);
        Button aboutButton = findViewById(R.id.button_about);

        photoCaptureButton.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        galleryButton.setOnClickListener(v -> openGalleryWithPermissionCheck());

        learnMoreButton.setOnClickListener(v -> {
            Intent learnMoreIntent = new Intent(MainActivity.this, LearnMoreActivity.class);
            startActivity(learnMoreIntent);
        });

        aboutButton.setOnClickListener(v -> {
            // TODO: Tambahkan logika tombol "About AppPle"
        });
    }
//  CAMERA PERMISSION & OPEN CAMERA
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE_CAMERA_PERMISSION);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Membuat file gambar agar hasil kamera tersimpan
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal membuat file foto", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider",  // pastikan ini sesuai di AndroidManifest.xml
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
            } else {
                Toast.makeText(this, "Gagal membuat file foto", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Tidak ada aplikasi kamera yang tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
//    GALLERY PERMISSION & OPEN GALLERY
    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ tidak perlu permission khusus untuk baca gambar
            return true;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_CODE_GALLERY_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Tidak perlu minta permission di Android 10 ke atas
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_GALLERY_PERMISSION);
        }
    }

    private void openGalleryWithPermissionCheck() {
        if (hasGalleryPermission()) {
            openGallery();
        } else {
            requestGalleryPermission();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Izin akses galeri ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, CheckingImages.class);

            if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    intent.putExtra("image_uri", selectedImageUri.toString());
                    intent.putExtra("source", "gallery");
                    startActivity(intent);
                }
            } else if (requestCode == REQUEST_CODE_CAMERA && photoUri != null) {
                intent.putExtra("image_uri", photoUri.toString());
                intent.putExtra("source", "camera");
                startActivity(intent);
            }
        }
    }
}
