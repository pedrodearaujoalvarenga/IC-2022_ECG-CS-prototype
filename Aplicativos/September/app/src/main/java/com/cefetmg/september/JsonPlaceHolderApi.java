package com.cefetmg.september;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET("coletar/posts")
    Call<List<Post>> getPosts();

    @POST("publicar/{id}")
    Call<Post> createPostspecific(@Body Post post, @Path("id") int id);

    @POST("publicar/auto")
    Call<Post> createPostLatest(@Body AutoPost post);


    @DELETE("deletar")
    Call<Void> deleteAllPosts();

    @DELETE("deletar/{id}")
    Call<Void> deleteSpecific(@Path("id") int id);


}