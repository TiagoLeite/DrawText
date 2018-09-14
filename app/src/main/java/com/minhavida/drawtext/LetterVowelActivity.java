package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class LetterVowelActivity extends AppCompatActivity {

    private char letter;
    private CanvasView canvasView;
    private int number;
    private EditText etAnswer;
    private ImageView imageViewNumber, imageViewFeedback;
    private MediaPlayer mediaPlayer;
    private TensorFlowClassifier tfClassifier;
    private float difficulty = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_vowel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }

        letter = getIntent().getCharExtra("letter", '0');

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        canvasView.setNumber(number);

        imageViewNumber = findViewById(R.id.iv_number);
        imageViewFeedback = findViewById(R.id.iv_feedback);

        loadNumber();

        loadModel();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNumber()
    {
        int drawableNumberId = getResources().getIdentifier("dotted_number_"+number, "drawable",
                this.getPackageName());
        imageViewNumber.setImageDrawable(VectorDrawableCompat.create(getResources(),
                drawableNumberId, null));
        imageViewNumber.setAlpha(1f-difficulty);
    }

    private void loadModel()
    {
        try
        {
            tfClassifier = TensorFlowClassifier.create(getAssets(), "TensorFlow", "mnist_model_graph.pb",
                    "labels.txt", 28, "input", "output", true);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void clearCanvas (View v)
    {
        canvasView.toString();
        canvasView.clearCanvas();
    }

}
