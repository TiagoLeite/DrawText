package com.minhavida.drawtext;

import java.io.Serializable;

public class NeuronLayer implements Serializable
{
    private boolean isFirstLayer;
    private boolean isLastLayer;
    private int layerSize; // numero de neuronios da camada
    private int inputSize; // numero de pesos ligados a cada neuronio da camada ( = numero de entradas no neuronio)

    private Neuron[] neurons;

    public NeuronLayer(int size, int inputSize)
    {
        this.layerSize = size;
        this.inputSize = inputSize;
        this.neurons = new Neuron[size];
        initNeurons();
    }

    public void forward(double[] input)
    {
        for (Neuron n : neurons)
        {
            n.output(input);
        }
    }

    private void initNeurons()
    {
        for (int i = 0; i < layerSize; i++)
        {
            neurons[i] = new Neuron(inputSize, i);
        }
    }

    public Neuron[] getNeurons() {
        return neurons;
    }

    public void setFirstLayer(boolean firstLayer) {
        isFirstLayer = firstLayer;
    }

    public void setLastLayer(boolean lastLayer) {
        isLastLayer = lastLayer;
    }

    public int getLayerSize() {
        return layerSize;
    }

    public boolean isFirstLayer() {
        return isFirstLayer;
    }

    public boolean isLastLayer() {
        return isLastLayer;
    }

    public void printOutput()
    {
        for (Neuron neuron : neurons)
        {
            neuron.printOutput();
        }
    }

    //Returns the neuron with be highest output value
    public Neuron getHighestNeuron()
    {
        Neuron best = neurons[0];
        for (Neuron neuron : neurons)
        {
            System.out.print(neuron.getY()+"|");
            if (best.getY() < neuron.getY())
                best = neuron;
        }
        return best;
    }

    //Returns the root mean squared error of the output of the neurons in this layer
    public double getRootMeanSquaredError()
    {
        double error = 0;
        for (Neuron neuron : neurons)
            error += neuron.getError();
        error  = error/neurons.length;
        //error = Math.sqrt(error);
        return error;
    }

    public void updateNeuronsErrors(double[] expected)
    {
        for (Neuron neuron : neurons)
            neuron.updateError(expected[neuron.getIndex()]);
    }

}






