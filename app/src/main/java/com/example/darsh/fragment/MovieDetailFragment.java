package com.example.darsh.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.example.darsh.adapter.GenresListAdapter;
import com.example.darsh.adapter.VideosListAdapter;
import com.example.darsh.helper.Constants;
import com.example.darsh.model.Movie;
import com.example.darsh.model.MovieVideo;
import com.example.darsh.model.MovieVideos;
import com.example.darsh.network.TmdbRestClient;
import com.example.darsh.popularmovies.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by darshan on 14/4/16.
 */
public class MovieDetailFragment extends Fragment implements VideosListAdapter.OnVideoClickListener {
    private Movie movie;

    /*
    List of views whose references are required to be updated
    once the network fetches data about them. View references are
    obtained beforehand for a performance gain.
    Movie duration, horizontally scrollable genres genresRecyclerView,
    tag line text are updated after a successful api query.
     */
    private TextView duration;
    private TextView tagLine;
    private RecyclerView genresRecyclerView;
    private RecyclerView videosRecyclerView;

    /*
    Reference to the overview text is stored as absence of tag line
    would leave unused space below the list of movie genres.
    In such a scenario, remove margins of overview text.
     */
    private TextView overview;


    private final String BACKDROP_IMAGE_URL = "http://image.tmdb.org/t/p/w500";
    private final String POSTER_IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            movie = intent.getParcelableExtra(Constants.INTENT_EXTRA_MOVIE);
            loadMovieDetails();
            loadMovieVideos();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(movie.getTitle());

        /*
        Setting up movie detail view
         */
        TextView title = (TextView) view.findViewById(R.id.text_view_title);
        title.setText(movie.getTitle());

        TextView releaseDate = (TextView) view.findViewById(R.id.text_view_release_date);
        releaseDate.setText(movie.getReleaseDate());

        TextView rating = (TextView) view.findViewById(R.id.text_view_rating);
        String voteAverage = Double.toString(movie.getVoteAverage());
        rating.setText(voteAverage);

        overview = (TextView) view.findViewById(R.id.text_view_overview);
        overview.setText(movie.getOverview());

        /*
        Initialise layout of genres RecyclerView beforehand.
        Obtain references to duration and tag line TextViews.
         */
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        genresRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_genres_list);
        genresRecyclerView.setLayoutManager(linearLayoutManager);
        genresRecyclerView.addItemDecoration(new SpacingItemDecoration(
                (int) getResources().getDimension(R.dimen.spacing_genre)));

        duration = (TextView) view.findViewById(R.id.text_view_duration);
        tagLine = (TextView) view.findViewById(R.id.text_view_tag_line);

        /*
        Load backdrop and poster images.
         */
        ImageView backdropImage = (ImageView) view.findViewById(R.id.image_view_backdrop);
        ImageView posterImage = (ImageView) view.findViewById(R.id.image_view_poster);

        Glide.with(view.getContext())
                .load(BACKDROP_IMAGE_URL + movie.getBackdropPath())
                .asBitmap()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .placeholder(R.drawable.image_placeholder)
                .into(backdropImage);

        Glide.with(view.getContext())
                .load(POSTER_IMAGE_URL + movie.getPosterPath())
                .placeholder(R.drawable.image_placeholder)
                .into(posterImage);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videosRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_videos_list);
        videosRecyclerView.setLayoutManager(linearLayoutManager);
        videosRecyclerView.addItemDecoration(new SpacingItemDecoration((int) getResources().getDimension(R.dimen.spacing_genre)));

        return view;
    }

    private void loadMovieDetails() {
        Call<Movie> call = TmdbRestClient.getInstance()
                .getMovieDetailsImpl()
                .getMovieDetails(movie.getId());
        Callback<Movie> callback = new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (!response.isSuccessful()) {
                    movie.setTagLine(getString(R.string.server_error));

                } else {
                    movie.setGenres(response.body().getGenres());
                    movie.setDuration(response.body().getDuration());
                    movie.setTagLine(response.body().getTagLine());
                }
                updateUI();
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                movie.setTagLine(getString(R.string.network_error_movie_detail));
                updateUI();
            }
        };
        call.enqueue(callback);
    }

    private void updateUI() {
        setupGenresList();
        setupAboutMovieView();
    }

    private void setupGenresList() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GenresListAdapter adapter = new GenresListAdapter(movie.getGenres());
                genresRecyclerView.setAdapter(adapter);
                genresRecyclerView.setHasFixedSize(true);
            }
        }, 500);
    }

    private void setupAboutMovieView() {
        String runtime = Integer.toString(movie.getDuration()) + " minutes";
        duration.setText(runtime);

        /*
        If the movie does not contain a tag line,
        remove margins of overview textview to get
        rid of unused space in layout.
         */
        if (movie.getTagLine() == null || movie.getTagLine().length() == 0) {
            tagLine.setVisibility(View.GONE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 0);
            overview.setLayoutParams(layoutParams);
            overview.invalidate();
            return;
        }
        tagLine.setText(movie.getTagLine());
    }

    private void loadMovieVideos() {
        Call<MovieVideos> call = TmdbRestClient.getInstance()
                .getMovieVidoesImpl()
                .getMovieVideos(movie.getId());
        Callback<MovieVideos> callback = new Callback<MovieVideos>() {
            @Override
            public void onResponse(Call<MovieVideos> call, Response<MovieVideos> response) {
                ArrayList<MovieVideo> videos;
                if (response.isSuccessful()) {
                    videos = response.body().getVideos();

                } else {
                    /*
                    If response is unsuccessful, create a temporary
                    MovieVideo object that has an invalid key and
                    error as it's name. This is an easier way to
                    inform user to avoiding switching between a TextView
                    and RecyclerView. Still wondering of an efficient
                    way to handle such cases.
                     */
                    MovieVideo temp = new MovieVideo();
                    temp.setKey("");
                    temp.setName(getString(R.string.error));

                    videos = new ArrayList<>();
                    videos.add(temp);
                }

                movie.setMovieVideos(videos);
                setupMovieVideos();
            }

            @Override
            public void onFailure(Call<MovieVideos> call, Throwable t) {
                /*
                Nothing is done here because other callbacks would also
                fail. Overview text would intimate user about checking
                if device is connected to the internet.
                 */
            }
        };
        call.enqueue(callback);
    }

    @Override
    public void onVideoClick(MovieVideo movieVideo) {
        if (!movieVideo.getSite().equalsIgnoreCase(Constants.YOUTUBE)) {
            return;
        }

        Intent intent;
        /*
        Play video using YouTube app if it exists,
        else default to browser.
        Taken from: http://stackoverflow.com/a/12439378/3946664
         */
        try {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Constants.URI_YOUTUBE_APP + movieVideo.getKey()));
        } catch (ActivityNotFoundException ex) {
            intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Constants.URI_YOUTUBE_BROWSER + movieVideo.getKey()));
        }
        startActivity(intent);
    }

    private void setupMovieVideos() {
        VideosListAdapter adapter = new VideosListAdapter(MovieDetailFragment.this, movie.getMovieVideos());
        videosRecyclerView.setAdapter(adapter);
        videosRecyclerView.setHasFixedSize(true);
    }

    private class SpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spacing;

        public SpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == 0) {
                return;
            }
            outRect.left = spacing;
        }
    }
}
