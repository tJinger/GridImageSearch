package com.walmart.gridimagesearch.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.walmart.gridimagesearch.R;
import com.walmart.gridimagesearch.models.ImageResult;

public class ImageDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        //Pull out the url from the Intent
        ImageResult result = (ImageResult) getIntent().getSerializableExtra("result");
        ImageView ivImageResult = (ImageView)findViewById(R.id.ivImageResult);
        Picasso.with(this).load(result.fullUrl).into(ivImageResult);
    }
}
