package com.android.crypt.chatapp.utility.Common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mulaliu on 15/3/26.
 */
public class StringUtils {

    /**
     * 验证邮箱
     * @param email
     * @return
     */
    public static boolean checkEmail(String email){
        boolean flag = false;
        try{
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     * @param mobileNumber
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber){
        boolean flag = false;
        try{
            Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(17([6-8]))|(18[0-3,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    /*
    将首字符转小写
     */
    public static String lowerFirstLetter(String words){
        String str = words;
        str = str.replaceFirst(str.substring(0, 1),str.substring(0, 1).toLowerCase());
        return str;
    }

    /*
    将首字符转大写
     */
    public static String upperFirstLetter(String words){
        String str = words;
        str = str.replaceFirst(str.substring(0, 1),str.substring(0, 1).toUpperCase());
        return str;
    }

    /**
     * 符合JSONObject的字符串转为Map
     * @param jsonString
     * @return
     */
    public static Map<String, String> jsonStrToMap(String jsonString) throws JSONException {
        JSONObject jo = new JSONObject(jsonString);
        Map map = new HashMap();
        Iterator it = jo.keys();
        // 遍历jsonObject数据，添加到Map对象
        while (it.hasNext())
        {
            String key = String.valueOf(it.next());
            String value = jo.getString(key);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 将符合的字符串转为List<Map>
     * @param jsonString
     * @return
     */
    public static List<Map<String, String>> jsonStrToListMap(String jsonString) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString,
                    new TypeToken<List<Map<String, String>>>() {
                    }.getType());
        } catch (Exception e) {
            // TODO: handle exception
        }
        return list;
    }

    /**
     * 将符合的字符串转为对象
     * @param jsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T jsonStrToClass(String jsonString, Class<T> cls) {
        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, cls);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return t;
    }

    /**
     * 将符合的字符串转为List<对象>
     * @param jsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonStrToListClass(String jsonString, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
        }
        return list;
    }

}
