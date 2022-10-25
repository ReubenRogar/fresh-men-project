package cn.hitwh.Encrypt;

import cn.hitwh.JPEG.JPEGs;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;
    private KeyXU originKey;
    private KeyXU finalKey;

    /**
     * ����dct���ݽ�����Կ
     * @param d dct����
     * @param k ��ʼ��Կ
     * @throws NoSuchAlgorithmException
     */
    public NewTypeEncrypt(ArrayList<int[]> d, KeyXU k) throws NoSuchAlgorithmException {
        DCTs = d;
        originKey = k;

        int[] mount = new int[64];
        int[] dct;
        for (int i = 0;i < DCTs.size();i++){
            dct = DCTs.get(i);
            int num = 0;
            for (int r = 1;r < dct.length;r++) {
                if(dct[r]!= 0)num++;
            }
            mount[num]++;
        }
        String feature = "";
        for(int i = 0;i < 64;i++) {
            feature += "" + i + ""+mount[i];
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] encodedHash = digest.digest(feature.getBytes(StandardCharsets.UTF_8));
        JPEGs.LOGGER.debug("size:"+encodedHash.length);
        String hashFeature = bytes2Str0b(encodedHash);
        JPEGs.LOGGER.debug("length:"+hashFeature.length());
        int count;
        String addXKey = "",addUKey = "";
        //ǰ�����x
        for(int i = 0;i < 28;i++){
            count = 0;
            for(int r = 0;r < 9;r++){
                if(hashFeature.charAt(r) == '1')count++;
            }
            hashFeature = hashFeature.substring(9);
            addXKey += ""+count;
        }

        count = 0;
        for(int i = 0;i < 4;i++){
            if(hashFeature.charAt(i) == '1')count++;
        }
        addXKey += ""+count;
        hashFeature = hashFeature.substring(4);
        //������u
        count = 0;
        for(int i = 0;i < 4;i++){
            if(hashFeature.charAt(i) == '1')count++;
        }
        addUKey += ""+count;
        hashFeature = hashFeature.substring(4);
        for(int i = 0;i < 28;i++){
            count = 0;
            for(int r = 0;r < 9;r++){
                if(hashFeature.charAt(r) == '1')count++;
            }
            hashFeature = hashFeature.substring(9);
            addUKey += ""+count;
        }
        finalKey = new KeyXU(  Double.valueOf(""+ originKey.x+addXKey),Double.valueOf(""+ originKey.u+addUKey));
    }


    /**
     * ��byte����ת�������ַ���
     */

    public static String bytes2Str0b(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
           sb.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
        return sb.toString();
    }

    /**
     * ��ʾ��Կ
     * @return ��Կ
     */
    public KeyXU getFinalKey() {
        return finalKey;
    }

    /**
     * DCC��������
     */
    public void DCCGroupScramble(){
        double[] scrambles = new double[DCTs.size()];
        scrambles[0] = finalKey.x;
        for(int i = 1;i < scrambles.length;i++){
           scrambles[i] = finalKey.u * scrambles[i-1]*(1-scrambles[i-1]);//x(n+1) = u * x(n) * (1 - x(n))
        }
        int start = 0,end = 0;
        dcts:
        while(start != DCTs.size() -1 && end != DCTs.size() -1){
            //Խ��0
            while(DCTs.get(start)[0] == 0 && start < DCTs.size()){
                start++;
                if(start == DCTs.size())break dcts;
            }
            end = start;
            //ȷ��ͬ��dcc��
            while(end < DCTs.size()-1 && DCTs.get(end)[0] *DCTs.get(end+1)[0] > 0 ){
                end++;
            }
            //���ͬ��dcc
            if(start != end){
                //��������
                for(int i = start;i < end;i++){
                    int min = i;
                    for(int j = i + 1;j <= end;j++){
                        if(scrambles[j] < scrambles[min]){
                            min = j;
                        }
                    }
                    double temp1 = scrambles[i];
                    scrambles[i] = scrambles[min];
                    scrambles[min] = temp1;
                    int temp2 = DCTs.get(i)[0];
                    DCTs.get(i)[0] = DCTs.get(min)[0];
                    DCTs.get(min)[0] = temp2;
                }//end for
            }//end if
            start = end + 1;
        }//end while
    }

    /**
     * DCC��������
     * @param max ͼƬ�����DCC��λ��
     * @param iterations ��������
     * @param resetInterval DCT���ü��
     */
    public void DCCIterativeScramble(int iterations,int resetInterval,int max) {
        for (int group = 1; group <= iterations; group++) {
            //�������г�ʼ��
            double[] scrambles = new double[(int) Math.ceil((double) DCTs.size() / (2 * group))];
            scrambles[0] = finalKey.x;
            for (int i = 1; i < scrambles.length; i++) {
                scrambles[i] = finalKey.u * scrambles[i - 1] * (1 - scrambles[i - 1]);//x(n+1) = u * x(n) * (1 - x(n))
            }
            Arrays.sort(scrambles);
            //�������
            int lastDC = 0;//��¼��start1 - 1����ֵ
            int value = 0;//��¼�������Һ�ĳλ�ϵ���ֵ
            SCR:
            for (int i = 0; i < scrambles.length - 1; i++) {
                String s = String.valueOf(scrambles[i]);
                int c = Integer.parseInt(s.substring(s.length() - 1));
                //�����ұ�����ȷ���Ƿ����ң�������1��������
                if (c % 2 != 0) {
                    int start1 = i * 2 * group;
                    int end1 = start1 + group - 1;
                    int start2 = end1 + 1;
                    int end2 = start2 + group - 1;
                    value = lastDC;
                    for (int index = start1; index <= end2; index++) {
                        int real = index > end1?(index - group) : index + group;
                        if(resetInterval != 0 && index % resetInterval == 0){
                            value = DCTs.get(real)[0];
                        }else{
                            value += DCTs.get(real)[0];
                        }
                        if(Math.abs(value) >= 1<<max){
                            //����lastDC
                            for(int x = i*2*group;x < (i+1)*2*group;x++){
                                if(resetInterval != 0 && index % resetInterval == 0){
                                    lastDC = DCTs.get(x)[0];
                                }else{
                                    lastDC += DCTs.get(x)[0];
                                }
                            }//end for
                            continue SCR;
                        }//end if
                    }//end for
                    //��������
                    for(int index = start1;index <= end1;index++){
                        int temp = DCTs.get(index)[0];
                        DCTs.get(index)[0] = DCTs.get(index + group)[0];
                        DCTs.get(index + group)[0] = temp;
                    }
                }//end if
                //����lastDC
                for(int index = i*2*group;index < (i+1)*2*group;index++){
                    if(resetInterval != 0 && index % resetInterval == 0){
                        lastDC = DCTs.get(index)[0];
                    }else{
                        lastDC += DCTs.get(index)[0];
                    }
                }//end for
            }//end for
        }//end for
    }


    /**
     * ��ͬ�γ�ACCȫ�ּ���
     */
    public void ACCRunGroupScramble(){
        ArrayList<NonZeroAC> runZero = new ArrayList<>();
        ArrayList<NonZeroAC> runOne = new ArrayList<>();
        ArrayList<Point> runZeroSite = new ArrayList<>();
        ArrayList<Point> runOneSite = new ArrayList<>();
        //��ȡ�γ�Ϊ1��0��0ACϵ������Ϣ
        for(int index = 0;index < DCTs.size();index++){
            int[] dct = DCTs.get(index);
            int last = 1;
            for (int i = 1;i < 64;i++){
                if(dct[i] != 0){
                    switch (i - last){
                        case 0:
                            runZero.add(new NonZeroAC(dct[i]));
                            runZeroSite.add(new Point(index,i));
                            break;
                        case 1:
                            runOne.add(new NonZeroAC(dct[i]));
                            runOneSite.add(new Point(index,i));
                            break;
                    }//end switch
                    last = i;
                }//end if
            }//end for
        }//end for
        runZero.get(0).id = finalKey.x;
        runOne.get(0).id = finalKey.x;
        for(int i = 1;i < runZero.size();i++){
            runZero.get(i).id = finalKey.u * runZero.get(i-1).id * (1 - runZero.get(i-1).id);//x(n+1) = u * x(n) * (1 - x(n))
        }
        for(int i = 1;i < runOne.size();i++){
            runOne.get(i).id = finalKey.u * runOne.get(i-1).id * (1 - runOne.get(i-1).id);//x(n+1) = u * x(n) * (1 - x(n))
        }

//        for(int i = 0;i < runZero.size() - 1;i++){
//            int min = i;
//            for(int j = i+1;j < runZero.size();j++){
//                if(runZero.get(j).id < runZero.get(min).id)
//                    min = j;
//            }
//            double temp1 = runZero.get(min).id;
//            runZero.get(min).id = runZero.get(i).id;
//            runZero.get(i).id = temp1;
//            int temp2 = runZero.get(min).value;
//            runZero.get(min).value = runZero.get(i).value;
//            runZero.get(i).value = temp2;
//        }//end if
        runZero.sort(new Comparator<NonZeroAC>() {
            @Override
            public int compare(NonZeroAC o1, NonZeroAC o2) {
                return o1.id > o2.id? 1 : -1 ;
            }
        });
//        for(int i = 0;i < runOne.size() - 1;i++){
//            int min = i;
//            for(int j = i+1;j < runOne.size();j++){
//                if(runOne.get(j).id < runOne.get(min).id)
//                    min = j;
//            }
//            double temp1 = runOne.get(min).id;
//            runOne.get(min).id = runOne.get(i).id;
//            runOne.get(i).id = temp1;
//            int temp2 = runOne.get(min).value;
//            runOne.get(min).value = runOne.get(i).value;
//            runOne.get(i).value = temp2;
//        }//end if
        runOne.sort(new Comparator<NonZeroAC>() {
            @Override
            public int compare(NonZeroAC o1, NonZeroAC o2) {
                return o1.id > o2.id? 1 : -1 ;
            }
        });
        for(int i = 0;i < runZeroSite.size();i++){
            DCTs.get(runZeroSite.get(i).x)[runZeroSite.get(i).y] = runZero.get(i).value;
        }
        for(int i = 0;i < runOneSite.size();i++){
            DCTs.get(runOneSite.get(i).x)[runOneSite.get(i).y] = runOne.get(i).value;
        }
    }


}

/**
 * �洢��0ACϵ������Ϣ
 */
class NonZeroAC{
    public double id;
    public int value;

    public NonZeroAC(int value){
        this.id = 0;
        this.value = value;
    }
}
