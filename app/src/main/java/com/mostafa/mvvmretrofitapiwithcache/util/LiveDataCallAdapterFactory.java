package com.mostafa.mvvmretrofitapiwithcache.util;

import androidx.lifecycle.LiveData;

import com.mostafa.mvvmretrofitapiwithcache.requests.responses.ApiResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class LiveDataCallAdapterFactory extends CallAdapter.Factory {
    /**
     * this method performs a number of check and then returns the response type for the retrofit requests
     * (@bodyType is the ResponseType . it can be RecipeResponse or RecipeReachResponse)
     * CHECK #1) RETURN TYPE RETURN LIVEDATA
     * CHECK #2)  TYPE LiveData<T>is of ApiResponse.class
     * CHECK #3)  Make sure ApiResponse is paramaeterized . AKA:ApiResponse<T> exists.
     *
     */
    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
       //check #1
        //make sure the CallAdapter is returning a type of LiveData
        if (CallAdapter.Factory.getRawType(returnType) != LiveData.class){
            return  null;
        }
        //check #2
        //type that LiveData is wrapping
        //TYPE "LiveData<T> what 'T'is"
        Type observableType = CallAdapter.Factory.getParameterUpperBound(0,(ParameterizedType) returnType);
        //Check if it's of type ApiResponse
        Type rawObservableType = CallAdapter.Factory.getRawType(observableType);
        if (rawObservableType != ApiResponse.class){
            throw new IllegalArgumentException("Type must be a defined Resource");
        }
        //check #3
        //check if ApiResponse is parameterized . AKA: Does ApiResponse<T> exists?(nust be wrap around T)
        //FYI: T is either RecipeResponse or RecipeSearchResponse
        if (!(observableType instanceof ParameterizedType)){
            throw new IllegalArgumentException("resource must be parameterized");
        }
        Type bodyType = CallAdapter.Factory.getParameterUpperBound(0,(ParameterizedType) observableType);
        return new LiveDataCallAdapter<Type>(bodyType);
    }
}
