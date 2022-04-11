package 加密;


public class DealWithImage {
    static {
        // 直流亮度表
        final DCTable DCL = new DCTable("./HuffmanTable/DC_luminance.txt");
        // 直流色度表
        final DCTable DCC = new DCTable("./HuffmanTable/DC_chrominance.txt");
        // 交流亮度表
        final ACTable ACL = new ACTable("./HuffmanTable/AC_luminance.txt");
        // 交流色度表
        final ACTable ACC = new ACTable("./HuffmanTable/AC_chrominance.txt");
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


}
