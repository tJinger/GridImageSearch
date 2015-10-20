package com.walmart.gridimagesearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.walmart.gridimagesearch.EditSettingsFragment;
import com.walmart.gridimagesearch.EndlessScrollListener;
import com.walmart.gridimagesearch.R;
import com.walmart.gridimagesearch.adapters.ImageResultsAdapter;
import com.walmart.gridimagesearch.models.ImageResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private StaggeredGridView gvResults;
    private ArrayList<ImageResult> imageResults;
    private ImageResultsAdapter aImageResults;
    private String size;
    private String color;
    private String type;
    private String site;
    private int sizePerPage = 8;
    private View view;
    JSONArray imageResultJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupViews();

        //Create the data source
        imageResults = new ArrayList<ImageResult>();
        //Attaches the data source to an adapter
        aImageResults = new ImageResultsAdapter(this, imageResults);
        //Link the adapter to the adapterview
        gvResults.setAdapter(aImageResults);

        size = getIntent().getStringExtra("size");
        color = getIntent().getStringExtra("color");
        type = getIntent().getStringExtra("type");
        site = getIntent().getStringExtra("site");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.miSearch);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchUrl = composeSearchUrl(0, query);
                Log.d("DEBUG", searchUrl);
                sendHttpClient(searchUrl,false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private void setupViews() {
        etSearch = (EditText) findViewById(R.id.etSearch);
        gvResults = (StaggeredGridView) findViewById(R.id.gvResults);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //launch the image display activity
                Intent i = new Intent(SearchActivity.this, ImageDisplayActivity.class);
                ImageResult result = imageResults.get(position);
                i.putExtra("result", result);
                startActivity(i);
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                Log.i("Info", "Current page is " + page);
                if (page < 8)
                   customLoadMoreDataFromApi(page);
                // or customLoadMoreDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
    }

    private void customLoadMoreDataFromApi(int page) {
        String query = etSearch.getText().toString();
        String searchUrl = composeSearchUrl(page, query);
        sendHttpClient(searchUrl, true);
    }

    public void onImageSearch(View view) {
        String query = etSearch.getText().toString();
        String searchUrl = composeSearchUrl(0, query);
        Log.d("DEBUG", searchUrl);
        sendHttpClient(searchUrl, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miSetting:
                showEditDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        EditSettingsFragment editSettingsDialog = EditSettingsFragment.newInstance("Advanced search option");
        editSettingsDialog.show(fm, "fragment_edit_name");
    }

    private String composeSearchUrl(int pageIdx, String query) {
        StringBuilder searchUrl = new StringBuilder();
        searchUrl.append("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=");
        searchUrl.append(query);
        searchUrl.append("&start=");
        int startIdx = pageIdx * sizePerPage;
        searchUrl.append(startIdx);
        searchUrl.append("&rsz=");
        searchUrl.append(sizePerPage);
        if (size != null && !size.equals(""))
            searchUrl.append("&imgsz=" + size);
        if (color != null && !color.equals(""))
            searchUrl.append("&imgcolor=" + color);
        if (type != null && !type.equals(""))
            searchUrl.append("&imgtype=" + type);
        if (site != null && !site.equals(""))
            searchUrl.append("&as_sitesearch=" + site);

        return searchUrl.toString();
    }

    private void sendHttpClient(String searchUrl, final boolean loadMore) {
        if (!isNetworkAvailable()) {
            Log.e("ERROR", "Network is not available");
            Toast.makeText(this, "Network is not available", Toast.LENGTH_SHORT);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(searchUrl.toString(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("DEBUG", response.toString());
                try {
                    imageResultJson = response.getJSONObject("responseData").getJSONArray("results");
                    if (!loadMore)
                      imageResults.clear();  //clear the existing image from the array
                    aImageResults.addAll(ImageResult.fromJSONARRAY(imageResultJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject e) {
                Log.e("ERROR", e.toString());
                Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_SHORT);
            }
        });
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
