package 加密;


import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class DealWithImage {
    public static final DCTable DCL;
    public static final DCTable DCC;
    public static final ACTable ACL;
    public static final ACTable ACC;
    private static double u = 3.79, x = 0.88;

    static {
        // 直流亮度表
        DCL = new DCTable("./HuffmanTable/DC_luminance.txt");
        // 直流色度表
        DCC = new DCTable("./HuffmanTable/DC_chrominance.txt");
        // 交流亮度表
        ACL = new ACTable("./HuffmanTable/AC_luminance.txt");
        // 交流色度表
        ACC = new ACTable("./HuffmanTable/AC_chrominance.txt");
    }
    /**
     * 提取DCT块
     * @param code 二进制字符串
     * @return 1*64数据块
     */
    public static ArrayList<int[]> getDCT(String code) {
        int[] arr = new int[64];
        int l = code.length();
        ArrayList<int[]> DCT = new ArrayList<>();
        int flag = 0;
        for (int i = 0; i < l; i++) {
            int k = 0;
            Point pd;//  读取值长度，读取码长度
            int[] pa;//
            if(flag == 3) flag = 0;
            if(flag == 0) {
                pd = DCL.getCategory(code);
            }
            else {
                pd = DCC.getCategory(code);
            }
            int codeHead = pd.y,codeTail = pd.x + pd.y;//codeHead = i + 1
            arr[k] = str0b2int(code.substring(codeHead,codeTail));//byte转int
            k++;
            i += pd.x + pd.y;
            if(flag == 0) {
                pa = ACL.getRunSize(code.substring(codeTail,l));
                while( pa[0] != 0||pa[1] != 0 ) {      //非0/0时
                    i += pa[1] + pa[2];
                    for (int j = 0; j < pa[0]; j++) {       //R
                        arr[k] = 0;
                        k++;
                    }
                }
                    codeHead = codeTail + pa[2];
                    codeTail = codeHead + pa[1];
                    arr[k] = str0b2int(code.substring(codeHead,codeTail));
                    k++;
                    pa = ACL.getRunSize(code.substring(codeTail));
                }
                i++;// 0/0,识别码为0，后移一位
                if(i == l-1) {
                    for (int j = k; j < 64; j++) {
                        arr[j] = 0;
                    }
                        DCT.add(arr);
                        return DCT;

                }
                codeTail++;
                if((i+1) % 8 != 0) {
                    codeHead = codeTail + 1;
                    while ((i + 1) % 8 != 0) {//移到字节尾
                        i++;
                        if(i == l-1) {
                            for (int j = k; j < 64; j++) {
                                arr[j] = 0;
                            } //
                                DCT.add(arr);
                                return DCT;
                        }
                        codeHead++;
                    }
                    i++;//跳到下一字节
                        if(i == l-1) {
                            for (int j = k; j < 64; j++) {
                                arr[j] = 0;
                                DCT.add(arr);
                                return DCT;
                            }//
                    codeHead++;
                }
                else {
                            codeHead = codeTail + 1;
                            i++;
                            if (i == l - 1) {
                                for (int j = k; j < 64; j++) {
                                    arr[j] = 0;
                                    DCT.add(arr);
                                    return DCT;
                                }//
                            }
                        }
                for(;k < 64 ;k++) arr[k] = 0;
                DCT.add(arr);
                flag++;
            }
            else {
                pa = ACC.getRunSize(code.substring(pd.x+pd.y,l));//R,(S,L)
                while(pa[0] != 0||pa[1] != 0){      //非0/0时
                    i += pa[1] + pa[2];
                    for (int j = 0; j < pa[0]; j++) {       //R
                        arr[k] = 0;
                        k++;
                    }
                    codeHead = codeTail + pa[2];
                    codeTail += codeHead + pa[1];
                    arr[k] = str0b2int(code.substring(codeHead,codeTail));
                    k++;
                    pa = ACL.getRunSize(code.substring(codeTail,l));
                }
                i++;// 0/0,识别码为0，后移一位
                codeTail++;
                if((i+1) % 8 != 0) {
                    codeHead = codeTail + 1;
                    while ((i + 1) % 8 != 0) {//移到字节尾
                        i++;
                        if (i == l - 1) {
                            for (int j = k; j < 64; j++) {
                                arr[j] = 0;
                                DCT.add(arr);
                                return DCT;
                            }//
                        }
                        codeHead++;
                    }
                    i++;//跳到下一字节
                    if (i == l - 1) {
                        for (int j = k; j < 64; j++) {
                            arr[j] = 0;
                            DCT.add(arr);
                            return DCT;
                        }//
                    }
                    codeHead++;
                }
                else{
                    codeHead = codeTail + 1;
                    i++;
                    if (i == l - 1) {
                        for (int j = k; j < 64; j++) {
                            arr[j] = 0;
                            DCT.add(arr);
                            return DCT;
                        }//
                    }
                }
                for(;k < 64 ;k++) arr[k] = 0;
                DCT.add(arr);
                flag++;
            }
        }
        return DCT;
    }

    /**
     * 1*64数组转二进制字符串
     * @param DCT 1*64数组
     * @return DCT码
     */
    public static String setDCT(ArrayList<int[]> DCT){
        String code = "";
        String temp;
        for(int i = DCT.size();i > 0;i++){
            int[] a = DCT.get(i);
            int[] b = DCT.get(i - 1);
            a[0] -= b[0];
            DCT.set(i,a);
        }
        int i = 0,j = 1;
        for (int[] ints : DCT) {
            if (i % 3 == 0) {//亮度
                temp = int2str0b(ints[0]);
                code += DCL.getHuffmanCode(temp.length()) + temp;
                int lastNum = 0;
                for (j = 1; j < 64; j++) {
                    if (ints[i] != 0) {
                        temp = int2str0b(ints[i]);
                        code += ACL.getHuffmanCode(j - lastNum - 1, temp.length());
                        code += temp;
                        lastNum = j;
                    } else if (lastNum < 63 && j == 63) {
                        code += ACL.get00();
                    }
                }
            } else {
                temp = int2str0b(ints[0]);
                code += DCC.getHuffmanCode(temp.length()) + temp;
                int lastNum = 0;
                for (j = 1; j < 64; j++) {
                    if (ints[i] != 0) {
                        temp = int2str0b(ints[i]);
                        code += ACC.getHuffmanCode(j - lastNum - 1, temp.length());
                        code += temp;
                        lastNum = j;
                    } else if (lastNum < 63 && j == 63) {
                        code += ACC.get00();
                    }
                }
            }
        }
    return code;
    }
    /**
     * 二进制字符串转int
     */
    public static int str0b2int(String s){
        int result = 0,temp =1;
        if(s.startsWith("0")){
            for (int i = s.length() - 1; i >= 0; i--) {
                result += temp * (s.charAt(i) == '0'? 1:0);
                temp *= 2;
            }
        }else {
            for (int i = s.length() - 1; i >= 0; i--) {
                result += temp * (s.charAt(i) - '0');
                temp *= 2;
            }
        }
        return result;
    }

    /**
     * int转二进制字符串
     * @param s
     * @return
     */
    public static String int2str0b(int s){
        String s1 = "";
        if(s < 0){
            s= -s;
            while(s>0){
                s1 += s%2 == 0? '1':'0';
                s /= 2;
            }
        }else{
            while(s > 0){
                s1 += s%2+'0';
            }
        }
        return s1;
    }

    /**
     * 异或处理二进制数组
     *
     * @param code 字节码
     * @return 异或后字节码
     */
    public static int xorCode(int code,int length) {
        int xorTarget = (int)((Math.pow(2,length)-Math.pow(2,length-1))*x +Math.pow(2,length-1));
        code = code ^ xorTarget;
        //x = u * x * (1 - x);
        return code;
    }

    /**
     * 对DC的字符串信息进行异或处理
     * @param DCBefore
     * @return
     */
    public static String changeDC(String DCBefore) {
        int DC = 0, delta = 1;
        byte DCs[] = DCBefore.getBytes();
        System.out.println(DCBefore);
        String result ="";
            for (int i = DCs.length - 1; i >= 0; i--) {
                if (DCs[i] == '1') DC += delta;
                delta *= 2;
            }
        System.out.println(DC);
            DC = xorCode(DC,DCBefore.length());
        System.out.println(DC);
            LinkedList<Character> DCAfter = new LinkedList<>();
            while (DC > 0) {
                DCAfter.addFirst((char)(DC%2+'0'));
                DC /= 2;
            }
            while(DCAfter.size()< DCs.length)DCAfter.addFirst('0');
            for (Character character : DCAfter) {
                result += character;
            }
        System.out.println(result);
        return result;
    }

    //把byte数组转二进制字符串
    public static String bytes2Str0b(byte[] target){
        String[] binaryArray =
                {
                        "0000","0001","0010","0011",
                        "0100","0101","0110","0111",
                        "1000","1001","1010","1011",
                        "1100","1101","1110","1111"
                };

        String outStr = "";
        int i =0;
        for (byte b : target) {
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
        }
        return outStr;
    }

    //二进制字符串转byte数组
    public static byte[] str0b2Bytes(String input){
            StringBuilder in = new StringBuilder(input);
            // 注：这里in.length() 不可在for循环内调用，因为长度在变化
            int remainder = in.length() % 8;
            if (remainder > 0)
                for (int i = 0; i < 8 - remainder; i++)
                    in.append("0");
            byte[] bts = new byte[in.length() / 8];
            // Step 8 Apply compression
            for (int i = 0; i < bts.length; i++)
                bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);
            return bts;
        }

    /**
     * 加密方法集中，对byte数组提取、转换、加密再返回修改过得数组
     */
    public static byte[] imageEncrypt(byte[] code){
        int i;
        for (i =code.length-1;i >=0 ;i--){
            if(code[i] == -1&&code[i+1] == -38) {
                i += 2;
                break;
            }
        }
        i += code[i] * 16 * 16 +code[i+1];
        byte[] target = new byte[code.length-2-i];
        System.arraycopy(code, 0 + i, target, 0, target.length);
        System.arraycopy(target,0,code,i,target.length);
        return code;
    }

    public static void main(String[] args) {
        int[] ints = new int[64];
        ArrayList<int[]> arr = new ArrayList<>();
        String code = "1110001011101000101000101000101011111001100100111111011100010011";
        arr = getDCT(code);
        ints = arr.get(1);
        for (int i = 0; i < 64; i++) {
            System.out.println(ints[i]);
        }
    }
}
