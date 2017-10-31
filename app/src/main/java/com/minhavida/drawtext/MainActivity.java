package com.minhavida.drawtext;

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
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private EditText et;
    private NeuralNetwork network;
    private MediaPlayer mp;

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

        et = (EditText)findViewById(R.id.et);

        double tol = 0.05;
        double speed = 0.05;
        double momentum = 0f;
        network = new NeuralNetwork(784, 10, 3, speed, momentum, tol);

        /*try
        {
            InputStream inputStream = getResources().openRawResource(R.raw.net_weights);
            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputreader);
            *//*String line = br.readLine();
            while (line!=null)
            {
                System.out.println(line);
                line = br.readLine();
            }
            in.close();*//*

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

        Button bt = (Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                /*Test test = convertImageToTest();
                network.forward(test.getInput());
                NeuronLayer layer = network.getLayerAt(network.getLayersNumber()-1); // last layer
                int val = layer.getHighestNeuron().getIndex();
                et.setText(val+"");*/
                Log.d("debug", floodFill(canvasView.getPixelsMatrix(), 28,
                        28)+"");
                et.setText(String.valueOf(floodFill(canvasView.getPixelsMatrix(), 28,28)));
                printMatrix(canvasView.getPixelsMatrix(), 28, 28);
            }
        });

        Button bt1 = (Button)findViewById(R.id.button1);

        final AlertDialog alert = new AlertDialog.Builder(this).create();
        View view = LayoutInflater.from(this).inflate(R.layout.alert, null);
        final EditText edittext = (EditText)view.findViewById(R.id.etAnswer);
        edittext.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1)});
        alert.setView(view);
        alert.setMessage("Help me to improve :)");
        alert.setTitle("Enter the correct answer");
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveData(edittext.getText().toString());
                        dialog.dismiss();
                    }
                });

        alert.setButton(AlertDialog.BUTTON_NEUTRAL, "CANCEL", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                //Log.d("debug", edittext.getText().toString());
                canvasView.clearCanvas();
                et.setText("");
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
                    edittext.setText("");
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

    private Test convertImageToTest()
    {
        double[] array = canvasView.getPixelsArray();
        Test test = new Test(0);
        test.setInput(array);
        return test;
    }

    public void saveData(String string)
    {
        string = string.concat(canvasView.toString());
        Log.d("dados", string);
        writeToFile(string, this);
        canvasView.clearCanvas();
        et.setText("");
        Toast.makeText(this, "Thanks ;)", Toast.LENGTH_LONG).show();
       /*try
        {
            Train.context = this;
            System.out.println(getApplicationInfo().dataDir);
            Train.init();
            char c = '\0';
            c = Train.findLetter(canvasView.getPixelsArray());
            System.out.println("RES = " + c);

        }
        catch (Exception e)
        {
            System.out.println("Error "+ e.getMessage()+e.getClass());
        }*/

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
        et.setText("");
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

    private int floodFill(double[][] mat, int lin, int col)
    {
        int floods = 0;
        for (int i = 1; i <= lin; i++ )
        {
            for (int j = 1; j <= col; j++)
            {
                if (flood(mat, i, j))
                    floods++;
            }
        }
        return floods;
    }

    private boolean flood(double[][] mat, int i, int j)
    {
        if(mat[i][j]==1)
        {
            mat[i][j] = 0;
            if(mat[i][j+1] == 1)
                flood(mat, i, j+1);
            if(mat[i][j-1] == 1)
                flood(mat, i, j-1);
            if(mat[i+1][j] == 1)
                flood(mat, i+1, j);
            if(mat[i-1][j] == 1)
                flood(mat, i-1, j);
            return true;
        }
        return false;
    }


    private void printMatrix(double[][] mat, int lin, int col)
    {
        for (int i = 1; i<=lin; i++)
        {
            for (int j = 1; j <= col; j++)
                System.out.print(mat[i][j]);
        }
        System.out.println();
    }
}






