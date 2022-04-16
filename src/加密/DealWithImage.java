package 加密;


import java.awt.*;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.LinkedList;

public class DealWithImage {
    // 直流亮度表
    public DCTable DCL;
    // 直流亮度表
    public DCTable DCC;
    // 直流亮度表
    public ACTable ACL;
    // 直流亮度表
    public ACTable ACC;
    //DCT 1*64数据
    public ArrayList<int[]> DCT = new ArrayList<>();

    //private static double u = 3.79, x = 0.88;

    /**
     * 构造器获取图片的huffman表和DCT数据
     * @param image
     */
    public DealWithImage(byte[] image){
        getHuffmanTable(image);
        int i;
        for (i = image.length - 1; i >= 0; i--) {
            if (image[i] == -1 && image[i + 1] == -38) {
                i += 2;
                break;
            }
        }
        i += image[i] * 16 * 16 + image[i + 1];
        byte[] target = new byte[image.length - 2 - i];
        System.arraycopy(image, 0 + i, target, 0, target.length);
        System.out.println(ImageToCode.byteToString(target));
        getDCT(bytes2Str0b(target));
        outputArr(DCT);
        target= str0b2Bytes(setDCT());
        System.arraycopy(target,0,image,i,target.length);
        ImageToCode.outImage(image,"./测试用图片/8纯蓝图.jpg","jpg");
    }



    /**
     * 提取DCT块
     * @param code 二进制字符串
     * @return 1*64数据块
     */
    public void  getDCT(String code) {
//测试
        System.out.println("全部数据:"+code);
        int[] arr = new int[64];//接收一个DCT块数据的数组
        DCTable dcTable;
        ACTable acTable;
        int flag = -1;//表区分标志
        //读DCT块
        while(true) {
            //应用Huffman表
            flag++;
            int index = 0;
            if (flag % 3 == 0) {

                dcTable = DCL;
                acTable = ACL;

            } else {

                dcTable = DCC;
                acTable = ACC;

            }

            //读取DC系数
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);

            if (pDC.x == 0) arr[index++] = 0;
            else arr[index++] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
//测试
            System.out.println(code.substring(pDC.y, pDC.x + pDC.y)+":"+arr[index-1]);

            code = code.substring(pDC.x + pDC.y);
//测试
            System.out.println("剩余数据:"+code);
            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//Size为0
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
//测试
                        System.out.println("剩余数据:"+code);
                        continue;
                    }
                }
                //Run个零
                for(int i = 0;i <pAC[0];i++){
                    arr[index++] = 0;
                }
                arr[index++] = str0b2int(code.substring(pAC[2], pAC[2]+pAC[1]));
//测试
                System.out.println(code.substring(pAC[2], pAC[2]+pAC[1])+":"+arr[index-1]);
                code = code.substring(pAC[2]+pAC[1]);
//测试
                System.out.println("剩余数据:"+code);
                //DCT块数据输入完毕
                if(index == 64){
                    break;
                }
                if(code == ""){
                    for(;index < 64;index++){
                        arr[index] = 0;
                    }
                    DCT.add(arr.clone());
                    outputArr(DCT);
                    System.out.println("--------------------------------------------------------------------------");
                    DCT = changeBias(DCT);
                    return;
                }
            }
            DCT.add(arr.clone());
            outputArr(DCT);
            System.out.println("--------------------------------------------------------------------------");
            if(code.length() < 8)break;
            else {
                while (code.length() % 8 != 0) {
                    code = code.substring(1);
                }
            }

        }
        DCT = changeBias(DCT);
        return;
    }

    /**
     * 去差分
     * @param DCT 二维数组
     * @return 去差分后数组
     */
    public static ArrayList<int[]> changeBias(ArrayList<int[]> DCT){
        for(int i = 1;i < DCT.size() ;i++){
            DCT.get(i)[0] += DCT.get(i - 1)[0];
        }
        return DCT;
    }


    /**
     * 1*64数组转二进制字符串
     * @return DCT码
     */
    public String setDCT(){
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
     * 获取图片中的huffman表
     * @param image
     */
    public void getHuffmanTable(byte[] image){
        Point DC_luminance = new Point();
        Point AC_luminance = new Point();
        Point DC_chrominance = new Point();
        Point AC_chrominance = new Point();
        for(int i = 0 ;i < image.length;i++){
            if(image[i] == -1 && image[i+1] == -60 && image[i+4] == 0){
                DC_luminance.x = i+5;
            }else if(image[i] == -1 && image[i+1] == -60 && image[i+4] == 16){
                DC_luminance.y = i-1;
                AC_luminance.x = i+5;
            }else if(image[i] == -1 && image[i+1] == -60 && image[i+4] == 1){
                AC_luminance.y = i-1;
                DC_chrominance.x = i+5;
            }else if(image[i] == -1 && image[i+1] == -60 && image[i+4] ==  17){
                DC_chrominance.y = i-1;
                AC_chrominance.x = i+5;
            }else if(image[i] == -1 && image[i+1] == -38){
                AC_chrominance.y = i-1;
                break;
            }
        }
            byte[] DC_L = new byte[DC_luminance.y - DC_luminance.x+1];
            byte[] DC_C = new byte[DC_chrominance.y - DC_chrominance.x +1];
            byte[] AC_L = new byte[AC_luminance.y - AC_luminance.x + 1];
            byte[] AC_C = new byte[AC_chrominance.y - AC_chrominance.x+1];
            System.arraycopy(image,DC_luminance.x,DC_L,0,DC_L.length);
            System.arraycopy(image,DC_chrominance.x,DC_C,0,DC_C.length);
            System.arraycopy(image,AC_luminance.x,AC_L,0,AC_L.length);
            System.arraycopy(image,AC_chrominance.x,AC_C,0,AC_C.length);
            DCC = new DCTable(DC_C);
            DCL = new DCTable(DC_L);
            ACC = new ACTable(AC_C);
            ACL = new ACTable(AC_L);
            DCC.outputDCTable("DCC");
            DCL.outputDCTable("DCL");
            ACC.outputACTable("ACC");
            ACL.outputACTable("ACL");
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
        int xorTarget = (int)((Math.pow(2,length)-Math.pow(2,length-1))*0.88 +Math.pow(2,length-1));
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
        for (int j = 0;j <target.length;j++) {
            byte b = target[j];
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
            if(b == -1)j++;
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
                ArrayList<Byte> Bts = new ArrayList<>();
        for (byte bt : bts) {
            Bts.add(bt);
            if(bt == -1){
                Bts.add((byte)0);
            }
        }
        bts = new byte[Bts.size()];
        for (int i =0;i <Bts.size();i++){
            bts[i] = Bts.get(i);
        }
            return bts;
        }

    //输出二维数组信息
    public static void outputArr(ArrayList<int[]> arr){
        for (int[] ints : arr) {
            System.out.print("{");
            for (int anInt : ints) {
                System.out.print(anInt+",");
            }
            System.out.println("}");
        }
    }

    public static void main(String[] args) {
        DealWithImage d = new DealWithImage(ImageToCode.imageToByte("./测试用图片/8纯蓝图.jpg"));
        d.getDCT(bytes2Str0b(ImageToCode.imageToByte("./测试用图片/8纯蓝图.jpg")));
    }
}
