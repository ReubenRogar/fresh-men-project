package cn.hitwh.JPEG;


import cn.hitwh.Encrypt.KeyXU;
import cn.hitwh.Encrypt.NewTypeEncrypt;
import cn.hitwh.Rc4.RC4;
import com.google.common.primitives.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import static cn.hitwh.JPEG.ImageToCode.imageToByte;

public class JPEGs {
    //图片名称
    private final String name;
    // 直流亮度表
    private DCTable DCL;
    // 直流色度表
    private DCTable DCC;
    // 交流亮度表
    private ACTable ACL;
    // 交流色度表
    private ACTable ACC;
    //DCT 1*64数据
    private ArrayList<int[]> yDCT;//yDCT数据
    private ArrayList<int[]> CbDCT;//CbDCT数据
    private ArrayList<int[]> CrDCT;//CrDCT数据
    private byte[] image;//图片所有数据
    private byte[] target;//压缩数据
    private int startOfSOS;//扫描行开始
    private int endOfImage;//图像结尾
    private int height;//图片的高度
    private int width;//图片的宽度
    private int samplingRatio;//图片的采样模式

    private int DDReset = 0;//FF DD段定义的扫描行复位间隔


    //Logback框架
    public static final Logger LOGGER = LoggerFactory.getLogger("JPEGs.class");

    /**
     * 构造器获取图片的huffman表和DCT数据
     */
    public JPEGs(String inFile) {
        name = inFile;
        image = imageToByte(name);
        ImageToCode.dataToFile(ImageToCode.byteToString(image), inFile + ".txt");
        //FF D8
        if (image[0] != -1 || image[1] != -40)
            throw new JPEGWrongStructureException("The start of the file doesn't match JPEG");
        LOGGER.debug("get Huffman Table！");
        getHuffmanTable();
        LOGGER.debug("get Huffman Table successfully!");

        for (int index = 0; index < image.length; index++) {
            if(image[index] == -1){
            //FF C0或FF C2
                switch (image[index + 1]) {
                    case -64:
                        case -62:
                        LOGGER.debug("SOF0");
                        DDReset = 0;
                        LOGGER.debug("DDReset reset");
                        index += 5;
                        height = (image[index] & 0xFF) * 16 * 16 + (image[index + 1] & 0xFF);
                        index += 2;
                        width = (image[index] & 0xFF) * 16 * 16 + (image[index + 1] & 0xFF);
                        LOGGER.debug(height + "*" + width);
                        index += 2;
                        if (image[index] != 3)
                            LOGGER.error("The image isn't the type of yCbCr and has " + image[index] + " sets");
                        else if (image[index + 2] == 34 && image[index + 5] == 17 && image[index + 8] == 17)
                            samplingRatio = 4;//22 11 11  420
                        else if (image[index + 2] == 17 && image[index + 5] == 17 && image[index + 8] == 17)
                            samplingRatio = 1;//11 11 11  444
                        else if ((image[index + 2] == 18 || image[index + 2] == 33) && image[index + 5] == 17 && image[index + 8] == 17)
                            samplingRatio = 2;//12/21 11 11  422
                        else {
                            LOGGER.debug(image[index + 2] + " " + image[index + 5] + " " + image[index + 8]);
                            throw new JPEGWrongStructureException("Unusual sampling!");
                        }
                        LOGGER.debug("sampling" + samplingRatio);
                    break;
                    //FF DA
                    case -38:
                        LOGGER.debug("SOS");
                        startOfSOS = 2 + index;
                    break;
                    //FF DD
                    case -35:
                        LOGGER.debug("DRI");
                        index += 4;
                        DDReset = (image[index] & 0xFF) * 16 * 16 + (image[index + 1] & 0xFF);
                        LOGGER.debug(image[index] + " " + image[index + 1]);
                        LOGGER.debug("DDReset:" + DDReset);
                    break;
                    //FF D9
                    case -39:
                        LOGGER.debug("EOF");
                        endOfImage = index - 1;
                        break;
                    }
                }
        }

        startOfSOS += image[startOfSOS] * 16 * 16 + image[startOfSOS + 1];
        target = new byte[endOfImage + 1 - startOfSOS];
        System.arraycopy(image, startOfSOS, target, 0, target.length);
        ImageToCode.dataToFile(ImageToCode.byteToString(target), inFile + "target1.txt");
        getTargetWithff00();


    }

    /**
     * 获取dct系数并显示过程结果
     */
    public  void encryptDCT() throws NoSuchAlgorithmException {
        getDCT();
        LOGGER.debug("Y:");
        LOGGER.debug(outputArr(yDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("Cb:");
        LOGGER.debug(outputArr(CbDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("Cr:");
        LOGGER.debug(outputArr(CrDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
            NewTypeEncrypt nte = new NewTypeEncrypt(yDCT, new KeyXU(0.6, 3.9));
            KeyXU key = nte.getFinalKey();
            LOGGER.debug("size:"+yDCT.size());
            LOGGER.debug(key.x + " " + key.u);

        LOGGER.debug("Y:");
        LOGGER.debug(outputArr(yDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("Cb:");
        LOGGER.debug(outputArr(CbDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("Cr:");
        LOGGER.debug(outputArr(CrDCT));
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");
        LOGGER.debug("///////////////////////////////////////////////////////////////////////////////");

        setDCT();
        if(target.length == endOfImage - startOfSOS + 1)
        System.arraycopy(target,0,image,startOfSOS,target.length);
        else{
            //压缩数据变动
            byte[] bytes = new byte[image.length - endOfImage+startOfSOS-1 +target.length];
            System.arraycopy(image,0,bytes,0,startOfSOS);
            System.arraycopy(target,0,bytes,startOfSOS,target.length);
            System.arraycopy(image,endOfImage+1,bytes,startOfSOS + target.length,image.length-1-endOfImage);
            image = bytes;
        }
        ImageToCode.outputImage("测试用图片/测试.jpg",image);
    }

   void debugDCT(){
       getDCT();

       //加密

       setDCT();
       if(target.length == endOfImage - startOfSOS + 1)
           System.arraycopy(target,0,image,startOfSOS,target.length);
       else{
           //压缩数据变动
           byte[] bytes = new byte[image.length - endOfImage+startOfSOS-1 +target.length];
           System.arraycopy(image,0,bytes,0,startOfSOS);
           System.arraycopy(target,0,bytes,startOfSOS,target.length);
           System.arraycopy(image,endOfImage+1,bytes,startOfSOS + target.length,image.length-1-endOfImage);
           image = bytes;
       }
       ImageToCode.outputImage("测试用图片/测试.jpg",image);
    }
    public String outputArr(ArrayList<int[]> DCT) {
            StringBuilder sb = new StringBuilder();
        for (int id = 0;id < DCT.size();id++) {
            int[] ints = DCT.get(id);
            sb.append("id = "+id+" {");
           for (int i = 0;i < 64;i++){
               sb.append(ints[i]+",");
           }
           sb.append("}\n");
        }
        return sb.toString();
    }

    /**
     * 提取DCT块
     */
     public static void rc4(int[] d) {
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

    public void getDCT() {
        var code = new StringBuffer();
        int bytes = 0;//压缩数据byte数组的输入数
        int allStart = 0;//Dc系数在压缩数据中的位置
        DCTable dcTable;
        ACTable acTable;
        yDCT = new ArrayList<>();
        CrDCT = new ArrayList<>();
        CbDCT = new ArrayList<>();
        ArrayList<int[]> DCT;
        int flag = -1;//表区分标志
        //读DCT块
        LOGGER.debug("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            int index = 1;
            if(samplingRatio == 0){
                throw new JPEGWrongStructureException("Unusual sampling!");
            }
            if (flag % (samplingRatio + 2) == samplingRatio) {
//                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CbDCT;
            } else if (flag % (samplingRatio + 2) == samplingRatio + 1){
//                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CrDCT;
            }else{
//                LOGGER.debug("亮度");
                dcTable = DCL;
                acTable = ACL;
                DCT = yDCT;
            }
//            LOGGER.debug("NO."+DCT.size());
            //RST间隔
            if(DDReset != 0 && yDCT.size()%(DDReset*samplingRatio) == 0 && yDCT.size() == samplingRatio*CrDCT.size()) {
                LOGGER.debug(code.substring(0,code.length()%8));
                allStart+=code.length()%8;
                code.delete(0, code.length() % 8);
            }
            var dct=new int[64];
            //读取DC系数
            while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
            Point pDC;//  读取category
            pDC = dcTable.getCategory(code);
            if(pDC.y == 0){
                LOGGER.debug("读取位置："+bytes+" "+"总长："+target.length);
                LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                return;
            }
            allStart += pDC.y;
            if (pDC.x == 0)
                dct[0] = 0;
            else dct[0] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
            allStart += pDC.x;
//测试
//            LOGGER.debug(code.substring(pDC.y, pDC.x + pDC.y)+":"+dct[0]+"allStart:"+allStart);

            code.delete(0,pDC.x + pDC.y);

            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
                while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//Size为0
                    if(pAC[0] == 0){// 0/0 EOB
                        code.delete(0,pAC[2]);
                        while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
                        allStart += pAC[2];
                        DCT.add(dct);
                        break;
                    }else if(pAC[0] != 15){
                        LOGGER.debug("剩余填充数据");
                        LOGGER.debug(code.substring(0, Math.min(code.length(), 100)));
                        LOGGER.debug("读取位置："+bytes+" "+"总长："+target.length);
                        LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                        LOGGER.debug("--------------------------------------------------------------------------");
                        return;
                    }
                }
                //Run个零
                index += pAC[0];
                if(index >= 64){
                    LOGGER.debug("读取位置："+bytes+" "+"总长："+target.length);
                    LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                }
                dct[index++] = str0b2int(code.substring(pAC[2],pAC[2]+pAC[1]));
                code.delete(0,pAC[2]+pAC[1]);
                allStart += pAC[2]+pAC[1];
                //DCT块数据输入完毕
                if(index == 64){
                    DCT.add(dct);
                    break;
                }
                if(code.isEmpty()){
                    LOGGER.debug("--------------------------------------------------------------------------");
                    return;
                }
            }
//            LOGGER.debug("--------------------------------------------------------------------------");
        }//while
    }




    /**
     * 1*64数组转二进制字符串
     */
    void setDCT(){
        var bytes = new ArrayList<Byte>();
        var sb  = new StringBuilder(8);
        DCTable dcTable;
        ACTable acTable;
        ArrayList<int[]> DCT;
        int index = 0;
        //set开始
        LOGGER.debug("----------------------setDCT------------------------");
        while(index < CbDCT.size()+CrDCT.size()+yDCT.size()){
            int i;
            if (index % (samplingRatio + 2) == samplingRatio) {
//                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CbDCT;
                i = index/(samplingRatio+2);
            } else if (index % (samplingRatio + 2) == samplingRatio + 1){
//                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CrDCT;
                i = index/(samplingRatio+2);
            }else{
//                LOGGER.debug("亮度");
                dcTable = DCL;
                acTable = ACL;
                DCT = yDCT;
                i = index/(samplingRatio+2)*samplingRatio + index%(samplingRatio+2);
            }
//            LOGGER.debug("NO."+i);
            //二进制转byte同时检查FF
            while(sb.length() >= 8){
                bytes.add((byte) Integer.parseInt(sb.substring(0,8), 2));
                sb.delete(0,8);
                //FF
                if(bytes.get(bytes.size()-1) == -1){
                   bytes.add((byte)0);
                }
            }
            //RST间隔
            if(DDReset != 0 && index != 0 && index%((samplingRatio+2)*DDReset) == 0){
                while (sb.length()%8 != 0)sb.append('1');
                while(sb.length() > 0){
                    bytes.add((byte) Integer.parseInt(sb.substring(0,8), 2));
                    sb.delete(0,8);
                    //FF
                    if(bytes.get(bytes.size()-1) == -1){
                        bytes.add((byte)0);
                    }
                }
                bytes.add((byte)-1);bytes.add((byte)((index/((samplingRatio+2)*DDReset)-1)%8-48));
                LOGGER.debug("FF D"+((index/((samplingRatio+2)*DDReset)-1)%8));
            }
            //DC
            int[] dct = DCT.get(i);
            String s = int2str0b(dct[0]);
            sb.append(dcTable.getHuffmanCode(s.length()));
            sb.append(s);
//            LOGGER.debug(s+":"+dct[0]);
//            LOGGER.debug("allStart:"+sb.length());
            //DC end
            //AC
            i = 1;
            int last = 0;
            while(i < 64){
                if(dct[i] != 0){
                    s = int2str0b(dct[i]);
                    sb.append(acTable.getHuffmanCode(i-last-1,s.length()));
                    sb.append(s);
                    last = i;
                }//if
                i++;
            }//while
            //ac系数不足63个，靠后全为0
            if(last != 63)sb.append(acTable.getEOB());
            //AC end
            index++;
//            LOGGER.debug("--------------------------------------------------------------------------");
        }//while
        while(sb.length()%8!=0)sb.append('1');
        while(sb.length() != 0){
            bytes.add((byte) Integer.parseInt(sb.substring(0,8), 2));
            sb.delete(0,8);
            //FF
            if(bytes.get(bytes.size()-1) == -1){
                bytes.add((byte)0);
            }
        }
        LOGGER.debug("before:"+(endOfImage - startOfSOS+1));
        target = Bytes.toArray(bytes);
        LOGGER.debug("after:"+target.length);
        ImageToCode.dataToFile(ImageToCode.byteToString(target), name + "target2.txt");
    }


    /**
     * 获取图片中的huffman表
     */
    private void getHuffmanTable(){
        Point DC_luminance = new Point();
        Point AC_luminance = new Point();
        Point DC_chrominance = new Point();
        Point AC_chrominance = new Point();
        for(int i = 0 ;i < image.length;i++){
            //FF C4
            if(image[i] == -1 && image[i+1] == -60){
                int count = 0;//明码计数
                for(int j = 0;j < 16;j++){
                    count += image[j+i+5];
                }
                if(count == (image[i+3]&0xFF)+16*16*((image[i+2])&0xFF)-3-16){
                    //一个DHT单独定义一个表
                    switch (image[i + 4]) {
                        case 0://00 第一DC表
                            DC_luminance.x = i + 5;
                            DC_luminance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 1://01 第二DC表
                            DC_chrominance.x = i + 5;
                            DC_chrominance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 16://10 第一AC表
                            AC_luminance.x = i + 5;
                            AC_luminance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 17://11 第二AC表
                            AC_chrominance.x = i + 5;
                            AC_chrominance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                    }
                }else{
                    //一个DHT定义多个表
                    i += 4;
                    if(image[i] == 0 || image[i] == 1 || image[i] == 16 || image[i] == 17) {
                        while (image[i] != -1) {//FF
                            count = 0;
                            for (int j = 0; j < 16; j++) {
                                count += image[j + i + 1];
                            }
                            switch (image[i]) {
                                case 0://00 第一DC表
                                    DC_luminance.x = i + 1;
                                    DC_luminance.y = i + count + 17;
                                    break;
                                case 1://01 第二DC表
                                    DC_chrominance.x = i + 1;
                                    DC_chrominance.y = i + count + 17;
                                    break;
                                case 16://10 第一AC表
                                    AC_luminance.x = i + 1;
                                    AC_luminance.y = i + count + 17;
                                    break;
                                case 17://11 第二AC表
                                    AC_chrominance.x = i + 1;
                                    AC_chrominance.y = i + count + 17;
                                    break;
                            }//switch
                            i += count + 17;
                        }//while
                    }//if
                }//if else
            }
        }//for
            if(DC_luminance.x == 0&&DC_chrominance.y == 0)throw new JPEGWrongStructureException();
            byte[] DC_L = new byte[DC_luminance.y - DC_luminance.x];
            byte[] DC_C = new byte[DC_chrominance.y - DC_chrominance.x ];
            byte[] AC_L = new byte[AC_luminance.y - AC_luminance.x];
            byte[] AC_C = new byte[AC_chrominance.y - AC_chrominance.x];
            System.arraycopy(image,DC_luminance.x,DC_L,0,DC_L.length);
            System.arraycopy(image,DC_chrominance.x,DC_C,0,DC_C.length);
            System.arraycopy(image,AC_luminance.x,AC_L,0,AC_L.length);
            System.arraycopy(image,AC_chrominance.x,AC_C,0,AC_C.length);
            DCC = new DCTable(DC_C);LOGGER.debug("DCC complete");
            DCL = new DCTable(DC_L);LOGGER.debug("DCL complete");
            ACC = new ACTable(AC_C);LOGGER.debug("ACC complete");
            ACL = new ACTable(AC_L);LOGGER.debug("ACL complete");
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
        StringBuilder s1 = new StringBuilder();
        if(s < 0){
            s= -s;
            do{
                s1.insert(0,s%2 == 0? '1':'0') ;
                s /= 2;
            }while(s > 0);
        }else if(s > 0){
            do{
                s1.insert(0, s%2 == 0?'0':'1');
                s /= 2;
            }while(s > 0);
        }
        return s1.toString();
    }


    //把byte转二进制字符串
    public static String byte2Str0b(byte b){
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }


    /**
     * 去差分
     * @param x 去差分还是差分
     */
    public void changeBias(int x){
        switch (x){
            case 0:
                for(int i = 1;i <=3;i++){//去差分
                    ArrayList<int[]> DC = null;
                    switch (i){
                        case 1:DC = yDCT;
                            break;
                        case 2:DC = CbDCT;
                            break;
                        case 3:DC = CrDCT;
                            break;
                    }
                    for(int j =0;j< DC.size()-1;j++){
                        DC.get(j+1)[0]+=DC.get(j)[0];
                    }
                }
                break;
            case 1:
                for(int i = 1;i <=3;i++){//差分
                    ArrayList<int[]> DC = null;
                    switch (i){
                        case 1:DC = yDCT;
                            break;
                        case 2:DC = CbDCT;
                            break;
                        case 3:DC = CrDCT;
                            break;
                    }
                    for(int j =DC.size()-1;j>0;j--){
                        DC.get(j)[0]-=DC.get(j-1)[0];
                    }
                }
                break;
            default:
                LOGGER.debug("无效参数输入");
                break;
        }

    }

    /**
     * 将数据中的FF 00转化为FF
     */
    private void getTargetWithff00(){
        ArrayList<Byte> temp = new ArrayList<>();
        for(int i = 0;i < target.length;i++){
            //FF D_
            if(target[i] == -1&&target[i+1] != 0){
                i ++;
                continue;
            }
            temp.add(target[i]);
            //FF 00
            if(target[i] == -1&&target[i+1] == 0){
                i++;
            }
        }
        target = Bytes.toArray(temp);
    }
    public static void main(String args[]){
        int[] d = {2,2,7,9,79};
        rc4(d);//调用rc4方法
    }
}
