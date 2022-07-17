package 加密;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;
    private Point originKey;
    private Point finalKey;

    public NewTypeEncrypt(ArrayList<int[]> d,Point k) throws NoSuchAlgorithmException {
        DCTs = d;
        originKey = k;

        int[] mount = new int[64];
        int[] dct;
        for (int i = 0;i < DCTs.size();i++){
            dct = DCTs.get(i);
            int num = 0;
            for (int r = 1;r < dct.length;r++) {
                if(dct[r]!= 0)num++;
            }
            mount[num]++;
        }
        String feature = "";
        for(int i = 0;i < 64;i++) {
            feature += "" + i + mount[i];
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] encodedHash = digest.digest(feature.getBytes(StandardCharsets.UTF_8));
        String hashfeature = DealWithImage.bytes2Str0b(encodedHash);

    }

//    public static void main(String[] args) throws NoSuchAlgorithmException {
//        String feature = "2312312435454632145376541212197378547431414496";
//        MessageDigest digest = MessageDigest.getInstance("SHA-512");
//        byte[] encodedhash = digest.digest(feature.getBytes(StandardCharsets.UTF_8));
//        System.out.println(encodedhash.length);
//    }
}
