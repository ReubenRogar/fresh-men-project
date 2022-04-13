package 加密;


import java.awt.*;
import java.awt.font.LineMetrics;
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
        ArrayList<int[]> DCT = new ArrayList<>();
        int[] arr = new int[64];
        DCTable dcTable;ACTable acTable;
        int flag = -1;
        while(true) {//读DCT块
            flag++;
            int index = 0;
            if (flag % 3 == 0) {
                dcTable = DCL;
                acTable = ACL;
            } else {
                dcTable = DCC;
                acTable = ACC;
            }
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);
            if (pDC.x == 0) arr[index] = 0;
            else arr[index++] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
            code = code.substring(pDC.x + pDC.y);
            int[] pAC;//读取run/size
            while(true) {//读取AC哈夫曼码
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){
                    if(pAC[0] == 0){// 0/0 EOB
                        for(;index < 64;index++){
                            arr[index] = 0;
                        }
                        code = code.substring(pAC[2]);
                        break;
                    }else {// F/0 16个零
                        for (int i = 0; i < 16; i++) {
                            arr[index++] = 0;
                        }
                        code = code.substring(pAC[2]);
                        continue;
                    }
                }
                for(int i = 0;i <pAC[0];i++){
                    arr[index++] = 0;
                }//Run个零
                arr[index++] = str0b2int(code.substring(pAC[2],pAC[1]+pAC[2]));
                code = code.substring(pAC[1]+pAC[2]);
                if(index == 64){
                    break;
                }
                if(code == ""){
                    for(;index < 64;index++){
                        arr[index] = 0;
                    }
                    DCT.add(arr);
                    return DCT;
                }
            }
            DCT.add(arr);
            if(code.length() < 8)break;
            else {
                while (code.length() % 8 != 0) {
                    code = code.substring(1);
                }
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
        for(int i = DCT.size()-1;i > 0;i--){
            DCT.get(i)[0] -= DCT.get(i - 1)[0];
        }//去差分
        int DCTs = 0,index = 1;
        for (int[] ints : DCT) {//遍历1*64数据块
            DCTable dcTable;
            ACTable acTable;
            if (DCTs % 3 == 0) {//亮度
                dcTable = DCL;
                acTable = ACL;
                //System.out.println("亮度");
            }else {//色度*2
                dcTable = DCC;
                acTable = ACC;
                //System.out.println("色度");
            }
                if(ints[0]!= 0){
                    temp = int2str0b(ints[0]);
                    code += dcTable.getHuffmanCode(temp.length()) + temp;
                }else{
                    code += "00";
                }
                int lastNum = 0;
                for (index = 1; index < 64; index++) {
                    if (ints[index] != 0) {
                        temp = int2str0b(ints[index]);
                        //System.out.println(ints[index] +" " +acTable.getHuffmanCode(index - lastNum - 1, temp.length()));
                        code += acTable.getHuffmanCode(index - lastNum - 1, temp.length());
                        code += temp;
                        lastNum = index;
                    } else if (lastNum < 63 && index == 63 && !(DCTs == DCT.size()-1&&code.length()%8 == 0)) {
                        code += acTable.getEOB();
                    }
                }
                while (code.length()%8!=0)code += "0";
            //System.out.println(code);
            DCTs++;
        }
        //System.out.println(code.length());
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
            result = -result;
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
        StringBuilder s1 = new StringBuilder("");
        if(s < 0){
            s= -s;
            do{
                s1.insert(0,s%2 == 0? '1':'0') ;
                s /= 2;
            }while(s > 0);
        }else{
            do{
                s1.insert(0, s%2 == 0?'0':'1');
                s /= 2;
            }while(s > 0);
        }
        return s1.toString();
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
        System.out.println();
    }
}
