package com.android.crypt.chatapp.utility.Crypt;


import android.util.Base64;
import com.android.crypt.chatapp.utility.Common.RunningData;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * Created by White on 2019/3/21.
 *
 */

/*

测试公钥
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDXSbCj0y+erR+qlhMkO1LLta359qAYqXnCAdTW1VKR25ZYzVs27jPBn1uJ4C/6baJP79l8nJ+Vksnst7mANWFaDIO6V+mAX2qsdp2qXJSrL47P4yf1939dydzzMiMjzw7pK5wPzcbY1OI2dWTVoosV2zw5TsfrmeZu32tJQhMkhQIDAQAB


测试私钥
MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANdJsKPTL56tH6qWEyQ7Usu1rfn2oBipecIB1NbVUpHblljNWzbuM8GfW4ngL/ptok/v2Xycn5WSyey3uYA1YVoMg7pX6YBfaqx2napclKsvjs/jJ/X3f13J3PMyIyPPDukrnA/NxtjU4jZ1ZNWiixXbPDlOx+uZ5m7fa0lCEySFAgMBAAECgYAobaMpMJhpsNMYgrQ3gphqvsRcA29PEkxWHWftrAOkdlsGdBHj/9liS92xx5La8Umgv0bVOshRG06mEF5acCvGmAeVfdXIA7F+1lH03O6Fuac4NowK4XGEmwg0aWOFJk/M0rRWYDLqRw2vZuxVmMt5ti51pULkKfkoCp6+cgNuIQJBAPL6JMNoKM9S9ZZ9zR437mWTMWQKYDu3SVqlDnqefBapOvq0TIaArt/ziM7YhuCUPVmgD+WgnDgXE9aR7fVG66kCQQDi06Aov9+dColDKcMEAcf0oF3/OYMQlGNNERcLGUSwfmkEQz6jlWgMhYosA4zIuR49K6wxinXd9ysyT2w3ZVt9AkA/IYdTzkhsNd3hkCYvZ9DlS21V1OxUF7dTefddHLiZGrdcVGRdvimxvpEZTWEeEOYv10rKGwT7/eMFqTzusdKhAkBb5dGlsNcwEOy7wVe5b99LRQ3QMcTgGr6AosPrQzmJKC30BCqErCuTpybr3iRTzmxp7B+/kpFedJ2TrInzh6UhAkEAohteMAWocygV8RVkYNzUUtAahdpRvVhNTU9xFPHYH0CU+CMmCTztEXzb05fg0Y8gX4iHkkFMPPPiosqIVUMiVQ==

*/



public class CryTool {
    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"; //加密填充方式

    /**
     *  初始化
     *
     *
     * **/
    public CryTool(){

    }


    /***
     *
     * RSA 部分
     * pub_key 加密， pri_key 解密
     *
     * ****/


    //用户计算自己的公司钥
    public void genPubAndPriKey(CryToolCallbacks callback){

        KeyPair keyPair= RSAUtils.generateRSAKeyPair(RSAUtils.DEFAULT_KEY_SIZE);
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        String pub_key = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        String pri_key = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
        String pri_key_en = aesEnWith(pri_key, RunningData.getInstance().getInnerAESKey());

        callback.calRSAKeysFinish(pub_key, pri_key, pri_key_en);
    }


    //pub_key 加密
    public String rsaEncrypt(String oriText, String pub_key){
        PublicKey key = getPublicKey(pub_key);
        try {
            //取公钥
            byte[] data = oriText.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] text_en_byte = cipher.doFinal(data);
            String text_en = Base64.encodeToString(text_en_byte, Base64.DEFAULT);

            return text_en;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //pri_key 解密
    public String rsaDecrypt(String enText, String pri_key){
        PrivateKey key = getPrivateKey(pri_key);

        try {
            byte[] data = Base64.decode(enText, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] text_de_byte = cipher.doFinal(data);

            String text_de = new String(text_de_byte, "UTF-8");
            return text_de;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //通过字符串生成私钥
    private PrivateKey getPrivateKey(String privateKeyData){

        PrivateKey privateKey = null;
        try {
            byte[] decodeKey = Base64.decode(privateKeyData, Base64.DEFAULT); //将字符串Base64解码
            PKCS8EncodedKeySpec x509= new PKCS8EncodedKeySpec(decodeKey);//创建x509证书封装类
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");//指定RSA
            privateKey = keyFactory.generatePrivate(x509);//生成私钥
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    //通过字符串生成公钥
    private PublicKey getPublicKey(String publicKeyData){

        PublicKey publicKey = null;
        try {
            byte[] decodeKey = Base64.decode(publicKeyData, Base64.DEFAULT);
            X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(x509);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKey;
    }


    /***
     *
     * AES 部分
     *
     * ****/
    public String aesEnWith(String originalString, String key){
        try {
            byte[] encryptedData = EncryptUtil.encryptAndBase64Encode(originalString.getBytes("UTF-8"), key.getBytes("UTF-8"), key.getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
            String encryptedString = new String(encryptedData, "UTF-8");
            return encryptedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String aesDeWith(String originalString, String key){
        try {
            byte[] decryptedData = EncryptUtil.decryptBase64EncodeData(originalString.getBytes("UTF-8"), key.getBytes("UTF-8"), key.getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
            String decryptedString = new String(decryptedData,"UTF-8");
            return decryptedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  "";

    }


    public byte[] aesImageEnWith(byte[] originalData, String key){
        try {
            byte[] encryptedData = EncryptUtil.encryptAndEncode(originalData, key.getBytes("UTF-8"), key.getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
            return encryptedData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] aesImageDeWith(byte[] originalData, String key){
        try {
            return EncryptUtil.decryptEncodeData(originalData, key.getBytes("UTF-8"), key.getBytes("UTF-8"), "AES/CBC/PKCS7Padding");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }


    /**
     *  Sha 512 加密
     *  私钥进行sha512 加密后，截取其中16位，作为AES 动态密钥
     */
    public String getMyIMMessageAESKey(String pri_key){
        String sha512 = shaEncrypt(pri_key);
        return sha512.substring(10,26);
    }


    public String shaEncrypt(String data) {
        MessageDigest messageDigest;
        String encodestr = data;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data.getBytes("UTF-8"));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    private String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }


    public String shaAccount(String data) {
        if (data == null){
            data = "";
        }

        byte[] dataBytes = data.getBytes();
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("SHA-1");
            md5.update(dataBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] resultBytes = md5.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : resultBytes) {
            if(Integer.toHexString(0xFF & b).length() == 1) {
                sb.append("0").append(Integer.toHexString(0xFF & b));
            } else {
                sb.append(Integer.toHexString(0xFF & b));
            }
        }

        return sb.toString();
    }


}


