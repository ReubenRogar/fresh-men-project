package JPEGs;

public class 测试 {
    public static void main(String[] args)
    {
        String s ="D://4.jpg";
        JPEGs jpeGs = new JPEGs();
        System.out.println(jpeGs.byte2string(jpeGs.image2byte(s)));
    }
}
