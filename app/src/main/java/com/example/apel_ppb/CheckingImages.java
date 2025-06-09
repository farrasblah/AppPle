package com.example.apel_ppb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.FormBody;

public class CheckingImages extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 103;
    private static final int REQUEST_CODE_GALLERY_PERMISSION = 100;

    private String source;
    private Uri imageUri;
    private File photoFile;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_images);
        ImageView imageView = findViewById(R.id.image_preview);
        Button scanButton = findViewById(R.id.button_scan);
        Button editButton = findViewById(R.id.button_edit);

        // Initialize Activity Result Launchers
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ImageView imageViewResult = findViewById(R.id.image_preview);
                        imageViewResult.setImageURI(imageUri);
                    }
                });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        ImageView imageViewResult = findViewById(R.id.image_preview);
                        imageViewResult.setImageURI(imageUri);
                    }
                });

        Intent intent = getIntent();
        if (intent != null) {
            String uriString = intent.getStringExtra("image_uri");
            source = intent.getStringExtra("source");

            if (uriString != null) {
                imageUri = Uri.parse(uriString);
                imageView.setImageURI(imageUri);
            }
        }

        scanButton.setOnClickListener(view -> {
            if (imageUri != null) {
                new Thread(() -> {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();

                        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("image", base64Image)
                                .build();

                        Request request = new Request.Builder()
                                .url("https://serverless.roboflow.com/ppb-project/4?api_key=1BfhNCzUHY29nea0wtNU")
                                .post(requestBody)
                                .build();

                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String jsonResponse = response.body().string();
                            runOnUiThread(() -> {
                                TextView resultText = findViewById(R.id.result_text);
                                resultText.setText(jsonResponse);
                            });
                        } else {
                            runOnUiThread(() -> {
                                TextView resultText = findViewById(R.id.result_text);
                                resultText.setText("Scan failed: " + response.code());
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            TextView resultText = findViewById(R.id.result_text);
                            resultText.setText("Error: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });

        editButton.setOnClickListener(v -> {
            if ("camera".equals(source)) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (photoFile != null) {
                    imageUri = FileProvider.getUriForFile(this,
                            getPackageName() + ".provider",
                            photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    cameraLauncher.launch(cameraIntent);
                }
            } else if ("gallery".equals(source)) {
                Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intentGallery.setType("image/*");
                galleryLauncher.launch(intentGallery);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // This method can be removed since we're using Activity Result API
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}