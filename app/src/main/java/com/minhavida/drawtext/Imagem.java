package com.minhavida.drawtext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Imagem
{
    private int[] pixelsArray;
    private char letter;
    private String path;
    public Imagem (String path, int tol) throws Exception
    {
        this.path = path;
        Bitmap image = BitmapFactory.decodeFile(path);
        //BufferedImage image = ImageIO.read(new File(path));
        int w = image.getWidth();
        int h = image.getHeight();
        //int[] dataBuffInt = new int[w*h];
        //int[] dataBuffInt = image.getRGB(0, 0, w, h, null, 0, w);
        pixelsArray = new int[w*h];
        image.getPixels(pixelsArray, 0, w, 0, 0, w, h);
        //int cont = 0, val;
        /*for (int x = 0; x < dataBuffInt.length; x++)
        {
            Color c = new Color(dataBuffInt[x]);
            *//*System.out.printf("(%2d) ", (cont++));   // = (dataBuffInt[100] >> 16) & 0xFF
            System.out.print(c.getRed()+ " ");   // = (dataBuffInt[100] >> 16) & 0xFF
            System.out.print(c.getGreen()+ " "); // = (dataBuffInt[100] >> 8)  & 0xFF
            System.out.print(c.getBlue()+ " ");  // = (dataBuffInt[100] >> 0)  & 0xFF
            System.out.println("["+c.getAlpha()+"]"); // = (dataBuffInt[100] >> 24) & 0xFF*//*
            val = c.getRed() + c.getBlue() + c.getGreen();
            if (val < tol)
            {
                pixelsArray[x] = 1;
                //System.out.print(" ");
            }
            else
            {
                pixelsArray[x] = 0;
                //System.out.print("0");
            }
            cont++;
            if (cont == w)//new line
            {
                //System.out.println();
                cont = 0;
            }
        }
        //System.out.println();*/
    }
    public double DTW(double[] serie_B, int sizeSerieB, double faixa)
    {
        int i, j, sizeOfThis;
        double dist;
        sizeOfThis = this.pixelsArray.length;
        double dtw[][] = new double[sizeOfThis+1][sizeSerieB+1];
        for(i=1; i<= sizeOfThis; i++)
            dtw[i][0] = Double.MAX_VALUE;

        for(i=1; i<=sizeSerieB; i++)
            dtw[0][i] = Double.MAX_VALUE;

        for(i=1; i <= sizeOfThis; i++)//banda Sakoe Chiba
        {
            for (j = 1; j <= sizeSerieB; j++)
            {
                if (Math.abs((i - 1) - (int) ((j - 1) * (((double) (sizeOfThis - 1) / (double) (sizeSerieB - 1))))) <= (int) (Math.ceil((faixa) * ((double) (sizeOfThis - 1)))))
                {
                    dtw[i][j] = 0;
                    //System.out.print("0");
                }
                else
                {
                    dtw[i][j] = Double.MAX_VALUE;
                    //System.out.print("1");
                }
            }
            //System.out.println();
        }

        dtw[0][0] = 0;

        for(i=1; i <= sizeOfThis; i++)
        {
            for(j=1; j <= sizeSerieB; j++)
            {
                if(dtw[i][j]==0)
                {
                    dist = Math.pow((pixelsArray[i-1] - serie_B[j-1]), 2);
                    dtw[i][j] = dist + minimum(dtw[i-1][j], dtw[i][j-1], dtw[i-1][j-1]);
                }
            }
        }
        return dtw[sizeOfThis][sizeSerieB];
    }

    public double minimum(double a, double b, double c)
    {
        double min;
        min = (a < b) ? a : b;
        min = (min < c) ? min : c;
        return min;
    }

    public int[] getPixelsArray()
    {
        return pixelsArray;
    }

    public int getSizeOfPixelsArray()
    {
        return this.pixelsArray.length;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public char getLetter() {
        return letter;
    }

    /*public ArrayList<Imagem> cropImagem(int tolerancia) throws Exception
    {
        ArrayList<Imagem> letters = new ArrayList<>();
        Bitmap image = BitmapFactory.decodeFile(path);
        int w = image.getWidth();
        int h = image.getHeight();
        int val, lastY = 0, cont = 0, x, y;
        Color color;
        boolean whiteColumn;
        String path;
        for (y = 0; y < w; y++)
        {
            whiteColumn = true;
            for (x = 0; x < h; x++)
            {
                color = new Color(image.getRGB(y, x));
                val = color.getRed() + color.getBlue() + color.getGreen();
                //System.out.println(x + " " + val);
                if (val < tolerancia)
                {
                    whiteColumn = false;
                    break;
                }
            }
            if(whiteColumn)
            {
                if(y-lastY > 5)
                {
                    //System.out.println("y= "+ y + " lastY = "+lastY);
                    BufferedImage dest = image.getSubimage(lastY, 0, (y-lastY), h);//(a, b, c, d) a = col, b = lin,  c = width, d = height
                    path = "C:\\Users\\Minha Vida\\IdeaProjects\\Imagem\\src\\"+(cont) +".jpg";
                    File outputfile = new File(path);
                    dest = resizeImage(dest);
                    ImageIO.write(dest, "jpg", outputfile);
                    lastY = y;
                    //System.out.println("CROP!");
                    letters.add(new Imagem(path, 450));
                    //System.out.println(path);
                    cont++;
                }
                else
                {
                    lastY = y;
                }
            }
        }
        return letters;
    }*/

    /*private BufferedImage resizeImage(BufferedImage originalImage)
    {
        BufferedImage resizedImage = new BufferedImage(50, 50, Image.SCALE_DEFAULT);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 50, 50, null);
        g.dispose();
        return resizedImage;
    }*/
}
