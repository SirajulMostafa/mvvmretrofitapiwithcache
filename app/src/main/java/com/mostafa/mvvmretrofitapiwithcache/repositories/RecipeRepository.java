package com.mostafa.mvvmretrofitapiwithcache.repositories;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.mostafa.mvvmretrofitapiwithcache.AppExecutors;
import com.mostafa.mvvmretrofitapiwithcache.models.Recipe;
import com.mostafa.mvvmretrofitapiwithcache.persistence.RecipeDao;
import com.mostafa.mvvmretrofitapiwithcache.persistence.RecipeDatabase;
import com.mostafa.mvvmretrofitapiwithcache.requests.ServiceGenerator;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.ApiResponse;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.RecipeResponse;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.RecipeSearchResponse;
import com.mostafa.mvvmretrofitapiwithcache.util.Constants;
import com.mostafa.mvvmretrofitapiwithcache.util.NetWorkBoundResource;
import com.mostafa.mvvmretrofitapiwithcache.util.Resource;

import java.util.List;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static RecipeRepository instance;
    private RecipeDao recipeDao;

    public static RecipeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeRepository(context);
        }
        return instance;
    }

    private RecipeRepository(Context context) {
        recipeDao = RecipeDatabase.getInstance(context).getRecipeDao();
    }

    //NetWorkBoundResource(cache,api/response)
    public LiveData<Resource<List<Recipe>>> searchRecipesApi(final String query, final int pageNumber) {
        return new NetWorkBoundResource<List<Recipe>, RecipeSearchResponse>(AppExecutors.getInstance()) {

            @Override
            public void saveCallResult(@NonNull RecipeSearchResponse item) {
                if (item.getRecipes() != null) {// recipe will be null if API KEY is expired
                    Recipe[] recipes = new Recipe[item.getRecipes().size()];
                    int index = 0;
                   for (long rowid: recipeDao.insertRecipes((Recipe[])(item.getRecipes().toArray(recipes)))){//insert into database cache and update if id is  already exist/ when rowid=-1
                       if(rowid == -1){
                           Log.d(TAG, "saveCallResult: CONFLICT... This recipe is already in the cache");
                           // if the recipe already exists... I don't want to set the ingredients or timestamp b/c
                           // they will be erased
                           recipeDao.updateRecipe(
                                   recipes[index].getRecipe_id(),
                                   recipes[index].getTitle(),
                                   recipes[index].getPublisher(),
                                   recipes[index].getImage_url(),
                                   recipes[index].getSocial_rank()
                           );
                       }
                       index++;
                   }
                }
            }

            @Override
            public boolean shouldFetch(@Nullable List<Recipe> data) {
//                return false;
                return true;//want to refresh everytime
            }

            @NonNull
            @Override
            public LiveData<List<Recipe>> loadFromDb() {
                return recipeDao.searchRecipes(query, pageNumber);
            }

            @NonNull
            @Override
            public LiveData<ApiResponse<RecipeSearchResponse>> createCall() {
               return ServiceGenerator.getRecipeApi()
               .searchRecipe(Constants.API_KEY,
                       query,
                       String.valueOf(pageNumber)
               );
                //return null;
            }
        }.getAsLiveData();
    }
    public LiveData<Resource<Recipe>> searchRecipeApi(final String recipeId){
        return new NetWorkBoundResource<Recipe, RecipeResponse>(AppExecutors.getInstance()){
            @Override
            protected void saveCallResult(@NonNull RecipeResponse item) {
                // will be null if API key is expired
                if(item.getRecipe() != null){
                    item.getRecipe().setTimestamp((int)(System.currentTimeMillis() / 1000));
                    recipeDao.insertRecipe(item.getRecipe());
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable Recipe data) {
                Log.d(TAG, "shouldFetch: recipe: " + data.toString());
                int currentTime = (int)(System.currentTimeMillis() / 1000);
                Log.d(TAG, "shouldFetch: current time: " + currentTime);
                int lastRefresh = data.getTimestamp();
                Log.d(TAG, "shouldFetch: last refresh: " + lastRefresh);
                Log.d(TAG, "shouldFetch: it's been " + ((currentTime - lastRefresh) / 60 / 60 / 24) +
                        " days since this recipe was refreshed. 30 days must elapse before refreshing. ");
                if((currentTime - data.getTimestamp()) >= Constants.RECIPE_REFRESH_TIME){
                    Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE?! " + true);
                    return true;
                }
                Log.d(TAG, "shouldFetch: SHOULD REFRESH RECIPE?! " + false);
                return false;
            }

            @NonNull
            @Override
            protected LiveData<Recipe> loadFromDb() {
                return recipeDao.getRecipe(recipeId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RecipeResponse>> createCall() {
                return ServiceGenerator.getRecipeApi().getRecipe(
                        Constants.API_KEY,
                        recipeId
                );
            }
        }.getAsLiveData();
    }
}
