package com.company;

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
    private static float sX = 1.5f;
    private static float sY = 1.5f;

    public static void main(String[] args) throws IOException {

        String filePath = "test-image.pgm";
        int[][] inputImage = readPGMFile(filePath);
        write(
                d1Tod2(
                        resizeBilinearGray(
                                d2Tod1(inputImage),
                                inputImage[0].length,
                                inputImage.length,
                                (int) (inputImage[0].length * sX),
                                (int) (inputImage.length * sY)),
                        (int) (inputImage[0].length * sX)),
                new File("teset_" + sX + "_" + sY + "_" + ".pgm"), 255);
    }

    /*Source : https://gist.github.com/armanbilge/3276d80030d1caa2ed7c
     * */
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


    /*
     * Source : https://stackoverflow.com/questions/3639198/how-to-read-pgm-images-in-java
     * */
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

        for (int i = 0; i < array.length; ++i)
            for (int j = 0; j < array[i].length; ++j) {
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

    public static int[] resizeBilinearGray(int[] pixels, int w, int h, int w2, int h2) {
        int[] temp = new int[w2 * h2];
        int A, B, C, D, x, y, index, gray;
        float x_ratio = ((float) (w)) / w2;
        float y_ratio = ((float) (h)) / h2;
        float x_diff, y_diff;
        int offset = 0;
        int out = 0;
        for (int i = 0; i < h2; i++) {
            for (int j = 0; j < w2; j++) {
                x = (int) (x_ratio * j);
                y = (int) (y_ratio * i);
                x_diff = (x_ratio * j) - x;
                y_diff = (y_ratio * i) - y;
                index = y * w + x;

                /*if (i<100) {
                    System.out.println(String.format("x: %d, y:%d, x_diff:%f, y_diff:%f, index:%d", x, y, x_diff, y_diff, index));
                }*/
                if (index + w + 1 < pixels.length) {

                    A = pixels[index] & 0xff;
                    B = pixels[index + 1] & 0xff;
                    C = pixels[index + w] & 0xff;
                    D = pixels[index + w + 1] & 0xff;

                    gray = (int) (
                            A * (1 - x_diff) * (1 - y_diff) + B * (x_diff) * (1 - y_diff) +
                                    C * (y_diff) * (1 - x_diff) + D * (x_diff * y_diff)
                    );

                    temp[offset++] = gray;
                } else{
                    ++out;
                    System.out.println(String.format("i: %d, j:%d", i,j));
                }
            }
        }
        System.out.println(out);

        return temp;
    }

}
