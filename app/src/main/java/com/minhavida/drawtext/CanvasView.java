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
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
        paint.setStrokeWidth(80f);
        bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
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
        path.moveTo(x, y);
        mx = x;
        my = y;
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

    private void upTouch()
    {
        path.lineTo(mx, my);
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
    public String toString()
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
                    pixels[i*w+j] = 1;
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
        int h = bm.getHeight();
        int w = bm.getWidth();
        int pixels[] = new int[h*w];
        float[] pixelsRet = new float[h*w];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
            {
                if (pixels[i * w + j] != 0)
                    pixelsRet[i * w + j] = 1f;
                else
                    pixelsRet[i * w + j] = 0f;
            }
        return pixelsRet;
    }
}



























































