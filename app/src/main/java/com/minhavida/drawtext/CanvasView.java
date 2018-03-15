package com.minhavida.drawtext;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        canvasPath = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
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
            Log.d("canvas", (x+mx)/(2f*width)+",");
            Log.d("canvas", (y+my)/(2f*height)+",");
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        width = getWidth();
        height = getHeight();
        Log.d("canvas", "W = " + width);
        Log.d("canvas", "H = " + height);
        final Path path = new Path();
        double vet[] = new double[]{
                0.17083333,
                0.30696163,
                0.17117469,
                0.2890553,
                0.1749255,
                0.27017042,
                0.18245532,
                0.25271574,
                0.18999282,
                0.23786812,
                0.19744131,
                0.22433473,
                0.20674436,
                0.21053061,
                0.22067079,
                0.19582838,
                0.24080794,
                0.18119489,
                0.26076096,
                0.170719,
                0.277179,
                0.163783,
                0.29581022,
                0.15822476,
                0.31810537,
                0.15308641,
                0.33697376,
                0.14910483,
                0.3554772,
                0.14625154,
                0.37600827,
                0.1433588,
                0.3936017,
                0.14173019,
                0.4115699,
                0.14058824,
                0.43014917,
                0.14,
                0.4480264,
                0.14,
                0.46658334,
                0.14,
                0.48440057,
                0.14,
                0.5008505,
                0.14,
                0.51874423,
                0.14058824,
                0.5386969,
                0.14285547,
                0.55940646,
                0.14627808,
                0.578736,
                0.1491998,
                0.59577245,
                0.1515425,
                0.6129704,
                0.15445603,
                0.63152677,
                0.158402,
                0.6492456,
                0.162336,
                0.6645388,
                0.16581291,
                0.6853164,
                0.17161307,
                0.71167415,
                0.18092634,
                0.73528475,
                0.19175373,
                0.75656474,
                0.20553762,
                0.7768119,
                0.22565132,
                0.790866,
                0.2439377,
                0.7980879,
                0.25726682,
                0.8039256,
                0.27138495,
                0.8084602,
                0.2861798,
                0.81162465,
                0.30028155,
                0.8127425,
                0.31321266,
                0.81319445,
                0.33259875,
                0.8138889,
                0.35250002,
                0.8101116,
                0.37155673,
                0.80212164,
                0.39124072,
                0.789588,
                0.41107288,
                0.77010703,
                0.43250564,
                0.7506949,
                0.44816926,
                0.73354965,
                0.4603759,
                0.71711373,
                0.47074306,
                0.6991609,
                0.47947362,
                0.681215,
                0.4858942,
                0.66557187,
                0.49048862,
                0.64906657,
                0.49458468,
                0.63253456,
                0.49755824,
                0.61046875,
                0.4987678,
                0.5822624,
                0.49882352,
                0.5604919,
                0.49882352,
                0.54490143,
                0.49882352,
                0.5293216,
                0.49882352,
                0.51442796,
                0.49882352,
                0.4932836,
                0.49882352,
                0.46437174,
                0.4982353,
                0.44122717,
                0.4970588,
                0.42554697,
                0.4964706,
                0.42791897,
                0.49303505,
                0.4481385,
                0.48809886,
                0.47071153,
                0.48484418,
                0.49690384,
                0.48272157,
                0.52196527,
                0.48235294,
                0.54489267,
                0.48235294,
                0.5647549,
                0.48235294,
                0.5812911,
                0.48235294,
                0.5972713,
                0.48307297,
                0.61325854,
                0.48568204,
                0.6296584,
                0.49060443,
                0.6452814,
                0.4965309,
                0.6618843,
                0.50360864,
                0.6822442,
                0.51444805,
                0.7006874,
                0.5255763,
                0.7201343,
                0.5399323,
                0.7461455,
                0.56085265,
                0.77085453,
                0.5823596,
                0.78868204,
                0.60325605,
                0.801081,
                0.6241626,
                0.8087683,
                0.64393294,
                0.81100565,
                0.6643038,
                0.8111111,
                0.68630624,
                0.8111111,
                0.7095772,
                0.8106246,
                0.72855324,
                0.80945873,
                0.741163,
                0.8053343,
                0.75705445,
                0.79664123,
                0.77665484,
                0.78519034,
                0.79416907,
                0.7717777,
                0.8083556,
                0.7569955,
                0.82026756,
                0.7387797,
                0.8313235,
                0.7173106,
                0.8417482,
                0.69507414,
                0.8513944,
                0.6761186,
                0.8584445,
                0.6580507,
                0.86386913,
                0.6332419,
                0.8701159,
                0.6096167,
                0.8744279,
                0.59141827,
                0.87675273,
                0.5729571,
                0.8790707,
                0.55359817,
                0.88139826,
                0.53530306,
                0.8830467,
                0.51723415,
                0.88411766,
                0.49575818,
                0.88529414,
                0.47561795,
                0.8858824,
                0.45782462,
                0.8858824,
                0.4392295,
                0.8858824,
                0.4206042,
                0.8858824,
                0.40275964,
                0.88530016,
                0.38565207,
                0.8835579,
                0.3691587,
                0.8812304,
                0.3484076,
                0.8765588,
                0.32655984,
                0.8707896,
                0.30725175,
                0.86552435,
                0.28347385,
                0.8573876,
                0.260645,
                0.84713066,
                0.23968124,
                0.83201617,
                0.22316758,
                0.81603944,
                0.21360263,
                0.8022741,
                0.20651038,
                0.7885094,
                0.19691603,
                0.7716672,
                0.18562584,
                0.7518004,
                0.17774664,
                0.7337426,
                0.17395619,
                0.71772176,
                0.17291667,
                0.70397073,
                0.16944444,
                0.690217};

        float margin = 5f;
        for (int k=2; k<vet.length-2; k+=2)
        {
            path.moveTo((float)width/(1.25f*width/height)*(float)vet[k-2]-margin,(float)height*(float)vet[k-1]-margin);
            path.lineTo((float)width/(1.25f*width/height)*(float)vet[k]-margin, (float)height*(float)vet[k+1]-margin);
        }
//        canvasPath.moveTo(100f, 100f);
//        canvasPath.lineTo(200f, 10f);

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
        else //TODO debug this
        {
            ValueAnimator pathAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                float[] point = new float[2];

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float val = animation.getAnimatedFraction();
                    PathMeasure pathMeasure = new PathMeasure(path, true);
                    pathMeasure.getPosTan(pathMeasure.getLength() * val* 100, point, null);
                    handPointer.setX(point[0]);
                    handPointer.setY(point[1]);
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handPointer.setVisibility(View.GONE);
                }
            }, 5000);}
    }

}



























































