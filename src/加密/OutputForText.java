package 加密;

import java.util.ArrayList;

public class OutputForText {
    /**
     * 8个字节一段输出字符串（后不足8位）
     */
    public static void outputStr8(String s){
        int i =0;
        for(;i <s.length()/8;i++){
            System.out.print(s.substring(i*8,i*8+8)+" ");
        }
        System.out.println(s.substring(i*8));
    }

    /**
     *8个字节一段输出字符串（前不足8位)
     */
    public static void output8Str(String s){
        int start = s.length()%8;
        System.out.print(s.substring(0,start)+" ");
        s = s.substring(start);
        outputStr8(s);
    }

    /**
     * 输出二维数组信息
     * @param arr
     */
    public static void outputArr(ArrayList<int[]> arr){
        for (int[] ints : arr) {
            System.out.print("{");
            for (int anInt : ints) {
                System.out.print(anInt+",");
            }
            System.out.println("}");
        }
    }
}
