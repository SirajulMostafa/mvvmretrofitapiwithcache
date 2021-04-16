 package com.mostafa.mvvmretrofitapiwithcache.viewmodels;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.mostafa.mvvmretrofitapiwithcache.R;
import com.mostafa.mvvmretrofitapiwithcache.adapters.OnRecipeListener;

 public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView title, publisher, socialScore;
    public AppCompatImageView image;
    OnRecipeListener onRecipeListener;

    public RecipeViewHolder(@NonNull View itemView, OnRecipeListener onRecipeListener) {
        super(itemView);

        this.onRecipeListener = onRecipeListener;

        title = itemView.findViewById(R.id.recipe_title);
        publisher = itemView.findViewById(R.id.recipe_publisher);
        socialScore = itemView.findViewById(R.id.recipe_social_score);
        image = itemView.findViewById(R.id.recipe_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onRecipeListener.onRecipeClick(getAdapterPosition());
    }
}





