package com.minhavida.drawtext;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class CanvasView extends View
{
    public int width;
    public int height;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private Paint paint;
    private float mx, my;
    private static final float TOLERANCE = 5;
    private Context context;
    private CanvasListener listener;
    private long timeStart, timeEnd;
    private static final long TIME_DELAY = 2000;
    private boolean hasFinished;

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(20f);
        bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
    }

    public void setListener(CanvasListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
    }

    private void onStartTouch(float x, float y)
    {
        timeStart = System.currentTimeMillis();
        if (timeStart - timeEnd <= TIME_DELAY)
            hasFinished = false;
        path.moveTo(x, y);
        mx = x;
        my = y;
    }

    private void upTouch()
    {
        hasFinished = true;
        path.lineTo(mx, my);
        Log.d("debug", "UP Canvas!");
        timeEnd = System.currentTimeMillis();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                if (hasFinished)
                {
                    listener.onFinish();
                    clearCanvas();
                }
            }
        }, TIME_DELAY);
    }

    private void moveTouch(float x, float y)
    {
        float dx = Math.abs(x - mx);
        float dy = Math.abs(y - my);
        if(dx >= TOLERANCE || dy >= TOLERANCE)
        {
            path.lineTo((x+mx)/2f, (y+my)/2f);
            mx = x;
            my = y;
        }
    }

    public void clearCanvas()
    {
        destroyDrawingCache();
        bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        path.reset();
        invalidate();
    }

    public Canvas getCanvas() {
        return canvas;
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

    private  Bitmap scaleBitmapAndKeepRation(Bitmap targetBmp,int reqHeightInPixels,int reqWidthInPixels)
    {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, targetBmp.getWidth(), targetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(targetBmp, 0, 0, targetBmp.getWidth(), targetBmp.getHeight(), m, true);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String toString2()
    {
        String string = "\n";
        //int h = bitmap.getHeight();
        //int w = bitmap.getWidth();
        //int pixels[] = new int[h*w];
        Bitmap bm = getDrawingCache();
        //bm = this.scaleBitmapAndKeepRation(bm, 50, 50);
        bm = Bitmap.createScaledBitmap(bm,28, 28, true);
        int h = bm.getHeight();
        int w = bm.getWidth();
        int pixels[] = new int[h*w];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);

        /*int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();*/

        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                if(pixels[i*w+j] != 0)
                {
                    //pixels[i*w+j] = 1;
                    System.out.print(pixels[i*w+j]);
                    string = string.concat(pixels[i*w+j]+"");
                }//System.out.print((pixels[i*w+j] & 0x00FFFFFF)+" ");
                else
                {
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
        //return getDrawingCache();
        Bitmap bm = getDrawingCache();
        bm = Bitmap.createScaledBitmap(bm, 28, 28, true);
        if(saveImage(bm))
            Log.d("debug", "Saved");
        else
            Log.d("debug", "Error saving");
        int h = bm.getHeight();
        int w = bm.getWidth();
        int pixels[] = new int[h*w];
        float[] pixelsRet = new float[h*w];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);

        String line;
        for (int i = 0; i < h; i++)
        {
            line = "";
            for (int j = 0; j < w; j++)
            {
                if (pixels[i * w + j] !=0)
                {
                    pixelsRet[i * w + j] = 1;
                    //line=line.concat(pixelsRet[i * w + j]+"");
                }
                else
                {
                    pixelsRet[i * w + j] = 0;
                    //line=line.concat(pixelsRet[i * w + j]+"");
                }
            }
            Log.d("debug", line);
        }

        return pixelsRet;
    }

    private boolean saveImage(Bitmap bitmap)
    {
        try
        {
            String filename = Environment.getExternalStorageDirectory() + "/char_image.png";
            Log.d("debug", filename);
            OutputStream outStream = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
            return true;
        }
        catch (Exception e)
        {
            Log.d("debug", e.getMessage()+" "+e.getClass());
            return false;
        }
    }

    public interface CanvasListener
    {
        public void onFinish();
    }
}



























































