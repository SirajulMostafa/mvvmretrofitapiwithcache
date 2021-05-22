package com.mostafa.mvvmretrofitapiwithcache.viewmodels;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mostafa.mvvmretrofitapiwithcache.models.Recipe;
import com.mostafa.mvvmretrofitapiwithcache.repositories.RecipeRepository;
import com.mostafa.mvvmretrofitapiwithcache.util.Resource;

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository repository;


    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository=RecipeRepository.getInstance(application);

    }

public LiveData<Resource<Recipe>> searchRecipeApi(String recipeId){
        return repository.searchRecipeApi(recipeId);
}

}





















