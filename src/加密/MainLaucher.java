package 加密;

public class MainLaucher {
    /**
     * 对图像提取DCT和还原的方法集合
     */
    public static void textDCT(String imagePath, String targetPath) {
        byte[] image = ImageToCode.imageToByte(imagePath);
        int i;
        for (i = image.length - 1; i >= 0; i--) {
            if (image[i] == -1 && image[i + 1] == -38) {
                i += 2;
                break;
            }
        }
        i += image[i] * 16 * 16 + image[i + 1];
        byte[] target = new byte[image.length - 2 - i];
        System.arraycopy(image, 0 + i, target, 0, target.length);
        System.out.println(DealWithImage.bytes2Str0b(target));
        target = DealWithImage.str0b2Bytes(DealWithImage.setDCT(DealWithImage.getDCT(DealWithImage.bytes2Str0b(target))));
        System.out.println(DealWithImage.bytes2Str0b(target));
        System.arraycopy(target, 0, image, i, target.length);
        ImageToCode.outImage(image, targetPath, "jpg");
    }

    public static void main(String[] args) {
        textDCT("./测试用图片/实验红图.jpg","./测试用图片/循环后实验红图.jpg");
    }
}
