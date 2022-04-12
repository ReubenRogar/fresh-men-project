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
            return new int[]{runSize.get(i).x,runSize.get(i).y,codeWord.get(i).length()};
        }

        public String getHuffmanCode(int run,int size){
            int i = 0;
            for(;i < runSize.size();i++){
                if(run == runSize.get(i).x&&size == runSize.get(i).y)break;
            }
            return codeWord.get(i);
        }
}
