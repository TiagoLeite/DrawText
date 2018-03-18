package com.minhavida.drawtext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    private EditText etAnswer;
    private ImageView imageViewNumber, imageViewFeedback;
    private MediaPlayer mediaPlayer;
    private TensorFlowClassifier tfClassifier;
    private float difficulty = 0f;
    private static final int REQUEST_CODE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_activity);

        canvasView = (CanvasView)findViewById(R.id.canvas);
        canvasView.setActivity(this);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //etAnswer = (EditText)findViewById(R.id.et_answer);

        number = getIntent().getIntExtra("number", -1);

        canvasView.setNumber(number);

        imageViewNumber = (ImageView)findViewById(R.id.iv_number);
        imageViewFeedback = (ImageView)findViewById(R.id.iv_feedback);

        loadNumber();

        checkWritingPermission();

        loadModel();

        Button btFind = (Button)findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view)
            {
                String text = "";
                float[] arrayImage = canvasView.getPixelsArray();
                Classification cls = tfClassifier.recognize(arrayImage);
                /*if (cls.getLabel() == null)
                    text += tfClassifier.name() + ": ?\n";
                else
                    text += String.format("%s (%.3f", cls.getLabel(), cls.getConf());
                etAnswer.setText(text.concat(")"));*/
                if (cls.getLabel().equals(number+"") && cls.getConf() > .5)
                {
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.correct_answer);
                    mediaPlayer.start();
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.like, null));
                    imageViewFeedback.setVisibility(View.VISIBLE);

                    if (difficulty < 1f)
                        difficulty += .2;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            imageViewFeedback.setVisibility(View.GONE);
                            canvasView.clearCanvas();
                            canvasView.animatePointer();
                            loadNumber();
                        }
                    }, 1500);
                }
                else
                {
                    imageViewFeedback.setImageDrawable(VectorDrawableCompat.create(getResources(),
                            R.drawable.dislike, null));
                    imageViewFeedback.setVisibility(View.VISIBLE);
                    mediaPlayer = MediaPlayer.create(view.getContext(), R.raw.wrong_answer);
                    mediaPlayer.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            canvasView.clearCanvas();
                            canvasView.animatePointer();
                            imageViewFeedback.setVisibility(View.GONE);
                        }
                    }, 1500);

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
        //canvasView.getBitmap().reconfigure(50, 50, Bitmap.Config.ARGB_8888);
        canvasView.toString();
        canvasView.clearCanvas();
        //etAnswer.setText("");
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
