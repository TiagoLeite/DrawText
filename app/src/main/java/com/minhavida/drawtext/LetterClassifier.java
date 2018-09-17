package com.minhavida.drawtext;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LetterClassifier extends TensorFlowClassifier
{
    private static final float THRESHOLD = 0.1f;
    private static final int NUM_CLASSES = 26;
    private TensorFlowInferenceInterface tfHelper;
    private String name, inputName;
    private boolean feedKeepProb;
    private String[] labels;
    private float[] output;
    private String[] outputNames;

    @Override
    public String name() {
        return name;
    }

    private static String[] readLabels(AssetManager am, String filename) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(filename)));
        String line;
        String[] labels = new String[NUM_CLASSES];
        for (int k = 0; k  < NUM_CLASSES ; k++)
        {
            line = br.readLine();
            labels[k] = line;
        }
        br.close();
        return labels;
    }

    /*public static LetterClassifier create(AssetManager am, String name, String modelPath,
                                              String labelFile, String inputName,
                                              String[] outputNames, boolean feedKeepProb) throws IOException
    {
        LetterClassifier tfc = new LetterClassifier();
        tfc.name = name;
        tfc.inputName = inputName;
        tfc.labels = readLabels(am, labelFile);
        tfc.tfHelper = new TensorFlowInferenceInterface(am, modelPath);
        tfc.outputNames = outputNames;
        tfc.output = new float[NUM_CLASSES];
        tfc.feedKeepProb = feedKeepProb;
        return tfc;
    }*/
}
