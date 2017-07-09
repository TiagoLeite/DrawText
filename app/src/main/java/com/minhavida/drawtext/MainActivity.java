package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
        //canvasView.print();
        try
        {
            Train.context = this;
            System.out.println(getApplicationInfo().dataDir);
            Train.init();
            char c =  Train.findLetter(canvasView.getPixelsArray());
            System.out.println("RES = " + c);

        }
        catch (Exception e)
        {
            System.out.println("Error "+ e.getMessage()+e.getClass());
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
