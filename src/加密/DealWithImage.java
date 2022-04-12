package 加密;


import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;

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
    //二进制字符串提取DCAC
    public static ArrayList<int[]> getDCAC(){
        StringBuilder ss = new StringBuilder("1110001011101000101000101000101011111001100100111111011100010011");
        ArrayList<int[]> DCTS = new ArrayList<>();
        int[] temp = new int[64];
        while(true){
            Point index = DCL.getCategory(ss.toString());
            temp[0] = str0b2int( ss.substring(index.x,index.x+index.y));
            ss.delete(0,index.x+index.y);


        }
    }
    public static ArrayList<int[]> getAndReturnDC(String code) {
        int[] arr = new int[64];
        int l = code.length();
        ArrayList<int[]> rDCT = new ArrayList<int[]>();
        for (int i = 0; i < l; i++) {
            int k = 0,flag = 1;
            Point pd = new Point();//（读取值长度，读取码长度）
            int[] pa = new int[3];//
            if(flag == 0) {
                pd = DCL.getCategory(code);
            }
            else {
                pd = DCC.getCategory(code);
            }
            arr[k] = method(code.substring(pd.x,pd.x+pd.y));//byte转int
            k++;
            if(flag == 0) {
                pa = ACL.getRunSize(code.substring(pd.x+pd.y,l));
            }
            else {
                pa = ACC.getRunSize(code.substring(pd.x+pd.y,l));//R,S,L
            }
            int mark = k;
            for (; k < mark + pa[0] +; k++) {
                arr[k] = 0;
            }
        }
    }
    /**
     * 二进制字符串转int
     */
    public static int str0b2int(String s){
        int result = 0,temp =1;
        for(int i = s.length()-1;i >= 0;i--){
            result += temp *(s.charAt(i) - '0');
            temp *= 2;
        }
        if(s.startsWith("0"))result = -result;
        return result;
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
    }
}
