package com.minhavida.drawtext;

public class Classification
{
    private float conf;
    private String label;

    Classification(){
        this.conf = -1.0f;
        this.label = "A";
    }

    void update(float conf, String label)
    {
        this.conf = conf;
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public float getConf()
    {
        return conf;
    }

}
