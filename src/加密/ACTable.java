package 加密;

import java.awt.*;
import java.io.*;
import java.util.Vector;

public class ACTable {
    /**
     * 交流Huffman表
     */
        public final Vector<Point> runSize;
        public final Vector<String> codeWord;
        public final String fileName;

        public ACTable(String fileName) {
            runSize = new Vector<Point>();
            codeWord = new Vector<String>();
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
                    while ((lineContent = br.readLine()) != null) {
                        String[] ss = lineContent.split("\\s\\s");
                        runSize.addElement(handleRS(ss[0]));
                        codeWord.addElement(ss[1]);
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
