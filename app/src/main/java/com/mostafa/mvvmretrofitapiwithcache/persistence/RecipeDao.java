package com.mostafa.mvvmretrofitapiwithcache.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.mostafa.mvvmretrofitapiwithcache.models.Recipe;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface RecipeDao {
    //... ignore if conflict
    @Insert(onConflict = IGNORE)
    long insertRecipes(Recipe... recipes);

    @Insert(onConflict = REPLACE)
    void insertRecipe(Recipe recipe);

    @Query(" UPDATE recipes SET title = :title,publisher = :publisher, image_url = :image_url, social_rank =:social_rank" +
            " WHERE recipe_id = :publisher")
    LiveData<List<Recipe>> updateRecipe(String recipe_id, String title, String publisher, String image_url, float social_rank);

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%' " +
            " ORDER BY social_rank DESC LIMIT (:pageNumber * 30)")
    LiveData<List<Recipe>> searchRecipes(String query, int pageNumber);
    //0-29 , PAGE 1
    //30-58 ,  PAGE 2
    // PAGE ...

    @Query("SELECT * FROM recipes WHERE recipe_id = :recipe_id")
    LiveData<Recipe> getRecipe (String recipe_id);


}
