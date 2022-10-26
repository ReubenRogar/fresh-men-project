package cn.hitwh.Encrypt;

import cn.hitwh.JPEG.JPEGs;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;
    final private KeyXU finalKey;
    public static ArrayList<String> log = new ArrayList<>();

    /**
     * ����dct���ݽ�����Կ
     * @param d dct����
     * @param k ��ʼ��Կ
     */
    public NewTypeEncrypt(ArrayList<int[]> d, KeyXU k) throws NoSuchAlgorithmException {
        DCTs = d;

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
        finalKey = new KeyXU(  Double.parseDouble(""+ k.x+addXKey),Double.parseDouble(""+ k.u+addUKey));
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
     * DC��������
     */
    public void DCGroupScramble(){
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
     * DC���黹ԭ
     */
    public void DCGroupDecode(){
        SortFactor[] scrambles = new SortFactor[DCTs.size()];
        scrambles[0] = new SortFactor(0,finalKey.x);
        for (int i = 1;i < scrambles.length;i++){
            scrambles[i] = new SortFactor(i,finalKey.u * scrambles[i-1].TAG*(1 - scrambles[i-1].TAG));
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
                        if(scrambles[j].TAG < scrambles[min].TAG){
                            min = j;
                        }
                    }
                    SortFactor temp1 = scrambles[i];
                    scrambles[i] = scrambles[min];
                    scrambles[min] = temp1;
                }//end for
            }//end if
            start = end + 1;
        }//end while
        for(int i = 0;i < DCTs.size();i++){
            scrambles[i].value = DCTs.get(i)[0];
        }
        Arrays.sort(scrambles,(o1,o2) -> o1.id > o2.id ? 1:-1);
        for(int i = 0;i < DCTs.size();i++){
              DCTs.get(i)[0]=scrambles[i].value;
        }
    }

    /**
     * DCC��������
     * @param max ͼƬ�����DCC��λ��
     * @param iterations ��������
     * @param resetInterval DCT���ü��
     */
    public int DCIterativeScramble(int iterations, int resetInterval, int max) {
        int count = 0;
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
            int value;//��¼�������Һ�ĳλ�ϵ���ֵ
            SCR:
            for (int i = 0; i <= scrambles.length - 2; i++) {
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
                    log.add("Group:"+group+"  ["+start1+"]:"+DCTs.get(start1)[0]+"   ["+start2+"]:"+DCTs.get(start2)[0]);
                    count++;
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
        return count;
    }

    /**
     * DCC��������
     * @param max ͼƬ�����DCC��λ��
     * @param iterations ��������
     * @param resetInterval DCT���ü��
     */
    public int DCIterativeDecode(int iterations, int resetInterval, int max){
        int count = 0;
        for(int group = iterations; group >= 1;group--){
            //�������г�ʼ��
            double[] scrambles = new double[(int) Math.ceil((double) DCTs.size() / (2 * group))];
            scrambles[0] = finalKey.x;
            for (int i = 1; i < scrambles.length; i++) {
                scrambles[i] = finalKey.u * scrambles[i - 1] * (1 - scrambles[i - 1]);//x(n+1) = u * x(n) * (1 - x(n))
            }
            Arrays.sort(scrambles);
            //�������
            int lastDC = 0;//��¼��start1 - 1����ֵ
            int value;//��¼�������Һ�ĳλ�ϵ���ֵ
            int lastDCSite = (scrambles.length - 2)*2*group - 1;
            int countStart;
            if(resetInterval == 0){
                countStart = 0;
            }else{
                countStart = lastDCSite / resetInterval *resetInterval;
            }
            while (countStart <= lastDCSite){
                lastDC +=DCTs.get(countStart++)[0];
            }
            RES:
            for(int i = scrambles.length - 2;i >= 0;i--) {
                String s = String.valueOf(scrambles[i]);
                int c = Integer.parseInt(s.substring(s.length() - 1));
                //�����ұ�����ȷ���Ƿ�ԭ��������1����ԭ
                if (c % 2 != 0) {
                    int start1 = i * 2 * group;
                    int end1 = start1 + group - 1;
                    int start2 = end1 + 1;
                    int end2 = start2 + group - 1;
                    value = lastDC;
                    for (int index = start1; index <= end2; index++) {
                        int real = index > end1 ? index - group : index + group;
                        if (resetInterval != 0 && index % resetInterval == 0) {
                            value = DCTs.get(real)[0];
                        } else {
                            value += DCTs.get(real)[0];
                        }
                        //����huf��ʾ��Χ
                        if (Math.abs(value) >= 1 << max) {
                            //����lastDC
                            if(resetInterval == 0)
                            for (int x = i * 2 * group - 1; x >= (i - 1) * 2 * group; x--) {
                                if (resetInterval != 0 && x % resetInterval == 0) {
                                    if(lastDC != DCTs.get(x)[0])throw new RuntimeException("��RSTͼƬlastDC�������");
                                    lastDCSite = (i - 1) * 2 * group - 1;
                                    lastDC = 0;
                                    countStart = lastDCSite / resetInterval * resetInterval;
                                    while (countStart <= lastDCSite) {
                                        lastDC += DCTs.get(countStart++)[0];
                                    }
                                    continue RES;
                                } else {
                                    lastDC -= DCTs.get(x)[0];
                                }
                            }//end for
                            continue RES;
                        }//end if
                    }//end for
                    //���л�ԭ
                    count++;
                    String l = "Group:"+group+"  ["+start1+"]:"+DCTs.get(start2)[0]+"   ["+start2+"]:"+DCTs.get(start1)[0];
                    if(!log.isEmpty() && l.equals(log.get(log.size()-1))){
                        log.remove(log.size()-1);
                    }else{
                        JPEGs.LOGGER.debug("------------------------------------");
                        JPEGs.LOGGER.debug("No."+count);
                        while(!log.isEmpty() && l.equals(log.get(log.size()-1))){
                            JPEGs.LOGGER.debug(log.get(log.size()-1));
                            log.remove(log.size()-1);
                        }
                    }
                    for (int index = start1; index <= end1; index++) {
                        int temp = DCTs.get(index)[0];
                        DCTs.get(index)[0] = DCTs.get(index + group)[0];
                        DCTs.get(index + group)[0] = temp;
                    }
                }//end if
                if(i > 0)
                for (int x = i * 2 * group - 1; x >= (i - 1) * 2 * group; x--) {
                    if (resetInterval != 0 && x % resetInterval == 0) {
                        lastDCSite = (i - 1) * 2 * group - 1;
                        lastDC = 0;
                        countStart = lastDCSite / resetInterval * resetInterval;
                        while (countStart <= lastDCSite) {
                            lastDC += DCTs.get(countStart++)[0];
                        }
                        break;
                    } else {
                        lastDC -= DCTs.get(x)[0];
                    }
                }//end for
            }//end Res for
        }//end for
        return count;
    }

    /**
     * ��ͬ�γ�ACCȫ�ּ���
     */
    public void ACRunGroupScramble(){
        ArrayList<SortFactor> runZero = new ArrayList<>();
        ArrayList<SortFactor> runOne = new ArrayList<>();
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
                            runZero.add(new SortFactor(dct[i]));
                            runZeroSite.add(new Point(index,i));
                            break;
                        case 1:
                            runOne.add(new SortFactor(dct[i]));
                            runOneSite.add(new Point(index,i));
                            break;
                    }//end switch
                    last = i;
                }//end if
            }//end for
        }//end for
        runZero.get(0).TAG = finalKey.x;
        runOne.get(0).TAG = finalKey.x;
        for(int i = 1;i < runZero.size();i++){
            runZero.get(i).TAG = finalKey.u * runZero.get(i-1).TAG * (1 - runZero.get(i-1).TAG);//x(n+1) = u * x(n) * (1 - x(n))
        }
        for(int i = 1;i < runOne.size();i++){
            runOne.get(i).TAG = finalKey.u * runOne.get(i-1).TAG * (1 - runOne.get(i-1).TAG);//x(n+1) = u * x(n) * (1 - x(n))
        }
        runZero.sort((o1, o2) -> o1.TAG > o2.TAG ? 1 : -1);
        runOne.sort((o1, o2) -> o1.TAG > o2.TAG ? 1 : -1);
        for(int i = 0;i < runZeroSite.size();i++){
            DCTs.get(runZeroSite.get(i).x)[runZeroSite.get(i).y] = runZero.get(i).value;
        }
        for(int i = 0;i < runOneSite.size();i++){
            DCTs.get(runOneSite.get(i).x)[runOneSite.get(i).y] = runOne.get(i).value;
        }
    }

    /**
     * ��ͬ�γ�ACCȫ�ֽ���
     */
        public void ACRunGroupDecode(){
            ArrayList<SortFactor> runZero = new ArrayList<>();
            ArrayList<SortFactor> runOne = new ArrayList<>();
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
                                runZero.add(new SortFactor());
                                runZeroSite.add(new Point(index,i));
                                break;
                            case 1:
                                runOne.add(new SortFactor());
                                runOneSite.add(new Point(index,i));
                                break;
                        }//end switch
                        last = i;
                    }//end if
                }//end for
            }//end for
            runZero.get(0).TAG = finalKey.x;
            runOne.get(0).TAG = finalKey.x;
            for(int i = 1;i < runZero.size();i++){
                runZero.get(i).TAG = finalKey.u * runZero.get(i-1).TAG * (1 - runZero.get(i-1).TAG);//x(n+1) = u * x(n) * (1 - x(n))
                runZero.get(i).id = i;
            }
            for(int i = 1;i < runOne.size();i++){
                runOne.get(i).TAG = finalKey.u * runOne.get(i-1).TAG * (1 - runOne.get(i-1).TAG);//x(n+1) = u * x(n) * (1 - x(n))
                runOne.get(i).id = i;
            }
            runZero.sort((o1, o2) -> o1.TAG > o2.TAG ? 1 : -1);
            runOne.sort((o1, o2) -> o1.TAG > o2.TAG ? 1 : -1);
            for(int i = 0;i < runZeroSite.size();i++){
                runZero.get(i).value = DCTs.get(runZeroSite.get(i).x)[runZeroSite.get(i).y]  ;
            }
            for(int i = 0;i < runOneSite.size();i++){
                runOne.get(i).value = DCTs.get(runOneSite.get(i).x)[runOneSite.get(i).y]  ;
            }
            runZero.sort((o1,o2) -> o1.id > o2.id ? 1 : -1);
            runOne.sort((o1,o2) -> o1.id > o2.id ? 1 : -1);
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
class SortFactor {
    public double TAG;
    public int value;
    public int id;

    public SortFactor(int value){
        this.TAG = 0;
        this.value = value;
        this.id = 0;
    }

    public SortFactor(){
        this.TAG = 0;
        this.value = 0;
        this.id = 0;
    }

    public SortFactor(int id,double TAG){
        this.id = id;
        this.TAG = TAG;
        this.value = Integer.MAX_VALUE;
    }
}
