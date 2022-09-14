package cn.hitwh;

import java.awt.*;
import java.io.*;

public class DCTable {
    /**
     * 直流Huffman表
     */

    private final int[] category;
    private final String[] codeWord;
    private final String fileName;

    public DCTable(byte[] image) {
        category = new int[12];
        codeWord = new String[12];
        fileName = "";
        byte[] length = new byte[16];
        System.arraycopy(image, 0, length, 0, length.length);
        for (int i = 16; i < 12 + 16; i++) {
            category[i - 16] = image[i] < 0 ? image[i] + 256 : image[i];
        }
        int index = 0, i = 0;
        long code = 0;
        while (index < 12) {
            while (length[i] == 0) {
                if (index != 0) code *= 2;
                i++;
            }
            for (int j = 0; j < length[i]; j++) {
                codeWord[index++] = long2str0b(code, i + 1);
                code++;
            }
            i++;
            code *= 2;
        }
    }

    public void outputDCTable(String fileName) {
        String DCTable = "";
        for (int i = 0; i < 12; i++) {
            DCTable += category[i] + "\s\s" + codeWord[i] + "\n";
        }
        ImageToCode.dataToFile(DCTable, "./测试用文档/" + fileName + ".txt");
    }

    public static String long2str0b(long code, int length) {
        String result = "";
        while (code > 0) {
            result = ((code % 2 == 1) ? "1" : "0") + result;
            code /= 2;
        }
        while (result.length() < length) {
            result = "0" + result;
        }
        return result;
    }


    public DCTable(String fileName) {
        category = new int[12];
        codeWord = new String[12];
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
                String lineContent;
                int index = 0;
                while ((lineContent = br.readLine()) != null) {
                    String[] ss = lineContent.split("\\s\\s");
                    category[index] = Byte.parseByte(ss[0]);
                    codeWord[index++] = ss[1];
                }
                br.close();
                fileReader.close();
            } catch (FileNotFoundException e) {
                JPEGs.LOGGER.error("-----------[INFORMATION] File does not exist! -----------");
                e.printStackTrace();
            } catch (IOException e) {
                JPEGs.LOGGER.error("-----------[INFORMATION] Io exception! -----------");
                e.printStackTrace();
            }
        }
    }

    //得到DC类别（长度）
    public Point getCategory(String code) {
        Point categoryAndCodeWordLength = new Point();
        int i = 0;
        for (; i < 12; i++) {
            if (code.startsWith(codeWord[i])) {
                break;
            }
        }
        if (i < 12) {
            JPEGs.LOGGER.info("DC{" + codeWord[i] + " 长度:" + category[i] + "}");
            categoryAndCodeWordLength.x = category[i];
            categoryAndCodeWordLength.y = codeWord[i].length();
            return categoryAndCodeWordLength;
        } else {
            JPEGs.LOGGER.error("无法识别的huffman码："+code);
            return new Point(0, 0);
        }
    }

    //得到DC哈夫曼码
    public String getHuffmanCode(int length) {
        int i = 0;
        for (; i < category.length; i++) {
            if (length == category[i]) break;
        }
        return codeWord[i];
    }

}