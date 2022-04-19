package 加密;

import java.awt.*;
import java.util.ArrayList;

public class rc4 {
    public void rc4(char[] Sbox , ArrayList<Point> Point){//得到Sbox
        int i =0, j = 0;
        char[] key = {1,2,3};//密钥
        char[] K = new char[256];
        char tmp = 0;
        for (i = 0 ; i < 256 ; i ++) {
            Sbox[i] = (char)i;
            K[i] = key[i % key.length];//超过长度则回到key[0]
        }
        for (i = 0 ; i < 256; i++) {
            j=(j + Sbox[i] + K[i]) % 256;//j = (j + i + key[i % Len]) % 256
            tmp = Sbox[i];
            Sbox[i] = Sbox[j]; //交换s[i]和s[j]
            Sbox[j] = tmp;
        }
        int T = 0 , m = 0;
        for(i = 0, j = 0 , m = 0 ; m < key.length ; m++) {
            i =(i + 1) % 256;
            j=(j+Sbox[i])%256;
            tmp = Sbox[i];
            Sbox[i] = Sbox[j]; //交换s[x]和s[y]
            Sbox[j] = tmp;
            T=(Sbox[i]+Sbox[j])%256;
            Point.get(i).x ^= Sbox[T];//?
        }
    }
}
