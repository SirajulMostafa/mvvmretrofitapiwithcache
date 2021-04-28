package com.mostafa.mvvmretrofitapiwithcache.requests;
import androidx.lifecycle.LiveData;

import com.mostafa.mvvmretrofitapiwithcache.requests.responses.ApiResponse;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.RecipeResponse;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.RecipeSearchResponse;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApi {

    // SEARCH
    @GET("api/search")
    LiveData<ApiResponse<RecipeSearchResponse>> searchRecipe(
            @Query("key") String key,
            @Query("q") String query,
            @Query("page") String page
    );

    // GET RECIPE REQUEST
    @GET("api/get")
    LiveData<ApiResponse<RecipeResponse>> getRecipe(
            @Query("key") String key,
            @Query("rId") String recipe_id
    );
}
