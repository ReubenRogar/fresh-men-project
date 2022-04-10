package 加密;


public class DealWithImage {
    private static String[] binaryArray =
            {
                    "0000","0001","0010","0011",
                    "0100","0101","0110","0111",
                    "1000","1001","1010","1011",
                    "1100","1101","1110","1111"};

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

    public static String getImageCode(byte[] code){
        int i;
        for (i =0;i <code.length;i++){
            if(code[i] == -1&&code[i+1] == -38) {
                i += 2;
                break;
            }
        }
        i += code[i] * 16 * 16 +code[i+1];
        byte[] target = new byte[code.length-2-i];
        System.arraycopy(code, 0 + i, target, 0, target.length);
        String outStr = "";
        i =0;
        for (byte b : target) {
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
        }
        return outStr;
    }
}
