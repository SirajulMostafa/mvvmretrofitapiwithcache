package com.mostafa.mvvmretrofitapiwithcache;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.mostafa.mvvmretrofitapiwithcache.adapters.OnRecipeListener;
import com.mostafa.mvvmretrofitapiwithcache.adapters.RecipeRecyclerAdapter;
import com.mostafa.mvvmretrofitapiwithcache.util.Testing;
import com.mostafa.mvvmretrofitapiwithcache.util.VerticalSpacingItemDecorator;
import com.mostafa.mvvmretrofitapiwithcache.viewmodels.RecipeListViewModel;

import static com.mostafa.mvvmretrofitapiwithcache.viewmodels.RecipeListViewModel.QUERY_EXHAUSTED;

public class RecipeListActivity extends BaseActivity implements OnRecipeListener {

    private static final String TAG = "RecipeListActivity";

    private RecipeListViewModel mRecipeListViewModel;
    private RecyclerView mRecyclerView;
    private RecipeRecyclerAdapter mAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        mRecyclerView = findViewById(R.id.recipe_list);
        mSearchView = findViewById(R.id.search_view);

        mRecipeListViewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);

        initRecyclerView();
        initSearchView();
        subscribeObservers();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    private void subscribeObservers() {
        mRecipeListViewModel.getRecipes().observe(this, listResource -> {
            if (listResource != null) {
                Log.d(TAG, "onChange: status: " + listResource.status);
                if (listResource.data != null) {
//                    Testing.printRecipes(listResource.data,"data");
                    // mAdapter.setRecipes(listResource.data);
                    switch (listResource.status) {
                        case LOADING: {
                            if (mRecipeListViewModel.getPageNumber() > 1) {
                                mAdapter.displayLoading();//pagination loading
                            } else {
                                mAdapter.displayOnlyLoading();//if pageNumber 1 it is the time when click category item to listactivity
                            }
                            break;
                        }
                        case ERROR: {
                            Log.e(TAG, "onChanged:  can not refresh the cache.");
                            Log.e(TAG, "onChanged:ERROR message:" + listResource.message);
                            Log.e(TAG, "onChanged: status: ERROR, #recipes:" + listResource.data.size());
                            mAdapter.hideLoading();
                            //after hiding loading animation when set the cache data bellow
                            mAdapter.setRecipes(listResource.data);
                            Toast.makeText(RecipeListActivity.this, listResource.message, Toast.LENGTH_SHORT).show();
                            if (listResource.message.equals(QUERY_EXHAUSTED)) {
                                mAdapter.setQueryExhausted();
                            }
                            break;
                        }
                        case SUCCESS: {
                            Log.d(TAG, "onChanged:  cache has been refreshed");
                            Log.d(TAG, "onChanged: status: SUCCESS #recipes:" + listResource.data.size());
                            mAdapter.hideLoading();

                            mAdapter.setRecipes(listResource.data);

                            break;
                        }
                    }

                }
            }
        });

        mRecipeListViewModel.getViewState().observe(this, new Observer<RecipeListViewModel.ViewState>() {
            @Override
            public void onChanged(RecipeListViewModel.ViewState viewState) {
                if (viewState != null) {
                    switch (viewState) {
                        case RECIPES: {
                            //recipe will show automatically  from another observer
                           // Toast.makeText(RecipeListActivity.this, "recipes", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case CATEGORIES: {
                           // Toast.makeText(RecipeListActivity.this, "categories", Toast.LENGTH_SHORT).show();
                            displaySearchCategories();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void displaySearchCategories() {
        mAdapter.displaySearchCategories();
    }
    private void searchRecipesApi(String q) {
        mRecyclerView.smoothScrollToPosition(0);
        mRecipeListViewModel.searchRecipesApi(q, 1);
        mSearchView.clearFocus();

    }

    private RequestManager initGlide(){
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_mood_bad_black_24dp);
      return   Glide.with(this).setDefaultRequestOptions(options);

    }

    private void initRecyclerView() {
        ViewPreloadSizeProvider<String> viewPreloadSizeProvider = new ViewPreloadSizeProvider<>();
        mAdapter = new RecipeRecyclerAdapter(this,initGlide(),viewPreloadSizeProvider);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewPreloader<String> recyclerViewPreLoader = new RecyclerViewPreloader<String>(
                Glide.with(this),
                mAdapter,
                viewPreloadSizeProvider,
                30
        );

        mRecyclerView.addOnScrollListener(recyclerViewPreLoader);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mRecyclerView.canScrollVertically(1)//if can not scroll vertically then we know we reach bottom of the list 30/page
                        && mRecipeListViewModel.getViewState().getValue() == RecipeListViewModel.ViewState.RECIPES) {//we start search next page only when viewState is recipe
                    mRecipeListViewModel.searchNextPage();
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchRecipesApi(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onRecipeClick(int position) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe", mAdapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        searchRecipesApi(category);
    }

    @Override
    public void onBackPressed() {
        if (mRecipeListViewModel.getViewState().getValue() == RecipeListViewModel.ViewState.CATEGORIES){

            super.onBackPressed();
        }else {
            mRecipeListViewModel.cancelRequest();
            mRecipeListViewModel.setViewCategories();
        }
    }
}

















