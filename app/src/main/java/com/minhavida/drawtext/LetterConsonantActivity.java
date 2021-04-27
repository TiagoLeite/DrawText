package com.minhavida.drawtext;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

public class LetterConsonantActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private boolean autoSoundEnabled = false;
    private ImageView imageViewLetterConsonant, imageViewFeedback, ivPlaySound;
    private MediaPlayer mediaPlayer;
    private ImageClassifier tfClassifier;
    private float difficulty = 0f;
    private double DIFFICULTY_LEVEL = 0.7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_consonant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }

        number = getIntent().getIntExtra("letter", 0);
        //number += 10;

        autoSoundEnabled = getIntent().getBooleanExtra("auto_sound_enabled", false);

        /*if (autoSoundEnabled)
        {
            int drawableNumberId = getResources().getIdentifier("som_"+number,
                    "raw", LetterVowelActivity.this.getPackageName());
            MediaPlayer mediaPlayer=MediaPlayer.create(LetterVowelActivity.this,
                    drawableNumberId);
            mediaPlayer.setVolume(0.15f, 0.15f);
            mediaPlayer.start();
        }*/

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);
        canvasView.setNumber(number);

        imageViewLetterConsonant = findViewById(R.id.iv_letter_consonant);
        imageViewFeedback = findViewById(R.id.iv_feedback);
        ivPlaySound = findViewById(R.id.iv_playsound);

        /*ivPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int drawableNumberId = getResources().getIdentifier("som_"+number,
                        "raw", LetterVowelActivity.this.getPackageName());
                MediaPlayer mediaPlayer=MediaPlayer.create(LetterVowelActivity.this,
                        drawableNumberId);
                mediaPlayer.setVolume(0.15f, 0.15f);
                mediaPlayer.start();
            }
        });*/

        loadLetter();
    }

    private void loadLetter()
    {
        char[]arr = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n',
                'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
        int drawableNumberId = getResources().getIdentifier("dotted_letter_"+arr[number],
                "drawable",
                this.getPackageName());

        imageViewLetterConsonant.setImageDrawable(AnimatedVectorDrawableCompat.create(
                this, drawableNumberId));

        imageViewLetterConsonant.setAlpha(1f-difficulty);

        Drawable drawable = imageViewLetterConsonant.getDrawable();
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
            DIFFICULTY_LEVEL = 0.7;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel2) {
            DIFFICULTY_LEVEL = 0.8;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel3) {
            DIFFICULTY_LEVEL = 0.9;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel4) {
            DIFFICULTY_LEVEL = 0.97;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel5) {
            DIFFICULTY_LEVEL = 0.9995;
            item.setChecked(true);
        }
        //Toast.makeText(this, DIFFICULTY_LEVEL + "", Toast.LENGTH_LONG).show();
        return true;
        //return super.onOptionsItemSelected(item);
    }

}
