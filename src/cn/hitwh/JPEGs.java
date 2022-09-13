package cn.hitwh;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageInputStream;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static cn.hitwh.ImageToCode.imageToByte;

public class JPEGs {
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

    //Logback框架
    public static final Logger LOGGER = LoggerFactory.getLogger("JPEGs.class");

        /**
         * 构造器获取图片的huffman表和DCT数据
         */
    public JPEGs(String inFile){
        image = imageToByte(inFile);
        //getHuffmanTable(image);
        DCC = new DCTable("./HuffmanTable/DC_chrominance.txt");
        DCL = new DCTable("./HuffmanTable/DC_luminance.txt");
        ACC = new ACTable("./HuffmanTable/AC_chrominance.txt");
        ACL = new ACTable("./HuffmanTable/AC_luminance.txt");
        LOGGER.debug("获取哈夫曼表！");
        for (start = image.length - 1; start >= 0; start--) {
            if (image[start] == -1 && image[start + 1] == -38) {
                start += 2;
                break;
            }
        }
        LOGGER.debug("start to get!");
        start += image[start] * 16 * 16 + image[start + 1];
        target = new byte[image.length - 2 - start];
        System.arraycopy(image, start, target, 0, target.length);
        getTargetWithff00();
    }

    public void simpleEn(String outFile){
        LOGGER.debug("simpleEnStart!");
        getDCTOnlyDC();
        LOGGER.debug("Y:"+yDC);
        LOGGER.debug("Cb:"+CbDC);
        LOGGER.debug("Cr:"+CrDC);

        simpleAct();

        StringBuilder sb = new StringBuilder();
        int bytes = 0;
        for(int i = 1;i <=CbDC.size()*6;i++){//加密放回
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
        }
        while(bytes < target.length)sb.append(byte2Str0b(target[bytes++]));
        while (sb.length()%8 == 0)sb.append("1");
        LOGGER.debug("Y:"+yDC);
        LOGGER.debug("Cb:"+CbDC);
        LOGGER.debug("Cr:"+CrDC);
        target = str0b2Bytes(new String(sb));
        byte[] temp = new byte[start+2+target.length];
        System.arraycopy(image,0,temp,0,start);
        temp[temp.length-1] = -39;
        temp[temp.length-2] = -1;
        System.arraycopy(target,0,temp,start,target.length);
        //outputImage(outFile,temp);
    }


    public static void main(String[] args) {
        String fileName = "1";
        JPEGs jpegs = new JPEGs("E:/test/"+fileName+ ".jpg");
        jpegs.simpleEn("E:/test/"+fileName+ ".jpg");
    }
    /**
     * 仅异或DC系数
     */
    private void simpleAct() {
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
                    if (temp.length() != key.length()) {
                        LOGGER.error("出错！");
                        return;
                    }
                    for (int j = 1; j < temp.length(); j++) {
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
     * 提取DCT块
     */
    private void getDCTOnlyDC() {
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
        LOGGER.debug("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            int index = 1;
            if (flag % 6 < 4) {
                LOGGER.debug("亮度");
                dcTable = DCL;
                acTable = ACL;
                DC = yDC;
            } else if(flag% 6 ==4){
                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CbDC;
            }else{
                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;
                DC = CrDC;
            }

            //读取DC系数
            while(code.length()<32&&bytes<target.length)code += byte2Str0b(target[bytes++]);
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);
            if(pDC.y == 0)return;
            allStart += pDC.y;
            if (pDC.x == 0)
                DC.add(new Point(0,allStart));
            else DC.add(new Point(str0b2int(code.substring(pDC.y, pDC.x + pDC.y)),allStart));//byte转int(DC)
            allStart += pDC.x;
//测试
            LOGGER.debug(code.substring(pDC.y, pDC.x + pDC.y)+":"+DC.get(DC.size()-1)+"allStart:"+allStart);

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
                        LOGGER.debug("剩余填充数据");
                        LOGGER.debug(code.substring(0, Math.min(code.length(), 100)));
                        LOGGER.debug(DC.toString());
                        LOGGER.debug("--------------------------------------------------------------------------");
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
                    LOGGER.debug("--------------------------------------------------------------------------");
                    return;
                }
            }
            LOGGER.debug("--------------------------------------------------------------------------");
        }
    }


    /**
     * 提取DCT块
     * @param code 二进制字符串
     */
    public void getDCT(String code) {
//测试
        System.out.print("全部数据:");
        OutputFormat.output8Str(code);
        int[] arr;//接收一个DCT块数据的数组
        DCTable dcTable;
        ACTable acTable;
        int flag = -1;//表区分标志
        //读DCT块
//测试
        LOGGER.debug("----------------------getDCT------------------------");
        while(true) {
            //应用Huffman表
            flag++;
            arr = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
            int index = 0;
            if (flag % 3 == 0) {
                LOGGER.debug("亮度");
                dcTable = DCL;
                acTable = ACL;

            } else {
                LOGGER.debug("色度");
                dcTable = DCC;
                acTable = ACC;


            }

            //读取DC系数
            Point pDC;//  读取categroy
            pDC = dcTable.getCategory(code);

            if (pDC.x == 0) index++;
            else arr[index++] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byte转int(DC)
//测试
            LOGGER.debug(code.substring(pDC.y, pDC.x + pDC.y)+":"+arr[index-1]);

            code = code.substring(pDC.x + pDC.y);
//测试
            LOGGER.debug("剩余数据:");
            OutputFormat.output8Str(code);
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
                        LOGGER.debug("剩余数据:");
                        OutputFormat.output8Str(code);
                        continue;
                    }else{
                        LOGGER.debug("剩余填充数据");
                        DCT.add(arr.clone());
                        OutputFormat.outputArr(DCT);
                        LOGGER.debug("--------------------------------------------------------------------------");
                        return;
                    }
                }
                //Run个零
                    index += pAC[0];
                    OutputFormat.output8Str(code.substring(0,code.length()%8+8));
                    arr[index++] = str0b2int(code.substring(pAC[2], pAC[2] + pAC[1]));
//测试
                LOGGER.debug(code.substring(pAC[2], pAC[2]+pAC[1])+":"+arr[index-1]+" index:"+(index-1));
                code = code.substring(pAC[2]+pAC[1]);
//测试
                LOGGER.debug("剩余数据:");
                OutputFormat.output8Str(code);
                //DCT块数据输入完毕
                if(index == 64){
                    break;
                }
                if(code.isEmpty()){
                    DCT.add(arr.clone());
                    OutputFormat.outputArr(DCT);
                    LOGGER.debug("--------------------------------------------------------------------------");
                    return;
                }
            }
            DCT.add(arr.clone());
            OutputFormat.outputArr(DCT);
            LOGGER.debug("--------------------------------------------------------------------------");
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
        //LOGGER.debug("------------------------setDCT-------------------------");
        int DCTs = 0,index = 1;
        for (int[] ints : DCT) {//遍历1*64数据块
            DCTable dcTable;
            ACTable acTable;
            if (DCTs % 3 == 0) {//亮度
                dcTable = DCL;
                acTable = ACL;
//测试
                //LOGGER.debug("亮度");
            }else {//色度*2
                dcTable = DCC;
                acTable = ACC;
//测试
                //LOGGER.debug("色度");
            }
                if(ints[0]!= 0){
                    temp = int2str0b(ints[0]);
                    code += dcTable.getHuffmanCode(temp.length()) + temp;
                }else{
                    temp ="00";
                    code += "00";
                }
//数据
            //LOGGER.debug("DC:"+ints[0]+" "+temp);
            //LOGGER.debug("数据:");
            OutputFormat.outputStr8(code);

                int lastNum = 0;
                for (index = 1; index < 64; index++) {
                    if (ints[index] != 0) {
                        temp = int2str0b(ints[index]);
//测试
                        //LOGGER.debug("AC:"+ints[index] +" " +acTable.getHuffmanCode(index - lastNum - 1, temp.length()));
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
            //LOGGER.debug("一个dct块输入结束：");
            //LOGGER.debug("数据:");
            //OutputFormat.outputStr8(code);

            DCTs++;
        }
        //LOGGER.debug(code.length());
    return code;
    }

    /**
     * 获取图片中的huffman表
     * @param image 图片信息
     */
    private void
    getHuffmanTable(byte[] image){
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
                        DC.get(j+1).x+=DC.get(j).x;
                    }
                }
                break;
            case 1:
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
                        DC.get(j).x-=DC.get(j-1).x;
                    }
                }
                break;
            default:
                LOGGER.debug("无效参数输入");
                break;
        }

    }

    private void getTargetWithff00(){
        byte[] temp = new byte[target.length - OutputFormat.countFF00(target)];
        int index = 0;
        for(int i = 0;i < target.length;i++){
            temp[index++] = target[i];
            if(target[i] == -1&&target[i+1] == 0){
                i++;
            }
        }
        target = temp;
    }
}
