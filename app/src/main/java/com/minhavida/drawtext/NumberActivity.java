package com.minhavida.drawtext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NumberActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private ImageView imageViewNumber;
    private ImageView imageViewFeedback;
    private MediaPlayer mediaPlayer;
    private ImageClassifier tfClassifier;
    private float screenTransparency = 0f;
    private static final int REQUEST_CODE_PERMISSION = 1;
    private double DIFFICULTY_LEVEL = 0.8;


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
            DIFFICULTY_LEVEL = 0.8;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel2) {
            DIFFICULTY_LEVEL = 0.9;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel3) {
            DIFFICULTY_LEVEL = 0.95;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel4) {
            DIFFICULTY_LEVEL = 0.975;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel5) {
            DIFFICULTY_LEVEL = 0.99;
            item.setChecked(true);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_activity);

        canvasView = findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        number = getIntent().getIntExtra("number", -1);

        boolean autoSoundEnabled = getIntent().getBooleanExtra("auto_sound_enabled", false);

        canvasView.setNumber(number);

        imageViewNumber = findViewById(R.id.iv_number);
        imageViewFeedback = findViewById(R.id.iv_feedback);
        ImageView ivPlaySound = findViewById(R.id.iv_playsound);


        if (autoSoundEnabled)
        {
            int drawableNumberId = getResources().getIdentifier("som_"+number,
                    "raw", NumberActivity.this.getPackageName());
            MediaPlayer mediaPlayer=MediaPlayer.create(NumberActivity.this, drawableNumberId);
            mediaPlayer.setVolume(0.15f, 0.15f);
            mediaPlayer.start();
        }

        ivPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                int drawableNumberId = getResources().getIdentifier("som_"+number,
                        "raw", NumberActivity.this.getPackageName());
                MediaPlayer mediaPlayer=MediaPlayer.create(NumberActivity.this, drawableNumberId);
                mediaPlayer.setVolume(0.15f, 0.15f);
                mediaPlayer.start();
                Log.d("debug", number + " sound clicked");
            }
        });

        loadNumber();

        checkWritingPermission();

        loadModel();

        Button btFind = (Button)findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view)
            {
                float[] arrayImage = canvasView.getPixelsArray();
                Classification cls = tfClassifier.recognize(arrayImage, 1);

                saveStatisticsToSpreadSheet(number, cls.getLabel(), cls.getConf(), DIFFICULTY_LEVEL);

                if (cls.getLabel().equals(number+"") && cls.getConf() > DIFFICULTY_LEVEL)
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

                    if (screenTransparency < 1f)
                        screenTransparency += .2;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setAnimation(null);
                            imageViewFeedback.setVisibility(View.GONE);
                            canvasView.clearCanvas();
                            loadNumber();
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getResources().getString(R.string.app_name));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }

    void saveStatisticsToSpreadSheet(final int label, final String prediction, final float confidence,
                                     final double difficultyLevel) {
        StringRequest request = new StringRequest(StringRequest.Method.POST,
            "https://script.google.com/macros/s/AKfycbwrVBeqDs4FTFimDDa3J2AyAxMVbVpQPdAvAkRajX8IlGf5jP5CC9_hZUhgKTzhIGJccw/exec",
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("DEBUG", Objects.requireNonNull(error.getMessage()));
                }
            }
        ) {
            @SuppressLint("DefaultLocale")
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", getOrCreateUserId());
                params.put("action", "addItem");
                params.put("label", String.valueOf(label));
                params.put("prediction", prediction);
                params.put("confidence", String.format("%.8f", confidence));
                params.put("difficultyLevel", String.format("%.8f", difficultyLevel));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(20000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


    private String getOrCreateUserId(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String userIdString = prefs.getString("user_id_generated", null);

        if(userIdString == null){
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            //do your thing with PreferenceConnector
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_id_generated", uuid);
            editor.apply();
            return uuid;
        }

        return userIdString;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadNumber()
    {
        int drawableNumberId = getResources().getIdentifier("dotted_number_"+number, "drawable",
                this.getPackageName());

        imageViewNumber.setImageDrawable(AnimatedVectorDrawableCompat.create(
                this, drawableNumberId));

        imageViewNumber.setAlpha(1f - screenTransparency);

        Drawable drawable = imageViewNumber.getDrawable();
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

    private void loadModel()
    {
        try
        {
            tfClassifier = ImageClassifier.create(getAssets(),
                    "TensorFlow", "cnn_numbers.pb",
                    "labels_numbers.txt", 128, "input_1",
                    "dense_2/Softmax", true, 10);
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

    private void checkWritingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // permission wasn't granted
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }
    }
}
