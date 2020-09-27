package com.minhavida.drawtext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class LetterVowelActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private ImageView imageViewLetterVowel;
    private ImageView imageViewFeedback;
    private MediaPlayer mediaPlayer;
    private ImageClassifier tfClassifier;
    private float difficulty = 0f;
    private double DIFFICULTY_LEVEL = 0.8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.letter_vowel_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }

        number = getIntent().getIntExtra("letter", 0);
        number += 10;

        boolean autoSoundEnabled = getIntent().getBooleanExtra("auto_sound_enabled", false);

        if (autoSoundEnabled)
        {
            int drawableNumberId = getResources().getIdentifier("som_"+number,
                    "raw", LetterVowelActivity.this.getPackageName());
            MediaPlayer mediaPlayer=MediaPlayer.create(LetterVowelActivity.this,
                    drawableNumberId);
            mediaPlayer.setVolume(0.15f, 0.15f);
            mediaPlayer.start();
        }

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        canvasView.setNumber(number);

        imageViewLetterVowel = findViewById(R.id.iv_letter_vowel);
        imageViewFeedback = findViewById(R.id.iv_feedback);
        ImageView ivPlaySound = findViewById(R.id.iv_playsound);
        ivPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int drawableNumberId = getResources().getIdentifier("som_"+number,
                        "raw", LetterVowelActivity.this.getPackageName());
                MediaPlayer mediaPlayer=MediaPlayer.create(LetterVowelActivity.this,
                        drawableNumberId);
                mediaPlayer.setVolume(0.15f, 0.15f);
                mediaPlayer.start();
            }
        });

        loadLetter();

        loadModel();

        final String arr[] = {"A", "E", "I", "O", "U"};

        Button btFind = (Button)findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view)
            {
                float[] arrayImage = canvasView.getPixelsArray3();
                Classification cls = tfClassifier.recognize(arrayImage, 3);
                Log.d("confidence", cls.getConf() + " conf");
                if (cls.getLabel().equals(arr[number-10]) && cls.getConf() > DIFFICULTY_LEVEL)
                {
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.correct_answer);
                    mediaPlayer.setVolume(0.025f, 0.025f);
                    mediaPlayer.start();
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.like, null));

                    imageViewFeedback.setVisibility(View.VISIBLE);
                    Animation zoomIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.zoom_out);
                    imageViewFeedback.setAnimation(zoomIn);
                    imageViewFeedback.startAnimation(zoomIn);

                    if (difficulty < 1f)
                        difficulty += .2;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            imageViewFeedback.setVisibility(View.GONE);
                            canvasView.clearCanvas();
                            loadLetter();
                        }
                    }, 2000);
                }
                else
                {
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.dislike, null));

                    imageViewFeedback.setVisibility(View.VISIBLE);
                    Animation zoomOut = AnimationUtils.loadAnimation(view.getContext(), R.anim.zoom_out);
                    imageViewFeedback.setAnimation(zoomOut);
                    imageViewFeedback.startAnimation(zoomOut);
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.wrong_answer);
                    mediaPlayer.setVolume(0.05f, 0.05f);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            canvasView.clearCanvas();
                            imageViewFeedback.setVisibility(View.GONE);
                        }
                    }, 2000);

                }
            }
        });

    }

    private void loadLetter()
    {
        char[]arr = {'a', 'e', 'i', 'o', 'u'};
        int drawableNumberId = getResources().getIdentifier("dotted_letter_"+arr[number-10],
                "drawable",
                this.getPackageName());

        imageViewLetterVowel.setImageDrawable(AnimatedVectorDrawableCompat.create(
                this, drawableNumberId));

        imageViewLetterVowel.setAlpha(1f-difficulty);

        Drawable drawable = imageViewLetterVowel.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat)
        {
            AnimatedVectorDrawableCompat avdc = (AnimatedVectorDrawableCompat) drawable;
            avdc.start();
        }
        else if(drawable instanceof AnimatedVectorDrawable)
        {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
            avd.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // show menu when menu button is pressed
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        menu.getItem(0).setChecked(true);
        menu.getItem(1).setChecked(false);
        menu.getItem(2).setChecked(false);
        menu.getItem(3).setChecked(false);
        menu.getItem(4).setChecked(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_nivel1) {
            DIFFICULTY_LEVEL = 0.9;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel2) {
            DIFFICULTY_LEVEL = 0.95;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel3) {
            DIFFICULTY_LEVEL = 0.99;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel4) {
            DIFFICULTY_LEVEL = 0.9999;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel5) {
            DIFFICULTY_LEVEL = 0.9999999;
            item.setChecked(true);
        }
        return true;
    }

    private void loadModel()
    {
        try
        {
            tfClassifier = LetterClassifier.create(getAssets(),
                    "TensorFlow", "letters_graph.pb",
                    "labels_letters.txt", 224, "input",
                    "final_result", true);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void clearCanvas (View v)
    {
        Log.d("debug", "Canvas cleaned!");
        Log.d("debug", canvasView.toString());
        canvasView.clearCanvas();
    }

}
