package com.mostafa.mvvmretrofitapiwithcache.requests;



import com.mostafa.mvvmretrofitapiwithcache.util.Constants;
import com.mostafa.mvvmretrofitapiwithcache.util.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.mostafa.mvvmretrofitapiwithcache.util.Constants.CONNECTION_TIMEOUT;
import static com.mostafa.mvvmretrofitapiwithcache.util.Constants.READ_TIMEOUT;
import static com.mostafa.mvvmretrofitapiwithcache.util.Constants.WRITE_TIMEOUT;

public class ServiceGenerator {
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)//establish connection to server
            .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//time b2in each byte read from the server
            .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)//time b2in each byte sent to server
            .retryOnConnectionFailure(false)//stop re try connection
            .build();


    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addCallAdapterFactory( new LiveDataCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    private static RecipeApi recipeApi = retrofit.create(RecipeApi.class);

    public static RecipeApi getRecipeApi(){
        return recipeApi;
    }
}
