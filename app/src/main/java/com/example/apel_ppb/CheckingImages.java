package com.example.apel_ppb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.*;

public class CheckingImages extends AppCompatActivity {

    private static final int REQUEST_CODE_CAMERA = 103;
    private Uri imageUri;
    private File photoFile;
    private ImageView imageView;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_images);

        imageView = findViewById(R.id.image_preview);
        Button scanButton = findViewById(R.id.button_scan);
        Button editButton = findViewById(R.id.button_edit);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imageView.setImageURI(imageUri);
                        scanImage(imageUri);
                    }
                });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
                        scanImage(imageUri);
                    }
                });

        Intent intent = getIntent();
        if (intent != null) {
            String uriString = intent.getStringExtra("image_uri");
            String source = intent.getStringExtra("source");

            if (uriString != null) {
                imageUri = Uri.parse(uriString);
                imageView.setImageURI(imageUri);
            }

            editButton.setOnClickListener(v -> {
                if ("camera".equals(source)) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    try {
                        photoFile = createImageFile();
                        if (photoFile != null) {
                            imageUri = FileProvider.getUriForFile(this,
                                    getPackageName() + ".provider",
                                    photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            cameraLauncher.launch(cameraIntent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Intent intentGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intentGallery.setType("image/*");
                    galleryLauncher.launch(intentGallery);
                }
            });
        }

        scanButton.setOnClickListener(view -> {
            if (imageUri != null) {
                scanImage(imageUri);
            }
        });
    }

    private void scanImage(Uri uri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();

                RequestBody fileBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg", fileBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://serverless.roboflow.com/ppb-project/5?api_key=lBFhNCZuHY29NeaOwtNU")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    runOnUiThread(() -> {
                        TextView resultText = findViewById(R.id.result_text);
                        resultText.setText(jsonResponse);
                        drawBoundingBoxes(jsonResponse, uri);
                    });
                } else {
                    runOnUiThread(() -> {
                        TextView resultText = findViewById(R.id.result_text);
                        resultText.setText("Scan failed: " + response.code() + "\n" + response.message());
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

    private void drawBoundingBoxes(String jsonResponse, Uri uri) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray predictions = jsonObject.getJSONArray("predictions");
            JSONObject imageInfo = jsonObject.getJSONObject("image");

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            bitmap = handleExifOrientation(uri, bitmap); // 💡 Koreksi orientasi gambar

            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);

            float originalWidth = (float) imageInfo.getInt("width");
            float originalHeight = (float) imageInfo.getInt("height");
            float scaleX = bitmap.getWidth() / originalWidth;
            float scaleY = bitmap.getHeight() / originalHeight;

            if (predictions.length() == 0) {
                // Display "Ops! No Apple Found!" message
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setTextSize(100f);
                float textWidth = paint.measureText("Ops! No Apple Found!");
                canvas.drawText("Ops! No Apple Found!",
                        (bitmap.getWidth() - textWidth) / 2,
                        bitmap.getHeight() / 2,
                        paint);
            } else {
                for (int i = 0; i < predictions.length(); i++) {
                    JSONObject prediction = predictions.getJSONObject(i);
                    float x = (float) prediction.getDouble("x");
                    float y = (float) prediction.getDouble("y");
                    float width = (float) prediction.getDouble("width");
                    float height = (float) prediction.getDouble("height");
                    String className = prediction.getString("class");
                    float confidence = (float) prediction.getDouble("confidence");

                    float left = (x - width / 2) * scaleX;
                    float top = (y - height / 2) * scaleY;
                    float right = (x + width / 2) * scaleX;
                    float bottom = (y + height / 2) * scaleY;

                    paint.setColor("sehat".equals(className) ? Color.GREEN : Color.RED);
                    RectF rect = new RectF(left, top, right, bottom);
                    canvas.drawRect(rect, paint);

                    paint.setColor(Color.WHITE);
                    paint.setTextSize(80f);
                    canvas.drawText(String.format("%s (%.2f%%)", className, confidence * 100), left, top - 20, paint);
                }
            }

            runOnUiThread(() -> imageView.setImageBitmap(mutableBitmap));
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                TextView resultText = findViewById(R.id.result_text);
                resultText.setText("Error drawing boxes: " + e.getMessage());
            });
        }
    }

    private Bitmap handleExifOrientation(Uri imageUri, Bitmap bitmap) {
        try {
            InputStream input = getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}