package 加密;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;
    private KeyXU originKey;
    private KeyXU finalKey;

    public NewTypeEncrypt(ArrayList<int[]> d,KeyXU k) throws NoSuchAlgorithmException {
        DCTs = d;
        originKey = k;

        int[] mount = new int[64];
        for(int i = 0;i <64;i++)
            mount[i] = 0;
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
        String hashFeature = DealWithImage.bytes2Str0b(encodedHash);
        int count;
        String addXKey = "",addUKey = "";
        //前半加密x
        for(int i = 0;i < 28;i++){
            count = 0;
            for(int r = 0;r < 9;r++){
                if(hashFeature.charAt(r) == '1')count++;
            }
            hashFeature = hashFeature.substring(9);
            addXKey += ""+count;
        }
        count = 0;
        for(int i = 0;i < 4;i++){
            if(hashFeature.charAt(i) == '1')count++;
        }
        addXKey += ""+count;
        hashFeature = hashFeature.substring(4);
        //后半加密u
        count = 0;
        for(int i = 0;i < 4;i++){
            if(hashFeature.charAt(i) == '1')count++;
        }
        addUKey += ""+count;
        hashFeature = hashFeature.substring(4);
        for(int i = 0;i < 28;i++){
            count = 0;
            for(int r = 0;r < 9;r++){
                if(hashFeature.charAt(r) == '1')count++;
            }
            hashFeature = hashFeature.substring(9);
            addUKey += ""+count;
        }
        finalKey = new KeyXU(  Double.valueOf(""+ originKey.x+addXKey),Double.valueOf(""+ originKey.y+addUKey));
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        
        System.out.println();
    }
}
