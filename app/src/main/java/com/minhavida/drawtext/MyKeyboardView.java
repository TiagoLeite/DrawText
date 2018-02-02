package com.minhavida.drawtext;

import android.content.Context;
import android.graphics.Canvas;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class MyKeyboardView extends KeyboardView
{
    private CanvasView canvasView;
    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        canvasView = new CanvasView(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas = canvasView.getCanvas();
        super.onDraw(canvas);
    }
}
