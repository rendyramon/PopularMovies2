package com.github.nikhilbhutani.popularmovies2.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.nikhilbhutani.popularmovies2.BuildConfig;
import com.github.nikhilbhutani.popularmovies2.R;
import com.github.nikhilbhutani.popularmovies2.adapters.MovieRecyclerViewAdapter;
import com.github.nikhilbhutani.popularmovies2.models.MovieList;
import com.github.nikhilbhutani.popularmovies2.models.Movie;
import com.github.nikhilbhutani.popularmovies2.models.MoviesTableTable;
import com.github.nikhilbhutani.popularmovies2.network.ApiClient;
import com.github.nikhilbhutani.popularmovies2.network.ApiInterface;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nikhil Bhutani on 8/16/2016.
 */
public class MoviesFragment extends Fragment {


    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private MovieRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ApiInterface apiInterface;
    List<Movie> movieList;
    Call<MovieList> movieCall;
    View view;
    NetworkInfo networkInfo;
    ProgressDialog progressDialog;
    private boolean mTwoPane = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        toolbar.setTitle(R.string.title_popular_movies);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.main);

        if (getActivity().findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.action_popular) {

                    progressDialog.show();
                    movieList.clear();
                    recyclerViewAdapter.notifyDataSetChanged();
                    movieCall = apiInterface.getPopularMovies(BuildConfig.API_KEY);
                    asyncCallForMovies();
                    toolbar.setTitle(R.string.title_popular_movies);

                } else if (id == R.id.action_topRated) {

                    progressDialog.show();
                    movieList.clear();
                    recyclerViewAdapter.notifyDataSetChanged();
                    movieCall = apiInterface.getTopRatedMovies(BuildConfig.API_KEY);
                    asyncCallForMovies();
                    toolbar.setTitle(R.string.title_topRated_movies);

                } else if (id == R.id.action_favorites) {
                    movieList.clear();
                    recyclerViewAdapter.notifyDataSetChanged();
                    movieList = MoviesTableTable.getRows(getActivity().getContentResolver().query(MoviesTableTable.CONTENT_URI,
                            null, null, null, null), true);
                    recyclerViewAdapter = new MovieRecyclerViewAdapter(getActivity(), movieList, mTwoPane);
                    recyclerView.setAdapter(recyclerViewAdapter);
                    toolbar.setTitle(R.string.title_favorite_movies);
                }
                return true;
            }
        });


        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        //   recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(getActivity(), 2);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(), 3);
        }
        recyclerView.setLayoutManager(mLayoutManager);

        //To ensure that movie call is Asynchronous
        movieCall = apiInterface.getPopularMovies(BuildConfig.API_KEY);

        if (savedInstanceState == null || !savedInstanceState.containsKey("Movies")) {
            progressDialog.show();
            asyncCallForMovies();

        } else {
            movieList = savedInstanceState.getParcelableArrayList("Movies");
            recyclerViewAdapter = new MovieRecyclerViewAdapter(getActivity(), movieList, mTwoPane);
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerViewAdapter);
        }

    }

    private void asyncCallForMovies() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            progressDialog.dismiss();
            Snackbar.make(view, R.string.check_network, Snackbar.LENGTH_SHORT).show();
        } else {
            movieCall.enqueue(new Callback<MovieList>() {
                @Override
                public void onResponse(Call<MovieList> call, Response<MovieList> response) {

                    MovieList allMovieResponse = response.body();


                    if (allMovieResponse != null) {
                        movieList = allMovieResponse.getResults();
                    }
                    progressDialog.dismiss();
                    recyclerViewAdapter = new MovieRecyclerViewAdapter(getActivity(), movieList, mTwoPane);
                    recyclerView.setAdapter(recyclerViewAdapter);

                }

                @Override
                public void onFailure(Call<MovieList> call, Throwable t) {

                    Log.d("OnFailure", "Failed to get movies");

                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {

            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("Movies", (ArrayList<? extends Parcelable>) movieList);
    }


}
