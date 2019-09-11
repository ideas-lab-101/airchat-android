package com.android.crypt.chatapp.utility.Crypt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * RSA: 既能用于数据加密也能用于数字签名的算法
 * RSA算法原理如下：

 1.随机选择两个大质数p和q，p不等于q，计算N=pq
 2.选择一个大于1小于N的自然数e，e必须与(p-1)(q-1)互素
 3.用公式计算出d：d×e = 1 (mod (p-1)(q-1))
 4.销毁p和q
 5.最终得到的N和e就是“公钥”，d就是“私钥”，发送方使用N去加密数据，接收方只有使用d才能解开数据内容

 基于大数计算，比DES要慢上几倍，通常只能用于加密少量数据或者加密密钥
 私钥加解密都很耗时，服务器要求解密效率高，客户端私钥加密，服务器公钥解密比较好一点
 * Created by Song on 2017/2/22.
 */

public class RSAUtils {

    public static final String RSA = "RSA"; // 非对称加密密钥算法
    /**
     * android系统的RSA实现是"RSA/None/NoPadding"，而标准JDK实现是"RSA/None/PKCS1Padding" ，
     * 这造成了在android机上加密后无法在服务器上解密的原因,所以android要和服务器相同即可。
     */
    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"; //加密填充方式
    public static final int DEFAULT_KEY_SIZE = 1024; //秘钥默认长度
    public static final byte[] DEFAULT_SPLIT = "#PART#".getBytes();    // 当要加密的内容超过bufferSize，则采用partSplit进行分块加密
    public static final int DEFAULT_BUFFERSIZE = (DEFAULT_KEY_SIZE / 8) - 11; // 当前秘钥支持加密的最大字节数


    private RSAUtils() {
        throw new UnsupportedOperationException("constrontor cannot be init");
    }

    /**
     * 随机生成RSA密钥对
     *
     * @param keyLength 密钥长度，范围：512～2048
     *                  一般1024
     *
     *  使用：
     *     KeyPair keyPair=RSAUtils.generateRSAKeyPair(RSAUtils.DEFAULT_KEY_SIZE);
    公钥
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    私钥
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
     * @return
     */

    public static KeyPair generateRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA);
            SecureRandom secureRandom = new SecureRandom();
            kpg.initialize(keyLength, secureRandom);
            return kpg.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


}
