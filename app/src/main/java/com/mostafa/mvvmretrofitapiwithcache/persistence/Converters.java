package com.mostafa.mvvmretrofitapiwithcache.persistence;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Converters {
    //converting that string to array
    @TypeConverter
    public static String[] fromString(String value) {
        Type listType = new TypeToken<String[]>() {}.getType();
        return new  Gson().fromJson(value,listType);
    }

//converting that array to string
    @TypeConverter
    public static String fromArrayList(String[] list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
