package com.minhavida.drawtext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.FileOutputStream;
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
    private static final float TOLERANCE = 10;
    private Context context;
    private AppCompatActivity activity;

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        canvasPath = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(getResources().getDimension(R.dimen.stroke_width));
        bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        handPointer = new ImageView(context);
        handPointer.setLayoutParams(new ViewGroup.LayoutParams((int)getResources().getDimension(R.dimen.hand_pointer_width),
                (int)getResources().getDimension(R.dimen.hand_pointer_height)));
        //handPointer.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hand_pointer));
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
        if(dx >= TOLERANCE || dy >= TOLERANCE)
        {
            canvasPath.lineTo((x+mx)/2f, (y+my)/2f);
            Log.d("canvas", (x+mx)/2f+",");///(2f*width)+",");
            Log.d("canvas", (y+my)/2f+",");////(2f*height)+",");
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
        /*if(saveImage(bm))
            Log.d("debug", "Saved");
        else
            Log.d("debug", "Error saving");*/
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

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        width = getWidth();
        height = getHeight();
        Log.d("canvas", "W = " + width);
        Log.d("canvas", "H = " + height);
        animatePointer();
    }

    private void animatePointer() {
        final Path path = new Path();
        final double vet[] = new double[]{
                121.68631,
                295.02542,
                122.57161,
                282.57028,
                124.912155,
                270.716,
                127.747696,
                259.09247,
                132.18875,
                246.09071,
                140.01186,
                229.10098,
                151.55215,
                210.79784,
                165.29861,
                195.95149,
                181.05206,
                185.19218,
                195.11307,
                178.33853,
                206.36118,
                173.81404,
                217.85507,
                169.55159,
                229.97946,
                165.5104,
                242.74355,
                161.70734,
                255.65005,
                158.70279,
                268.8474,
                156.19888,
                283.04865,
                153.9329,
                298.18915,
                151.5787,
                313.7945,
                148.5118,
                329.34473,
                145.65329,
                344.5589,
                143.41476,
                359.84326,
                141.66649,
                374.7097,
                140.70119,
                388.53357,
                140.375,
                402.65735,
                140.375,
                416.84735,
                140.79764,
                430.28772,
                141.6012,
                443.25473,
                142.27151,
                456.02466,
                143.02908,
                469.2059,
                143.72928,
                481.93713,
                144.68175,
                493.68762,
                145.43452,
                505.3278,
                146.1283,
                517.40125,
                147.41563,
                529.0569,
                148.65723,
                539.92017,
                149.99457,
                550.84955,
                151.82303,
                562.1924,
                154.65677,
                577.2582,
                161.40454,
                595.8267,
                172.98956,
                612.3658,
                187.63632,
                626.7678,
                204.37949,
                639.08606,
                221.21358,
                649.30206,
                238.30792,
                657.88495,
                255.44504,
                663.2201,
                268.73087,
                665.796,
                279.23694,
                667.2928,
                290.42084,
                668.2073,
                302.26593,
                668.45435,
                313.38202,
                668.1614,
                323.62775,
                667.8665,
                334.5844,
                666.7041,
                349.79965,
                665.1953,
                364.74948,
                663.2964,
                380.84442,
                661.1665,
                397.39923,
                659.7578,
                408.61047,
                656.6747,
                422.63364,
                651.71124,
                437.2156,
                644.4898,
                449.54404,
                633.34937,
                463.37515,
                619.01056,
                477.82108,
                605.9452,
                488.93637,
                595.1294,
                496.78262,
                583.36847,
                503.97702,
                564.32263,
                513.39764,
                544.2461,
                522.3738,
                529.8436,
                527.40375,
                515.6942,
                530.80414,
                502.29904,
                532.15466,
                480.38034,
                530.5044,
                457.30576,
                527.8525,
                442.42773,
                526.02313,
                427.74518,
                524.0684,
                406.19153,
                521.06433,
                385.94363,
                518.39355,
                373.41058,
                517.12573,
                356.49622,
                515.96655,
                338.98895,
                515.3573,
                325.88153,
                515.21875,
                327.3237,
                513.042,
                340.21152,
                510.1395,
                351.78607,
                508.47247,
                364.70578,
                507.06934,
                385.19437,
                505.44434,
                406.88452,
                504.4375,
                422.81012,
                504.93243,
                445.78772,
                507.66364,
                468.81952,
                511.31506,
                484.31036,
                514.1446,
                504.77573,
                518.8207,
                524.6531,
                524.6802,
                537.2412,
                529.49023,
                553.9263,
                536.18335,
                570.1708,
                543.14465,
                585.3656,
                551.26807,
                606.9829,
                565.0255,
                625.79114,
                579.92145,
                639.14453,
                593.1155,
                651.18713,
                607.1122,
                661.64496,
                622.2536,
                668.1309,
                635.0353,
                672.34424,
                648.8854,
                675.9543,
                666.21204,
                677.83594,
                681.1675,
                678.80237,
                695.5199,
                678.47107,
                710.9468,
                676.0356,
                729.2141,
                671.0296,
                752.2066,
                663.3354,
                775.85864,
                655.5713,
                793.9566,
                644.6967,
                811.2639,
                627.86896,
                831.27783,
                608.0934,
                847.7416,
                592.2767,
                857.99176,
                577.5554,
                866.06195,
                558.09546,
                874.90594,
                537.4369,
                881.42175,
                514.4546,
                886.7617,
                489.4313,
                891.11017,
                463.2422,
                893.9497,
                436.82843,
                896.59247,
                409.701,
                898.9964,
                382.3692,
                898.98193,
                354.6763,
                896.3981,
                326.42682,
                892.5615,
                298.09973,
                887.80786,
                269.4395,
                882.04956,
                241.09311,
                872.7579,
                214.58502,
                859.8118,
                190.0568,
                842.35474,
                166.44435,
                817.0438,
                148.08318,
                789.531,
                134.8162,
                766.08624,
                123.77414,
                745.3937,
                116.37398,
                726.3688,
                113.07743,
                711.32465,
                112.30469,
                699.33124};

        float margin = 5f;
        for (int k=2; k<vet.length-2; k+=2)
        {
            //path.moveTo((float)width/(1.25f*width/height)*(float)vet[k-2]-margin,(float)height*(float)vet[k-1]-margin);
            path.moveTo((float)vet[k-2],(float)vet[k-1]);
            //path.lineTo((float)width/(1.25f*width/height)*(float)vet[k]-margin, (float)height*(float)vet[k+1]-margin);
            path.lineTo((float)vet[k], (float)vet[k+1]);
        }
        //canvasPath.moveTo(100f, 100f);
        //canvasPath.lineTo(200f, 10f);

        handPointer.setTranslationX((float)vet[0]);
        handPointer.setTranslationY((float)vet[1]);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            ObjectAnimator animator = ObjectAnimator.ofFloat(handPointer, View.X, View.Y, path);
            animator.setDuration(5000);
            animator.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handPointer.setVisibility(View.GONE);
                }
            }, 5500);
        }
        else
        {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int k=0; k<vet.length-1; k+=2)
                    {
                        final int finalK = k;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handPointer.animate()
                                        .x((float)vet[finalK])
                                        .y((float)vet[finalK+1])
                                        .setDuration(75);
                            }
                        });
                        try
                        {
                            Thread.sleep(85);
                        }
                        catch (Exception e)
                        {
                            return;
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handPointer.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }
    }

    public void setActivity(NumberActivity activity) {
        this.activity = activity;
    }
}



























































