package com.mostafa.mvvmretrofitapiwithcache.util;

public class Constants {

    public static final String BASE_URL = "https://recipesapi.herokuapp.com/";

    // YOU NEED YOUR OWN API KEY!!!!!!!!!!!!! https://www.food2fork.com/about/api
    public static final String API_KEY = "dadc63b6325aaf398163b40fea9b5e79";
    public static final int CONNECTION_TIMEOUT = 10;//10 SECOND
    public static final int READ_TIMEOUT  = 2;
    public static final int WRITE_TIMEOUT = 2;
    public static final int RECIPE_REFRESH_TIME = 60*60*24*30 ;//30days


    public static final String[] DEFAULT_SEARCH_CATEGORIES =
            {"barbecue", "Breakfast", "Chicken", "Beef", "Brunch", "Dinner", "Wine", "Italian"};

    public static final String[] DEFAULT_SEARCH_CATEGORY_IMAGES =
            {
                    "barbecue",
                    "breakfast",
                    "chicken",
                    "beef",
                    "brunch",
                    "dinner",
                    "wine",
                    "italian"
            };
}
