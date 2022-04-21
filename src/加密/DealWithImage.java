package 加密;
import java.awt.*;
import java.util.ArrayList;

import static 加密.ImageToCode.*;
import static 加密.ImageToCode.imageToByte;
import static 加密.OutputForText.*;

public class DealWithImage {
    // 直流亮度表
    private DCTable DCL;
    // 直流色度表
    private DCTable DCC;
    // 交流亮度表
    private ACTable ACL;
    // 交流色度表
    private ACTable ACC;
    //DCT 1*64数据
    private ArrayList<int[]> DCT = new ArrayList<>();
    private ArrayList<int[]> yDC;
    private ArrayList<int[]> CbDC;
    private ArrayList<int[]> CrDC;
    private byte[] image;
    private byte[] target;
    private int start;




    /**
     * 构造器获取图片的huffman表和DCT数据
     */
    public DealWithImage(String inFile){
        image = imageToByte(inFile);
        DCC = new DCTable("./HuffmanTable/DC_chrominance.txt");
        ACC = new ACTable("./HuffmanTable/AC_chrominance.txt");
        DCL = new DCTable("./HuffmanTable/DC_luminance.txt");
        ACL = new ACTable("./HuffmanTable/AC_luminance.txt");
        //getHuffmanTable(image);
        for (start = image.length - 1; start >= 0; start--) {
            if (image[start] == -1 && image[start + 1] == -38) {
                start += 2;
                break;
            }
        }
        start += image[start] * 16 * 16 + image[start + 1];
        target = new byte[image.length - 2 - start];
        System.arraycopy(image, start, target, 0, target.length);
    }

    public void simpleEn(String outFile) {
        getDCT();
        changeBias(0);
        System.out.print("Y:");outputArr(yDC);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.print("Cb:");outputArr(CbDC);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.print("Cr:");outputArr(CrDC);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------");
        changeBias(1);

        //rc4();


        //System.out.print("EnY:");outputArr(yDC);
        //System.out.print("EnCb:");outputArr(CbDC);
        //System.out.print("EnCr:");outputArr(CrDC);
        setDCT(outFile);
    }


    public static void main(String[] args) {
        String fileName = "128粉线图";
        DealWithImage dealWithImage = new DealWithImage("E:/测试/"+fileName+".jpg");
        dealWithImage.simpleEn("E:/测试/"+fileName+"-.jpg");
        //DealWithImage DealWithImage = new DealWithImage("E:/测试/"+fileName+"-.jpg");
        //DealWithImage.simpleEn("E:/测试/"+fileName+"--.jpg");
    }

/**
 *
 *   RC4加密
 *
 */

    public void rc4() {//得到Sbox
        int i = 0, j = 0;
        char[] Sbox = new char[256];
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
        int T,index;
        for (int flag = 1; flag <= CbDC.size()+CrDC.size()+yDC.size(); flag++) {
            ArrayList<int[]> DC,EnDC;
            switch (flag%6) {
                case 5:
                    //System.out.print("Cb: ");
                    DC = CbDC;
                    index = flag/6;
                   break;
                case 0:
                    //System.out.print("Cr: ");
                    DC = CrDC;
                    index = flag/6-1;
                    break;
                default:
                    //System.out.print("Y: ");
                    DC = yDC;
                    index = flag/6*4+flag%6-1;
                    break;
            }
                i = (i + 1) % 256;
                j = (j + Sbox[i]) % 256;
                tmp = Sbox[i];
                Sbox[i] = Sbox[j]; //交换s[x]和s[y]
                Sbox[j] = tmp;
                T = (Sbox[i] + Sbox[j]) % 256;
                DC.get(index)[0]^=Sbox[T];
            //System.out.print(" index:"+index+" x:"+EnDC.get(index).x+" y:"+EnDC.get(index).y+" LastY:"+lastY);

        }
    }

    /**
     * 提取DCT块
     */
    public void getDCT() {
        String code = "";
        int bytes = 0;//压缩数据byte数组的输入数
        while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
        DCTable dcTable;
        ACTable acTable;
        yDC = new ArrayList<>();
        CrDC = new ArrayList<>();
        CbDC = new ArrayList<>();
        ArrayList<int[]> DC;
        int flag = -1;//表区分标志
        //读DCT块
        System.out.println("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            int index = 1;
            if (flag % 6 < 4) {
                //System.out.println("亮度");
                dcTable = DCL;
                acTable = ACL;
                DC = yDC;
            } else if(flag% 6 ==4){
                //System.out.println("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CbDC;
            }else{
                //System.out.println("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CrDC;
            }
            int[] arr = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            //读取DC系数
            Point pDC;//  读取category
            pDC = dcTable.getCategory(code);
            if(pDC.y == 0)return;
            if (pDC.x != 0)arr[0] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
//测试
            System.out.println(code.substring(pDC.y, pDC.x + pDC.y)+":"+arr[0]);

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
                        DC.add(arr.clone());
                        break;
                    }else if(pAC[0] != 15){
                        System.out.println("剩余填充数据");
                        System.out.println(code.substring(0, Math.min(code.length(), 100)));
                        System.out.println(DC);
                        System.out.println("--------------------------------------------------------------------------");
                        return;
                    }else{
                        index += 16;
                        code = code.substring(pAC[2]);
                        continue;
                    }
                }
                //Run个零+1个ac
                index += pAC[0];
                arr[index++] = str0b2int(code.substring(pAC[2], pAC[2] + pAC[1]));
                code = code.substring(pAC[2]+pAC[1]);
                while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
                //DCT块数据输入完毕
                if(index > 63){
                    DC.add(arr.clone());
                    break;
                }
                if(code.isEmpty()){
                    System.out.println("--------------------------------------------------------------------------");
                    return;
                }
            }
        }
    }

    /**
     * 放回DC系数
     */
    public void setDCT(String outFile) {
        StringBuilder sb = new StringBuilder();
        int bytes = 0;//bytes为target数组的位置
        for(int flag = 0;flag <CbDC.size()+yDC.size()+CrDC.size();flag++){//加密放回
            ArrayList<int[]> EnDC,DC;
            DCTable dcTable;
            ACTable acTable;
            String temp;
            int index;
            switch (flag % 6) {
                case 4:
                    DC = CbDC;
                    dcTable = DCC;
                    acTable = ACC;
                    index = flag / 6;
                    System.out.print("EnCbDC["+index+"]");
                    break;
                case 5:
                    DC = CrDC;
                    dcTable = DCC;
                    acTable = ACC;
                    index = flag / 6;
                    System.out.print("EnCrDC["+index+"]");
                    break;
                default:
                    DC = yDC;
                    dcTable = DCL;
                    acTable = ACL;
                    index = flag / 6 * 4 + flag % 6;
                    System.out.print("EnyDC["+index+"]");
                    break;
            }
            temp = int2str0b(DC.get(index)[0]);
            temp = dcTable.getHuffmanCode(temp.length()) +temp;
            sb.append(temp);
            int last = 0;
            for(int i = 1;i <64;i++){
                if(DC.get(index)[i] != 0){
                    temp = int2str0b(DC.get(index)[i]);
                    temp = acTable.getHuffmanCode(i - last - 1,temp.length()) + temp;
                    sb.append(temp);
                    last = i;
                }
            }
        }
        while (sb.length() %8 !=0)sb.append("1");
        target = str0b2Bytes(new String(sb));
        byte[] temp = new byte[start+2+target.length];
        System.arraycopy(image,0,temp,0,start);
        temp[temp.length-1] = -39;
        temp[temp.length-2] = -1;
        System.arraycopy(target,0,temp,start,target.length);
        outputImage(outFile,temp);
        System.out.println("----------------------------上图结束----------------------------------");
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
        if(s < 0) {
            s = -s;
            do {
                s1.insert(0, s % 2 == 0 ? '1' : '0');
                s /= 2;
            } while (s > 0);
        }else if(s > 0){
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
            ArrayList<int[]> DC;
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
                        DC.get(j+1)[0]+=DC.get(j)[0];
                    }
                    break;
                case 1:
                    for(int j =DC.size()-1;j>0;j--){
                        DC.get(j)[0]-=DC.get(j-1)[0];
                    }
                    break;
                default:
                    System.out.println("无效参数输入");
                    break;
            }

        }
    }
}
