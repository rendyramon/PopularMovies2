package com.github.nikhilbhutani.popularmovies2.fragments;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.nikhilbhutani.popularmovies2.R;
import com.github.nikhilbhutani.popularmovies2.adapters.MovieRecyclerViewAdapter;
import com.github.nikhilbhutani.popularmovies2.models.MovieList;
import com.github.nikhilbhutani.popularmovies2.models.Movie;
import com.github.nikhilbhutani.popularmovies2.network.ApiClient;
import com.github.nikhilbhutani.popularmovies2.network.ApiInterface;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Nikhil Bhutani on 8/16/2016.
 */
public class MoviesFragment extends Fragment {

    private MovieRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ApiInterface apiInterface;
    RecyclerView recyclerView;
    List<Movie> movieList;
    Toolbar toolbar;
    Call<MovieList> movieCall;
    View view;
    ProgressDialog progressDialog;

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

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Popular Movies");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.main);


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.action_popular) {

                    progressDialog.show();
                    movieCall = apiInterface.getPopularMovies("Enter API Key");
                    asyncCallForMovies();
                    toolbar.setTitle("Popular Movies");

                } else if (id == R.id.action_topRated) {

                    progressDialog.show();
                    movieCall = apiInterface.getTopRatedMovies("Enter API Key");
                    asyncCallForMovies();
                    toolbar.setTitle("Top Rated Movies");
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
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
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
        movieCall = apiInterface.getPopularMovies("Enter API Key");

        if (savedInstanceState == null || !savedInstanceState.containsKey("Movies")) {
            progressDialog.show();
            asyncCallForMovies();

        } else {
            movieList = savedInstanceState.getParcelableArrayList("Movies");
            recyclerViewAdapter = new MovieRecyclerViewAdapter(getActivity(), movieList);
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(recyclerViewAdapter);
        }

    }

    private void asyncCallForMovies() {
        movieCall.enqueue(new Callback<MovieList>() {
            @Override
            public void onResponse(Call<MovieList> call, Response<MovieList> response) {

                MovieList allMovieResponse = response.body();
                movieList = allMovieResponse.getResults();

                progressDialog.dismiss();
                recyclerViewAdapter = new MovieRecyclerViewAdapter(getActivity(), movieList);
                recyclerView.setAdapter(recyclerViewAdapter);

            }

            @Override
            public void onFailure(Call<MovieList> call, Throwable t) {

                Log.d("OnFailure", "Failed to get movies");

            }
        });
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