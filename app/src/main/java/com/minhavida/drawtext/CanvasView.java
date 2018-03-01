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
                0.06153846,
                0.22826985,
                0.06546145,
                0.20885465,
                0.07411577,
                0.1875064,
                0.083555296,
                0.16477232,
                0.09403928,
                0.14415252,
                0.10915906,
                0.124285184,
                0.13139002,
                0.10309927,
                0.15634511,
                0.08797169,
                0.17961827,
                0.0781527,
                0.20322613,
                0.0698634,
                0.23118488,
                0.060780287,
                0.26220012,
                0.050865196,
                0.29375798,
                0.04201674,
                0.3255497,
                0.03582686,
                0.35876378,
                0.032051064,
                0.39337236,
                0.029743372,
                0.42744824,
                0.028461538,
                0.46063817,
                0.026923077,
                0.49200502,
                0.025384616,
                0.52413416,
                0.024615385,
                0.55534893,
                0.024615385,
                0.583789,
                0.024615385,
                0.6113199,
                0.024615385,
                0.63793486,
                0.024615385,
                0.6653078,
                0.026017644,
                0.6910547,
                0.028325336,
                0.7141244,
                0.031354934,
                0.73912257,
                0.038048495,
                0.7676008,
                0.049207177,
                0.79235816,
                0.061091263,
                0.81231016,
                0.07250155,
                0.8351886,
                0.08695819,
                0.8593859,
                0.10551518,
                0.88026536,
                0.12631842,
                0.8973962,
                0.14865729,
                0.9085578,
                0.17102662,
                0.9167892,
                0.19126888,
                0.9253629,
                0.21346135,
                0.93190753,
                0.23713444,
                0.9355769,
                0.2605413,
                0.93653846,
                0.28579134,
                0.93653846,
                0.30996853,
                0.93653846,
                0.33277282,
                0.93088216,
                0.35719725,
                0.91677034,
                0.38234195,
                0.8990104,
                0.4043474,
                0.8756963,
                0.42466164,
                0.8475271,
                0.4453151,
                0.8171851,
                0.46401995,
                0.78774893,
                0.47874656,
                0.76124376,
                0.4877933,
                0.73225474,
                0.49076924,
                0.7026677,
                0.49076924,
                0.6766274,
                0.49076924,
                0.65221804,
                0.49076924,
                0.62983793,
                0.49076924,
                0.6070294,
                0.49076924,
                0.58327883,
                0.49076924,
                0.5580502,
                0.49076924,
                0.5323734,
                0.49076924,
                0.50951207,
                0.49076924,
                0.4867412,
                0.49076924,
                0.4866555,
                0.49076924,
                0.51807255,
                0.49076924,
                0.54857534,
                0.49076924,
                0.5737536,
                0.49076924,
                0.59962547,
                0.49076924,
                0.62809634,
                0.49076924,
                0.6561938,
                0.49076924,
                0.6810046,
                0.49164754,
                0.710259,
                0.49395522,
                0.7397152,
                0.49847153,
                0.7673346,
                0.50692564,
                0.7911559,
                0.51908964,
                0.813,
                0.53424615,
                0.83498275,
                0.55248165,
                0.85503435,
                0.57373345,
                0.87384176,
                0.5948604,
                0.8910333,
                0.6160389,
                0.90644544,
                0.63800496,
                0.920679,
                0.6592401,
                0.9315386,
                0.6805701,
                0.9394978,
                0.70292586,
                0.9447185,
                0.72647065,
                0.9461538,
                0.75013345,
                0.9461538,
                0.7744717,
                0.94519234,
                0.79886955,
                0.94010955,
                0.8221989,
                0.9309678,
                0.8443226,
                0.91431123,
                0.86813307,
                0.88847375,
                0.89084285,
                0.86198366,
                0.9072227,
                0.8353322,
                0.92088753,
                0.8077834,
                0.93153864,
                0.781198,
                0.9391286,
                0.7478829,
                0.94754326,
                0.71097916,
                0.9559356,
                0.67760503,
                0.9627819,
                0.64332956,
                0.9675319,
                0.608834,
                0.97,
                0.5736881,
                0.9723077,
                0.5448219,
                0.97384614,
                0.52396536,
                0.97384614,
                0.49734837,
                0.97384614,
                0.46462357,
                0.97384614,
                0.43804455,
                0.9746154,
                0.41195273,
                0.9753846,
                0.37885532,
                0.9753846,
                0.3463518,
                0.9753846,
                0.32095978,
                0.9753846,
                0.30046672,
                0.9736462,
                0.27473697,
                0.9696128,
                0.24756458,
                0.9626031,
                0.224191,
                0.95191246,
                0.200376,
                0.9381018,
                0.17740919,
                0.9219274,
                0.15275586,
                0.9029739,
                0.12711622,
                0.8817571,
                0.10729853,
                0.8613963,
                0.09211116,
                0.84240216,
                0.07656444,
                0.8223601,
                0.06113589,
                0.80372053,
                0.050313555,
                0.78701,
                0.043741748,
                0.7702141};

        float margin = 10f;
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
        }
        else //TODO debug this
        {
            final PathMeasure pm;
            pm = new PathMeasure(path, false);
            final ValueAnimator.AnimatorUpdateListener listener =
                    new ValueAnimator.AnimatorUpdateListener()
                    {
                        float point[] = new float[2];
                        @Override
                        public void onAnimationUpdate (ValueAnimator animation) {
                            float val = animation.getAnimatedFraction();
                            pm.getPosTan(pm.getLength() * val, point, null);
                            handPointer.setTranslationX(point[0]);
                            handPointer.setTranslationY(point[1]);
                        }
                    };
            ValueAnimator a = ValueAnimator.ofFloat(0, 0);
            a.setDuration(5000);
            a.addUpdateListener(listener);
            a.start();
        }
    }

}



























































