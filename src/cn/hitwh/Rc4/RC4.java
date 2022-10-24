package cn.hitwh.Rc4;

public class RC4
{
	public static void rc4_init(int[] s, int[] key, int Len)
	{
		int i = 0;
		int j = 0;
		int[] k = new int[256];

		int tmp = 0;
		for (i = 0;i < 256;i++)
		{
			s[i] = i;
			k[i] = key[i % Len];
		}
		for (i = 0; i < 256; i++)
		{
			j = (j + s[i] + k[i]) % 256;
			//����s[i]��s[j]
			tmp = s[i];
			s[i] = s[j]; 
			s[j] = tmp;
		}
	}

	//�ӽ���
	public static void rc4_crypt(int[] s, int[] Data, int Len)
	{
		int i = 0;
		int j = 0;
		int t = 0;
		int k = 0;

		int tmp;
		for (k = 0;k < Len;k++)
		{
			i = (i + 1) % 256;
			j = (j + s[i]) % 256;
			//����s[x]��s[y]
			tmp = s[i];
			s[i] = s[j];
			s[j] = tmp;
			t = (s[i] + s[j]) % 256;
			Data[k] ^= s[t];
		}
	}
}

