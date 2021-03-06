package com.github.nikhilbhutani.popularmovies2.network;

import com.github.nikhilbhutani.popularmovies2.models.MovieList;
import com.github.nikhilbhutani.popularmovies2.models.MovieReviewList;
import com.github.nikhilbhutani.popularmovies2.models.MovieVideosList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Nikhil Bhutani on 8/17/2016.
 */
public interface ApiInterface {

    @GET("top_rated?")
    Call<MovieList> getTopRatedMovies(@Query("api_key") String apiKey);
    //http://api.themoviedb.org/3/movie/top_rated?api_key=1234


    @GET("popular?")
    Call<MovieList> getPopularMovies(@Query("api_key") String apiKey);
    //http://api.themoviedb.org/3/movie/popular?api_key=1234

    @GET("{id}/videos?")
    Call<MovieVideosList> getTrailer(@Path("id") int id, @Query("api_key") String apiKey);
    //http://api.themoviedb.org/3/movie/297761/videos?api_key=1234

    @GET("{id}/reviews?")
    Call<MovieReviewList> getReview(@Path("id") int id, @Query("api_key") String apiKey);
    //http://api.themoviedb.org/3/movie/297761/reviews?api_key=1234
}
