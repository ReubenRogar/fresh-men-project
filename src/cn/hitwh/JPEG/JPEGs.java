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
    //ͼƬ����
    private final String name;
    // ֱ�����ȱ�
    private DCTable DCL;
    // ֱ��ɫ�ȱ�
    private DCTable DCC;
    // �������ȱ�
    private ACTable ACL;
    // ����ɫ�ȱ�
    private ACTable ACC;
    //DCT 1*64����
    private ArrayList<int[]> yDCT;//yDCT����
    private ArrayList<int[]> CbDCT;//CbDCT����
    private ArrayList<int[]> CrDCT;//CrDCT����
    private byte[] image;//ͼƬ��������
    private byte[] target;//ѹ������
    private int startOfSOS;//ɨ���п�ʼ
    private int endOfImage;//ͼ���β
    private int height;//ͼƬ�ĸ߶�
    private int width;//ͼƬ�Ŀ��
    private int samplingRatio;//ͼƬ�Ĳ���ģʽ

    private int DDReset = 0;//FF DD�ζ����ɨ���и�λ���


    //Logback���
    public static final Logger LOGGER = LoggerFactory.getLogger("JPEGs.class");

    /**
     * ��������ȡͼƬ��huffman���DCT����
     */
    public JPEGs(String inFile) {
        name = inFile;
        image = imageToByte(name);
        ImageToCode.dataToFile(ImageToCode.byteToString(image), inFile + ".txt");
        //FF D8
        if (image[0] != -1 || image[1] != -40)
            throw new JPEGWrongStructureException("The start of the file doesn't match JPEG");
        LOGGER.debug("get Huffman Table��");
        getHuffmanTable();
        LOGGER.debug("get Huffman Table successfully!");

        for (int index = 0; index < image.length; index++) {
            if(image[index] == -1){
            //FF C0��FF C2
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
     * ��ȡdctϵ������ʾ���̽��
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
        //Y
        NewTypeEncrypt nteY = new NewTypeEncrypt(yDCT, new KeyXU(0.6, 3.9));
        KeyXU key = nteY.getFinalKey();
        LOGGER.debug("size:"+yDCT.size());
        LOGGER.debug(key.x + " " + key.u);


        //Cb
        NewTypeEncrypt nteCb = new NewTypeEncrypt(CbDCT, new KeyXU(0.6, 3.9));
        key = nteCb.getFinalKey();
        LOGGER.debug("size:"+CbDCT.size());
        LOGGER.debug(key.x + " " + key.u);


        //Cr
        NewTypeEncrypt nteCr = new NewTypeEncrypt(CrDCT, new KeyXU(0.6, 3.9));
        key = nteCr.getFinalKey();
        LOGGER.debug("size:"+CrDCT.size());
        LOGGER.debug(key.x + " " + key.u);
        nteY.DCCGroupScramble();
        nteCr.DCCGroupScramble();
        nteCb.DCCGroupScramble();
        nteY.DCCIterativeScramble(15,DDReset*samplingRatio,DCL.getMax());
        nteCb.DCCIterativeScramble(15,DDReset,DCC.getMax());
        nteCr.DCCIterativeScramble(15,DDReset,DCC.getMax());
        nteY.ACCRunGroupScramble();
        nteCb.ACCRunGroupScramble();
        nteCr.ACCRunGroupScramble();
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
            //ѹ�����ݱ䶯
            byte[] bytes = new byte[image.length - endOfImage+startOfSOS-1 +target.length];
            System.arraycopy(image,0,bytes,0,startOfSOS);
            System.arraycopy(target,0,bytes,startOfSOS,target.length);
            System.arraycopy(image,endOfImage+1,bytes,startOfSOS + target.length,image.length-1-endOfImage);
            image = bytes;
        }
        ImageToCode.outputImage("������ͼƬ/����.jpg",image);
    }

   void debugDCT(){
       getDCT();

       //����

       setDCT();
       if(target.length == endOfImage - startOfSOS + 1)
           System.arraycopy(target,0,image,startOfSOS,target.length);
       else{
           //ѹ�����ݱ䶯
           byte[] bytes = new byte[image.length - endOfImage+startOfSOS-1 +target.length];
           System.arraycopy(image,0,bytes,0,startOfSOS);
           System.arraycopy(target,0,bytes,startOfSOS,target.length);
           System.arraycopy(image,endOfImage+1,bytes,startOfSOS + target.length,image.length-1-endOfImage);
           image = bytes;
       }
       ImageToCode.outputImage("������ͼƬ/����.jpg",image);
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
     * ��ȡDCT��
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
        int bytes = 0;//ѹ������byte�����������
        int allStart = 0;//Dcϵ����ѹ�������е�λ��
        DCTable dcTable;
        ACTable acTable;
        yDCT = new ArrayList<>();
        CrDCT = new ArrayList<>();
        CbDCT = new ArrayList<>();
        ArrayList<int[]> DCT;
        int flag = -1;//�����ֱ�־
        //��DCT��
        LOGGER.debug("----------------------getDCT------------------------");
        while(true) {
            //Ӧ��Huffman��
            flag++;
            int index = 1;
            if(samplingRatio == 0){
                throw new JPEGWrongStructureException("Unusual sampling!");
            }
            if (flag % (samplingRatio + 2) == samplingRatio) {
//                LOGGER.debug("ɫ��");
                dcTable = DCC;
                acTable = ACC;
                DCT = CbDCT;
            } else if (flag % (samplingRatio + 2) == samplingRatio + 1){
//                LOGGER.debug("ɫ��");
                dcTable = DCC;
                acTable = ACC;
                DCT = CrDCT;
            }else{
//                LOGGER.debug("����");
                dcTable = DCL;
                acTable = ACL;
                DCT = yDCT;
            }
//            LOGGER.debug("NO."+DCT.size());
            //RST���
            if(DDReset != 0 && yDCT.size()%(DDReset*samplingRatio) == 0 && yDCT.size() == samplingRatio*CrDCT.size()) {
//                LOGGER.debug(code.substring(0,code.length()%8));
                allStart+=code.length()%8;
                code.delete(0, code.length() % 8);
            }
            var dct=new int[64];
            //��ȡDCϵ��
            while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
            Point pDC;//  ��ȡcategory
            pDC = dcTable.getCategory(code);
            if(pDC.y == 0){
                LOGGER.debug("��ȡλ�ã�"+bytes+" "+"�ܳ���"+target.length);
                LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                return;
            }
            allStart += pDC.y;
            if (pDC.x == 0)
                dct[0] = 0;
            else dct[0] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byteתint(DC)
            allStart += pDC.x;
//����
//            LOGGER.debug(code.substring(pDC.y, pDC.x + pDC.y)+":"+dct[0]+"allStart:"+allStart);

            code.delete(0,pDC.x + pDC.y);

            //��ȡACϵ��
            int[] pAC;//���ڶ�ȡrun/size
            //��ȡAC��������
            while(true) {
                while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
                pAC = acTable.getRunSize(code);
                if(pAC[1] == 0){//SizeΪ0
                    if(pAC[0] == 0){// 0/0 EOB
                        code.delete(0,pAC[2]);
                        while(code.length()<32&&bytes<target.length)code.append(byte2Str0b(target[bytes++]));
                        allStart += pAC[2];
                        DCT.add(dct);
                        break;
                    }else if(pAC[0] != 15){
                        LOGGER.debug("ʣ���������");
                        LOGGER.debug(code.substring(0, Math.min(code.length(), 100)));
                        LOGGER.debug("��ȡλ�ã�"+bytes+" "+"�ܳ���"+target.length);
                        LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                        LOGGER.debug("--------------------------------------------------------------------------");
                        return;
                    }
                }
                //Run����
                index += pAC[0];
                if(index >= 64){
                    LOGGER.debug("��ȡλ�ã�"+bytes+" "+"�ܳ���"+target.length);
                    LOGGER.debug(" "+target[bytes-3]+" "+target[bytes-2]+" "+target[bytes-1]);
                }
                dct[index++] = str0b2int(code.substring(pAC[2],pAC[2]+pAC[1]));
                code.delete(0,pAC[2]+pAC[1]);
                allStart += pAC[2]+pAC[1];
                //DCT�������������
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
     * 1*64����ת�������ַ���
     */
    void setDCT(){
        var bytes = new ArrayList<Byte>();
        var sb  = new StringBuilder(8);
        DCTable dcTable;
        ACTable acTable;
        ArrayList<int[]> DCT;
        int index = 0;
        //set��ʼ
        LOGGER.debug("----------------------setDCT------------------------");
        while(index < CbDCT.size()+CrDCT.size()+yDCT.size()){
            int i;
            if (index % (samplingRatio + 2) == samplingRatio) {
//                LOGGER.debug("ɫ��");
                dcTable = DCC;
                acTable = ACC;
                DCT = CbDCT;
                i = index/(samplingRatio+2);
            } else if (index % (samplingRatio + 2) == samplingRatio + 1){
//                LOGGER.debug("ɫ��");
                dcTable = DCC;
                acTable = ACC;
                DCT = CrDCT;
                i = index/(samplingRatio+2);
            }else{
//                LOGGER.debug("����");
                dcTable = DCL;
                acTable = ACL;
                DCT = yDCT;
                i = index/(samplingRatio+2)*samplingRatio + index%(samplingRatio+2);
            }
//            LOGGER.debug("NO."+i);
            //������תbyteͬʱ���FF
            while(sb.length() >= 8){
                bytes.add((byte) Integer.parseInt(sb.substring(0,8), 2));
                sb.delete(0,8);
                //FF
                if(bytes.get(bytes.size()-1) == -1){
                   bytes.add((byte)0);
                }
            }
            //RST���
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
            //acϵ������63��������ȫΪ0
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
     * ��ȡͼƬ�е�huffman��
     */
    private void getHuffmanTable(){
        Point DC_luminance = new Point();
        Point AC_luminance = new Point();
        Point DC_chrominance = new Point();
        Point AC_chrominance = new Point();
        for(int i = 0 ;i < image.length;i++){
            //FF C4
            if(image[i] == -1 && image[i+1] == -60){
                int count = 0;//�������
                for(int j = 0;j < 16;j++){
                    count += image[j+i+5];
                }
                if(count == (image[i+3]&0xFF)+16*16*((image[i+2])&0xFF)-3-16){
                    //һ��DHT��������һ����
                    switch (image[i + 4]) {
                        case 0://00 ��һDC��
                            DC_luminance.x = i + 5;
                            DC_luminance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 1://01 �ڶ�DC��
                            DC_chrominance.x = i + 5;
                            DC_chrominance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 16://10 ��һAC��
                            AC_luminance.x = i + 5;
                            AC_luminance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                        case 17://11 �ڶ�AC��
                            AC_chrominance.x = i + 5;
                            AC_chrominance.y = i + 5 + (image[i + 3]&0xFF) + 16 * 16 * (image[i + 2]&0xFF) - 3;
                            break;
                    }
                }else{
                    //һ��DHT��������
                    i += 4;
                    if(image[i] == 0 || image[i] == 1 || image[i] == 16 || image[i] == 17) {
                        while (image[i] != -1) {//FF
                            count = 0;
                            for (int j = 0; j < 16; j++) {
                                count += image[j + i + 1];
                            }
                            switch (image[i]) {
                                case 0://00 ��һDC��
                                    DC_luminance.x = i + 1;
                                    DC_luminance.y = i + count + 17;
                                    break;
                                case 1://01 �ڶ�DC��
                                    DC_chrominance.x = i + 1;
                                    DC_chrominance.y = i + count + 17;
                                    break;
                                case 16://10 ��һAC��
                                    AC_luminance.x = i + 1;
                                    AC_luminance.y = i + count + 17;
                                    break;
                                case 17://11 �ڶ�AC��
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
     * �������ַ���תint
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
     * intת�������ַ���
     * @param s ����
     * @return ��ѭ0��ͷΪ���������ַ���
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


    //��byteת�������ַ���
    public static String byte2Str0b(byte b){
        return Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
    }


    /**
     * ȥ���
     * @param x ȥ��ֻ��ǲ��
     */
    public void changeBias(int x){
        switch (x){
            case 0:
                for(int i = 1;i <=3;i++){//ȥ���
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
                for(int i = 1;i <=3;i++){//���
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
                LOGGER.debug("��Ч��������");
                break;
        }

    }

    /**
     * �������е�FF 00ת��ΪFF
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
