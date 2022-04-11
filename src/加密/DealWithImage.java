package 加密;


import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class DealWithImage {
    public static final DCTable DCL;
    public static final DCTable DCC;
    public static final ACTable ACL;
    public static final ACTable ACC;

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
    public static String getDCL(String code,DCTable dcs){
        int[] dcsLength = dcs.getCategory(code);
        return code.substring(dcsLength[1]-1,dcsLength[0]+dcsLength[1]);
    }

    /**
     * 异或处理二进制数组
     *
     * @param code 字节码
     * @return 异或后字节码
     */
    public static byte[] xorCode(byte[] code) {
        double u = 3.79, x = 0.88;
        for (int i = 0; i < code.length; i++) {
            if (code[i] != -1) {
                code[i] = (byte) ((int) code[i] ^ (int) (x * 1024));
                x = u * x * (1 - x);
            }
        }
        return code;
    }

    public static int xorCode(int code) {
        double u = 3.79, x = 0.88;
        code = code ^ (int) (x * 1024);
        x = u * x * (1 - x);
        return code;
    }

    public static String changeDC(String DCBefore) {
        int DC = 0, delta = 1;
        byte DCs[] = DCBefore.getBytes();
        String result ="";
        if (DCs[0] == '0') {//DC差分为负数
            for (int i = DCs.length - 1; i >= 0; i--) {
                if (DCs[i] == '0') DC += delta;
                delta *= 2;
            }
            DC = xorCode(DC);
            LinkedList<Character> DCAfter = new LinkedList<>();
            while (DC > 0) {
                DCAfter.addFirst((char)(DC%2==0?'1':'0'));
                DC /= 2;
            }
            for (Character character : DCAfter) {
                result += character;
            }
        } else {//DC差分为正数
            for (int i = DCs.length - 1; i >= 0; i--) {
                if (DCs[i] == '1') DC += delta;
                delta *= 2;
            }
            DC = xorCode(DC);
            LinkedList<Character> DCAfter = new LinkedList<>();
            while (DC > 0) {
                DCAfter.addFirst((char)(DC%2+'0'));
                DC /= 2;
            }
            for (Character character : DCAfter) {
                result += character;
            }
        }
        return result;
    }
}
