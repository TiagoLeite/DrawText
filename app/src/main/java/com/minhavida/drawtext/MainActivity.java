package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private CanvasView canvasView;
    private EditText et;
    private NeuralNetwork network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasView = (CanvasView)findViewById(R.id.canvas);
        canvasView.setDrawingCacheEnabled(true);

        et = (EditText)findViewById(R.id.et);

        network = new NeuralNetwork(400, 10, 2, 0.025, 0.01);
        try
        {
            InputStream inputStream = getResources().openRawResource(R.raw.out);
            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputreader);
            /*String line = br.readLine();
            while (line!=null)
            {
                System.out.println(line);
                line = br.readLine();
            }
            in.close();*/

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
        }

        Button bt = (Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Test test = convertImageToTest();
                network.forward(test.getInput());
                NeuronLayer layer = network.getLayerAt(network.getLayersNumber()-1); // last layer
                int val = layer.getHighestNeuron().getIndex();
                //Toast.makeText(getApplicationContext(), val+"", Toast.LENGTH_LONG).show();
                et.setText(val+"");
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

    public void print(View v)
    {
        String string = et.getText().toString();
        string = string.concat(canvasView.toString());
        Log.d("dados", string);
        writeToFile(string, this);
        canvasView.clearCanvas();
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
        //canvasView.print();
        canvasView.clearCanvas();
    }
}
