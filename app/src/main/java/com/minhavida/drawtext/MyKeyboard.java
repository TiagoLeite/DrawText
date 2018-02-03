package com.minhavida.drawtext;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;

import java.io.IOException;

public class MyKeyboard extends InputMethodService implements CanvasView.CanvasListener
{
    private ViewGroup kv;
    private CanvasView canvasView;
    private Keyboard keyboard;
    private TensorFlowClassifier tfClassifier;

    @Override
    public View onCreateInputView()
    {
        kv = (ViewGroup) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.layout.qwerty);
        canvasView = (CanvasView)kv.findViewById(R.id.canvas_view);
        canvasView.setListener(this);
        //((KeyboardView)kv.findViewById(R.id.keyboard)).setKeyboard(keyboard);
        //((KeyboardView)kv.findViewById(R.id.keyboard)).setOnKeyboardActionListener(this);

        kv.findViewById(R.id.bt_backspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                InputConnection ic = getCurrentInputConnection();
                ic.deleteSurroundingText(1, 0);
            }
        });

        loadModel();
        return kv;
    }

    @Override
    public boolean isFullscreenMode() {
        return false;
    }

    @Override
    public void onFinish()
    {
        InputConnection ic = getCurrentInputConnection();
        String userText = performInference();
        ic.commitText(userText,1);
        /*switch (primaryCode)
        {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code)){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }*/
    }

    private void loadModel()
    {
        try
        {
            tfClassifier = TensorFlowClassifier.create(getAssets(), "TensorFlow", "mnist_model_graph.pb",
                    "labels.txt", 28, "input", "output", true);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error initializing classifiers!", e);
        }
    }

    private String performInference()
    {
        String text;
        float[] arrayImage = canvasView.getPixelsArray();
        Classification cls = tfClassifier.recognize(arrayImage);
        if (cls.getLabel() == null)
            text = tfClassifier.name() + "[?]";
        else
            text = cls.getLabel();
        return text;
    }

    /*@Override
    public void onKey(int primaryCode, int[] keyCodes)
    {
        InputConnection ic = getCurrentInputConnection();
        switch (primaryCode)
        {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code)){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }
    }*/

}
