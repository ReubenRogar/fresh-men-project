package 加密;

public class DealWithImage {
    public static byte[] xorCode(byte[] code){
        double u = 3.79,x = 0.88;
        for(int i = 0 ;i < code.length-2;i++){
            if(code[i] == -1&&code[i+1] == -38){
                i+=14;
                    while (i<code.length-2) {
                        if(code[i] != -1){
                            code[i] = (byte) ((int) code[i] ^ (int) (x * 127));
                            x = u * x*(1 - x);
                            i++;
                        }
                    }

            }
        }
        return code;
    }
    public static void main(String[] args) {
        /*
        byte[] a = {127,68,0,1,2,3,4,5,6,127};
        for (byte b : a) {
            System.out.print(b+" ");
        }
        System.out.println();
        for (byte b : xorCode(a)) {
            System.out.print(b+" ");
        }
    }*/
    }
}
