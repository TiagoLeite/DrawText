package com.minhavida.drawtext;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifier implements Classifier
{
    private static final float THRESHOLD = 0.1f;
    private static final int NUM_CLASSES = 10;
    private TensorFlowInferenceInterface tfInferenceInterface;
    private String name, inputName, outputName;
    private int inputSize;
    private boolean feedKeepProb;
    private List<String> labels;
    private float[] output;
    private String[] outputNames;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Classification recognize(final float[] pixels, int channels)
    {
        tfInferenceInterface.feed(inputName, pixels, 1, inputSize, inputSize, channels);
        /*if (feedKeepProb)
            tfInferenceInterface.feed("keep_prob", new float[]{1});*/
        tfInferenceInterface.run(outputNames);
        tfInferenceInterface.fetch(outputName, output);

        Classification ans = new Classification();

        for (int i = 0; i < output.length; i++)
        {
            Log.d("debug", output[i]+"");
            if (output[i] > THRESHOLD && output[i] > ans.getConf())
                ans.update(output[i], labels.get(i));
        }
        return ans;
    }

    private static List<String> readLabels(AssetManager am, String filename) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(filename)));
        String line;
        List<String> labels = new ArrayList<>();
        while((line = br.readLine()) != null)
            labels.add(line);
        br.close();
        return labels;
    }

    public static ImageClassifier create(AssetManager am, String name, String modelPath,
                                         String labelFile, int inputSize, String inputName,
                                         String outputName, boolean feedKeepProb) throws IOException
    {
        ImageClassifier tfc = new ImageClassifier();
        tfc.name = name;
        tfc.inputName = inputName;
        tfc.outputName = outputName;
        tfc.labels = readLabels(am, labelFile);
        tfc.tfInferenceInterface = new TensorFlowInferenceInterface(am, modelPath);
        tfc.inputSize = inputSize;
        tfc.outputNames = new String[]{outputName};
        tfc.outputName = outputName;
        tfc.output = new float[NUM_CLASSES];
        tfc.feedKeepProb = feedKeepProb;
        return tfc;
    }

}



















