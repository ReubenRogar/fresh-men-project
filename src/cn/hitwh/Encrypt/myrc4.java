package cn.hitwh.Encrypt;

import java.io.UnsupportedEncodingException;

public class myrc4 {
    public  static  void main(String[] args) throws UnsupportedEncodingException {
        String encryStr = RC4Util.encryRC4String("114514", "123456","UTF-8");
        System.out.println("加密后得到得字符串："+encryStr);
        String decryStr = RC4Util.decryRC4(encryStr, "123456","UTF-8");
        System.out.println("解密后得到得字符串："+decryStr);
    }
}
