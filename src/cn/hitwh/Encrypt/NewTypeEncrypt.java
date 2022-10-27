package cn.hitwh.Encrypt;

import cn.hitwh.JPEG.JPEGs;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class NewTypeEncrypt {
    private ArrayList<int[]> DCTs;//处理的DCT数据
    int resetInterval;//DCT重置间隔
    int max;//图片允许的DCC的位数
    final private KeyXU finalKey;//利用初始密钥和图像特征计算得到的最终密钥

    /**
     * 依据dct数据建立密钥
     * @param d dct数据
     * @param k 初始密钥
     */
    public NewTypeEncrypt(ArrayList<int[]> d, KeyXU k,int resetInterval,int max) throws NoSuchAlgorithmException {
        DCTs = d;
        this.resetInterval = resetInterval;
        this.max = max;
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
        //前半加密x
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
        //后半加密u
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
     * 把byte数组转二进制字符串
     */

    public static String bytes2Str0b(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
           sb.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }
        return sb.toString();
    }

    /**
     * 获取DCTs中第i个DC系数真值
     * @param i DC系数位置
     * @return DC系数真值
     */
    public int getValue(int i){
        int index;
        int value = 0;
        if (resetInterval != 0){
            index = i/resetInterval*resetInterval;
        }else{
            index = 0;
        }
        while (index <= i){
            value += DCTs.get(index++)[0];
        }
        return value;
    }


    /**
     * DC分组置乱
     */
    public void DCGroupScramble(){
        double[] scrambles = new double[DCTs.size()];
        scrambles[0] = finalKey.x;
        for(int i = 1;i < scrambles.length;i++){
           scrambles[i] = finalKey.u * scrambles[i-1]*(1-scrambles[i-1]);//x(n+1) = u * x(n) * (1 - x(n))
        }
        int start = 0,end = 0;
        DCTs:
        while(start != DCTs.size() -1 && end != DCTs.size() -1){
            //越过0
            while(DCTs.get(start)[0] == 0){
                start++;
                if(start == DCTs.size())break DCTs;
            }
            end = start;
            //确定同号dcc数
            while(end < DCTs.size()-1 && DCTs.get(end)[0] *DCTs.get(end+1)[0] > 0 ){
                end++;
            }
            //多个同号dcc
            if(start != end){
                //置乱排序
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
     * DC分组还原
     */
    public void DCGroupDecode(){
        SortFactor[] scrambles = new SortFactor[DCTs.size()];
        scrambles[0] = new SortFactor(0,finalKey.x);
        for (int i = 1;i < scrambles.length;i++){
            scrambles[i] = new SortFactor(i,finalKey.u * scrambles[i-1].TAG*(1 - scrambles[i-1].TAG));
        }
        int start = 0,end = 0;
        DCTs:
        while(start != DCTs.size() -1 && end != DCTs.size() -1){
            //越过0
            while(DCTs.get(start)[0] == 0){
                start++;
                if(start == DCTs.size())break DCTs;
            }
            end = start;
            //确定同号dcc数
            while(end < DCTs.size()-1 && DCTs.get(end)[0] *DCTs.get(end+1)[0] > 0 ){
                end++;
            }
            //多个同号dcc
            if(start != end){
                //置乱排序
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
     * DCC迭代置乱
     * @param iterations 迭代次数
     */
    public void DCIterativeScramble(int iterations) {
        for (int group = 1; group <= iterations; group++) {
            //置乱序列初始化
            double[] scrambles = new double[(int) Math.ceil((double) DCTs.size() / (2 * group))];
            scrambles[0] = finalKey.x;
            for (int i = 1; i < scrambles.length; i++) {
                scrambles[i] = finalKey.u * scrambles[i - 1] * (1 - scrambles[i - 1]);//x(n+1) = u * x(n) * (1 - x(n))
            }
            Arrays.sort(scrambles);
            //处理分组
            int lastDC = 0;//记录到start1 - 1的真值
            int preValue;//记录目前某位上的值
            int srcValue;//记录假如置乱后某位上的真值
            SCR:
            for (int i = 0; i <= scrambles.length - 2; i++) {
                //设置lastDC
                if (i > 0) {
                    for (int x = (i - 1) * 2 * group; x < i * 2 * group; x++) {
                        if (resetInterval != 0 && x % resetInterval == 0) {
                            lastDC = DCTs.get(x)[0];
                        } else {
                            lastDC += DCTs.get(x)[0];
                        }
                    }//end for
                }
                String s = String.valueOf(scrambles[i]);
                int c = Integer.parseInt(s.substring(s.length() - 1));
                //以置乱比特流确定是否置乱，奇数表‘1’则置乱
                if (c % 2 != 0) {
                    int start1 = i * 2 * group;
                    int end1 = start1 + group - 1;
                    int start2 = end1 + 1;
                    int end2 = start2 + group - 1;
                    preValue = lastDC;
                    srcValue = lastDC;
                    for (int index = start1; index <= end2; index++) {
                        int real = index > end1 ? (index - group) : index + group;
                        if (resetInterval != 0 && index % resetInterval == 0) {
                            preValue = DCTs.get(index)[0];
                            srcValue = DCTs.get(real)[0];
                        } else {
                            preValue += DCTs.get(index)[0];
                            srcValue += DCTs.get(real)[0];
                        }
                        if (Math.abs(preValue) >= 1 << max || Math.abs(srcValue) >= 1 << max) {
                            //无法转置
                            continue SCR;
                        }//end if
                    }//end for
                    //进行置乱
                    for (int index = start1; index <= end1; index++) {
                        int temp = DCTs.get(index)[0];
                        DCTs.get(index)[0] = DCTs.get(index + group)[0];
                        DCTs.get(index + group)[0] = temp;
                    }
                }//end if
            }//end for
        }//end for
    }

    /**
     * DCC迭代解密
     * @param iterations 迭代次数
     */
    public void DCIterativeDecode(int iterations){
        for(int group = iterations; group >= 1;group--){
            //置乱序列初始化
            double[] scrambles = new double[(int) Math.ceil((double) DCTs.size() / (2 * group))];
            scrambles[0] = finalKey.x;
            for (int i = 1; i < scrambles.length; i++) {
                scrambles[i] = finalKey.u * scrambles[i - 1] * (1 - scrambles[i - 1]);//x(n+1) = u * x(n) * (1 - x(n))
            }
            Arrays.sort(scrambles);
            //处理分组
            int lastDC;//记录到start1 - 1的真值
            int preValue;//记录目前某位上的值
            int srcValue;//记录假如置乱后某位上的真值
            int lastDCSite = (scrambles.length - 2)*2*group - 1;
            lastDC = getValue(lastDCSite);
            RES:
            for(int i = scrambles.length - 2;i >= 0;i--) {
                //设置lastDC
                if(i < scrambles.length - 2) {
                    for (int x = (i + 1) * 2 * group - 1; x >= i * 2 * group; x--) {
                        if (resetInterval != 0 && x % resetInterval == 0) {
                            lastDCSite = i * 2 * group - 1;
                            lastDC = getValue(lastDCSite);
                            break;
                        } else {
                            lastDC -= DCTs.get(x)[0];
                        }
                    }//end for
                }
                String s = String.valueOf(scrambles[i]);
                int c = Integer.parseInt(s.substring(s.length() - 1));
                //以置乱比特流确定是否还原，奇数‘1’则还原
                if (c % 2 != 0) {
                    int start1 = i * 2 * group;
                    int end1 = start1 + group - 1;
                    int start2 = end1 + 1;
                    int end2 = start2 + group - 1;
                    preValue = lastDC;
                    srcValue = lastDC;
                    for (int index = start1; index <= end2; index++) {
                        int real = index > end1 ? index - group : index + group;
                        if (resetInterval != 0 && index % resetInterval == 0) {
                            preValue = DCTs.get(index)[0];
                            srcValue = DCTs.get(real)[0];
                        } else {
                            preValue += DCTs.get(index)[0];
                            srcValue += DCTs.get(real)[0];
                        }
                        //超过huf表示范围
                        if ( Math.abs(preValue) >= 1 << max||Math.abs(srcValue) >= 1 <<max) {
                            continue RES;
                        }//end if
                    }//end for
                    //进行还原
                    for (int index = start1; index <= end1; index++) {
                        int temp = DCTs.get(index)[0];
                        DCTs.get(index)[0] = DCTs.get(index + group)[0];
                        DCTs.get(index + group)[0] = temp;
                    }
                }//end if
            }//end Res for
        }//end for
    }

    /**
     * 相同游程ACC全局加密
     */
    public void ACRunGroupScramble(){
        ArrayList<SortFactor> runZero = new ArrayList<>();
        ArrayList<SortFactor> runOne = new ArrayList<>();
        ArrayList<Point> runZeroSite = new ArrayList<>();
        ArrayList<Point> runOneSite = new ArrayList<>();
        //获取游程为1或0非0AC系数的信息
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
     * 相同游程ACC全局解密
     */
        public void ACRunGroupDecode(){
            ArrayList<SortFactor> runZero = new ArrayList<>();
            ArrayList<SortFactor> runOne = new ArrayList<>();
            ArrayList<Point> runZeroSite = new ArrayList<>();
            ArrayList<Point> runOneSite = new ArrayList<>();
            //获取游程为1或0非0AC系数的信息
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
 * 存储非0AC系数的信息
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
