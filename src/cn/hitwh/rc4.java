package cn.hitwh;

import java.awt.*;
import java.util.ArrayList;

public class rc4 {
    public static void  rc4(char[] sBox , ArrayList<Point> Point){//得到Sbox
        int i, j = 0;
        char[] key = {1,2,3};//密钥
        char[] K = new char[256];
        char tmp;
        for (i = 0 ; i < 256 ; i ++) {
            sBox[i] = (char)i;
            K[i] = key[i % key.length];//超过长度则回到key[0]
        }
        for (i = 0 ; i < 256; i++) {
            j=(j + sBox[i] + K[i]) % 256;//j = (j + i + key[i % Len]) % 256
            tmp = sBox[i];
            sBox[i] = sBox[j]; //交换s[i]和s[j]
            sBox[j] = tmp;
        }
        int T,m;
        for(i = 0, j = 0 , m = 0 ; m < key.length ; m++) {
            i =(i + 1) % 256;
            j=(j+sBox[i])%256;
            tmp = sBox[i];
            sBox[i] = sBox[j]; //交换s[x]和s[y]
            sBox[j] = tmp;
            T=(sBox[i]+sBox[j])%256;
            Point.get(i).x ^= sBox[T];//?
        }
    }


    public static void main(String[] args) {
        JPEGs DC = new JPEGs("./测试用图片/1.jpg");
        DC.simpleEn("./测试用图片/1.jpg");

    }
}