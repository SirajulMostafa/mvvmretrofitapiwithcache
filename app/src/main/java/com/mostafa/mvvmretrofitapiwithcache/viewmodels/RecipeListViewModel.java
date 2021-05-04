package com.mostafa.mvvmretrofitapiwithcache.viewmodels;


import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.mostafa.mvvmretrofitapiwithcache.models.Recipe;
import com.mostafa.mvvmretrofitapiwithcache.repositories.RecipeRepository;
import com.mostafa.mvvmretrofitapiwithcache.util.Resource;

import java.util.List;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";
    public static final String QUERY_EXHAUSTED = "No more results.";

    public enum ViewState {CATEGORIES, RECIPES}

    private MutableLiveData<ViewState> viewState;
    private MediatorLiveData<Resource<List<Recipe>>> recipes = new MediatorLiveData<>();
    private RecipeRepository recipeRepository;

    // query extras
    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private String query;
    private boolean cancelRequest;
    private long requestStartTime;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
        init();
    }

    private void init() {
        if (viewState == null) {
            viewState = new MutableLiveData<>();
            viewState.setValue(ViewState.CATEGORIES);
            Log.d(TAG, "init: ---------aa" + viewState);

        } else {
            Log.d(TAG, "init: ---------pp");
        }
    }

    public LiveData<ViewState> getViewState() {
        return viewState;
    }
    public void setViewCategories(){
        viewState.setValue(ViewState.CATEGORIES);
    }

    public LiveData<Resource<List<Recipe>>> getRecipes() {
        return recipes;
    }

    public int getPageNumber(){
        return pageNumber;
    }



    public void searchRecipesApi(String query, int pageNumber){
        if(!isPerformingQuery){
            if(pageNumber == 0){
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            this.query = query;
            isQueryExhausted = false;
            executeSearch();
        }
    }

    public  void searchNextPage(){
        if (!isQueryExhausted && !isPerformingQuery){
            pageNumber++;
            executeSearch();
        }
    }

    private void executeSearch(){
        requestStartTime = System.currentTimeMillis();
        isPerformingQuery = true;
        cancelRequest = false;
        viewState.setValue(ViewState.RECIPES);
        final LiveData<Resource<List<Recipe>>> repositorySource = recipeRepository.searchRecipesApi(query, pageNumber);
        recipes.addSource(repositorySource, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<Recipe>> listResource) {
                if (!cancelRequest) {
                    if (listResource != null) {//continue observing
                        if (listResource.status == Resource.Status.SUCCESS) {
                            Log.d(TAG, "onChanged: REQUEST TIME: " + (System.currentTimeMillis() - requestStartTime) / 1000 + " seconds.");
                            Log.d(TAG, "onChanged: page number: " + pageNumber);
                            Log.d(TAG, "onChanged: " + listResource.data);

                            isPerformingQuery = false;//after success stop query
                            if (listResource.data != null) {
                                if (listResource.data.size() == 0) {//no more result
                                    Log.d(TAG, "onChanged: query is exhausted...");
                                    recipes.setValue(
                                            new Resource<List<Recipe>>(
                                                    Resource.Status.ERROR,
                                                    listResource.data,
                                                    QUERY_EXHAUSTED
                                            )
                                    );
                                    isQueryExhausted=true;
                                }
                            }
                            recipes.removeSource(repositorySource);//if success removeSource
                        }
                        else if (listResource.status == Resource.Status.ERROR) {//if error
                            Log.d(TAG, "onChanged: REQUEST TIME: " + (System.currentTimeMillis() - requestStartTime) / 1000 + " seconds.");
                            isPerformingQuery = false;
                            if (listResource.message.equals(QUERY_EXHAUSTED)){
                                isQueryExhausted = true;
                            }
                            recipes.removeSource(repositorySource);//also removeSource
                        }
                        recipes.setValue(listResource);
                    } else {
                        recipes.removeSource(repositorySource);
                    }
                }else {
                    recipes.removeSource(repositorySource);
                }
            }

        });
    }

    public void cancelRequest() {
       if (isPerformingQuery){
           Log.d(TAG, "cancelRequest: : canceling search request");
           cancelRequest = true;
           isPerformingQuery= false;
           pageNumber = 1;
       }
    }
    //    public void searchRecipesApi(String query, int pageNumber) {
//        final LiveData<Resource<List<Recipe>>> repositorySource = recipeRepository.searchRecipesApi(query, pageNumber);
//        recipes.addSource(repositorySource, new Observer<Resource<List<Recipe>>>() {
//            @Override
//            public void onChanged(Resource<List<Recipe>> listResource) {
//                //react to the data
//                recipes.setValue(listResource);
//            }
//        });
//    }
}















