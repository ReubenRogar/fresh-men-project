package 加密;

import java.io.*;
import java.util.ArrayList;

public class ACTable {
    /**
     * 交流Huffman表
     */

        private final ArrayList<Integer> size;
        private final ArrayList<String> codeWord;
        private final String fileName;

        public ACTable(String fileName) {
            size = new ArrayList<>();
            codeWord = new ArrayList<>();
            this.fileName = fileName;
            init();
        }

        private int handleS(String s){
            String[] ss = s.split("/");
            int x;char a;
            a = ss[1].charAt(0);
            if(a == 'A')x = 10;
            else x = Integer.parseInt(a+"");
            return x;
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
                        size.add(handleS(ss[0]));
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

        public int getSize(String code){
            int i = 0;
            for(;i<codeWord.size();i++){
                if(code.startsWith(codeWord.get(i))){
                    break;
                }
            }
            return size.get(i);
        }

        public void outPut(){
            for (Integer integer : size) {
                System.out.println(integer);
            }
        }
}
