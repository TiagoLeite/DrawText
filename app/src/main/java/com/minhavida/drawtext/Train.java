package com.minhavida.drawtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ListView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Train
{
    public static int HEIGHT = 50;
    public static int WIDHT = 50;
    public static double FACTOR = 0.7;
    public static List<Character> list;
    public static Context context;

    //Resize: yourImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
    public static void init() throws Exception
    {
        ArrayList<Imagem> bancoImagem = new ArrayList<>();
        String path;
        list = new ArrayList<>();
        for(char c = 'a'; c <= 'a'; c++)
        {
            System.out.println("ch = " + c + " de " + list.size());
            //path = "C:\\Users\\Minha Vida\\Documents\\DrawText\\app\\src\\" + c;
            int i = 0;
            Character charac = new Character(c);
            list.add(charac);
            for(i = 0; i < 1; i++)
            {
                //InputStream ins = context.getResources().openRawResource(R.raw.a0);
                InputStream ins = context.getResources().openRawResource(
                        context.getResources().getIdentifier(c+""+i,
                                "raw", context.getPackageName()));
                //InputStream is = context.getResources().openRawResource(R.raw.);
                //BitmapFactory.decodeStream(ins);
                Imagem im = new Imagem(ins, 650);
                im.setLetter(c);
                bancoImagem.add(im);
                charac.train(im);
            }
            //charac.print();
            //System.out.println();
        }
        /*for(char c = 'A'; c <= 'Z'; c++)
        {
            try
            {
                path = "C:\\Users\\Minha Vida\\IdeaProjects\\Imagem\\src\\" + c;
                for(int i=0; i<10; i++)
                {
                    try
                    {
                        Imagem im = new Imagem(path.concat("\\"+ i + ".jpg"), 650);
                        im.setLetter(c);
                        bancoImagem.add(im);
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
            catch (Exception e)
            {

            }
        }*/
        /*Imagem word = new Imagem("C:\\Users\\Minha Vida\\IdeaProjects\\Imagem\\src\\word.jpg", 600);
        ArrayList<Imagem> letters = word.cropImagem(400);
        for(Imagem letter : letters)
        {*/

        /*}
        System.out.println();*/
    }

    public static char findLetter(int vetor[])
    {
        long start, finish;
        System.out.println("Start find letter " + list.size());
        Imagem letter = new Imagem(vetor);
        double dtw, bestMatch = Double.MAX_VALUE;
        char c = '0';
        for(Character character : list)
        {
            start = System.currentTimeMillis();
            dtw = letter.DTW(character.getLetterVet(), HEIGHT*WIDHT, 0.001);//0.25% de banda
            finish = System.currentTimeMillis();
            if(dtw < bestMatch)
            {
                bestMatch = dtw;
                c = character.getCharacter();
                //System.out.println(c+" >>> "+bestMatch);
            }
            System.out.println("Time = " + (finish-start)/1000f+" s");
        }
        System.out.println(c+"("+bestMatch+")");
        System.out.println("Finish find letter");
        return c;
    }

    static class Character
    {
        private char character;
        private double[][] letterVet;
        public Character(char c)
        {
            this.character = c;
            this.letterVet = new double[(HEIGHT+2)][(WIDHT+2)];
        }

        public char getCharacter() {
            return character;
        }

        public void train(Imagem imagem)
        {
            int vet_image[] = imagem.getPixelsArray();
            int n = imagem.getSizeOfPixelsArray();
            for(int i = 0 ; i < HEIGHT; i++)
            {
                for (int j = 0; j < WIDHT; j++)
                    if(vet_image[WIDHT*i+j] != -1)
                    {
                        letterVet[i+1][j+1] = 1;
                        //letterVet[i+1][j+1] = vet_image[WIDHT*i+j];
                    }
                    else
                        letterVet[i+1][j+1] = 0;

            }
            //expand(letterVet);
            /*expand(letterVet);
            expand(letterVet);
            expand(letterVet);
            expand(letterVet);
            expand(letterVet);*/
            print();
        }

        private void expand(double vet[][])
        {
            for (int i = 1; i <= HEIGHT; i++)
            {
                for(int j = 1; j <= WIDHT; j++)
                {
                    if(vet[i][j] != 0)
                    {
                        vet[i][j+1] = Math.max(vet[i][j+1], vet[i][j]*FACTOR);
                        vet[i][j-1] = Math.max(vet[i][j-1], vet[i][j]*FACTOR);
                        vet[i-1][j] = Math.max(vet[i-1][j], vet[i][j]*FACTOR);
                        vet[i+1][j] = Math.max(vet[i+1][j], vet[i][j]*FACTOR);
                    }
                }
            }
        }

        public void print()
        {
            for (int i =0 ; i < HEIGHT; i++)
            {
                for (int j =0 ; j < WIDHT; j++)
                {
                    //if(letterVet[i][j] != 0)
                    System.out.printf("%d", Math.round(letterVet[i][j]));
                }
                System.out.println();
            }
        }

        public double[] getLetterVet()
        {
            double []vet = new double[HEIGHT*WIDHT];
            for (int i = 1; i <= HEIGHT; i++)
                for (int j = 1; j <= WIDHT; j++)
                    vet[(i-1)*WIDHT+(j-1)] = letterVet[i][j];
            //System.arraycopy();
            return vet;
        }
    }
}









