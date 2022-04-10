package 加密;

import java.util.ArrayList;

public class DealWithImage {
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

    public static byte[] getImageCode(byte[] code){
        int i;
        for (i =0;i <code.length;i++){
            if(code[i] == -1&&code[i+1] == -38) {
                i += 2;
                break;
            }
        }
        i += code[i] * 16 * 16 +code[i+1];
        byte[] target = new byte[code.length-2-i];
        for(int j = 0;j < target.length;j++){
            target[j] = code[j+i];
        }
        return target;
    }
}
