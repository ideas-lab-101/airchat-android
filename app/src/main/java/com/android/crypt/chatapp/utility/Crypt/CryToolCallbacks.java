package com.android.crypt.chatapp.utility.Crypt;

/**
 * Created by White on 2019/3/21.
 */

public interface CryToolCallbacks {
    /**
     *
     * 公钥 pub_key        不加密，上传服务器
     * 私钥 pri_key        目前传空。对于私钥的操作，使用加密后私钥，包括调用CryTool.saveKeys()保存。私钥解密在内存中进行
     * 加密私钥 pri_key_en  需要在UI展示给用户的，永远是加密后的私钥（显示私钥字符串和生成私钥二维码）
     *
     * **/
    void calRSAKeysFinish(String pub_key, String pri_key, String pri_key_en);

}
