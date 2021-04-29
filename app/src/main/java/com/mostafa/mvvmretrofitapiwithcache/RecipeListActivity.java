package com.mostafa.mvvmretrofitapiwithcache;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mostafa.mvvmretrofitapiwithcache.adapters.OnRecipeListener;
import com.mostafa.mvvmretrofitapiwithcache.adapters.RecipeRecyclerAdapter;
import com.mostafa.mvvmretrofitapiwithcache.util.Testing;
import com.mostafa.mvvmretrofitapiwithcache.util.VerticalSpacingItemDecorator;
import com.mostafa.mvvmretrofitapiwithcache.viewmodels.RecipeListViewModel;

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
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
    }
        private void subscribeObservers(){
        mRecipeListViewModel.getRecipes().observe(this,listResource -> {
            if (listResource !=null){
                Log.d(TAG,"onChange: status: "+listResource.status);
                if (listResource.data !=null){
                    Testing.printRecipes(listResource.data,"data");
                }
            }
        });

        mRecipeListViewModel.getViewState().observe(this, new Observer<RecipeListViewModel.ViewState>() {
            @Override
            public void onChanged(RecipeListViewModel.ViewState viewState) {
                if (viewState !=null){
                    switch (viewState){
                        case RECIPES:{
                            //recipe will show automatically  from another observer
                            Toast.makeText(RecipeListActivity.this, "recipes", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case CATEGORIES:{
                            Toast.makeText(RecipeListActivity.this, "categories", Toast.LENGTH_SHORT).show();
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
    private  void searchRecipesApi(String q){
        mRecipeListViewModel.searchRecipesApi(q,1);

    }

    private void initRecyclerView(){
        mAdapter = new RecipeRecyclerAdapter(this);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initSearchView(){
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

}

















