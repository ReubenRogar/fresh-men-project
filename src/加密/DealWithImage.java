package 加密;


import java.nio.charset.StandardCharsets;

public class DealWithImage {
    public static final DCTable DCL;
    public static  final DCTable DCC;
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
        int dclLength = dcs.getCategory(code);
        return code.substring(dclLength,2*dclLength+1);
    }
    /**
     * 异或处理二进制数组
     * @param code 字节码
     * @return 异或后字节码
     */
    public static byte[] xorCode(byte[] code){
        double u = 3.79,x = 0.88;
        for(int i = 0 ;i < code.length;i++){
                        if(code[i] != -1){
                            code[i] =(byte) ((int) code[i] ^ (int) (x * 127));
                            x = u * x * (1 - x);
                        }
        }
        return code;
    }

    public static String changeDC(String DCbefore){
        long DC = 0;
        byte DCs[] = DCbefore.getBytes();
        if(DCs[0] == '0'){
            for()
        }
    }
}
