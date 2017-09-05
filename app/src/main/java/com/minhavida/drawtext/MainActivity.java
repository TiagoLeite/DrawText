package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
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
        String s = canvasView.toString();
        System.out.print(s);
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

    private void writeToFile(String data, Context context)
    {
        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("treino.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
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
