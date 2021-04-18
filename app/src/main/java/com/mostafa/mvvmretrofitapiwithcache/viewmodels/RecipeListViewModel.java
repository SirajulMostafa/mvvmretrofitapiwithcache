package com.mostafa.mvvmretrofitapiwithcache.viewmodels;


import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";
    public enum ViewState {CATEGORIES, RECIPES};
    private MutableLiveData<ViewState> viewState ;
    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    private void init() {
        if (viewState == null){
            viewState = new MutableLiveData<>();
            viewState.setValue(ViewState.CATEGORIES);
            Log.d(TAG,"init: ---------aa"+ viewState);

        }else {
            Log.d(TAG,"init: ---------pp");
        }
    }

    public MutableLiveData<ViewState> getViewState() {
        return viewState;
    }
}















