package cn.hitwh;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;

import static cn.hitwh.ImageToCode.imageToByte;

public class JPEGs {
    // 直流亮度表
    private DCTable DCL;// 直流色度表
    private DCTable DCC;// 交流亮度表
    private ACTable ACL;// 交流色度表
    private ACTable ACC;//DCT 1*64数据
    private ArrayList<Point> yDC;
    private ArrayList<Point> CbDC;
    private ArrayList<Point> CrDC;
    private ArrayList<int[]> yDCT;//yDCT数据
    private ArrayList<int[]> CbDCT;//CbDCT数据
    private ArrayList<int[]> CrDCT;//CrDCT数据
    final private byte[] image;//图片所有数据
    private byte[] target;//压缩数据
    private int startOfSOS;//扫描行开始
    private int height;//图片的高度
    private int width;//图片的宽度
    private int samplingRatio;//图片的采样模式

    private int DDReset = 0;//FF DD段定义的扫描行复位间隔


    //Logback框架
    public static final Logger LOGGER = LoggerFactory.getLogger("JPEGs.class");

        /**
         * 构造器获取图片的huffman表和DCT数据
         */
    public JPEGs(String inFile){
        image = imageToByte(inFile);

        //FF D8
        if(image[0] != -1 || image[1] != -40)throw new JPEGWrongStructureException("The start of the file doesn't match JPEG");
        LOGGER.debug("get Huffman Table！");
        getHuffmanTable();
        LOGGER.debug("get Huffman Table successfully!");

        for (int index = 0; index < image.length; index++) {
            //FF C0或FF C2
            if(image[index] == -1 && (image[index + 1] == -64 || image[index + 1] == -62)){
                LOGGER.debug("SOF0");
                index += 5;
                LOGGER.debug(""+image[index]+" "+image[index+1]+"*"+image[index+2]+" "+image[index+3]);
                height = (image[index] < 0 ? image[index]+256 : image[index])*16*16 + (image[index+1] < 0 ? image[index+1]+256 : image[index+1]);
                index += 2;
                width = (image[index] < 0 ? image[index]+256 : image[index])*16*16 + (image[index+1] < 0 ? image[index+1]+256 : image[index+1]);
                LOGGER.debug(height+ "*" + width);
                index += 2;
                if(image[index] != 3)LOGGER.error("The image isn't the type of yCbCr and has "+ image[index] + " sets");
                else if(image[index + 2] == 34 && image[index + 5] == 17 && image[index + 8] == 17) samplingRatio = 4;//22 11 11  420
                else if(image[index + 2] == 17 && image[index + 5] == 17 && image[index + 8] == 17) samplingRatio = 1;//11 11 11  444
                else if((image[index + 2] == 18 || image[index + 2] == 33) && image[index + 5] == 17 && image[index + 8] == 17)
                    samplingRatio = 2;//12/21 11 11  422
                else {
                    LOGGER.debug(image[index + 2] + " " + image[index + 5] + " "+image[index + 8]);
                    throw new JPEGWrongStructureException("Unusual sampling!");
                }
                LOGGER.debug("sampling"+ samplingRatio);
            }
            //FF DA
            else if (image[index] == -1 && image[index + 1] == -38) {
                LOGGER.debug("SOS");
                startOfSOS = 2 + index;
            }
            //FF DD
            else if (image[index] == -1 && image[index + 1] == -35){
                LOGGER.debug("DRI");
                index += 4;
                DDReset = (image[index] >= 0 ? image[index] : image[index]+256)*16*16 + (image[index+1] < 0 ? image[index+1]+256 : image[index+1]);
                LOGGER.debug(image[index]+" "+image[index+1]);
                LOGGER.debug("DDReset:"+DDReset);
            }
        }

        startOfSOS += image[startOfSOS] * 16 * 16 + image[startOfSOS + 1];
        target = new byte[image.length - 2 - startOfSOS];
        System.arraycopy(image, startOfSOS, target, 0, target.length);
        ImageToCode.dataToFile(ImageToCode.byteToString(image),inFile+".txt");
        getTargetWithff00();


    }

    /**
     * 获取dct系数并显示过程结果
     */
    public  void debugDCT(){
        getDCT();
//        LOGGER.debug("Y:");
//        LOGGER.debug(outputArr(yDCT));
//        LOGGER.debug("Cb:");
//        LOGGER.debug(outputArr(CbDCT));
//        LOGGER.debug("Cr:");
//        LOGGER.debug(outputArr(CrDCT));
    }

    /**
     * 提取DCT块
     */
    public void getDCT() {
        var code = new StringBuffer();
        int bytes = 0;//压缩数据byte数组的输入数
        while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
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
                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CbDCT;
            } else if (flag % (samplingRatio + 2) == samplingRatio + 1){
                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DCT = CrDCT;
            }else{
                LOGGER.debug("亮度");
                dcTable = DCL;
                acTable = ACL;
                DCT = yDCT;
            }
            LOGGER.debug("NO."+DCT.size());
            if(DDReset != 0 && yDCT.size()%(DDReset*samplingRatio) == 0)
                code.delete(0,code.length()%8);
            var dct=new int[64];
            //读取DC系数
            while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
            Point pDC;//  读取categroy
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
            LOGGER.debug(code.substring(pDC.y, pDC.x + pDC.y)+":"+dct[0]+"allStart:"+allStart);

            code.delete(0,pDC.x + pDC.y);
            while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
            //读取AC系数
            int[] pAC;//用于读取run/size
            //读取AC哈夫曼码
            while(true) {
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
                while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
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
            LOGGER.debug("--------------------------------------------------------------------------");
        }//while
    }




    /**
     * 1*64数组转二进制字符串
     */
    void setDCT(){
        var sb  = new StringBuffer();
        DCTable dcTable;
        ACTable acTable;
        ArrayList<int[]> DCT;
        int index = 0;
        //set开始
        LOGGER.debug("----------------------setDCT------------------------");
        while(index < CbDCT.size()+CrDCT.size()+yDCT.size()){
            int i;
            switch (index%6){
                case 4:
                    //Cb
                    dcTable = DCC;
                    acTable = ACC;
                    DCT = CbDCT;
                    i = index/6;
                    break;
                case 5:
                    //Cr
                    dcTable = DCC;
                    acTable = ACC;
                    DCT = CrDCT;
                    i = index/6;
                    break;
                default:
                    //Y
                    dcTable = DCL;
                    acTable = ACL;
                    DCT = yDCT;
                    i = index/6*4+index%6;
                    break;
            }//switch
            //DC
            int[] dct = DCT.get(i);
            String s = int2str0b(dct[0]);
            sb.append(dcTable.getHuffmanCode(s.length()));
            sb.append(s);
            //DC end
            //AC
            i = 1;
            int last = 1;
            while(i < 64){
                if(dct[i] != 0){
                    s = int2str0b(dct[i]);
                    sb.append(acTable.getHuffmanCode(i-last,s.length()));
                    sb.append(s);
                    last = i;
                }//if
                i++;
            }//while
            //ac系数不足63个，靠后全为0
            if(last != 63)sb.append(acTable.getEOB());
            //AC end
        }//while
        while(sb.length()%8!=0)sb.append('1');
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
                if(count == byte2int(image[i+3])+16*16*byte2int(image[i+2])-3-16) {
                    //一个DHT单独定义一个表
                    switch (image[i + 4]) {
                        case 0://00 第一DC表
                            DC_luminance.x = i + 5;
                            DC_luminance.y = i + 5 + byte2int(image[i + 3]) + 16 * 16 * byte2int(image[i + 2]) - 3;
                            break;
                        case 1://01 第二DC表
                            DC_chrominance.x = i + 5;
                            DC_chrominance.y = i + 5 + byte2int(image[i + 3]) + 16 * 16 * byte2int(image[i + 2]) - 3;
                            break;
                        case 16://10 第一AC表
                            AC_luminance.x = i + 5;
                            AC_luminance.y = i + 5 + byte2int(image[i + 3]) + 16 * 16 * byte2int(image[i + 2]) - 3;
                            break;
                        case 17://11 第二AC表
                            AC_chrominance.x = i + 5;
                            AC_chrominance.y = i + 5 + byte2int(image[i + 3]) + 16 * 16 * byte2int(image[i + 2]) - 3;
                            break;
                    }
                }else{
                    //一个DHT定义多个表
                    i += 4;
                    while (image[i] != -1){//FF
                        count = 0;
                        for(int j = 0;j < 16;j++){
                            count += image[j+i+1];
                        }
                        switch (image[i]){
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
            DCC = new DCTable(DC_C);LOGGER.debug("DCC complate");
            DCL = new DCTable(DC_L);LOGGER.debug("DCL complate");
            ACC = new ACTable(AC_C);LOGGER.debug("ACC complate");
            ACL = new ACTable(AC_L);LOGGER.debug("ACL complate");
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
    //把byte转二进制字符串
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
                for (int i = 0; i < 8 - remainder; i++)
                    in.append("0");
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
     * @return 去差分后数组
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
        byte[] temp = new byte[target.length - OutputFormat.countFF00(target)];
        int index = 0;
        for(int i = 0;i < target.length;i++){
            if(target[i] == -1&&target[i+1] != 0){
                i += 1;
                continue;
            }
            temp[index++] = target[i];
            if(target[i] == -1&&target[i+1] == 0){
                i++;
            }
        }
        target = temp;
    }

    public static int byte2int(byte b){
        if(b < 0)return b+256;
        else return b;
    }
}
