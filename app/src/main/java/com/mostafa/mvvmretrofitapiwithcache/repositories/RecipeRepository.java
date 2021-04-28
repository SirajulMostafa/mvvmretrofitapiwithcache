package com.mostafa.mvvmretrofitapiwithcache.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.mostafa.mvvmretrofitapiwithcache.AppExecutors;
import com.mostafa.mvvmretrofitapiwithcache.models.Recipe;
import com.mostafa.mvvmretrofitapiwithcache.persistence.RecipeDao;
import com.mostafa.mvvmretrofitapiwithcache.persistence.RecipeDatabase;
import com.mostafa.mvvmretrofitapiwithcache.requests.ServiceGenerator;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.ApiResponse;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.RecipeSearchResponse;
import com.mostafa.mvvmretrofitapiwithcache.util.Constants;
import com.mostafa.mvvmretrofitapiwithcache.util.NetWorkBoundResource;
import com.mostafa.mvvmretrofitapiwithcache.util.Resource;

import java.util.List;

public class RecipeRepository {
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

}
