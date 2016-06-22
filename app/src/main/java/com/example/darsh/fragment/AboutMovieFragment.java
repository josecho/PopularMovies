package com.example.darsh.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.darsh.adapter.BackdropMovieImagesAdapter;
import com.example.darsh.helper.Constants;
import com.example.darsh.model.MovieImage;
import com.example.darsh.model.MovieImages;
import com.example.darsh.network.TmdbRestClient;
import com.example.darsh.popularmovies.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by darshan on 22/6/16.
 */
public class AboutMovieFragment extends Fragment {
    private int movieId;
    private String movieTitle;

    private ArrayList<MovieImage> movieImages;
    private BackdropMovieImagesAdapter adapter;

    private String tagLine;
    private String overview;

    private String cast;
    private String director;

    private TextView textViewCast;
    private TextView textViewDirector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            movieId = getArguments().getInt(Constants.BUNDLE_ID);
            movieTitle = getArguments().getString(Constants.BUNDLE_TITLE);
            tagLine = getArguments().getString(Constants.BUNDLE_TAG_LINE);
            overview = getArguments().getString(Constants.BUNDLE_OVERVIEW);

        } else {
            movieId = savedInstanceState.getInt(Constants.BUNDLE_ID);
            movieTitle = savedInstanceState.getString(Constants.BUNDLE_TITLE);
            tagLine = savedInstanceState.getString(Constants.BUNDLE_TAG_LINE);
            overview = savedInstanceState.getString(Constants.BUNDLE_OVERVIEW);
            movieImages = savedInstanceState.getParcelableArrayList(Constants.BUNDLE_IMAGES);
            cast = savedInstanceState.getString(Constants.BUNDLE_CAST);
            director = savedInstanceState.getString(Constants.BUNDLE_DIRECTOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_about_movie, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(movieTitle);

        adapter = new BackdropMovieImagesAdapter(getFragmentManager());
        if (movieImages != null) {
            adapter.addMovieImages(movieImages);
        }
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager_movie_images);
        viewPager.setAdapter(adapter);

        TextView textViewTagLine = (TextView) view.findViewById(R.id.text_view_tag_line);
        textViewTagLine.setText(tagLine);

        TextView textViewOverview = (TextView) view.findViewById(R.id.text_view_overview);
        textViewOverview.setText(overview);

        textViewCast = (TextView) view.findViewById(R.id.text_view_cast);
        textViewDirector = (TextView) view.findViewById(R.id.text_view_director);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadImages();
        loadCredits();
    }

    private void loadImages() {
        Call<MovieImages> call = TmdbRestClient.getInstance()
                .getMovieBackdropImagesImpl()
                .getMovieBackdropImages(movieId);
        Callback<MovieImages> callback = new Callback<MovieImages>() {
            private MovieImage movieImage;

            @Override
            public void onResponse(Call<MovieImages> call, Response<MovieImages> response) {
                if (!response.isSuccessful() ||
                        response.body().getMovieImages().size() == 0) {
                    movieImage = new MovieImage();
                    update();
                }
                movieImages = response.body().getMovieImages();
                update();
            }

            @Override
            public void onFailure(Call<MovieImages> call, Throwable t) {
                movieImage = new MovieImage();
                update();
            }

            private void update() {
                if (movieImage != null) {
                    movieImages = new ArrayList<>();
                    movieImages.add(movieImage);

                    movieImage = null;
                }
                setupViewPager();
            }
        };
        call.enqueue(callback);
    }

    private void setupViewPager() {
        adapter.addMovieImages(movieImages);
        adapter.notifyDataSetChanged();
    }

    private void loadCredits() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.BUNDLE_ID, movieId);
        outState.putString(Constants.BUNDLE_TITLE, movieTitle);
        outState.putString(Constants.BUNDLE_TAG_LINE, tagLine);
        outState.putString(Constants.BUNDLE_OVERVIEW, overview);
        outState.putParcelableArrayList(Constants.BUNDLE_IMAGES, movieImages);
        outState.putString(Constants.BUNDLE_CAST, cast);
        outState.putString(Constants.BUNDLE_DIRECTOR, director);
    }
}
