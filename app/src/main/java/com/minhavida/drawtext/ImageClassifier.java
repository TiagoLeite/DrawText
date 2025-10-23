package com.minhavida.drawtext;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageClassifier implements Classifier
{
    private static final String TAG = "ImageClassifier";
    private static final float THRESHOLD = 0.7f;

    private Interpreter tflite;
    private String name;
    private int inputSize;
    private List<String> labels;
    private float[][] output;
    private int numClasses;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Classification recognize(final float[] pixels, int channels)
    {
        // Preparar entrada no formato [1, height, width, channels]
        float[][][][] input = new float[1][inputSize][inputSize][channels];

        // Reorganizar os pixels para o formato correto
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int pixelIndex = i * inputSize + j;
                for (int c = 0; c < channels; c++) {
                    input[0][i][j][c] = pixels[pixelIndex * channels + c];
                }
            }
        }

        // Executar inferência
        tflite.run(input, output);

        // Processar resultados e encontrar a classe com maior confiança
        Classification ans = new Classification();

        for (int i = 0; i < output[0].length; i++)
        {
            if (output[0][i] > THRESHOLD && output[0][i] > ans.getConf()) {
                ans.update(output[0][i], labels.get(i));
            }
        }

        // Log para debug
        Log.d(TAG, "Recognition result: " + ans.getLabel() + " with confidence: " + ans.getConf());

        return ans;
    }

    /**
     * Lê os labels do arquivo de texto
     */
    private static List<String> readLabels(AssetManager am, String filename) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(filename)));
        String line;
        List<String> labels = new ArrayList<>();
        while((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();

        Log.d(TAG, "Loaded " + labels.size() + " labels from " + filename);
        return labels;
    }

    /**
     * Carrega o arquivo .tflite do assets
     */
    private static MappedByteBuffer loadModelFile(AssetManager am, String modelPath) throws IOException
    {
        Log.d(TAG, "Loading model: " + modelPath);

        try {
            //PRIMEIRA TENTATIVA: Usar openFd (mais eficiente, mas não funciona com compressão)
            android.content.res.AssetFileDescriptor fileDescriptor = am.openFd(modelPath);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            inputStream.close();

            Log.d(TAG, "Model loaded successfully using openFd: " + modelPath);
            return buffer;

        } catch (IOException e) {
            // Se falhar (arquivo comprimido), usar método alternativo
            Log.w(TAG, "openFd failed (file might be compressed), using InputStream method...");
            return loadModelFileFromInputStream(am, modelPath);
        }
    }

    private static MappedByteBuffer loadModelFileFromInputStream(AssetManager am, String modelPath) throws IOException
    {
        InputStream inputStream = am.open(modelPath);

        // Ler todo o conteúdo do arquivo para um ByteBuffer
        byte[] modelBytes = new byte[inputStream.available()];
        inputStream.read(modelBytes);
        inputStream.close();

        // Criar ByteBuffer e copiar os dados
        ByteBuffer buffer = ByteBuffer.allocateDirect(modelBytes.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(modelBytes);

        Log.d(TAG, "Model loaded successfully using InputStream: " + modelPath);

        // Retornar como MappedByteBuffer (cast seguro)
        return (MappedByteBuffer) buffer;
    }

    /**
     * Cria uma instância do ImageClassifier
     *
     * @param am AssetManager do Android
     * @param name Nome do classificador
     * @param modelPath Caminho do modelo .tflite no assets
     * @param labelFile Caminho do arquivo de labels no assets
     * @param inputSize Tamanho da entrada (ex: 128x128)
     * @param inputName Nome do tensor de entrada (não usado no TFLite, mas mantido para compatibilidade)
     * @param outputName Nome do tensor de saída (não usado no TFLite, mas mantido para compatibilidade)
     * @param feedKeepProb Flag de keep probability (não usado no TFLite, mas mantido para compatibilidade)
     * @param numClasses Número de classes de saída
     */
    public static ImageClassifier create(AssetManager am, String name, String modelPath,
                                         String labelFile, int inputSize, String inputName,
                                         String outputName, boolean feedKeepProb, int numClasses) throws IOException
    {
        ImageClassifier tfc = new ImageClassifier();
        tfc.name = name;
        tfc.inputSize = inputSize;
        tfc.numClasses = numClasses;
        tfc.labels = readLabels(am, labelFile);

        // Carregar o modelo TFLite
        MappedByteBuffer modelFile = loadModelFile(am, modelPath);

        // Configurar opções do interpretador
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4); // Usar 4 threads para melhor performance

        // Criar o interpretador TFLite
        tfc.tflite = new Interpreter(modelFile, options);

        // Preparar array de saída [1][numClasses]
        tfc.output = new float[1][numClasses];

        Log.d(TAG, "ImageClassifier created successfully");
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Input size: " + inputSize);
        Log.d(TAG, "Number of classes: " + numClasses);

        return tfc;
    }

    /**
     * Libera os recursos do TFLite
     * Deve ser chamado quando o classificador não for mais necessário
     */
    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
            Log.d(TAG, "ImageClassifier closed");
        }
    }

    /**
     * Retorna informações sobre o modelo carregado
     */
    public String getModelInfo() {
        return "ImageClassifier: " + name +
                ", Input size: " + inputSize +
                ", Classes: " + numClasses;
    }
}