package com.android.crypt.chatapp.msgDetail.IMTools;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.android.crypt.chatapp.utility.Common.MediaManager;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.utility.Websocket.Model.SendMessageBody;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.utility.Common.AudioDecode;
import com.android.crypt.chatapp.utility.Common.FucUtil;
import com.android.crypt.chatapp.utility.Common.JsonParser;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by White on 2019/6/3.
 */

public class DownLoadVoice {
    public boolean isPlaying = false;

    private static DownLoadVoice mInstance;
    private Context context;
    private SendMessageBody cur_voice;

    public VoiceProcessCallback callbacks;

    public interface VoiceProcessCallback{
        void downLoadStart();
        void downLoadEnd(boolean haserror);

        void playStart();
        void playFinish();
        void transStart(SendMessageBody file);
        void transEnd(SendMessageBody fileurl, String result, int has_error);  //
    }

    public static DownLoadVoice getInstance() {
        if (mInstance == null) {
            synchronized (DownLoadVoice.class) {
                if (mInstance == null) {
                    mInstance = new DownLoadVoice();
                }
            }
        }
        return mInstance;
    }

    public DownLoadVoice(){

    }


    public void addListener(Context context, VoiceProcessCallback callback){
        this.callbacks = callback;
        this.context = context;
    }

    public void loadFile(final SendMessageBody body, final int op_type){ // op_type == 0 播放。 op_type == 1 翻译
        final String fileurl = body.getFileUrl();
        cur_voice = body;
        if (this.isPlaying == true){
            stopPlay();
            if (op_type == 1){
                translateMethod(fileurl);
            }
            return;
        }
        //下载路径
        final String allurl = RunningData.getInstance().echoIMPicUrl() + fileurl;
        //本地路径 斜线替换成下划线  / to -
        final String fileName = fileurl.replace("/", "_");
        final String voicePath =  RunningData.getInstance().getVoiceUrl();//getContext().getFilesDir()+ "/";
        final String fileLocal = voicePath + fileName;


        File voicePathFile = new File(voicePath);
        if (!voicePathFile.exists()){
            voicePathFile.mkdir();
        }


        if (fileIsExists(fileLocal)){
            if(op_type == 0){
//                Logger.d("播放本地录音 " + fileLocal);
                playVoice(fileLocal);
            }else{
                translateMethod(fileLocal);
            }
            return;
        }

        if (callbacks != null){
            callbacks.downLoadStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkGo.<File>get(allurl)
                        .cacheKey(fileLocal)
                        .cacheMode(CacheMode.IF_NONE_CACHE_REQUEST)
                        .execute(new FileCallback(voicePath, fileName) {//在下载中，可以设置下载下来的文件的名字
                            @Override
                            public void downloadProgress(Progress progress) {
                                super.downloadProgress(progress);
                                Logger.d("EasyHttpActivity" +"当前下载了:" + progress.currentSize + ",总共有:" + progress.totalSize+",下载速度为:"+progress.speed); //这个totalSize一直是初始值-1，很尴尬
                                if (callbacks != null){
                                    double percent = progress.currentSize / progress.totalSize;
                                    if (percent < 0){
                                        percent = 0;
                                    }
                                    if (percent > 1){
                                        percent = 1;
                                    }
                                }
                            }

                            @Override
                            public void onError(Response<File> response) {
                                super.onError(response);
                                Logger.d("下载错误" + response.message());
                                if (callbacks != null){
                                    callbacks.downLoadEnd(true);
                                }
                            }

                            @Override
                            public void onCacheSuccess(Response<File> response) {
                                super.onCacheSuccess(response);
                            }

                            @Override
                            public void onSuccess(Response<File> response) {
                                Logger.d("onSuccess onSuccess onSuccess");
                            }

                            @Override
                            public void onFinish() {
                                super.onFinish();
                                Logger.d("完成");
                                if (callbacks != null){
                                    callbacks.downLoadEnd(false);
                                }
                                if(op_type == 0){
                                    playVoice(fileLocal);
                                    Logger.d("播放远程录音 " + fileLocal);
                                }else{
                                    translateMethod(fileurl);
                                }
                            }
                        });
            }
        }).start();
    }

    private void playVoice(String audioPath){
        isPlaying = true;
        if (callbacks != null){
            callbacks.playStart();
        }

        if (fileIsExists(audioPath)){
            MediaManager.playSound(context, audioPath, new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (callbacks != null){
                        callbacks.playFinish();
                    }
                    MediaManager.release();
                    isPlaying = false;
                }
            });
        }else {
            if (callbacks != null){
                callbacks.downLoadEnd(true);
            }
        }
    }

    private void stopPlay(){
        if (isPlaying == true){
            isPlaying = false;
            MediaManager.pause();
            if (callbacks != null){
                callbacks.playFinish();
            }
        }
    }

    public boolean fileIsExists(String strFile) {
        try {
            File f=new File(strFile);
            if(!f.exists()) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    //**** 翻译
    private SpeechRecognizer mIat = null;
    private StringBuilder resultBuffer = null;
    private AudioDecode audioDecode = null;
    private boolean mTranslateEnable = true;  //防止重复翻译

    private void translateMethod(String audioPath){
        if (mTranslateEnable == false){
            if (callbacks != null){
                callbacks.transEnd(cur_voice, "",1);
            }
            return;
        }
        mTranslateEnable = false;
        //初始化识别无UI识别对象
        //使用SpeechRecognizer对象，可根据回调消息自定义界面；
        if (callbacks != null){
            callbacks.transStart(cur_voice);
        }
        if(mIat == null){
            mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
            resultBuffer = new StringBuilder();

            mIat.setParameter(SpeechConstant.PARAMS, null);
            //设置语法ID和 SUBJECT 为空，以免因之前有语法调用而设置了此参数；或直接清空所有参数，具体可参考 DEMO 的示例。
            mIat.setParameter( SpeechConstant.CLOUD_GRAMMAR, null);
            mIat.setParameter( SpeechConstant.SUBJECT, null );
            //设置返回结果格式，目前支持json,xml以及plain 三种格式，其中plain为纯听写文本内容
            mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
            mIat.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
            //此处engineType为“cloud”
            mIat.setParameter( SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //设置语音输入语言，zh_cn为简体中文
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            //设置结果返回语言
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
            mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
            // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
            //取值范围{1000～10000}
            mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
            //设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入，
            //自动停止录音，范围{0~10000}
            mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
            //设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
            mIat.setParameter(SpeechConstant.ASR_PTT,"1");
            mIat.setParameter(SpeechConstant.AUDIO_SOURCE,"-1");

        }
        resultBuffer.delete(0, resultBuffer.length());
        startReco(audioPath);
    }

    private void startReco(final String audioPath) {
        try{
            if(audioDecode == null){
                audioDecode = AudioDecode.newInstance();
            }
            audioDecode.prepare(audioPath);
            audioDecode.setOnCompleteListener(new AudioDecode.OnCompleteListener() {
                @Override
                public void completed(final ArrayList<byte[]> pcmData) {
                    int ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        Logger.d("听写失败,错误码：" + ret);
                        if (callbacks != null){
                            callbacks.transEnd(cur_voice, "转化失败 ("+ ret + ")",2);
                        }
                        mIat.cancel();
                        return;
                    }
                    if (pcmData != null) {
                        //写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                        //必须要先保存到本地，才能被讯飞识别
                        for (byte[] data : pcmData) {
                            final ArrayList<byte[]> buffers = FucUtil.splitBuffer(data, data.length, 4800);
                            for (byte[] buffer: buffers) {
                                mIat.writeAudio(buffer, 0, buffer.length);
                            }
                            mIat.writeAudio(data, 0, data.length);
                        }
                        mIat.stopListening();
                    } else {
                        mIat.cancel();
                    }
                    audioDecode.release();
                    audioDecode = null;
                    Logger.d("------completed-----------");
                }
            });
            audioDecode.startAsync();
        }catch (Exception e){}
    }


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Logger.d( "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Logger.d("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override public void onBeginOfSpeech() {
            Logger.d("开始说话");
        }

        @Override public void onEndOfSpeech() {
            Logger.d("结束说话");
        }
        @Override public void onResult(RecognizerResult results, boolean isLast) {
            if( mTranslateEnable == false){
                combineResult( results );
            }else{
                mIat.cancel();
            }
            if (isLast) {
                String result= resultBuffer.toString();
                Logger.d("最终结果 = " + result);
                if (callbacks != null){
                    callbacks.transEnd(cur_voice, result,0);
                }
                mIat.cancel();
                mTranslateEnable = true;
            }
        }
        @Override public void onError(SpeechError error) {
            Logger.d(error.getPlainDescription(true));
            mIat.cancel();
            mTranslateEnable = true;
            if (callbacks != null){
                callbacks.transEnd(cur_voice,"转化失败 ("+ error.getErrorCode() + ")",3);
            }
        }
        @Override public void onVolumeChanged(int volume, byte[] data) {/*Logger.d("当前正在说话，音量大小：" + volume);*/}

        @Override public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    private void combineResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        Gson gson = new Gson();
        resultBuffer.append(text);
    }


}
