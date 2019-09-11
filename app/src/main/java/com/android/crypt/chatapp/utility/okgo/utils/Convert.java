/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.crypt.chatapp.utility.okgo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.readystatesoftware.chuck.internal.support.JsonConvertor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：16/9/28
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class Convert {

    private static Gson create() {
        return Convert.GsonHolder.gson;
    }

    private static class GsonHolder {
//      private static Gson gson = new Gson();
        /**
         * 解决Gson将int自动转为double
         */
        public static Gson gson = new GsonBuilder().
            registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src == src.longValue())
                        return new JsonPrimitive(src.longValue());
                    return new JsonPrimitive(src);
                }
            }).create();
    }

    public static <T> T fromJson(String json, Class<T> type) throws JsonIOException, JsonSyntaxException {
        return create().fromJson(json, type);
    }

    public static <T> T fromJson(String json, Type type) {
        return create().fromJson(json, type);
    }

    public static <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        return create().fromJson(reader, typeOfT);
    }

    public static <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        return create().fromJson(json, classOfT);
    }

    public static <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
        return create().fromJson(json, typeOfT);
    }

    public static String toJson(Object src) {
        return create().toJson(src);
    }

    public static String toJson(Object src, Type typeOfSrc) {
        return create().toJson(src, typeOfSrc);
    }

    public static String formatJson(String json) {
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(json);
            return JsonConvertor.getInstance().toJson(je);
        } catch (Exception e) {
            return json;
        }
    }

    public static String formatJson(Object src) {
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(toJson(src));
            return JsonConvertor.getInstance().toJson(je);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 将数据转换为adapter需要的List<Map>
     * @param src
     * @return
     */
    public static List<Map<String, String>> formatToListMap(Object src) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(toJson(src));
            list =  JsonConvertor.getInstance().fromJson(je,
                    new TypeToken<List<Map<String, String>>>() {
                    }.getType());
        } catch (Exception e) { }
        return list;
    }

    /**
     * 将数据转换为 JSonObject
     * @param src
     * @return
     * @throws JSONException
     */
    public static JSONObject formatToJson(Object src){
        JSONObject jo = new JSONObject();
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(toJson(src));
            String jsonStr =  JsonConvertor.getInstance().toJson(je);
            jo = new JSONObject(jsonStr);
        } catch (Exception e) { }
        return jo;
    }

    /**
     * 将数据转换为 Map<String, String>
     * @param src
     * @return
     * @throws JSONException
     */
    public static Map<String, String> formatToMap(Object src){
        Map map = new HashMap();
        try {
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(toJson(src));
            map =  JsonConvertor.getInstance().fromJson(je,
                    new TypeToken<Map<String, String>>() {
                    }.getType());
        } catch (Exception e) { }
        return map;
    }
}
