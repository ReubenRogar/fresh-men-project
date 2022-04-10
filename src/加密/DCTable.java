package 加密;

import java.io.*;

public class DCTable {
    /**
     * 直流Huffman表
     */
        public final int[] category;
        public final String[] codeWord;
        public final String fileName;

        public DCTable(String fileName) {
            category = new int[12];
            codeWord = new String[12];
            this.fileName = fileName;
            init();
        }

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
                        codeWord[index++] = ss[1];
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

    }
