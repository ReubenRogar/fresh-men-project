public class ts {
    public class ts {
        ushort** genHuffman(byte JbinDHT[],int *offset)
        {
            int j = *offset;//移动dht的指针//dht？
            ushort **data = (ushort **)malloc(sizeof(ushort *) * 17);
            data[0]=(ushort *)malloc(sizeof(ushort)*17);//0用来保存各个码长的数量
            data[0][0] = 0;//递推值 基址


            for (int i = 1; i < 17; i++,j++)
            {
                data[0][i] = JbinDHT[j];//图像数据？
                data[i] = (ushort *)malloc(sizeof(ushort)*(data[0][i]+1)); //data[i][]相当于存储基址
            }
            for (int i = 1; i < 17; i++)
            {
                for (int k = 0; k < data[0][i];k++,j++)
                {
                    data[i][k] = JbinDHT[j];//存dht的数据
                }
                ushort lastbase = data[0][i - 1];//date[0][16]
                ushort base = data[0][i];//date[0][17]
                data[i][base] = (data[i - 1][lastbase] + lastbase) * 2;//date[17][date[0][17]] = (date[16][date[0][16]] + [date[0][16]）*2
            }
    *offset = j;
            return data;
        }
    //算法思路：创建17个长度不一的二维数组，[0]号存储每个数组长度，即每个码字的长度；下标[1]到[16]分别存储码字（权值）；每个一维数组最后一位存储“基址”。

    //字节流转为2进制流：
            for (int i = offset, k = 0; i < lenth - 2; i++, k++)//offset?
        {
            byte temp = Jbin[i];//Jbin?
            for (int j = 7; j >= 0; j--)//？
            {
                bits[k*8+j] = temp&1;
                temp = temp >> 1;
                bitsize++;//算bitsize大小
            }
            if(Jbin[i]==0xff)//FF
            {
                i++;
            }
        }
        //译为2进制的时候，注意对0xff00特殊处理。
        //译码得到dc系数：
        //…此前将压缩数据转为bit形式存储
//译码
    for (int k = 0; k < bitsize;)
        {
            for(int a=1;a<4;a++)//颜色分量
            {
                ushort **dc = Huffman[0][colors[a].DC];//分别处理三个DCT？
                ushort **ac = Huffman[1][colors[a].AC];
                for (int i = 0; i < colors[a].block; i++)//block？
                {
                    //block
                    //dc
                    //int temp = k;
                    ushort code = hufDecode(&k, dc, bits); //移动k
                    if(k>=bitsize)
                     goto BR;//跳到BR
                    if (code == 0xff)//blocks == FF时失败？
                    {
                        printf("DC\n");
                        printf("hufdecode failed\n");
                        printf("k=%d,bitsize=%d", k, bitsize);
                        return NULL;
                    }

                    int    dcNum    = numDecode(k,code, bits);
                    int coloroffset = colors[a].offset;
                    dcform[a][coloroffset].value  = dcNum;
                    dcform[a][coloroffset].lenth  = code;
                    dcform[a][coloroffset].offset = k;
                    colors[a].offset++;

                    k = k + code;

                    //ac ac 译码 -> 跳过
                    for(int m=1;m<64;m++)//dc占一位
                    {
                        code = hufDecode(&k, ac, bits);

                        if(code == 0xff)
                        {
                            printf("AC\n");
                            printf("hufdecode failed\n");
                            printf("k=%d,bitsize=%d\n", k, bitsize);
                            return NULL;
                        }
                        if(code==0)
                        {
                            break;
                        }
                        m+=bytehigh4((byte)code);//0的个数
                        k+=bytelow4((byte)code);//k跳过ac系数
                    }
                }

            }
        }
        BR:
                //dc系数 diff
                for (int a = 1; a < numColor+1;a++)
        {
            for (int i = 1; i < colors[a].offset; i++)
            {
                dcform[a][i].value += dcform[a][i - 1].value;
            }
        }
    }

}
