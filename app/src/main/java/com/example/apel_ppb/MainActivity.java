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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        Button seeAllButton = findViewById(R.id.button_see_all);
        RecyclerView articleBannerRecyclerView = findViewById(R.id.article_banner_recycler_view);

        // Set up RecyclerView for horizontal banners
        articleBannerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Article> previewArticles = getSampleArticles();
        ArticleBannerAdapter adapter = new ArticleBannerAdapter(previewArticles);
        articleBannerRecyclerView.setAdapter(adapter);

        photoCaptureButton.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        galleryButton.setOnClickListener(v -> openGalleryWithPermissionCheck());

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ArticleFilterActivity.class);
            startActivity(intent);
        });
    }

    // Adapter for RecyclerView
    private class ArticleBannerAdapter extends RecyclerView.Adapter<ArticleBannerAdapter.ViewHolder> {
        private List<Article> articles;

        public ArticleBannerAdapter(List<Article> articles) {
            this.articles = articles;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Article article = articles.get(position);
            holder.titleTextView.setText(article.getTitle());
            holder.bannerImage.setImageResource(article.getImageResId());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LearnMoreActivity.class);
                intent.putExtra("url", article.getUrl());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            ImageView bannerImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.banner_title);
                bannerImage = itemView.findViewById(R.id.banner_image);
            }
        }
    }

    // Fungsi untuk mengambil sample artikel
    private List<Article> getSampleArticles() {
        List<Article> articles = new ArrayList<>();
        articles.add(new Article("Jarang Bertemu Dokter Berkat Apel", "fruit", "https://www.alodokter.com/jarang-bertemu-dokter-berkat-manfaat-apel ", R.drawable.apple_benefits));
        articles.add(new Article("All Apple Varities", "fruit", "https://waapple.org/varieties/all/ ", R.drawable.apple_types));
        articles.add(new Article("Common Apple Tree Disease and How to Treat Them", "diseases,care_tips", "https://plantmegreen.com/blogs/news/common-apple-tree-diseases-how-to-treat-them?srsltid=AfmBOooFE4wrS-Tddrn0GryYVECBxSY_Iy9EglfHgIEZLXM1QBTx26Xg ", R.drawable.apple_diseases));
        return articles;
    }

    // CAMERA PERMISSION & OPEN CAMERA
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
                        getPackageName() + ".provider",
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

    // GALLERY PERMISSION & OPEN GALLERY
    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        } else if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Izin akses kamera ditolak", Toast.LENGTH_SHORT).show();
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