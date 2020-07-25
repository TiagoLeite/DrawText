package com.minhavida.drawtext;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CanvasView extends View
{
    private int width;
    private int height;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path canvasPath;
    private ImageView handPointer;
    private Paint paint;
    private float mx, my;
    private static final float MOVE_TOLERANCE = 10;
    private Context context;
    private AppCompatActivity activity;
    private int number;
    private Thread pointerThread;
    private boolean pointerAnimation = true;
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
        paint.setStrokeWidth(getResources().getDimension(R.dimen.stroke_width));
        this.paint = paint;
        bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas();
        canvas.setBitmap(bitmap);
        handPointer = new ImageView(context);
        handPointer.setLayoutParams(new ViewGroup.LayoutParams((int)getResources().getDimension(R.dimen.hand_pointer_width),
                (int)getResources().getDimension(R.dimen.hand_pointer_height)));
        handPointer.setImageDrawable(VectorDrawableCompat.create(getResources(), R.drawable.hand_pointer, null));

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        ((ViewGroup)this.getParent()).addView(handPointer);
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
        Bitmap bm = getDrawingCache();
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
        bm = Bitmap.createScaledBitmap(bm, 28, 28, true);
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
        int pixels[] = new int[h * w];
        float[] pixelsRet = new float[h * w * 3];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        int cont = 0;
        for (int i = 0; i < h; i++)
        {
            for (int j = 0; j < w; j++)
            {
                /*float redValue = Color.red(pixels[i * w + j]);
                float greenValue = Color.green(pixels[i * w + j]);
                float blueValue = Color.blue(pixels[i * w + j]);*/
                //Log.d("debug", redValue + " " + blueValue + " " + greenValue);
                /*pixelsRet[cont++] = (float) ((redValue - 128.0) / 128.0);
                pixelsRet[cont++] = (float) ((greenValue - 128.0) / 128.0);
                pixelsRet[cont++] = (float) ((blueValue - 128.0) / 128.0);*/
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
                pixelsRet[cont++] = pixels[i*w+j] != 0 ? 1 : 0;
            }
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

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        width = getWidth();
        height = getHeight();
        Log.d("path", "W = " + width);
        Log.d("path", "H = " + height);
        animatePointer();
    }

    public void finishPointerAnimation()
    {
        pointerAnimation = false;
    }

    public void animatePointer()
    {
        if (isAnimating)
            return;
        isAnimating = true;
        handPointer.setVisibility(VISIBLE);
        pointerAnimation = true;
        final Path path = new Path();
        final float vet[] = new float[2048];
        int p = 0;
        try
        {
            int rawNumberId = getResources().getIdentifier("number_"+number+"_path",
                    "raw", context.getPackageName());
            InputStream is = getResources().openRawResource(rawNumberId);
            DataInputStream dis = new DataInputStream(is);
            while (dis.available() > 0)
            {
                String test = dis.readLine();
                if (test.split(" ").length == 2)//TODO: improve this (uniform the paths files)
                {
                    vet[p++] = Float.parseFloat(test.split(" ")[0]);
                    vet[p++] = Float.parseFloat(test.split(" ")[1]);
                }
                else
                {
                    if (p%2 == 0)
                        vet[p++] = Float.parseFloat(test)*canvas.getWidth();
                    else
                        vet[p++] = Float.parseFloat(test)*canvas.getHeight();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //float margin = 5f;
        for (int k=2; k < p-2; k+=2)
        {
            //path.moveTo((float)width/(1.25f*width/height)*(float)vet[k-2]-margin,(float)height*(float)vet[k-1]-margin);
            path.moveTo((float)vet[k-2],(float)vet[k-1]);
            //path.lineTo((float)width/(1.25f*width/height)*(float)vet[k]-margin, (float)height*(float)vet[k+1]-margin);
            path.lineTo((float)vet[k], (float)vet[k+1]);
        }

        handPointer.setTranslationX(vet[0]);
        handPointer.setTranslationY(vet[1]);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            ObjectAnimator animator = ObjectAnimator.ofFloat(handPointer, View.X, View.Y, path);
            animator.setDuration(6000);
            animator.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handPointer.setVisibility(View.GONE);
                }
            }, 6500);
        }
        /*else
        {
            final int finalP = p;
            pointerThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int k = 0; k < finalP -1 && pointerAnimation; k+=2)
                    {
                        final int finalK = k;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handPointer.animate()
                                        .x((float)vet[finalK])
                                        .y((float)vet[finalK+1])
                                        .setDuration(40);
                            }
                        });
                        try
                        {
                            Thread.sleep(60);
                        }
                        catch (Exception e)
                        {
                            return;
                        }
                    }
                    isAnimating = false;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handPointer.setVisibility(View.GONE);
                        }
                    });
                }
            });
            pointerThread.start();
        }*/
    }

    public void setActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}



























































