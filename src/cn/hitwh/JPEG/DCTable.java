package cn.hitwh.JPEG;

import java.awt.*;

public class DCTable {
    /**
     * 直流Huffman表
     */

    private final int[] category;
    private final String[] codeWord;

    public DCTable(byte[] image) {
        category = new int[image.length-16];
        codeWord = new String[image.length-16];
        byte[] length = new byte[16];
        System.arraycopy(image, 0, length, 0, length.length);
        for (int i = 16; i < image.length; i++) {
            category[i - 16] = image[i]&0xFF;
        }
        int index = 0, i = 0;
        long code = 0;
        while (index < image.length-16) {
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

    public int getMax(){
        return category.length-1;
    }

    public void outputDCTable(String fileName) {
        StringBuilder DCTable = new StringBuilder();
        for (int i = 0; i < category.length; i++) {
            DCTable.append(category[i] + "\s\s" + codeWord[i] + "\n");
        }
        ImageToCode.dataToFile(DCTable.toString(), "./测试用文档/" + fileName + ".txt");
    }

    public static String long2str0b(long code, int length) {
        StringBuilder result = new StringBuilder();
        while (code > 0) {
            result.insert(0,(code % 2 == 1) ? "1" : "0");
            code /= 2;
        }
        while (result.length() < length) {
            result.insert(0,"0");
        }
        return result.toString();
    }




    //得到DC类别（长度）
    public Point getCategory(StringBuilder codeS) {
        String code = codeS.toString();
        Point categoryAndCodeWordLength = new Point();
        int i = 0;
        for (; i < codeWord.length; i++) {
            if (code.startsWith(codeWord[i])) {
                break;
            }
        }
        if (i < codeWord.length) {
            categoryAndCodeWordLength.x = category[i];
            categoryAndCodeWordLength.y = codeWord[i].length();
            return categoryAndCodeWordLength;
        } else {
            return new Point(0, 0);
        }
    }

    //得到DC哈夫曼码
    public String getHuffmanCode(int length) {
        int i = 0;
        for (; i < category.length; i++) {
            if (length == category[i]) break;
        }
        if(i == category.length){
            throw new JPEGWrongStructureException("Wrong length! length:"+length);
        }
        return codeWord[i];
    }

}