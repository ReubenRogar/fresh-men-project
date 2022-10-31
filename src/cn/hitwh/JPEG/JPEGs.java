package cn.hitwh.JPEG;


import cn.hitwh.Encrypt.KeyXU;
import cn.hitwh.Encrypt.NewTypeEncrypt;
import com.google.common.primitives.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static cn.hitwh.JPEG.ImageToCode.imageToByte;

public class JPEGs {
    //ͼƬ����
    private final String path;
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

    private int resetInterval = 0;//FF DD�ζ����ɨ���и�λ���

    public String getPath() {
        return path;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getSamplingRatio() {
        return samplingRatio;
    }

    //Logback���
    public static final Logger LOGGER = LoggerFactory.getLogger("JPEGs.class");

    /**
     * ��������ȡͼƬ��huffman���DCT����
     */
    public JPEGs(String inFile) throws JPEGWrongStructureException{
        path = inFile;
        image = imageToByte(path);
        ImageToCode.dataToFile(ImageToCode.byteToString(image), inFile + ".txt");
        LOGGER.debug(path);
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
                        resetInterval = 0;
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
                        resetInterval = (image[index] & 0xFF) * 16 * 16 + (image[index + 1] & 0xFF);
                        LOGGER.debug(image[index] + " " + image[index + 1]);
                        LOGGER.debug("DDReset:" + resetInterval);
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
        getTargetWithff00();
        try {
            getDCT();
        }catch (ArrayIndexOutOfBoundsException e){
            throw new JPEGWrongStructureException("��֧������JPEG");
        }
    }

    /**
     * ��ȡ������dctϵ������ʾ���̽��
     */
    public  void encryptDCT(int iterations) throws NoSuchAlgorithmException {
        //Y
        NewTypeEncrypt nteY = new NewTypeEncrypt(yDCT, new KeyXU(0.6, 3.9), resetInterval *samplingRatio,DCL.getMax());
        //Cb
        NewTypeEncrypt nteCb = new NewTypeEncrypt(CbDCT, new KeyXU(0.6, 3.9), resetInterval,DCC.getMax());
        //Cr
        NewTypeEncrypt nteCr = new NewTypeEncrypt(CrDCT, new KeyXU(0.6, 3.9), resetInterval,DCC.getMax());
        //����
        nteY.DCGroupScramble();
        nteCr.DCGroupScramble();
        nteCb.DCGroupScramble();
        nteY.DCIterativeScramble(iterations);
        nteCb.DCIterativeScramble(iterations);
        nteCr.DCIterativeScramble(iterations);
        nteY.ACRunGroupScramble();
        nteCb.ACRunGroupScramble();
        nteCr.ACRunGroupScramble();
    }

    /**
     * ��ȡ������dctϵ������ʾ���̽��
     */
    public  void decodeDCT(int it) throws NoSuchAlgorithmException {
        //Y
        NewTypeEncrypt nteY = new NewTypeEncrypt(yDCT, new KeyXU(0.6, 3.9), resetInterval *samplingRatio,DCL.getMax());
        //Cb
        NewTypeEncrypt nteCb = new NewTypeEncrypt(CbDCT, new KeyXU(0.6, 3.9), resetInterval,DCC.getMax());
        //Cr
        NewTypeEncrypt nteCr = new NewTypeEncrypt(CrDCT, new KeyXU(0.6, 3.9), resetInterval,DCC.getMax());

        nteY.ACRunGroupDecode();
        nteCb.ACRunGroupDecode();
        nteCr.ACRunGroupDecode();
        nteY.DCIterativeDecode(it);
        nteCb.DCIterativeDecode(it);
        nteCr.DCIterativeDecode(it);
        nteY.DCGroupDecode();
        nteCr.DCGroupDecode();
        nteCb.DCGroupDecode();
    }

    /**
     * JPEG���ݷŻ�
     */
    public void setImage(String outFile){
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
        ImageToCode.outputImage(outFile,image);

    }

    /**
     * ���DCT����
     * @param DCT �洢DCT���ݵ�list
     * @return ��һ��1��DCTΪ��ʽ���
     */
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
     * ��ȡdct����
     */
    public void getDCT() throws ArrayIndexOutOfBoundsException{
        StringBuilder code = new StringBuilder();
        int bytes = 0;//ѹ������byte�����������
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
            if(resetInterval != 0 && yDCT.size()%(resetInterval *samplingRatio) == 0 && yDCT.size() == samplingRatio*CrDCT.size()) {
//                LOGGER.debug(code.substring(0,code.length()%8));
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
            if (pDC.x == 0)
                dct[0] = 0;
            else dct[0] = str0b2int(code.substring(pDC.y, pDC.x + pDC.y));//byteתint(DC)
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
        }//while
    }




    /**
     * 1*64����ת�������ַ���
     */
    void setDCT(){
        ArrayList<Byte> bytes = new ArrayList<>();
        StringBuilder sb  = new StringBuilder();
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
            if(resetInterval != 0 && index != 0 && index%((samplingRatio+2)* resetInterval) == 0){
                while (sb.length()%8 != 0)sb.append('1');
                while(sb.length() > 0){
                    bytes.add((byte) Integer.parseInt(sb.substring(0,8), 2));
                    sb.delete(0,8);
                    //FF
                    if(bytes.get(bytes.size()-1) == -1){
                        bytes.add((byte)0);
                    }
                }
                bytes.add((byte)-1);bytes.add((byte)((index/((samplingRatio+2)* resetInterval)-1)%8-48));
//                LOGGER.debug("FF D"+((index/((samplingRatio+2)*DDReset)-1)%8));
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
        LOGGER.debug("--------------------------------------------------------------------------");
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
            DCC.outputDCTable("�������ĵ�/DCC.txt");
            DCL.outputDCTable("�������ĵ�/DCL.txt");
            ACL.outputACTable("�������ĵ�/ACL.txt");
            ACC.outputACTable("�������ĵ�/ACC.txt");
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

}
