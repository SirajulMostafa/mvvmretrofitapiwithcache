package com.mostafa.mvvmretrofitapiwithcache.util;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.mostafa.mvvmretrofitapiwithcache.AppExecutors;
import com.mostafa.mvvmretrofitapiwithcache.requests.responses.ApiResponse;

//CacheObject: Type for Resource data
//RequestObject: Type for API response.(Network request)
public abstract class NetWorkBoundResource<CacheObject, RequestObject> {
    private static final String TAG = "NetWorkBoundResource";
    private final AppExecutors appExecutors;
    private final MediatorLiveData<Resource<CacheObject>> results = new MediatorLiveData<>();

    public NetWorkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();
    }

    private void init() {
        //when app is running first job is looking data from database
        //update LiveData for loading status
        //loading() method is called when there view the cache retrive the cache
        //ofcourse the first time app has no cache so loading(null) is set for cache
        results.setValue(Resource.loading(null));
        //observe LiveData source from local db
//       //* 1) observe local db
        final LiveData<CacheObject> dbSource = loadFromDb();
        //dbSource if the data is changes  the onChanged method is fired
        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(CacheObject cacheObject) {
                results.removeSource(dbSource);//if i don't remove dbSource it is continuing  to listening
//              //  * 2)if <condition> query the network</condition>
                if (shouldFetch(cacheObject)) {//whether the fetch for not
                    //if the method return true thn step *3)
                    //get data from the network
                    fetchFromNetwork(dbSource);
                } else {
                    //otherwise view the data from the database cache.
                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(CacheObject cacheObject) {
                            setValue(Resource.success(cacheObject));
                        }
                    });
                }
            }
        });

    }

    /**
     * 1) observe local db
     * 2)if <condition> query the network</condition>
     * 3) stop observing the local db
     * 4)insert new data into database
     * 5) begin observing local db again to see the refresh data from network
     *
     * @param dbSource
     */
    private void fetchFromNetwork(final LiveData<CacheObject> dbSource) {
        //step 4 and step5
        // update LiveData  for loading status
        results.addSource(dbSource, new Observer<CacheObject>() {
            @Override
            public void onChanged(CacheObject cacheObject) {
                setValue(Resource.loading(cacheObject));
            }
        });
        final LiveData<ApiResponse<RequestObject>> apiResponseLiveData = createCall();
        results.addSource(apiResponseLiveData, new Observer<ApiResponse<RequestObject>>() {
            @Override
            public void onChanged(ApiResponse<RequestObject> requestObjectApiResponse) {
                results.removeSource(dbSource);
                results.removeSource(apiResponseLiveData);
                /*
                3 cases:
                1) apiSuccessResponse
                1) apiErrorResponse
                1) apiEmptyResponse
                 */
                if (requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse) {
                    Log.d(TAG, "onChanged: ApiSuccessResponse.");
                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            //save the response tot the local db
//                            saveCallResult(null);
                            saveCallResult(processResponse((ApiResponse.ApiSuccessResponse) requestObjectApiResponse));
                            //used thread diskIo bcz need background thread
                            //if we use it main thread the app will crushed
                            appExecutors.mainThread().execute(new Runnable() {
                                @Override
                                public void run() {
                                    results.addSource(loadFromDb(), new Observer<CacheObject>() {
                                        @Override
                                        public void onChanged(CacheObject cacheObject) {
                                            setValue(Resource.success(cacheObject));
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else if (requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse) {
                    Log.d(TAG, "onChanged: ApiEmptyResponse");
                    appExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            results.addSource(loadFromDb(), new Observer<CacheObject>() {
                                @Override
                                public void onChanged(CacheObject cacheObject) {
                                    setValue(Resource.success(cacheObject));
                                }
                            });
                        }
                    });
                } else if (requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse) {
                    Log.d(TAG, "onChanged: ApiErrorResponse ");
                    results.addSource(dbSource, new Observer<CacheObject>() {
                        @Override
                        public void onChanged(CacheObject cacheObject) {
                            setValue(
                                    Resource.error(
                                            ((ApiResponse.ApiErrorResponse) requestObjectApiResponse).getErrorMessage(),
                                    cacheObject
                                    )
                        );
                        }
                    });
                }
            }
        });

    }

    private RequestObject processResponse(ApiResponse.ApiSuccessResponse apiSuccessResponse) {
        return (RequestObject) apiSuccessResponse.getBody();
    }

    //postValue() is background thread//post value wont posted immediately
//setValue() is mainThread thread// set value immediately
    private void setValue(Resource<CacheObject> newValue) {
        if ((results.getValue() != newValue)) {
            results.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    public abstract void saveCallResult(@NonNull RequestObject item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    public abstract boolean shouldFetch(@Nullable CacheObject data);

    // Called to get the cached data from the database.
    @NonNull
    @MainThread
    public abstract LiveData<CacheObject> loadFromDb();

    // Called to create the API call.
    @NonNull
    @MainThread
    public abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<CacheObject>> getAsLiveData() {
        return results;
    }

}
