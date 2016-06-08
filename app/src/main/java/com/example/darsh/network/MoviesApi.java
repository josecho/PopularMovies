package com.example.darsh.network;

import com.example.darsh.model.Movie;
import com.example.darsh.model.MoviesList;
import com.example.darsh.model.MovieVideos;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by darshan on 4/4/16.
 */
public class MoviesApi {
    public interface PopularMovies {
        @GET("movie/popular")
        Call<MoviesList> getPopularMovies(@Query("page") Integer page);
    }

    public interface TopRatedMovies {
        @GET("movie/top_rated")
        Call<MoviesList> getTopRatedMovies(@Query("page") Integer page);
    }

    public interface MovieDetails {
        @GET("movie/{id}")
        Call<Movie> getMovieDetails(@Path("id") Integer id);
    }

    public interface MovieDetailVideos {
        @GET("movie/{id}/videos")
        Call<MovieVideos> getMovieVideos(@Path("id") Integer id);
    }
}
