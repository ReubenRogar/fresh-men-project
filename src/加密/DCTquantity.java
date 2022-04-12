package 加密;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;


import javax.imageio.ImageIO;


public class DCTquantity {


    public static void main(String[] args) {
        File file = new File("C:\\大一年度计划\\实验图像\\ccc.jpg");
        FileChannel fc = null;
        if (file.exists() && file.isFile()) {
            try {
                FileInputStream fs = new FileInputStream(file);
                fc = fs.getChannel();
                System.out.println(fc.size() + "-----fc.size()");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(file.length() + "-----file.length  B");
        System.out.println(file.length() * 1024 + "-----file.length  kb");
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int width = bi.getWidth();
        int height = bi.getHeight();
        int number = -1 ;
        if(width%8>0&&height%8==0)
            number = (width/8+1)*(height/8);
        else if(width%8==0&&height%8>0)
            number = (width/8)*(height/8+1);
        else if(width%8==0&&height%8==0)
            number = (width/8)*(height/8);
        else if(width%8>0&&height%8>0)
            number = (width/8+1)*(height/8+1);
        System.out.println("宽：像素-----" + width + "高：像素" + height+" DCT块数"+number);
    }
}
