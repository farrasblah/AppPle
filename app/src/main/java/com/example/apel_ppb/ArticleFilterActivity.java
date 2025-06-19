package com.example.apel_ppb;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArticleFilterActivity extends AppCompatActivity {

    private ArticleAdapter adapter;
    private List<Article> allArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_filter);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_articles);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        allArticles = getSampleArticles();
        adapter = new ArticleAdapter(filterArticles("all"));
        recyclerView.setAdapter(adapter);

        Button allButton = findViewById(R.id.button_all);
        Button fruitButton = findViewById(R.id.button_fruit);
        Button diseasesButton = findViewById(R.id.button_diseases);
        Button careTipsButton = findViewById(R.id.button_care_tips);
        ImageButton backButton = findViewById(R.id.back_button);

        allButton.setOnClickListener(v -> updateArticles("all"));
        fruitButton.setOnClickListener(v -> updateArticles("fruit"));
        diseasesButton.setOnClickListener(v -> updateArticles("diseases"));
        careTipsButton.setOnClickListener(v -> updateArticles("care_tips"));
        backButton.setOnClickListener(v -> finish()); // Navigate back to previous activity

    }

    private List<Article> getSampleArticles() {
        List<Article> articles = new ArrayList<>();
        articles.add(new Article("Jarang Bertemu Dokter Berkat Apel", "fruit", "https://www.alodokter.com/jarang-bertemu-dokter-berkat-manfaat-apel ", R.drawable.apple_benefits));
        articles.add(new Article("All Apple Varities", "fruit", "https://waapple.org/varieties/all/ ", R.drawable.apple_types));
        articles.add(new Article("Common Apple Tree Disease and How to Treat Them", "diseases,care_tips", "https://plantmegreen.com/blogs/news/common-apple-tree-diseases-how-to-treat-them?srsltid=AfmBOooFE4wrS-Tddrn0GryYVECBxSY_Iy9EglfHgIEZLXM1QBTx26Xg ", R.drawable.apple_diseases));

        // THE FRUITS
        articles.add(new Article("An Apple a Day Keeps the Doctor Away — Fact or Fiction?", "fruit", "https://www.healthline.com/nutrition/an-apple-a-day-keeps-the-doctor-away", R.drawable.doc));
        articles.add(new Article("Mengenal Arti An Apple a Day Keeps the Doctor Away", "fruit", "https://hellosehat.com/nutrisi/fakta-gizi/an-apple-a-day-keeps-the-doctor-away/", R.drawable.hello));
        articles.add(new Article("The 61 Best Apple Recipes for Fall and Beyond", "fruit", "https://www.foodnetwork.com/recipes/photos/apple-recipes", R.drawable.recipe));
        articles.add(new Article("10 Resep olahan apel kekinian, unik, lezat, dan menyehatkan", "fruit", "https://www.briliofood.net/resep/10-resep-olahan-apel-kekinian-unik-lezat-dan-menyehatkan-201006h.html", R.drawable.olah));
        articles.add(new Article("10 Popular Types of Apples—and Which Ones Are Best for Baking and Snacking", "fruit", "https://www.marthastewart.com/types-of-apples-8347829", R.drawable.ten));

        // DISEASES
        articles.add(new Article("Ketahui, Ini 4 Penyakit yang Menyerang Tanaman Apel", "diseases", "https://www.kompas.com/homey/read/2022/09/04/095956376/ketahui-ini-4-penyakit-yang-menyerang-tanaman-apel", R.drawable.kompas));
        articles.add(new Article("3 Penyakit yang Menyerang Tanaman Apel dan Cara Mengobatinya", "diseases", "https://www.idntimes.com/science/discovery/penyakit-yang-menyerang-tanaman-apel-1-00-kkpdh-mvr2j8", R.drawable.idn));
        articles.add(new Article("Penyakit Busuk Buah Apel: Gejala, Penyebab, Penanggulangan, dan Pencegahannya", "diseases", "https://gdm.id/penyakit-busuk-buah-apel/", R.drawable.gdm));
        articles.add(new Article("Apple Diseases", "diseases", "https://www.canr.msu.edu/apples/pest_management/diseases", R.drawable.dis));
        articles.add(new Article("Apple Pests", "diseases", "https://extension.usu.edu/planthealth/ipm/notes_ag/fruit-list-apple", R.drawable.usu));

        // CARE_TIPS
        articles.add(new Article("How to Grow Apple Trees From Seed: 3 Steps", "care_tips", "https://www.instructables.com/How-to-grow-apple-trees-from-seed/", R.drawable.how));
        articles.add(new Article("Tips for Growing Apple Trees", "care_tips", "https://www.finegardening.com/project-guides/fruits-and-vegetables/tips-for-growing-apple-trees?srsltid=AfmBOoqPLscY_ToZrPRQSUEtY1fztxAGh-FK8ELBcy6rxRIJ2DaX9p2O", R.drawable.tree));
        articles.add(new Article("Growing apples in the home garden", "care_tips", "https://extension.umn.edu/fruit/growing-apples", R.drawable.garden));
        articles.add(new Article("How to Grow Apples from Seed", "care_tips", "https://www.haxnicks.co.uk/blogs/grow-at-home/apples-from-seed?srsltid=AfmBOopBrvXAciGOVjoRoGs_5xn7LiwUJm_hohcAeLWcrU-BrW8vGIL4", R.drawable.seed));
        return articles;
    }

    private List<Article> filterArticles(String category) {
        List<Article> filtered = new ArrayList<>();
        for (Article article : allArticles) {
            if (category.equals("all") || article.getCategory().contains(category)) {
                filtered.add(article);
            }
        }
        return filtered;
    }

    private void updateArticles(String category) {
        adapter.updateArticles(filterArticles(category));
    }

    private class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
        private List<Article> articles;

        public ArticleAdapter(List<Article> articles) {
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
                Intent intent = new Intent(ArticleFilterActivity.this, LearnMoreActivity.class);
                intent.putExtra("url", article.getUrl());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        public void updateArticles(List<Article> newArticles) {
            this.articles = newArticles;
            notifyDataSetChanged();
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
}