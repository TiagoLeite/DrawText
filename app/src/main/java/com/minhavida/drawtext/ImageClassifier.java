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
        // Medir tempo de inferência
        long startTime = System.currentTimeMillis();
        
        // Log de debug da entrada
        Log.d(TAG, "========================================");
        Log.d(TAG, "📥 ENTRADA DO MODELO:");
        Log.d(TAG, "  Array size: " + pixels.length);
        Log.d(TAG, "  Expected size: " + (inputSize * inputSize));
        Log.d(TAG, "  Channels: " + channels);
        Log.d(TAG, "  Input shape: [1, " + inputSize + ", " + inputSize + ", " + channels + "]");
        
        // Verificar alguns valores
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE, sum = 0;
        int nonZeroCount = 0;
        for (float pixel : pixels) {
            min = Math.min(min, pixel);
            max = Math.max(max, pixel);
            sum += pixel;
            if (pixel > 0) nonZeroCount++;
        }
        Log.d(TAG, "  Pixel range: [" + min + ", " + max + "]");
        Log.d(TAG, "  Average: " + (sum / pixels.length));
        Log.d(TAG, "  Non-zero pixels: " + nonZeroCount + " (" + 
              String.format("%.1f%%", 100.0 * nonZeroCount / pixels.length) + ")");
        Log.d(TAG, "========================================");
        
        // Preparar entrada no formato [1, height, width, channels]
        float[][][][] input = new float[1][inputSize][inputSize][channels];

        // Reorganizar os pixels para o formato correto
        // IMPORTANTE: pixels[] é um array flat de tamanho inputSize*inputSize
        // Cada pixel tem apenas 1 valor (grayscale), não múltiplos canais
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int pixelIndex = i * inputSize + j;
                // Para grayscale (1 canal), simplesmente copiar o valor
                input[0][i][j][0] = pixels[pixelIndex];
            }
        }

        // Executar inferência
        long inferenceStart = System.currentTimeMillis();
        tflite.run(input, output);
        long inferenceTime = System.currentTimeMillis() - inferenceStart;

        // Processar resultados e encontrar a classe com maior confiança
        Classification ans = new Classification();

        // Encontrar top 3 predições para logging
        float[] confidences = new float[output[0].length];
        System.arraycopy(output[0], 0, confidences, 0, output[0].length);
        
        for (int i = 0; i < output[0].length; i++)
        {
            if (output[0][i] > THRESHOLD && output[0][i] > ans.getConf()) {
                ans.update(output[0][i], labels.get(i));
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // ========== LOGS DETALHADOS ==========
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 PREDIÇÃO DO MODELO - " + name);
        Log.d(TAG, "========================================");
        Log.d(TAG, "⏱️  Tempo de inferência: " + inferenceTime + "ms");
        Log.d(TAG, "⏱️  Tempo total: " + totalTime + "ms");
        Log.d(TAG, "----------------------------------------");
        
        // Encontrar e mostrar top 3 predições
        int[] topIndices = getTopKIndices(confidences, 3);
        Log.d(TAG, "📊 TOP 3 PREDIÇÕES:");
        for (int i = 0; i < topIndices.length && i < 3; i++) {
            int idx = topIndices[i];
            String label = labels.get(idx);
            float conf = confidences[idx];
            String bar = getConfidenceBar(conf);
            Log.d(TAG, String.format("  %d. %s: %.2f%% %s", 
                (i+1), label, conf * 100, bar));
        }
        
        Log.d(TAG, "----------------------------------------");
        Log.d(TAG, "✅ RESULTADO FINAL: " + ans.getLabel() + 
                   " (confiança: " + String.format("%.2f%%", ans.getConf() * 100) + ")");
        
        if (ans.getConf() < THRESHOLD) {
            Log.w(TAG, "⚠️  AVISO: Confiança abaixo do threshold (" + 
                  String.format("%.2f%%", THRESHOLD * 100) + ")");
        }
        
        // Mostrar todas as probabilidades (para debug detalhado)
        Log.d(TAG, "----------------------------------------");
        Log.d(TAG, "📋 TODAS AS PROBABILIDADES:");
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > 0.01) { // Mostrar apenas > 1%
                Log.d(TAG, String.format("  %s: %.2f%%", 
                    labels.get(i), output[0][i] * 100));
            }
        }
        Log.d(TAG, "========================================");

        return ans;
    }
    
    /**
     * Retorna os índices das top K predições
     */
    private int[] getTopKIndices(float[] array, int k) {
        int[] indices = new int[Math.min(k, array.length)];
        float[] copy = new float[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        
        for (int i = 0; i < indices.length; i++) {
            int maxIdx = 0;
            for (int j = 1; j < copy.length; j++) {
                if (copy[j] > copy[maxIdx]) {
                    maxIdx = j;
                }
            }
            indices[i] = maxIdx;
            copy[maxIdx] = -1; // Marcar como usado
        }
        
        return indices;
    }
    
    /**
     * Gera barra visual de confiança
     */
    private String getConfidenceBar(float confidence) {
        int bars = (int)(confidence * 20); // 20 caracteres max
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < bars ? "█" : "░");
        }
        sb.append("]");
        return sb.toString();
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