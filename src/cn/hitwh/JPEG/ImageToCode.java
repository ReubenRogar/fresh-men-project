package cn.hitwh.JPEG;


import java.io.*;

/**
 * �ļ���ȡ
 */
public class ImageToCode {
    //ͼƬ��byte����
    public static byte[] imageToByte(String path) {
        byte[] data = null;
        
        try (FileInputStream input = new FileInputStream(path);
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


    //byte���鵽16�����ַ���
    public static String byteToString(byte[] data) {
        
        if (data == null || data.length <= 1) return "0x";
        StringBuilder sb = new StringBuilder();
        int[] buf = new int[data.length];
        
        //byte����ת����ʮ����
        for (int i = 0; i < data.length; i++) {
            buf[i] = data[i] < 0 ? (data[i] + 256) : (data[i]);
        }
        
        //ʮ����ת����ʮ������
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

    //byte���鵽ͼƬ
    public static void outputImage(String fileName, byte[] matrix) {
        FileOutputStream outSTr;
        BufferedOutputStream Buff;
        try {
            outSTr = new FileOutputStream(fileName);
            Buff = new BufferedOutputStream(outSTr);
            Buff.write(matrix);
            Buff.flush();
            Buff.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}