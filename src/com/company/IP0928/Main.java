package com.company.IP0928;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;

public class Main {


    /**
     * Magic number representing the binary PGM file type.
     */
    private static final String MAGIC = "P5";
    /**
     * Character indicating a comment.
     */
    private static final char COMMENT = '#';
    /**
     * The maximum gray value.
     */
    private static final int MAXVAL = 255;


    /*Scale Factor*/
    private static float sX = 5f;
    private static float sY = 5f;

    public static void main(String[] args) throws IOException {

        String filePath = "0928test-image.pgm";
        int[][] inputImage = readPGMFile(filePath);

        write(
                d1Tod2(
                        resizeBilinearGray(
                                d2Tod1(inputImage),
                                inputImage[0].length,
                                inputImage.length,
                                (int)(inputImage[0].length * sX),
                                (int)(inputImage.length * sY)),
                        (int) (inputImage[0].length * sX)),
                new File("out_" + sX + "_" + sY + "_" + ".pgm"), MAXVAL);
    }


    public static void write(final int[][] image, final File file, final int maxval) throws IOException {
        if (maxval > MAXVAL)
            throw new IllegalArgumentException("The maximum gray value cannot exceed " + MAXVAL + ".");
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        try {
            stream.write(MAGIC.getBytes());
            stream.write("\n".getBytes());
            stream.write(Integer.toString(image[0].length).getBytes());
            stream.write(" ".getBytes());
            stream.write(Integer.toString(image.length).getBytes());
            stream.write("\n".getBytes());
            stream.write(Integer.toString(maxval).getBytes());
            stream.write("\n".getBytes());
            for (int i = 0; i < image.length; ++i) {
                for (int j = 0; j < image[0].length; ++j) {
                    final int p = image[i][j];
                    if (p < 0 || p > maxval)
                        throw new IOException("Pixel value " + p + " outside of range [0, " + maxval + "].");
                    stream.write(image[i][j]);
                }
            }
        } finally {
            stream.close();
        }
    }



    private static int[][] readPGMFile(String filePath) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(filePath);
        Scanner scan = new Scanner(fileInputStream);
        // Discard the magic number
        scan.nextLine();
        // Discard the comment line
        scan.nextLine();
        // Read pic width, height and max value
        int picWidth = scan.nextInt();
        int picHeight = scan.nextInt();
        int maxvalue = scan.nextInt();

        int[][] image = new int[picHeight][picWidth];


        fileInputStream.close();

        // Now parse the file as binary data
        fileInputStream = new FileInputStream(filePath);
        DataInputStream dis = new DataInputStream(fileInputStream);

        // look for 4 lines (i.e.: the header) and discard them
        int numnewlines = 4;
        while (numnewlines > 0) {
            char c;
            do {
                c = (char) (dis.readUnsignedByte());
            } while (c != '\n');
            numnewlines--;
        }

        for (int row = 0; row < picHeight; row++) {
            for (int col = 0; col < picWidth; col++) {
                image[row][col] = dis.readUnsignedByte();
            }
        }

        return image;
    }


    static public int[] d2Tod1(int[][] array) {

        int[] newArray = new int[array.length * array[0].length];

        for (int i = 0; i < array.length; i++)
            for (int j = 0; j < array[i].length; j++) {
                newArray[i * array[0].length + j] = array[i][j];
            }

        return newArray;
    }

    static public int[][] d1Tod2(int[] array, int width) {

        int[][] newArray = new int[array.length / width][width];

        for (int i = 0; i < array.length; ++i) {
            newArray[i / width][i % width] = array[i];
        }

        return newArray;
    }

    public static int[] resizeBilinearGray(int[] inputImagePixels, int inputWidth, int inputHeight, int outWidth, int outHeight) {

        int[] temp = new int[outWidth * outHeight];
        int A, B, C, D, x, y, index, gray;

        float x_ratio = ((float) (inputWidth)) / outWidth;
        float y_ratio = ((float) (inputHeight)) / outHeight;

        float x_diff, y_diff;

        int offset = 0;

        for (int i = 0; i < outHeight; i++) {
            for (int j = 0; j < outWidth; j++) {
                x = (int) (x_ratio * j);
                y = (int) (y_ratio * i);
                x_diff = (x_ratio * j) - x;
                y_diff = (y_ratio * i) - y;
                index = y * inputWidth + x;

                if (index + inputWidth + 1 < inputImagePixels.length) {

                    A = inputImagePixels[index];
                    B = inputImagePixels[index + 1] ;
                    C = inputImagePixels[index + inputWidth] ;
                    D = inputImagePixels[index + inputWidth + 1] ;

                    gray = (int) (
                            A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff) +
                                    C * (y_diff) * (1 - x_diff) + D * (x_diff * y_diff)
                    )   ;

                    temp[offset++] = gray;
                } else if (index + inputWidth < inputImagePixels.length){

                    A = inputImagePixels[index] ;
                    B = inputImagePixels[index + 1] ;
                    C = inputImagePixels[index + inputWidth] ;


                    gray = (int) (
                            A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff) +
                                    C * (y_diff) * (1 - x_diff)
                    );

                    temp[offset++] = gray;
                } else if (index + 1 < inputImagePixels.length){
                    A = inputImagePixels[index] ;
                    B = inputImagePixels[index + 1] ;

                    gray = (int) (
                            A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff));

                    temp[offset++] = gray;
                }


            }
        }

        return temp;
    }



}
