package com.minhavida.drawtext;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ContentProvider;
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
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class CanvasView extends View
{
    public int width;
    public int height;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private ImageView handPointer;
    private Paint paint;
    private float mx, my;
    private static final float TOLERANCE = 10;
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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ViewGroup)this.getParent()).addView(handPointer);

        final Path path = new Path();

        double vet[] = new double[]{
                130.36581,
                228.90253,
                134.32236,
                217.35529,
                139.25128,
                205.49744,
                145.39507,
                194.60492,
                154.41661,
                183.58337,
                165.05943,
                173.94055,
                175.92502,
                166.04999,
                187.66306,
                157.22464,
                200.68274,
                149.21152,
                214.49226,
                142.16925,
                227.3614,
                138.21286,
                241.93402,
                133.26651,
                253.97339,
                130.0,
                266.23193,
                127.0,
                277.29294,
                124.42676,
                290.96658,
                124.0,
                301.86017,
                122.569916,
                313.52887,
                121.0,
                324.31653,
                119.0,
                336.57486,
                119.0,
                350.97812,
                118.0,
                363.46622,
                117.0,
                377.39328,
                117.0,
                389.23776,
                117.0,
                404.11044,
                117.0,
                417.96527,
                117.0,
                431.70352,
                117.0,
                444.48093,
                117.0,
                455.49603,
                118.832,
                469.52777,
                124.763885,
                482.09012,
                130.69672,
                495.85544,
                137.61847,
                506.6427,
                143.09515,
                518.0483,
                149.52414,
                530.90955,
                158.90955,
                543.1718,
                172.56244,
                551.6884,
                187.7211,
                559.62775,
                206.56934,
                564.2522,
                224.00891,
                569.2083,
                239.625,
                573.17126,
                253.68506,
                573.0,
                264.154,
                573.0,
                275.11798,
                573.0,
                288.99786,
                573.0,
                299.90588,
                573.0,
                311.64386,
                570.1914,
                326.42584,
                562.4192,
                342.16168,
                554.50946,
                354.49054,
                543.66724,
                367.33276,
                534.0723,
                377.92767,
                522.78906,
                387.21094,
                507.49597,
                396.50134,
                494.5807,
                402.47308,
                481.81213,
                406.87512,
                466.8392,
                412.3869,
                453.5744,
                416.35638,
                437.24307,
                419.29285,
                421.9638,
                420.0,
                409.11322,
                420.0,
                396.56424,
                420.0,
                383.50717,
                420.0,
                369.77298,
                420.0,
                357.967,
                420.0,
                345.0898,
                420.0,
                332.19995,
                420.0,
                320.2333,
                420.0,
                333.82413,
                420.0,
                345.49957,
                422.37488,
                363.65564,
                422.0,
                375.948,
                422.0,
                389.7187,
                422.0,
                399.84573,
                422.0,
                410.04074,
                422.0,
                426.63266,
                422.0,
                440.33475,
                422.0,
                450.4955,
                423.0,
                463.1516,
                425.05054,
                475.01044,
                428.00348,
                488.76,
                431.95203,
                500.6417,
                436.82086,
                514.6748,
                447.6748,
                528.5326,
                459.5326,
                541.3917,
                474.08752,
                547.6408,
                485.92236,
                553.6055,
                498.81653,
                559.11945,
                509.6792,
                565.51636,
                521.549,
                569.0,
                533.9138,
                569.0,
                545.7555,
                569.0,
                559.9042,
                569.0,
                571.47687,
                569.0,
                585.4996,
                569.0,
                596.33185,
                567.95306,
                607.14075,
                562.96045,
                619.07916,
                559.0535,
                632.7859,
                555.08844,
                647.64636,
                550.13153,
                657.7369,
                541.2055,
                670.58905,
                530.5635,
                680.71826,
                514.6984,
                687.65076,
                501.9054,
                696.5473,
                488.04834,
                702.4758,
                472.66998,
                707.0,
                460.81326,
                710.3956,
                440.61682,
                714.34045,
                419.4966,
                717.63696,
                405.97653,
                720.25586,
                390.88657,
                723.5283,
                376.3112,
                726.0,
                364.64008,
                728.0,
                349.66464,
                728.0,
                330.7968,
                728.0,
                310.05237,
                728.0,
                299.16302,
                728.0,
                284.42197,
                726.10547,
                263.6019,
                721.1505,
                249.79248,
                715.39624,
                235.9948,
                708.49744,
                220.17514,
                699.88135,
                201.50484,
                681.5048,
                187.19116,
                668.19116,
                176.27998,
                658.27997,
                165.48703,
                643.4784,
                156.04536,
                628.04535,
                148.15005,
                612.8752,
                141.6164,
                598.2328,
                137.64882,
                587.9465};

        for (int k=2; k<vet.length-2; k+=2)
        {
            path.moveTo((float)vet[k-2]-10, (float) vet[k-1]-10);
            path.lineTo( (float)vet[k]-10, (float) vet[k+1]-10);
        }
//        path.moveTo(100f, 100f);
//        path.lineTo(200f, 10f);

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
            a.setDuration(2000);
            a.addUpdateListener(listener);
            a.start();
        }
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
            Log.d("canvas", x+",");
            Log.d("canvas", y+",");
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
}



























































