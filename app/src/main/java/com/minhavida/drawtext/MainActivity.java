package com.minhavida.drawtext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private EditText etAnswer;
    private NeuralNetwork network;
    private MediaPlayer mp;
    private TensorFlowClassifier tfClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp = MediaPlayer.create(this, R.raw.error);

        canvasView = (CanvasView)findViewById(R.id.canvas);
        canvasView.requestFocus();
        canvasView.setDrawingCacheEnabled(true);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        etAnswer = (EditText)findViewById(R.id.et_answer);

        loadModel();

        /*double tol = 0.05;
        double speed = 0.05;
        double momentum = 0f;
        network = new NeuralNetwork(784, 10, 3, speed, momentum, tol);

        try
        {
            InputStream inputStream = getResources().openRawResource(R.raw.net_weights);
            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputreader);
            for (NeuronLayer layer : network.getNeuronLayers())
            {
                for (Neuron neuron : layer.getNeurons())
                {
                    String line = br.readLine();
                    line = line.replaceFirst(" ", "");
                    String[] tokens = line.split(" ");
                    for (int i = 0; i<tokens.length; i++)
                    {
                        double d = Double.parseDouble(tokens[i]);
                        neuron.setWAt(i, d);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("debug", e.getClass()+e.getMessage());
            e.printStackTrace();
            return;
        }*/

        Button btFind = (Button)findViewById(R.id.bt_find);
        btFind.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view)
            {
                String text = "";
                float[] array = canvasView.getPixelsArray();
                Classification cls = tfClassifier.recognize(array);
                if (cls.getLabel() == null)
                    text += tfClassifier.name() + ": ?\n";
                else
                    text += String.format("%s (%.1f", cls.getLabel(), 100*cls.getConf());
                /*Test test = convertImageToTest();
                network.forward(test.getInput());
                NeuronLayer layer = network.getLayerAt(network.getLayersNumber()-1); // last layer
                int val = layer.getHighestNeuron().getIndex();
                etAnswer.setText(val+"");*/
                etAnswer.setText(text.concat("%)"));
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
                etAnswer.setText("");
                dialog.dismiss();
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try
                {
                    mp.start();
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

    /*private Test convertImageToTest()
    {
        double[] array = canvasView.getPixelsArray();
        Test test = new Test(0);
        test.setInput(array);
        return test;
    }*/

    public void saveData(String string)
    {
        string = string.concat(canvasView.toString());
        Log.d("dados", string);
        writeToFile(string, this);
        canvasView.clearCanvas();
        etAnswer.setText("");
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
        etAnswer.setText("");
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
}
