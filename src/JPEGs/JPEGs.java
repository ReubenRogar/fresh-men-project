package JPEGs;

import javax.imageio.stream.FileImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 整体用到的方法是先将图片数据转为16进制保存，再在16进制数据中定位到SOF0数据段位置，对SOF0中的数据进行拆解划分
 * 程序有些地方可能会有一些问题，或者函数的安排可能混乱一点，微调一下
 */

public class JPEGs {
    /**
     * 将图片文件转换成16进制数据
     */
        //图片到byte数组
        public byte[] image2byte(String path){
            byte[] data = null;
            FileImageInputStream input = null;
            try {
                input = new FileImageInputStream(new File(path));
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int numBytesRead = 0;
                while ((numBytesRead = input.read(buf)) != -1) {
                    output.write(buf, 0, numBytesRead);
                }
                data = output.toByteArray();
                output.close();
                input.close();
            }
            catch (FileNotFoundException ex1) {
                ex1.printStackTrace();
            }
            catch (IOException ex1) {
                ex1.printStackTrace();
            }
            return data;
        }
        public String byte2string(byte[] data){
            if(data==null||data.length<=1) return "0x";
            if(data.length>200000) return "0x";
            StringBuffer sb = new StringBuffer();
            int buf[] = new int[data.length];
            //byte数组转化成十进制
            for(int k=0;k<data.length;k++){
                buf[k] = data[k]<0?(data[k]+256):(data[k]);
            }
            //十进制转化成十六进制
            for(int k=0;k<buf.length;k++){
                if(buf[k]<16) sb.append("0"+Integer.toHexString(buf[k]));
                else sb.append(Integer.toHexString(buf[k]));
            }
            return "0x"+sb.toString().toUpperCase();
        }


    /**
     * 将图片文件转换成16进制数据结束
     */



    public static SOF0 translate(String Jhin){
        SOF0 s = new SOF0();
        int offset = 0;
        for(int i=0;i<Jhin.length();i++){
            if(Jhin.charAt(i)=='f'&&Jhin.charAt(i)=='f'&&Jhin.charAt(i)=='c'&&Jhin.charAt(i)=='0')
                offset = i;
            break;
        }
        offset += 4;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.SOF0length = 16*16*16*(Jhin.charAt(offset)-87);
        else s.SOF0length = 16*16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.SOF0length += 16*16* (Jhin.charAt(offset)-87);
        else s.SOF0length += 16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.SOF0length += 16*(Jhin.charAt(offset)-87);
        else s.SOF0length += 16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.SOF0length += Jhin.charAt(offset)-87;
        else s.SOF0length += Jhin.charAt(offset)-48;//SOF0长度

        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.numColor = 16*(Jhin.charAt(offset)-87);
        else s.numColor = 16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.numColor += Jhin.charAt(offset)-87;
        else s.numColor += Jhin.charAt(offset)-48;//颜色分量

        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.hight = 16*16*16*(Jhin.charAt(offset)-87);
        else s.hight = 16*16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.hight += 16*16*(Jhin.charAt(offset)-87);
        else s.hight += 16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.hight += 16*(Jhin.charAt(offset)-87);
        else s.hight += 16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.hight += Jhin.charAt(offset)-87;
        else s.hight += Jhin.charAt(offset)-48;//得到高度

        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.width = 16*16*16*(Jhin.charAt(offset)-87);
        else s.width = 16*16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.width += 16*16*(Jhin.charAt(offset)-87);
        else s.width += 16*16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.width += 16*(Jhin.charAt(offset)-87);
        else s.width += 16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.width += Jhin.charAt(offset)-87;
        else s.width += Jhin.charAt(offset)-48;//得到宽度

        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.numComponents = 16*(Jhin.charAt(offset)-87);
        else s.numComponents = 16*(Jhin.charAt(offset)-48);
        offset ++;
        if(Jhin.charAt(offset)>='a'&&Jhin.charAt(offset)<='z') s.numComponents += Jhin.charAt(offset)-87;
        else s.numComponents += Jhin.charAt(offset)-48;

        for (int i = 0; i < 3; i++) {
            offset += 2;
            if(Jhin.charAt(offset) == '1'){
                offset += 2;
                 s.ySample = Jhin.charAt(offset)-48;//y的采样系数
                offset += 2;
                s.yTable = Jhin.charAt(offset)-48;//得到y表号
            }
            if(Jhin.charAt(offset) == '2'){
                offset += 2;
                s.cbSample = Jhin.charAt(offset)-48;//cb的采样系数
                offset += 2;
                s.cbTable = Jhin.charAt(offset)-48;//得到cb表号
            }
            if(Jhin.charAt(offset) == '3'){
                offset += 2;
                s.crSample = Jhin.charAt(offset)-48;//cr的采样系数
                offset += 2;
                s.crTable = Jhin.charAt(offset)-48;//得到cr表号
            }
        }

        return s;
        /**
         * 最后得到的ySample与cbSample与crSample三者相比得到的比例即为采样模式的最简比
         */

    }

}

