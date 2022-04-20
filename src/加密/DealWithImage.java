package 加密;



import java.awt.*;
import java.util.ArrayList;

import static 加密.ImageToCode.*;
import static 加密.ImageToCode.imageToByte;
import static 加密.OutputForText.*;

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
        System.out.println("获取哈夫曼表！");
        for (start = image.length - 1; start >= 0; start--) {
            if (image[start] == -1 && image[start + 1] == -38) {
                start += 2;
                break;
            }
        }
        System.out.println("startGet!");
        start += image[start] * 16 * 16 + image[start + 1];
        target = new byte[image.length - 2 - start];
        System.arraycopy(image, start, target, 0, target.length);
    }

    public void simpleEn(String outFile){
        System.out.println("simpleEnStart!");
        getDCTOnlyDC();
        System.out.println("Y:"+yDC);
        System.out.println("Cb:"+CbDC);
        System.out.println("Cr:"+CrDC);

        //simpleAct();
        char[] Sbox = new char[256];
        //rc4(Sbox);
        rc4(Sbox);
        StringBuilder sb = new StringBuilder();
        int bytes = 0;
        /*for(int i = 1;i <=CbDC.size()*6;i++){//加密放回
            ArrayList<Point> DC;
            int index;
            switch (i % 6) {
                case 5 -> {
                    DC = CbDC;
                    index = i / 6;
                }
                case 0 -> {
                    DC = CrDC;
                    index = i / 6 - 1;
                }
                default -> {
                    DC = yDC;
                    index = i / 6 * 4 + i % 6 - 1;
                }
            }
            while(sb.length()<DC.get(index).y+16&&bytes < target.length)sb.append(byte2Str0b(target[bytes++]));
            if(DC.get(index).x != 0) {
                String temp = int2str0b(DC.get(index).x);
                sb.replace(DC.get(index).y, DC.get(index).y + temp.length(), temp);
            }
        }*/
        while(bytes < target.length)sb.append(byte2Str0b(target[bytes++]));
        System.out.println("Y:"+yDC);
        System.out.println("Cb:"+CbDC);
        System.out.println("Cr:"+CrDC);
        target = str0b2Bytes(new String(sb));
        byte[] temp = new byte[start+2+target.length];
        System.arraycopy(image,0,temp,0,start);
        temp[temp.length-1] = -39;
        temp[temp.length-2] = -1;
        System.arraycopy(target,0,temp,start,target.length);
        outputImage(outFile,temp);
    }


    public static void main(String[] args) {

        DealWithImage dealWithImage = new DealWithImage("./测试用图片/1.jpg");
        dealWithImage.simpleEn("./测试用图片/1.jpg");
    }
    /**
     * 仅异或DC系数
     */
    public void simpleAct() {
        int xs;
        String temp, key,result;
        double u = 3.79, x = 0.88;
        for(int o = 1;o <=3;o++) {
            ArrayList<Point> DC = switch (o) {
                case 1 -> yDC;
                case 2 -> CbDC;
                case 3 -> CrDC;
                default -> null;
            };
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
                    //System.out.println("temp:" + temp + " key:" + key + " result:" + result);
                    DC.set(i, new Point(str0b2int(result), DC.get(i).y));
                }
            }
        }
        }

/**
 *
 *   RC4
 *
 */

    public void rc4(char[] Sbox) {//得到Sbox
        int i = 0, j = 0;
        char[] key = {1, 2, 3};//密钥
        char[] K = new char[256];
        char tmp = 0;
        for (i = 0; i < 256; i++) {
            Sbox[i] = (char) i;
            K[i] = key[i % key.length];//超过长度则回到key[0]
        }
        for (i = 0; i < 256; i++) {
            j = (j + Sbox[i] + K[i]) % 256;//j = (j + i + key[i % Len]) % 256
            tmp = Sbox[i];
            Sbox[i] = Sbox[j]; //交换s[i]和s[j]
            Sbox[j] = tmp;
        }
        int T = 0, m = 0;
        for(int o = 1;o <= 3;o++) {
            ArrayList<Point> DC ;
            switch (o) {
                case 1 :DC = yDC;
                            break;
                case 2 :DC = CbDC;
                            break;
                case 3 :DC = CrDC;
                            break;
                default :DC = null;
            };
            for (i = 0, j = 0, m = 0; m < key.length; m++) {
                i = (i + 1) % 256;
                j = (j + Sbox[i]) % 256;
                tmp = Sbox[i];
                Sbox[i] = Sbox[j]; //交换s[x]和s[y]
                Sbox[j] = tmp;
                T = (Sbox[i] + Sbox[j]) % 256;
            }
            if (o == 0) {
                for (int k = 0; k < 4; k++) {
                    DC.get(k).x ^= Sbox[T];//?
                }
            }
            else DC.get(0).x ^= Sbox[T];
        }
    }

    /**
     * 提取DCT块
     */
    public void getDCTOnlyDC() {
        String code = "";
        int bytes = 0;//压缩数据byte数组的输入数
        while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
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
            Point pDC;//  读取category
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
            while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//Size为0
                    if(pAC[0] == 0){// 0/0 EOB
                        code = code.substring(pAC[2]);
                        while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
                        allStart += pAC[2];
                        break;
                    }else if(pAC[0] != 15){
                        System.out.println("剩余填充数据");
                        System.out.println(code.substring(0, Math.min(code.length(), 100)));
                        System.out.println(DC);
                        System.out.println("--------------------------------------------------------------------------");
                        return;
                    }
                }
                //Run个零+1个ac
                index += pAC[0]+1;
                code = code.substring(pAC[2]+pAC[1]);
                while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
                allStart += pAC[2]+pAC[1];
                //DCT块数据输入完毕
                if(index == 64){
                    break;
                }
                if(code.isEmpty()){
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
            Point pDC;//  读取category
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
                if(code.isEmpty()){
                    DCT.add(arr.clone());
                    outputArr(DCT);
                    System.out.println("--------------------------------------------------------------------------");
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
     * @param image 图片信息
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
     * @param s 数字
     * @return 遵循0开头为负二进制字符串
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


    //把byte数组转二进制字符串
    public static String bytes2Str0b(byte[] bytes){
        String[] binaryArray =
                {
                        "0000","0001","0010","0011",
                        "0100","0101","0110","0111",
                        "1000","1001","1010","1011",
                        "1100","1101","1110","1111"
                };

        String outStr = "";
        int i =0;
        for (int j = 0;j <bytes.length;j++) {
            byte b = bytes[j];
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
            if(b == -1)j++;
        }
        return outStr;
    }
    //把byte数组转二进制字符串
    public static String byte2Str0b(byte b){
        String[] binaryArray =
                {
                        "0000","0001","0010","0011",
                        "0100","0101","0110","0111",
                        "1000","1001","1010","1011",
                        "1100","1101","1110","1111"
                };

        String outStr = "";
        int i =0;
            i = (b&0xF0) >> 4;
            outStr+=binaryArray[i];
            i=b&0x0F;
            outStr+=binaryArray[i];
        return outStr;
    }

    //二进制字符串转byte数组
    public static byte[] str0b2Bytes(String input){
            StringBuilder in = new StringBuilder(input);
            // 注：这里in.length() 不可在for循环内调用，因为长度在变化
            int remainder = in.length() % 8;
            if (remainder > 0)
                in.append("0".repeat(8 - remainder));
            byte[] bts = new byte[in.length() / 8];
            // Step 8 Apply compression
            for (int i = 0; i < bts.length; i++)
                bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);
                ArrayList<Byte> Bts = new ArrayList<>();
        for (int i = 0;i<bts.length;i++) {
            Bts.add(bts[i]);
            if(bts[i] == -1&&bts[i+1] != 0){
                Bts.add((byte)0);
            }
        }
        bts = new byte[Bts.size()];
        for (int i =0;i <Bts.size();i++){
            bts[i] = Bts.get(i);
        }
            return bts;
        }

    /**
     * 去差分
     * @param x 去差分还是差分
     */
    public void changeBias(int x){
        for(int i = 1;i <=3;i++){
            ArrayList<Point> DC;
            switch (i) {
                case 1:
                    DC = yDC;
                    break;
                case 2:
                    DC = CbDC;
                    break;
                case 3:
                    DC = CrDC;
                    break;
                default:
                    DC = null;
            };
            switch (x){
                case 0://去差分
                    for(int j =0;j< DC.size()-1;j++){
                        DC.get(j+1).x+=DC.get(j).x;
                    }
                    break;
                case 1:
                    for(int j =DC.size()-1;j>0;j--){
                        DC.get(j).x-=DC.get(j-1).x;
                    }
                    break;
                default:
                    System.out.println("无效参数输入");
                    break;
            }

        }
    }
}
