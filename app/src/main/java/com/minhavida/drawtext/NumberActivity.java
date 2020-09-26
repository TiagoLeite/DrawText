package com.minhavida.drawtext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class NumberActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private int number;
    private ImageView imageViewNumber;
    private AnimatedVectorDrawableCompat avdc;
    private AnimatedVectorDrawable avd;
    private ImageView imageViewFeedback;
    private MediaPlayer mediaPlayer;
    private ImageClassifier tfClassifier;
    private float difficulty = 0f;
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
            DIFFICULTY_LEVEL = 0.85;
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
            DIFFICULTY_LEVEL = 0.99;
            item.setChecked(true);
        }
        else if (item.getItemId() == R.id.menu_nivel5) {
            DIFFICULTY_LEVEL = 0.9999;
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
                Log.d("confidence", cls.getConf() + " conf");
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

                    if (difficulty < 1f)
                        difficulty += .2;

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

        Button bt1 = (Button)findViewById(R.id.button1);

        final AlertDialog alert = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.alert, null);
        final EditText etDialog = (EditText)view.findViewById(R.id.etAnswerDialog);
        etDialog.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1)});
        alert.setView(view);
        alert.setMessage("Help me to improve :)");
        alert.setTitle("Enter the correct answer");
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveData(etDialog.getText().toString());
                        dialog.dismiss();
                    }
                });

        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                //Log.d("debug", edittext.getText().toString());
                canvasView.clearCanvas();
                //etAnswer.setText("");
                dialog.dismiss();
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try
                {
                    //mediaPlayer.start();
                    Window window = alert.getWindow();
                    if (window != null)
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    etDialog.setText("");
                    alert.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return;
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadNumber()
    {
        int drawableNumberId = getResources().getIdentifier("dotted_number_"+number, "drawable",
                this.getPackageName());

        imageViewNumber.setImageDrawable(AnimatedVectorDrawableCompat.create(
                this, drawableNumberId));

        imageViewNumber.setAlpha(1f-difficulty);

        Drawable drawable = imageViewNumber.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat)
        {
            avdc = (AnimatedVectorDrawableCompat)drawable;
            avdc.start();
        }
        else if(drawable instanceof AnimatedVectorDrawable)
        {
            avd = (AnimatedVectorDrawable)drawable;
            avd.start();
        }
    }

    private void loadModel()
    {
        try
        {
            tfClassifier = ImageClassifier.create(getAssets(),
                    "TensorFlow", "mnist_model_graph.pb",
                    "labels_mnist.txt", 28, "input",
                    "output", true);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }

    public void saveData(String string)
    {
        string = string.concat(canvasView.toString());
        Log.d("dados", string);
        writeToFile(string, this);
        canvasView.clearCanvas();
        //etAnswer.setText("");
        Toast.makeText(this, "Thanks ;)", Toast.LENGTH_LONG).show();
    }


    private void writeToFile(final String data, Context context)
    {
        Log.d("debug", "started");
        try
        {
            File file = new File(context.getExternalCacheDir(), "train.txt");
            //FileOutputStream fOut = openFileOutput(context.getExternalCacheDir()+"/train.txt", MODE_APPEND);
            FileOutputStream fOut = new FileOutputStream (file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(data);
            osw.flush();
            osw.close();
            //FileWriter out = new FileWriter(new File(context.getExternalCacheDir(), "train.txt"));
            // out.append(data);
            //out.close();
            System.out.print("\nWRITTEN!!!");
            Log.d("debug", "WRITTEN in " + context.getExternalCacheDir());
        }
        catch (Exception e)
        {
            System.out.print("\nError: ");
            e.printStackTrace();
            Log.d("debug", "ERROR");
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void clearCanvas (View v)
    {
        Log.d("debug", "Canvas cleaned!");
        Log.d("debug", canvasView.toString());
        canvasView.clearCanvas();
    }

    public void sendEmail(View v)
    {
        String filename = "train.txt";
        File filelocation = new File(v.getContext().getExternalCacheDir(), filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"tiago.tmleite@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Training file");
        startActivity(emailIntent);
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
