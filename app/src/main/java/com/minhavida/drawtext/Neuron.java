package com.minhavida.drawtext;

import java.util.Random;

public class Neuron
{
    private double[] w; //vetor de pesos
    private double[] momentum;
    private double s; //saida do neuronio (antes de passar pela funcao de ativacao)
    private double y; //saida do neuronio apos passar pela funcao de ativacao;
    private double delta;
    private int index;
    private double error;

    public Neuron(int wSize, int index) {
        w = new double[wSize];
        momentum = new double[wSize];
        this.index = index;
        initWeights();
    }

    private void initWeights()
    {
        int size = w.length;
        Random rand = new Random();
        for (int i = 0; i < size; i++)
        {
            w[i] = (rand.nextDouble())-0.5f;// interval [-0.5, 0.5]
            //w[i] = (rand.nextDouble()*0.1f);//
            //System.out.println(w[i]);
        }
    }

    public void output(double[] input)
    {
        int size = input.length;
        s = w[0]*(-1f); //bias
        for (int i = 0; i < size; i++)
        {
            s += w[i+1]*input[i];
        }
        y = activationFunction(s);
        //System.out.printf("%1.3f|%1.3f\n", s, y);
        //System.out.println(y+"\n");
    }

    public void updateDelta(double expected)
    {
        delta = (expected - y)*activationFunctionDerivative(s);
    }

    public void updateError(double expected)//root squared error
    {
        error = Math.abs((expected - y));//*(expected - y));
        //error = (expected - y);
        //error *= error;
    }

    public void updateDelta(Neuron[] neuronsInNextLayer)
    {
        delta = 0f;
        for (Neuron neuron : neuronsInNextLayer)
            delta += neuron.getDelta()*neuron.getWAt(this.index);
        delta *= activationFunctionDerivative(this.s);
    }

    public void updateWeights(Neuron[] neuronsInPreviousLayer)
    {
        int size = neuronsInPreviousLayer.length;
        double n = NeuralNetwork.LEARNING_SPEED;
        double alpha = NeuralNetwork.NETWORK_MOMENTUM;
        double variation;
        w[0] += n*delta*(-1f); //bias
        for (int i = 0; i < size; i++)
        {
            variation = n*delta*(neuronsInPreviousLayer[i].getY());// + alpha*momentum[i+1];
            w[i+1] += variation;
            //momentum[i+1] = variation;
        }
    }

    public void updateWeights(double[] input)
    {
        int size = input.length;
        double alpha = NeuralNetwork.NETWORK_MOMENTUM;
        double n = NeuralNetwork.LEARNING_SPEED;
        double variation;
        w[0] += n*delta*(-1f); //bias
        for (int i = 0; i < size; i++)
        {
            variation = n*delta*(input[i]);// + alpha*momentum[i+1];
            w[i+1] += variation;
            //momentum[i+1] = variation;
        }
    }

    //Logistic function:
    public double activationFunction(double val) {
        return Math.exp(val)/(Math.exp(val)+1f) ;
    }

    //Logistic function's derivative:
    public double activationFunctionDerivative(double val)
    {
        //System.out.println((activationFunction(val)*(1f-activationFunction(val))));
        return (activationFunction(val)*(1f-activationFunction(val)));
    }

    public double getY() {
        return y;
    }

    public double getDelta() {
        return delta;
    }

    public double getWAt(int pos)
    {
        return w[pos];
    }

    public void printOutput()
    {
        System.out.printf("(%.3f)%.3f|", this.s,  this.y);
    }

    public int getIndex() {
        return index;
    }

    public double getError() {
        return error;
    }

    public double[] getW() {
        return w;
    }

    public String getWToString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length; i++)
        {
            sb.append(" ");
            sb.append(w[i]);
        }
        return String.valueOf(sb);
    }

    public void setWAt(int pos, double w) {
        this.w[pos] = w;
    }
}
