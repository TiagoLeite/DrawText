package com.minhavida.drawtext;

import java.io.Serializable;
import java.util.Random;

public class Test implements Serializable
{
    private double[] input = new double[784];
    private double[] expected = new double[10];
    private int pos;
    private int expectedPos;
    private int id;
    private double error;

    public Test(int id)
    {
        this.id = id;
        pos = 0;
    }

    public void addExpected(String s)
    {
        expectedPos = Integer.valueOf(s);
        expected[expectedPos] = 1;
        for (int i = 0; i < expected.length; i++)
        {
            if (i == expectedPos)
                continue;
            expected[i] = 0;
        }
    }

    public void addInput(String s)
    {
        for (int i = 0; i < s.length(); i++)
        {
            input[pos++] = Double.parseDouble(String.valueOf(s.charAt(i)));
        }
    }

    public double[] getExpected() {
        return expected;
    }

    public double[] getInput() {
        return input;
    }

    public void print()
    {
        for (int i = 0; i < 10; i++)
        {
            System.out.print((int)expected[i]);
        }
        System.out.println();
        for (int i = 0; i < 20; i++)
        {
            for (int j = 0; j < 20; j++)
                System.out.print((int)input[i*20+j]);
            System.out.println();
        }
        System.out.println();
    }

    public int getPos() {
        return expectedPos;
    }

    public int getId() {
        return id;
    }

    public void updateError(boolean flag)
    {
        if (flag)
            this.error = 0;
        else
            this.error = 1;
    }

    public double getError() {
        return error;
    }

    public void putNoise()//add some noise (random reversed values) to the test
    {
        Random rand = new Random();
        int size = input.length;
        for (int i = 0; i < size; i++)
        {
            if(rand.nextInt()%16==0)
                input[i] = 1 - (int)input[i];
        }
    }

    public void setInput(double[] input) {
        this.input = input;
    }

}
