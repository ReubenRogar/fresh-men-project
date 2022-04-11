package 加密;

import java.io.*;

public class DCTable {
    /**
     * 直流Huffman表
     */

        private final int[] codeWordLength;
        private final int[] category;
        private final String[] codeWord;
        private final String fileName;

        public DCTable(String fileName) {
            category = new int[12];
            codeWord = new String[12];
            codeWordLength = new int[12];
            this.fileName = fileName;
            init();
        }
        //取表数据
        private void init() {
            File file = new File(fileName);
            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader br = new BufferedReader(fileReader);
                    String lineContent = null;
                    int index = 0;
                    while ((lineContent = br.readLine()) != null) {
                        String[] ss = lineContent.split("\\s\\s");
                        category[index] = Integer.parseInt(ss[0]);
                        codeWord[index] = ss[1];
                        codeWordLength[index++] = Integer.parseInt(ss[2]);
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
        //得到DC类别（长度）
        public int[] getCategory(String code){
            int[] categoryAndCodeWordLength = new int[2];
            int i = 0;
            for(;i < 12;i++){
                if(code.startsWith(codeWord[i])){
                    break;
                }
            }
            categoryAndCodeWordLength[0] = category[i];
            categoryAndCodeWordLength[1] = codeWordLength[i];
            return categoryAndCodeWordLength;
        }

    }
