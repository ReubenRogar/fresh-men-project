package 何泓博2021211991;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;

public class CodeOfImage {
    
    private static final String SRC_FILE = "E:\\PS素材\\素材 Photoshop 2020平面设计从新手到高手\\第1章\\1.3\\花.jpg";
    private static final String DEST_FILE = "E:\\PS素材\\素材 Photoshop 2020平面设计从新手到高手\\第1章\\1.3\\花.txt";
    
    public static void main(String[] args) {
        try {
            if (args[0].equals("1")) {
                System.out.println(dataToFile(byteToString(imageToByte(SRC_FILE))));
            } else {
                System.out.println(byteToString(imageToByte(SRC_FILE)));
            }
        } catch (Exception e) {
            System.out.println(byteToString(imageToByte(SRC_FILE)));
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
    
    public static boolean dataToFile(String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DEST_FILE))) {
            bw.write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}