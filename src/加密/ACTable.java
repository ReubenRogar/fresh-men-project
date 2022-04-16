package 加密;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class ACTable {
    /**
     * 交流Huffman表
     */

        private final ArrayList<Point> runSize;
        private final ArrayList<String> codeWord;
        private final String fileName;

        public ACTable(byte[] image){
            fileName = "";
            runSize = new ArrayList<>();
            codeWord = new ArrayList<>();
            byte[] length = new byte[16];
            System.arraycopy(image,0,length,0,length.length);
            for(int i = length.length;i < image.length;i++){
                int temp;
                if(image[i] < 0)temp = 256+image[i];
                else temp = image[i];
                runSize.add(new Point(temp/16,temp%16));
            }
            int i = 0;
            long codeW = 0;
            while(codeWord.size()<162){
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
        String ACTable = "";
        for(int i = 0;i <162;i++){
            ACTable += runSize.get(i).x+"/"+runSize.get(i).y+"\s\s"+codeWord.get(i)+"\n";
        }
        ImageToCode.dataToFile(ACTable,"./测试用文档/"+filename+".txt");
    }
    public static String long2str0b(long codew,int length){
        String result = "";
        while(codew > 0){
            result = ((codew%2 == 1)?"1":"0") +result;
            codew /= 2;
        }
        while(result.length() < length){
            result = "0" +result;
        }
        return result;
    }

        public ACTable(String fileName) {
            runSize = new ArrayList<>();
            codeWord = new ArrayList<>();
            this.fileName = fileName;
            init();
        }

    private Point handleRS(String s) {
        int x, y;
        String[] ss = s.split("/");
        if (ss[0].charAt(0) >= 'A')
            x = ss[0].charAt(0) - 'A' + 10;
        else
            x = ss[0].charAt(0) - '0';
        if (ss[1].charAt(0) >= 'A')
            y = ss[1].charAt(0) - 'A' + 10;
        else
            y = ss[1].charAt(0) - '0';
        return (new Point(x, y));
    }

        private void init() {
            File file = new File(fileName);
            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader br = new BufferedReader(fileReader);
                    String lineContent = null;
                    while ((lineContent = br.readLine()) != null) {
                        String[] ss = lineContent.split("\\s\\s");
                        runSize.add(handleRS(ss[0]));
                        codeWord.add(ss[1]);
                    }
                    br.close();
                    fileReader.close();
                } catch (FileNotFoundException e) {
                    System.out.println("-----------[INFORMATION] File does not exist! -----------");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("-----------[INFORMATION] Io exception! -----------");
                    e.printStackTrace();
                }
            }
        }

        public int[] getRunSize(String code){
            int i = 0;
            for(;i<codeWord.size();i++){
                if(code.startsWith(codeWord.get(i))){
                    break;
                }
            }
            System.out.println("AC{"+codeWord.get(i)+"前零数:"+runSize.get(i).x+"长度:"+runSize.get(i).y+"}");
            return new int[]{runSize.get(i).x,runSize.get(i).y,codeWord.get(i).length()};
        }

        public String getHuffmanCode(int run,int size){
            int i = 0;
            String result = "";
            if(run > 16){
                for(;i < run / 16;i++){
                    result += getHuffmanCode(16,0);
                }
                run %= 16;
            }
            for(i = 0;i < runSize.size();i++){
                if(run == runSize.get(i).x&&size == runSize.get(i).y)break;
            }
            if(i == 162)i = 0;
            result += codeWord.get(i);
            return result;
        }

        public String getEOB(){
            return getHuffmanCode(0,0);
        }
}
