package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View
{
    private Bitmap bitmap;
    private Canvas canvas;
    private Path canvasPath;
    private Paint paint;
    private float mx, my;
    private static final float MOVE_TOLERANCE = 10;
    private Context context;
    private int number;
    private boolean isAnimating = false;

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        this.canvasPath = new Path();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.black));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(getResources().getDimension(R.dimen.user_drawing_stroke_width));
        this.paint = paint;
        bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas();
        canvas.setBitmap(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(canvasPath, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

    }

    private void onStartTouch(float x, float y)
    {
        canvasPath.moveTo(x, y);
        mx = x;
        my = y;
    }

    private void moveTouch(float x, float y)
    {
        float dx = Math.abs(x - mx);
        float dy = Math.abs(y - my);
        if(dx >= MOVE_TOLERANCE || dy >= MOVE_TOLERANCE)
        {
            canvasPath.lineTo((x+mx)/2f, (y+my)/2f);
            Log.d("path", (x+mx)/((float)2*canvas.getWidth())+"");
            Log.d("path", (y+my)/((float)2*canvas.getHeight())+"");
            mx = x;
            my = y;
        }
    }

    public void clearCanvas()
    {
        destroyDrawingCache();
        bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        canvasPath.reset();
        invalidate();
    }

    private void upTouch()
    {
        canvasPath.lineTo(mx, my);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                onStartTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;

        }
        return true;
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String toString()
    {
        String string = "\n";
        Bitmap bm = getDrawingCache();
        bm = Bitmap.createScaledBitmap(bm,28, 28, true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[] pixels = new int[h*w];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                if(pixels[i*w+j] != 0)
                {
                    pixels[i*w+j] = 1;
                    System.out.print(pixels[i*w+j]);
                    string = string.concat(pixels[i*w+j]+"");
                }
                else
                {
                    pixels[i*w+j] = 0;
                    System.out.print(pixels[i*w+j]);
                    string = string.concat(pixels[i*w+j]+"");
                }
            }
            System.out.println();
            string = string.concat("\n");
        }
        string = string.concat("=\n");
        System.out.println("w:"+w+",h:"+h);
        return string;
    }

    public float[] getPixelsArray()
    {
        Bitmap bm = getDrawingCache();
        bm = Bitmap.createScaledBitmap(bm, 128, 128, true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[] pixels = new int[h*w];
        float[] pixelsRet = new float[h*w];

        bm.getPixels(pixels, 0, w, 0, 0, w, h);

        String line;
        for (int i = 0; i < h; i++)
        {
            line = "";
            for (int j = 0; j < w; j++)
            {
                if (pixels[i * w + j] != 0)
                {
                    pixelsRet[i * w + j] = 1;
                    line=line.concat("1");
                }
                else
                {
                    pixelsRet[i * w + j] = 0;
                    line=line.concat("0");
                }
            }
            Log.d("debug", line);
        }

        return pixelsRet;
    }

    public float[] getPixelsArray3()
    {
        Bitmap bm = getDrawingCache();
        bm = Bitmap.createScaledBitmap(bm, 224, 224, true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int[] pixels = new int[h * w];
        float[] pixelsRet = new float[h * w * 3];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        int cont = 0;
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
            }
        }
        return pixelsRet;
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int width = getWidth();
        int height = getHeight();
        Log.d("path", "W = " + width);
        Log.d("path", "H = " + height);
    }

    public void setActivity(AppCompatActivity activity) {
    }

    public void setNumber(int number) {
        this.number = number;
    }
}



























































