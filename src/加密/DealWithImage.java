package 加密;


import java.nio.charset.StandardCharsets;
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
    public static String getDC(String code,DCTable dcs){
        int[] dcsLength = dcs.getCategory(code);
        return code.substring(dcsLength[1]-1,dcsLength[0]+dcsLength[1]);
    }

    /**
     * 异或处理二进制数组
     *
     * @param code 字节码
     * @return 异或后字节码
     */
    public static int xorCode(int code,int length) {
        int xorTarget = (int)((Math.pow(2,length)-Math.pow(2,length-1))*x +Math.pow(2,length-1));
        code = code ^ xorTarget;
        x = u * x * (1 - x);
        return code;
    }

    /**
     * 对DC的字符串信息进行异或处理
     * @param DCBefore
     * @return
     */
    public static String changeDC(String DCBefore) {
        int DC = 0, delta = 1;
        byte DCs[] = DCBefore.getBytes();
        String result ="";
            for (int i = DCs.length - 1; i >= 0; i--) {
                if (DCs[i] == '1') DC += delta;
                delta *= 2;
            }
            System.out.println(DC);
            DC = xorCode(DC,DCBefore.length());
            LinkedList<Character> DCAfter = new LinkedList<>();
            while (DC > 0) {
                DCAfter.addFirst((char)(DC%2+'0'));
                DC /= 2;
            }
            if(DCAfter.size()< DCs.length)DCAfter.addFirst('0');
            for (Character character : DCAfter) {
                result += character;
            }
        return result;
    }

    public static void main(String[] args) {
        String text ="0100101";
        String text1 ="1011010";
        System.out.println(changeDC(text));
        System.out.println(changeDC(changeDC(text)));
    }
}
