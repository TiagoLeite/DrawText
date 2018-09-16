package com.minhavida.drawtext;


public interface Classifier
{
    String name();
    Classification recognize(final float pixels[], int channels);
}
