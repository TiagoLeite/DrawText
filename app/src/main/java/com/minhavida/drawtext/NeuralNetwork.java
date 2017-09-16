package com.minhavida.drawtext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork
{
    public static double LEARNING_SPEED;
    public static double NETWORK_TOLERANCE;
    public static double NETWORK_MOMENTUM;
    private int layersNumber;
    private int inputSize, outputSize;
    private List<NeuronLayer> neuronLayers;

    public NeuralNetwork(int inputSize, int outputSize, int layersNumber, double learningSpeed, double momentum,
                         double tolerance) {
        this.layersNumber = layersNumber;
        this.neuronLayers = new ArrayList<>(layersNumber);
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        NeuralNetwork.LEARNING_SPEED = learningSpeed;
        NeuralNetwork.NETWORK_TOLERANCE = tolerance;
        NeuralNetwork.NETWORK_MOMENTUM = momentum;
        mountLayers();
    }

    public void train(double[] input, double[] expected)
    {
        forward(input);
        backward(input, expected);
    }

    public void forward(double[] input)
    {
        for(NeuronLayer layer : neuronLayers)
        {
            layer.forward(input);
            input = new double[layer.getLayerSize()];
            int i = 0;
            for (Neuron n : layer.getNeurons())
                input[i++] = n.getY();
            //layer = neuronLayers.get(1);
            //System.out.println("==============");
            //layer.forward(input);
        }
    }

    public void backward(double[] x, double[] expected) // x = input array from training values
    {
        int layerIndex = neuronLayers.size()-1;
        NeuronLayer nextLayer;
        NeuronLayer layer = neuronLayers.get(layerIndex--);//gets the last layer (output layer)
        NeuronLayer previousLayer = neuronLayers.get(layerIndex--);//previous of last layer
        int index = 0;
        for (Neuron neuron : layer.getNeurons())
        {
            neuron.updateDelta(expected[index++]);
            //neuron.updateError(expected[index++]); //take care of the index!!!
            //System.out.println("Expected>"+expected[index-1]);
            neuron.updateWeights(previousLayer.getNeurons());
        }
        while (layerIndex >= 0)
        {
            nextLayer = layer;
            layer = previousLayer;
            previousLayer = neuronLayers.get(layerIndex--);
            for (Neuron neuron : layer.getNeurons())
            {
                neuron.updateDelta(nextLayer.getNeurons()); //we dont need to update error here (only in last layer)
                neuron.updateWeights(previousLayer.getNeurons());
            }
        }
        //just need now to update the first layer
        nextLayer = layer;
        layer = previousLayer;

        for (Neuron neuron : layer.getNeurons())
        {
            neuron.updateDelta(nextLayer.getNeurons());
            neuron.updateWeights(x);
        }
    }

    protected void mountLayers()//mounting 2 layers to the network
    {
        NeuronLayer previousLayer;
        NeuronLayer layer = new NeuronLayer(inputSize, inputSize+1);//First layer, +1 because of bias
        addLayer(layer);
        previousLayer = layer;
        layer = new NeuronLayer(100, previousLayer.getLayerSize()+1);//Secondvlayer
        addLayer(layer);
        previousLayer = layer;
        layer = new NeuronLayer(outputSize, previousLayer.getLayerSize()+1);//Third (and last) layer
        addLayer(layer);


        //apagar depois:
        //n = layer.getNeurons();
        //n[0].setW(new double[]{.1, .8, .5});
        //addLayer(layer);
    }

    private void addLayer(NeuronLayer layer)
    {
        if(neuronLayers.isEmpty())
        {
            neuronLayers.add(layer);
            layer.setLastLayer(true);
            layer.setFirstLayer(true);
        }
        else
        {
            neuronLayers.get(neuronLayers.size()-1).setLastLayer(false);
            layer.setLastLayer(true);
            neuronLayers.add(layer);
        }
    }

    public int getLayersNumber() {
        return layersNumber;
    }

    public NeuronLayer getLayerAt(int pos)
    {
        return neuronLayers.get(pos);
    }

    public NeuronLayer getOutputLayer()
    {
        return neuronLayers.get(neuronLayers.size()-1);
    }

    public static void setLearningSpeed(double learningSpeed) {
        LEARNING_SPEED = learningSpeed;
    }

    public static double getLearningSpeed() {
        return LEARNING_SPEED;
    }

    public void saveToFile(String fileName)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(this.toString());
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                } catch (Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    @Override
    public String toString() {
        String string = "";
        for (NeuronLayer layer : neuronLayers)
        {
            for (Neuron neuron : layer.getNeurons())
            {
                string  = string.concat(neuron.getWToString());
                string = string.concat("\n");
            }
        }
        return string;
    }

    public List<NeuronLayer> getNeuronLayers() {
        return neuronLayers;
    }
}





















