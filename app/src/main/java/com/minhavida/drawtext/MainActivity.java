package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvasView = (CanvasView)findViewById(R.id.canvas);
        canvasView.setDrawingCacheEnabled(true);

    }

    public void print(View v)
    {
        final String string = canvasView.toString();
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
