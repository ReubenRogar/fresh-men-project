package cn.hitwh.Rc4;

import java.util.Scanner;
public class A {
    public static void main(String args[]){
        int[] d = {2,2,7,9,79};
        int[] s = new int[256];
        String key;
        System.out.println("input the key:");
        Scanner in = new Scanner(System.in);
        key = in.next();
        int[] intKey = new int[d.length];
        if(key.length()<=d.length) {
            int i = 0;
            for (; i < key.length(); i++) {
                intKey[i] = key.charAt(i);
            }
            for(int j = i+1 ;j<d.length;j++){
                intKey[j] = intKey[j-i-1];
            }
        }
        if(key.length()>d.length){
            for (int i = 0; i < d.length; i++) {
                intKey[i] = key.charAt(i);
            }
        }
        System.out.print("init data:");
        for (int j : d) {
            System.out.print(j + " ");
        }
        System.out.println();
        RC4.rc4_init(s, intKey, d.length );//s为RC4算法置乱箱；intKey为密钥数组；d为传入的加密数组
        RC4.rc4_crypt(s,d,d.length );
        System.out.print("after en: ");
        for (int j : d) {
            System.out.print(j + " ");
        }
        System.out.println();
        RC4.rc4_init(s, intKey, d.length);
        RC4.rc4_crypt(s,d,d.length);
        System.out.print("en 2: ");
        for (int j : d) {
            System.out.print(j + " ");
        }
    }
}
