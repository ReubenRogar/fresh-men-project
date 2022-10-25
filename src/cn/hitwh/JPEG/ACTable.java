package cn.hitwh.JPEG;

import java.awt.*;
import java.util.ArrayList;

public class ACTable {
    /**
     * 交流Huffman表
     */

        private final ArrayList<Point> runSize;
        private final ArrayList<String> codeWord;

        public ACTable(byte[] image){
            runSize = new ArrayList<>();
            codeWord = new ArrayList<>();
            byte[] length = new byte[16];
            System.arraycopy(image,0,length,0,length.length);
            //JPEGs.LOGGER.debug(ImageToCode.byteToString(image));
            for(int i = length.length;i < image.length;i++){
                int temp = image[i]&0xFF;
                runSize.add(new Point(temp/16,temp%16));
            }
            int i = 0;
            long codeW = 0;
            while(codeWord.size()< image.length-16){
                while(length[i] == 0){
                    if(codeWord.size()>0)codeW*=2;
                    i++;
                }
                for(int j = 0;j <length[i];j++) {
                    codeWord.add(long2str0b(codeW,i+1));
                    codeW++;
                }
                i++;
                codeW*=2;
            }
        }


    public void outputACTable(String filename){
        StringBuilder ACTable = new StringBuilder();
        for(int i = 0;i <runSize.size();i++){
            ACTable.append(runSize.get(i).x+"/"+runSize.get(i).y+"\s\s"+codeWord.get(i)+"\n");
        }
        ImageToCode.dataToFile(ACTable.toString(),"./测试用文档/"+filename+".txt");
    }
    public static String long2str0b(long code,int length){
        StringBuilder result = new StringBuilder();
        while(code > 0){
            result.insert(0,(code%2 == 1)?"1":"0");
            code /= 2;
        }
        while(result.length() < length){
            result.insert(0,"0");
        }
        return result.toString();
    }

    /**
     * 获取run/size
     * @param codeS 二进制数据
     * @return 包含三个元素的数组，第一位为run，第二位为size，第三位为识别码长度
     */
    public int[] getRunSize(StringBuffer codeS){
            String code = codeS.toString();
            int i = 0;
            for(;i<codeWord.size();i++){
                if(code.startsWith(codeWord.get(i))){
                    break;
                }
            }
            if(i<codeWord.size()){
//                JPEGs.LOGGER.info("AC{"+codeWord.get(i)+"前零数:"+runSize.get(i).x+"长度:"+runSize.get(i).y+"}");
                return new int[]{runSize.get(i).x,runSize.get(i).y,codeWord.get(i).length()};
            }else{
                JPEGs.LOGGER.error("无法识别的哈夫曼表:"+code);
                return new int[]{-1,0,0};
            }
        }

        public String getHuffmanCode(int run,int size){
            int i = 0;
            var result = new StringBuilder();
            if(run >= 16){
                for(;i < run / 16;i++){
                    result.append(getHuffmanCode(15,0));
                }
                run %= 16;
            }
            for(i = 0;i < runSize.size();i++){
                if(run == runSize.get(i).x&&size == runSize.get(i).y)break;
            }
            if(i == runSize.size()){
                JPEGs.LOGGER.debug("run:"+run+" size:"+size);
                throw new JPEGWrongStructureException("Wrong Run/Size!");
            }

            result.append(codeWord.get(i));
//            JPEGs.LOGGER.debug("AC{"+result+"前零数:"+run+"长度:"+size+"}");
            return result.toString();
        }

        public String getEOB(){
            return getHuffmanCode(0,0);
        }
}
