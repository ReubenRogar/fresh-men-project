package cn.hitwh.Encrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;
    private KeyXU originKey;
    private KeyXU finalKey;

    /**
     * 依据dct数据建立密钥
     * @param d dct数据
     * @param k 初始密钥
     * @throws NoSuchAlgorithmException
     */
    public NewTypeEncrypt(ArrayList<int[]> d, KeyXU k) throws NoSuchAlgorithmException {
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
            feature += "" + i + ""+mount[i];
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] encodedHash = digest.digest(feature.getBytes(StandardCharsets.UTF_8));
        String hashFeature = bytes2Str0b(encodedHash);
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
        finalKey = new KeyXU(  Double.valueOf(""+ originKey.x+addXKey),Double.valueOf(""+ originKey.u+addUKey));
    }


    //把byte数组转二进制字符串
    public static String bytes2Str0b(byte[] bytes){
        String[] binaryArray =
                {
                        "0000","0001","0010","0011",
                        "0100","0101","0110","0111",
                        "1000","1001","1010","1011",
                        "1100","1101","1110","1111"
                };

        String outStr = "";
        int i;
        for (int j = 0;j <bytes.length;j++) {
            byte b = bytes[j];
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
            if(b == -1)j++;
        }
        return outStr;
    }

    public KeyXU getFinalKey() {
        return finalKey;
    }

    public void DCCGroupScrambling(ArrayList<int[]> dct){
        double[] scrambles = new double[dct.size()];
        scrambles[0] = finalKey.x;
        for(int i = 1;i < scrambles.length;i++){
           scrambles[i] = finalKey.u * scrambles[i-1]*(1-scrambles[i-1]);//x(n+1) = u * x(n) * (1 - x(n))
        }
        int start = 0,end = 0;
        while (dct.get(start)[0] == 0)start++;
        end = start + 1;
        while(start != dct.size() -1 && end != dct.size() -1){
            while(dct.get(end)[0] *dct.get(end)[0] > 0)end++;
        }
    }
}
