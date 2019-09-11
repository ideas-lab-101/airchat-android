package com.android.crypt.chatapp.utility.Crypt;

import android.util.Base64;

import java.security.InvalidParameterException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * AES encryption and decryption
 * <p>
 * 1. key's length >= 16
 * 2. iv's length > 16
 * 3. "transformation": AES/CBC/PKCS5Padding
 * 4. iv=12(bytes) length=128(bits)
 * 5. iv=24(bytes) length=192(bits)
 * 6. iv=32(bytes) length=256(bits)
 * <p>
 * Created by White on 2019/3/19.
 */



public class EncryptUtil {

    private EncryptUtil() {
    }

    /**
     * Base64 decode then AES decrypt
     *
     * @param data           Data to decrypt
     * @param key            Decrypt key
     * @param iv             Decrypt key
     * @param transformation AES/CBC/PKCS5Padding
     * @return Decrypted bytes
     * @throws Exception Decrypt exception
     */
    public static byte[] decryptBase64EncodeData(byte[] data, byte[] key, byte[] iv, String transformation) throws Exception {

        if (data == null || data.length == 0
                || key == null || key.length < 16
                || iv == null || iv.length < 16
                || transformation == null || transformation.length() == 0) {
            throw (new InvalidParameterException());
        }

        byte[] textBytes = Base64.decode(data, Base64.DEFAULT);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static byte[] decryptEncodeData(byte[] data, byte[] key, byte[] iv, String transformation) throws Exception {

        if (data == null || data.length == 0
                || key == null || key.length < 16
                || iv == null || iv.length < 16
                || transformation == null || transformation.length() == 0) {
            throw (new InvalidParameterException());
        }

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(data);
    }

    /**
     * AES encrypt then base64 decode
     *
     * @param data           Data to encrypt
     * @param key            Encrypt key
     * @param iv             Encrypt key
     * @param transformation AES/CBC/PKCS5Padding
     * @return Encrypted bytes
     * @throws Exception Encrypt exception
     */
    public static byte[] encryptAndBase64Encode(byte[] data, byte[] key, byte[] iv, String transformation) throws Exception {

        if (data == null || data.length == 0
                || key == null || key.length == 0
                || iv == null || iv.length == 0
                || transformation == null || transformation.length() == 0) {
            throw (new InvalidParameterException());
        }

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return Base64.encode(cipher.doFinal(data), Base64.DEFAULT);
    }

    public static byte[] encryptAndEncode(byte[] data, byte[] key, byte[] iv, String transformation) throws Exception {

        if (data == null || data.length == 0
                || key == null || key.length == 0
                || iv == null || iv.length == 0
                || transformation == null || transformation.length() == 0) {
            throw (new InvalidParameterException());
        }

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec newKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(data);
    }

}