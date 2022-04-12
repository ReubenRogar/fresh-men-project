package 加密;


import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

public class DealWithImage {
    public static final DCTable DCL;
    public static final DCTable DCC;
    public static final ACTable ACL;
    public static final ACTable ACC;
    private static double u = 3.79, x = 0.88;

    static {
        // 直流亮度表
        DCL = new DCTable("./HuffmanTable/DC_luminance.txt");
        // 直流色度表
        DCC = new DCTable("./HuffmanTable/DC_chrominance.txt");
        // 交流亮度表
        ACL = new ACTable("./HuffmanTable/AC_luminance.txt");
        // 交流色度表
        ACC = new ACTable("./HuffmanTable/AC_chrominance.txt");
    }

    //返回一段压缩数据的DCT
    String s = "11100010 11101000 10100010 10001010 11111001 10010011 11110111 00010011";

    public static ArrayList<int[]> getAndReturnDC(String code) {
        int[] arr = new int[64];
        int l = code.length();
        ArrayList<int[]> rDCT = new ArrayList<int[]>();
        for (int i = 0; i < l; i++) {
            int k = 0,flag = 1;
            Point pd = new Point();//（读取值长度，读取码长度）
            Point pa = new Point();
            if(flag == 0) {
                pd = DCL.getCategory(code);
            }
            else {
                pd = DCC.getCategory(code);
            }
            int [k] = method(code.substring(pd.x,pd.x+pd.y));//byte转int
            k++;
            if(flag == 0) {
                pa = ACL.getCategory(code);
            }
            else {
                pa = ACC.getCategory(code);
            }

        }
    }

    /**
     * 异或处理二进制数组
     *
     * @param code 字节码
     * @return 异或后字节码
     */
    public static int xorCode(int code, int length) {
        int xorTarget = (int) ((Math.pow(2, length) - Math.pow(2, length - 1)) * x + Math.pow(2, length - 1));
        code = code ^ xorTarget;
        //x = u * x * (1 - x);
        return code;
    }

    /**
     * 对DC的字符串信息进行异或处理
     *
     * @param DCBefore
     * @return
     */
    public static String changeDC(String DCBefore) {
        int DC = 0, delta = 1;
        byte DCs[] = DCBefore.getBytes();
        System.out.println(DCBefore);
        String result = "";
        for (int i = DCs.length - 1; i >= 0; i--) {
            if (DCs[i] == '1') DC += delta;
            delta *= 2;
        }
        System.out.println(DC);
        DC = xorCode(DC, DCBefore.length());
        System.out.println(DC);
        LinkedList<Character> DCAfter = new LinkedList<>();
        while (DC > 0) {
            DCAfter.addFirst((char) (DC % 2 + '0'));
            DC /= 2;
        }
        while (DCAfter.size() < DCs.length) DCAfter.addFirst('0');
        for (Character character : DCAfter) {
            result += character;
        }
        System.out.println(result);
        return result;
    }

    //把byte数组转二进制字符串
    public static String bytes2Str0b(byte[] target) {
        String[] binaryArray =
                {
                        "0000", "0001", "0010", "0011",
                        "0100", "0101", "0110", "0111",
                        "1000", "1001", "1010", "1011",
                        "1100", "1101", "1110", "1111"
                };

        String outStr = "";
        int i = 0;
        for (byte b : target) {
            i = (b & 0xF0) >> 4;
            outStr += binaryArray[i];
            i = b & 0x0F;
            outStr += binaryArray[i];
        }
        return outStr;
    }

    //二进制字符串转byte数组
    public static byte[] str0b2Bytes(String input) {
        StringBuilder in = new StringBuilder(input);
        // 注：这里in.length() 不可在for循环内调用，因为长度在变化
        int remainder = in.length() % 8;
        if (remainder > 0)
            for (int i = 0; i < 8 - remainder; i++)
                in.append("0");
        byte[] bts = new byte[in.length() / 8];

        // Step 8 Apply compression
        for (int i = 0; i < bts.length; i++)
            bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);

        return bts;
    }

    /**
     * 加密方法集中，对byte数组提取、转换、加密再返回修改过得数组
     */
    public static byte[] imageEncrypt(byte[] code) {
        int i;
        for (i = code.length - 1; i >= 0; i--) {
            if (code[i] == -1 && code[i + 1] == -38) {
                i += 2;
                break;
            }
        }
        i += code[i] * 16 * 16 + code[i + 1];
        byte[] target = new byte[code.length - 2 - i];
        System.arraycopy(code, 0 + i, target, 0, target.length);
        target = str0b2Bytes(getAndReturnDC(bytes2Str0b(target), DCL));
        System.arraycopy(target, 0, code, i, target.length);
        return code;
    }

    public static void main(String[] args) {
        try {
            ImageToCode.outImage(imageEncrypt(ImageToCode.imageToByte("E:\\大一年度计划\\实验图像\\实验红图.jpg")),"E:\\大一年度计划\\实验图像\\实验红图加密后.jpg","jpg");
            ImageToCode.outImage(imageEncrypt(ImageToCode.imageToByte("E:\\大一年度计划\\实验图像\\实验红图加密后.jpg")),"E:\\大一年度计划\\实验图像\\实验红图解密后.jpg","jpg");
        }catch (Exception e){
            e.printStackTrace();
    }
}
}


