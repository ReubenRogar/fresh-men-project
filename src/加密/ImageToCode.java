package 加密;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageToCode {
    private static final String TARGET = "实验红图";
    private static final String SRC_FILE = "E:\\大一年度计划\\实验图像\\"+TARGET+".jpg";
    private static final String DEST_FILE = "E:\\大一年度计划\\5.txt";
    
    public static void main(String[] args) {
        try {
            //if (args[0].equals("1")) {
                //System.out.println(dataToFile(byteToString(imageToByte(SRC_FILE))));
                //outImage(DealWithImage.xorCode(imageToByte(SRC_FILE)),"E:\\大一年度计划\\实验图像\\加密后"+TARGET+".jpg","jpg");
                //outImage(DealWithImage.xorCode(DealWithImage.xorCode(imageToByte(SRC_FILE))),"E:\\大一年度计划\\实验图像\\解密后"+TARGET+".jpg","jpg");

                /*System.out.println(byteToString(imageToByte(SRC_FILE)));
                System.out.println("加密后:");
                System.out.println(byteToString(DealWithImage.xorCode(imageToByte(SRC_FILE))));
                System.out.println("解密后:");
                System.out.println(byteToString(DealWithImage.xorCode(DealWithImage.xorCode(imageToByte(SRC_FILE)))));
                */
                //dataToFile(byteToString(imageToByte(SRC_FILE)),"E:\\大一年度计划\\实验图像\\"+TARGET+".txt");
            //dataToFile(byteToString(DealWithImage.xorCode(imageToByte(SRC_FILE))),"E:\\大一年度计划\\实验图像\\加密后"+TARGET+".txt");
            //dataToFile(byteToString(DealWithImage.xorCode(DealWithImage.xorCode(imageToByte(SRC_FILE)))),"E:\\大一年度计划\\实验图像\\解密后"+TARGET+".txt");
            //System.out.println(getImageCode(imageToByte(SRC_FILE)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //图片到byte数组
    public static byte[] imageToByte(String path) {
        byte[] data = null;
        
        try (FileImageInputStream input = new FileImageInputStream(new File(path));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            
            byte[] buf = new byte[1024];
            int numBytesRead;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //byte数组到图片
    public static void outImage(byte[] bytes,String fileName,String type){
        if(type.isEmpty()){
            type = "jpg";
        }
        try{
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(input);
            File target =new File(fileName);
            ImageIO.write(image,type,target);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    //byte数组到16进制字符串
    public static String byteToString(byte[] data) {
        
        if (data == null || data.length <= 1) return "0x";
        StringBuilder sb = new StringBuilder();
        int[] buf = new int[data.length];
        
        //byte数组转化成十进制
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i] < 0 ? (data[i] + 256) : (data[i]);
        }
        
        //十进制转化成十六进制
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] < 16) {
                sb.append("0").append(Integer.toHexString(buf[i])).append(" ");
            } else {
                sb.append(Integer.toHexString(buf[i])).append(" ");
            }
            if ((i + 1) % 16 == 0) {
                sb.append("\n");
            }
        }
        return sb.toString().toUpperCase();
        
    }
    
    public static void dataToFile(String data,String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}