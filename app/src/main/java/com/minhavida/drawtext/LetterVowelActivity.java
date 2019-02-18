package com.minhavida.drawtext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class LetterVowelActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private ImageView imageViewNumber, imageViewFeedback, ivPlaySound;
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

        number = getIntent().getIntExtra("letter", 0);
        number += 10;

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        canvasView.setNumber(number);

        imageViewNumber = findViewById(R.id.iv_number);
        imageViewFeedback = findViewById(R.id.iv_feedback);
        ivPlaySound = findViewById(R.id.iv_playsound);
        ivPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int drawableNumberId = getResources().getIdentifier("som_"+number,
                        "raw", LetterVowelActivity.this.getPackageName());
                MediaPlayer mediaPlayer=MediaPlayer.create(LetterVowelActivity.this,
                        drawableNumberId);
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
                canvasView.finishPointerAnimation();
                float[] arrayImage = canvasView.getPixelsArray3();
                Classification cls = tfClassifier.recognize(arrayImage, 3);
                if (cls.getLabel().equals(arr[number-10]) && cls.getConf() > .75)
                {
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.correct_answer);
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
                            canvasView.animatePointer();
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
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            canvasView.clearCanvas();
                            canvasView.animatePointer();
                            imageViewFeedback.setVisibility(View.GONE);
                        }
                    }, 2000);

                }
            }
        });

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

    private void loadLetter()
    {
        char[]arr = {'a', 'e', 'i', 'o', 'u'};
        int drawableNumberId = getResources().getIdentifier("dotted_letter_"+arr[number-10],
                "drawable",
                this.getPackageName());
        imageViewNumber.setImageDrawable(VectorDrawableCompat.create(getResources(),
                drawableNumberId, null));
        imageViewNumber.setAlpha(1f-difficulty);
    }

    private void loadModel()
    {
        try
        {
            tfClassifier = LetterClassifier.create(getAssets(),
                    "TensorFlow", "letters_graph.pb",
                    "labels_letters.txt", 224, "input",
                    "final_result", true);
            /*String[] tensorNames = new String[]{"final_result"};
            tfClassifier = LetterClassifier.create(getAssets(), "TensorFlow",
                    "letters_graph.pb",
                    "labels_letters.txt",
                    "input",
                    tensorNames,
                    true);*/

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
