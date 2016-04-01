package com.dgreenhalgh.android.largemargebackground;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import rx.Observable;

public interface GettyService {

    @Headers("Api-Key: 2ec9jyzqe8tdf7u35e6eh5nk")
    @GET("search/images")
    Observable<GettyResponse> listGettyImages(@Query("fields") String fields,
                                                @Query("page_size") int pageSize,
                                                @Query("phrase") String location);
}
