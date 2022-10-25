package cn.hitwh.Encrypt;

public class RC4 {
    private static void rc4_init(int[] s, int[] key, int Len)
    {
        int i = 0;
        int j = 0;
        int[] k = new int[256];

        int tmp = 0;
        for (i = 0;i < 256;i++)
        {
            s[i] = i;
            k[i] = key[i % Len];
        }
        for (i = 0; i < 256; i++)
        {
            j = (j + s[i] + k[i]) % 256;
            //????s[i]??s[j]
            tmp = s[i];
            s[i] = s[j];
            s[j] = tmp;
        }
    }

    //?????
    private static void rc4_crypt(int[] s, int[] Data, int Len)
    {
        int i = 0;
        int j = 0;
        int t = 0;
        int k = 0;

        int tmp;
        for (k = 0;k < Len;k++)
        {
            i = (i + 1) % 256;
            j = (j + s[i]) % 256;
            //????s[x]??s[y]
            tmp = s[i];
            s[i] = s[j];
            s[j] = tmp;
            t = (s[i] + s[j]) % 256;
            Data[k] ^= s[t];
        }
    }
    public static void Rc4(int[] d,String key)
    {
        int[] s = new int[256];
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
        RC4.rc4_init(s, intKey, d.length );//sΪRC4�㷨�����䣻intKeyΪ��Կ���飻dΪ����ļ�������
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
