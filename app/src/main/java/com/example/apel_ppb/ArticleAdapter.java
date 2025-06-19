package com.example.apel_ppb;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articles;

    public ArticleAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.titleTextView.setText(article.getTitle());
        // Tambahkan click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), LearnMoreActivity.class);
            intent.putExtra("url", article.getUrl()); // Kirim URL artikel
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(android.R.id.text1);
        }
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }
}