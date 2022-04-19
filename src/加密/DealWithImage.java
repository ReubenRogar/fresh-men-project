package 加密;


import jdk.swing.interop.SwingInterOpUtils;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

import static 加密.ImageToCode.*;
import static 加密.ImageToCode.imageToByte;
import static 加密.OutputForText.output8Str;
import static 加密.OutputForText.outputArr;

public class DealWithImage {
    // 直流亮度表
    private DCTable DCL;
    // 直流亮度表
    private DCTable DCC;
    // 直流亮度表
    private ACTable ACL;
    // 直流亮度表
    private ACTable ACC;
    //DCT 1*64数据
    private ArrayList<int[]> DCT = new ArrayList<>();
    private ArrayList<Point> yDC;
    private ArrayList<Point> CbDC;
    private ArrayList<Point> CrDC;
    private byte[] image;
    private byte[] target;
    private int start;

    /**
     * 构造器获取图片的huffman表和DCT数据
     */
    public DealWithImage(String inFile){
        image = imageToByte(inFile);
        getHuffmanTable(image);
        for (start = image.length - 1; start >= 0; start--) {
            if (image[start] == -1 && image[start + 1] == -38) {
                start += 2;
                break;
            }
        }
        start += image[start] * 16 * 16 + image[start + 1];
        target = new byte[image.length - 2 - start];
        System.arraycopy(image, 0 + start, target, 0, target.length);
    }

    public void simpleEn(){
        getDCTOnlyDC(bytes2Str0b(target));
        /*
        for(int i = 1;i <=3;i++){//去差分
            ArrayList<Point> DC = null;
            switch (i){
                case 1:DC = yDC;
                break;
                case 2:DC = CbDC;
                break;
                case 3:DC = CrDC;
                break;
            }
            for(int j =0;j< DC.size()-1;j++){
                DC.set(j+1,new Point(DC.get(j+1).x+ DC.get(j).x,DC.get(j+1).y));
            }
        }*/
        System.out.println("Y:"+yDC);
        System.out.println("Cb:"+CbDC);
        System.out.println("Cr:"+CrDC);
        /*
        for(int i = 1;i <=3;i++){//差分
            ArrayList<Point> DC = null;
            switch (i){
                case 1:DC = yDC;
                    break;
                case 2:DC = CbDC;
                    break;
                case 3:DC = CrDC;
                    break;
            }
            for(int j =DC.size()-1;j>0;j--){
                DC.set(j,new Point(DC.get(j).x- DC.get(j-1).x,DC.get(j).y));
            }
        }*/
        simpleAct();

        StringBuilder sb = new StringBuilder(bytes2Str0b(target));
        System.out.println(sb);
        for(int i = 1;i <=3;i++){//加密放回
            ArrayList<Point> DC = null;
            switch (i){
                case 1:DC = yDC;
                    break;
                case 2:DC = CbDC;
                    break;
                case 3:DC = CrDC;
                    break;
            }
            for(int j =0;j< DC.size();j++){
                if(DC.get(j).x != 0) {
                    String temp = int2str0b(DC.get(j).x);
                    sb.replace(DC.get(j).y, DC.get(j).y + temp.length(), temp);
                }
            }
        }
        System.out.println(sb);
        System.out.println("Y:"+yDC);
        System.out.println("Cb:"+CbDC);
        System.out.println("Cr:"+CrDC);
        target = str0b2Bytes(new String(sb));
        System.arraycopy(target,0,image,start,target.length);
        outputImage("E:/测试/512难图--.jpg",image);
    }


    public static void main(String[] args) {
        //output8Str(bytes2Str0b(imageToByte("E:/测试/8蓝图.jpg")));
        DealWithImage dealWithImage = new DealWithImage("E:/测试/512难图-.jpg");
        dealWithImage.simpleEn();
        System.out.println(int2str0b(0));
    }
    /**
     * 仅异或DC系数
     */
    public void simpleAct() {
        int xs;
        String temp, key,result;
        double u = 3.79, x = 0.88;
        for(int o = 1;o <=3;o++) {
            ArrayList<Point> DC = null;
            switch (o) {
                case 1:
                    DC = yDC;
                    break;
                case 2:
                    DC = CbDC;
                    break;
                case 3:
                    DC = CrDC;
                    break;
            }
            for (int i = 0; i < DC.size(); i++) {
                if (DC.get(i).x!=0) {
                    temp = int2str0b(DC.get(i).x);
                    xs = (int) (Math.pow(2, temp.length() - 1) * (1 + x));
                    x = x * u * (1 - x);
                    key = int2str0b(xs);
                    result = "" + temp.charAt(0);
                    for (int j = 1; j < temp.length(); j++) {
                        if (temp.length() != key.length()) {
                            System.out.println("出错！");
                            return;
                        }
                        if (temp.charAt(j) == key.charAt(j)) result += "0";
                        else result += "1";
                    }
                    System.out.println("temp:" + temp + " key:" + key + " result:" + result);
                    DC.set(i, new Point(str0b2int(result), DC.get(i).y));
                }
            }
        }
        }




    /**
     * 提取DCT块
     * @param code 二进制字符串
     * @return 1*64数据块
     */
    public void getDCTOnlyDC(String code) {
        int allStart = 0;//Dc系数在压缩数据中的位置
        DCTable dcTable;
        ACTable acTable;
        yDC = new ArrayList<>();
        CrDC = new ArrayList<>();
        CbDC = new ArrayList<>();
        ArrayList<Point> DC;
        int flag = -1;//表区分标志
        //读DCT块
        System.out.println("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            int index = 1;
            if (flag % 6 < 4) {
                System.out.println("亮度");
                dcTable = DCL;
                acTable = ACL;
                DC = yDC;
            } else if(flag% 6 ==4){
                System.out.println("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CbDC;
            }else{
                System.out.println("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CrDC;
            }

            //读取DC系数
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);
            if(pDC.y == 0)return;
            allStart += pDC.y;
            if (pDC.x == 0)
                DC.add(new Point(0,allStart));
            else DC.add(new Point(str0b2int(code.substring(pDC.y, pDC.x + pDC.y)),allStart));//byte转int(DC)
            allStart += pDC.x;
//测试
            System.out.println(code.substring(pDC.y, pDC.x + pDC.y)+":"+DC.get(DC.size()-1)+"allStart:"+allStart);

            code = code.substring(pDC.x + pDC.y);
            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//Size为0
                    if(pAC[0] == 0){// 0/0 EOB
                        code = code.substring(pAC[2]);
                        allStart += pAC[2];
                        break;
                    }else if(pAC[0] != 15){
                        System.out.println("剩余填充数据");
                        System.out.println(code.substring(0,code.length()<100?code.length():100));
                        System.out.println(DC);
                        System.out.println("--------------------------------------------------------------------------");
                        return;
                    }
                }
                //Run个零+1个ac
                index += pAC[0]+1;
                code = code.substring(pAC[2]+pAC[1]);
                allStart += pAC[2]+pAC[1];
                //DCT块数据输入完毕
                if(index == 64){
                    break;
                }
                if(code == ""){
                    System.out.println("--------------------------------------------------------------------------");
                    return;
                }
            }
            System.out.println("--------------------------------------------------------------------------");
        }
    }


    /**
     * 提取DCT块
     * @param code 二进制字符串
     * @return 1*64数据块
     */
    public void getDCT(String code) {
//测试
        System.out.print("全部数据:");
        OutputForText.output8Str(code);
        int[] arr;//接收一个DCT块数据的数组
        DCTable dcTable;
        ACTable acTable;
        int flag = -1;//表区分标志
        //读DCT块
//测试
        System.out.println("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            arr = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
            int index = 0;
            if (flag % 3 == 0) {
                System.out.println("亮度");
                dcTable = DCL;
                acTable = ACL;

            } else {
                System.out.println("色度");
                dcTable = DCC;
                acTable = ACC;


            }

            //读取DC系数
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);

            if (pDC.x == 0) index++;
            else arr[index++] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
//测试
            System.out.println(code.substring(pDC.y, pDC.x + pDC.y)+":"+arr[index-1]);

            code = code.substring(pDC.x + pDC.y);
//测试
            System.out.print("剩余数据:");
            OutputForText.output8Str(code);
            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//Size为0
                    if(pAC[0] == 0){// 0/0 EOB
                        code = code.substring(pAC[2]);
                        break;
                    }else if(pAC[0] == 16){// F/0 16个零
                        index+=16;
                        code = code.substring(pAC[2]);
//测试
                        System.out.print("剩余数据:");
                        OutputForText.output8Str(code);
                        continue;
                    }else{
                        System.out.println("剩余填充数据");
                        DCT.add(arr.clone());
                        outputArr(DCT);
                        System.out.println("--------------------------------------------------------------------------");
                        DCT = changeBias(DCT);
                        return;
                    }
                }
                //Run个零
                    index += pAC[0];
                    output8Str(code.substring(0,code.length()%8+8));
                    arr[index++] = str0b2int(code.substring(pAC[2], pAC[2] + pAC[1]));
//测试
                System.out.println(code.substring(pAC[2], pAC[2]+pAC[1])+":"+arr[index-1]+" index:"+(index-1));
                code = code.substring(pAC[2]+pAC[1]);
//测试
                System.out.print("剩余数据:");
                OutputForText.output8Str(code);
                //DCT块数据输入完毕
                if(index == 64){
                    break;
                }
                if(code == ""){
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
//测试
        //System.out.println("------------------------setDCT-------------------------");
        int DCTs = 0,index = 1;
        for (int[] ints : DCT) {//遍历1*64数据块
            DCTable dcTable;
            ACTable acTable;
            if (DCTs % 3 == 0) {//亮度
                dcTable = DCL;
                acTable = ACL;
//测试
                //System.out.println("亮度");
            }else {//色度*2
                dcTable = DCC;
                acTable = ACC;
//测试
                //System.out.println("色度");
            }
                if(ints[0]!= 0){
                    temp = int2str0b(ints[0]);
                    code += dcTable.getHuffmanCode(temp.length()) + temp;
                }else{
                    temp ="00";
                    code += "00";
                }
//数据
            //System.out.println("DC:"+ints[0]+" "+temp);
            //System.out.print("数据:");
            OutputForText.outputStr8(code);

                int lastNum = 0;
                for (index = 1; index < 64; index++) {
                    if (ints[index] != 0) {
                        temp = int2str0b(ints[index]);
//测试
                        //System.out.println("AC:"+ints[index] +" " +acTable.getHuffmanCode(index - lastNum - 1, temp.length()));
                        code += acTable.getHuffmanCode(index - lastNum - 1, temp.length());
                        code += temp;
                        lastNum = index;
                    } else if (lastNum < 63 && index == 63 && !(DCTs == DCT.size()-1&&code.length()%8 == 0)) {
                        code += acTable.getEOB();
                    }
                }
                if(DCTs!=DCT.size()-1)while (code.length()%8!=0)code += "0";
                else while (code.length()%8!=0)code += "1";
//测试
            //System.out.println("一个dct块输入结束：");
            //System.out.print("数据:");
            //OutputForText.outputStr8(code);

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


}
