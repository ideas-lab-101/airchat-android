package com.android.crypt.chatapp.msgDetail.IMTools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.crypt.chatapp.utility.Common.RunningData;
import com.android.crypt.chatapp.widget.RoundImageView;
import com.orhanobut.logger.Logger;
import com.android.crypt.chatapp.R;

import java.io.File;



import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;

public class RecordButton extends android.support.v7.widget.AppCompatButton {
    private MsgBgView parentLayout;
    private Context mContext;
    private boolean isError = false;
    public RecordButton(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private String mFile =  RunningData.getInstance().getVoiceUrl() + "voice_"+ System.currentTimeMillis()+".amr";
    private OnFinishedRecordListener finishedListener;
    /**
     * 最短录音时间
     **/
    private int MIN_INTERVAL_TIME = 1000;
    /**
     * 最长录音时间
     **/
    private int MAX_SEC = 30;
    private int MAX_INTERVAL_TIME = 1000 * MAX_SEC;


    private static View view;
    private TextView mStateTV;
    private RoundImageView mStateIV;
    private MediaRecorder mRecorder;
    private ObtainDecibelThread mThread;
    private Handler volumeHandler;


    private float y ;


    public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
        finishedListener = listener;
    }


    private static long startTime;
    private static long endTime;
    private String recordTips = "手指上滑,取消发送";
    private Dialog recordDialog;
    private static int[] res = { R.drawable.ic_volume_0,
            R.drawable.ic_volume_1,
            R.drawable.ic_volume_2,
            R.drawable.ic_volume_3,
            R.drawable.ic_volume_4,
            R.drawable.ic_volume_5,
            R.drawable.ic_volume_6,
            R.drawable.ic_volume_7,
            R.drawable.ic_volume_8 };



    @SuppressLint("HandlerLeak")
    private void init() {
        String voice_baseUrl =  RunningData.getInstance().getVoiceUrl();
        File voice_file = new File(voice_baseUrl);
        if (!voice_file.exists()){
            voice_file.mkdir();
        }

        volumeHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == -100){
                    stopRecording();
                    recordDialog.dismiss();
                }else if(msg.what != -1){
                    mStateIV.setImageResource(res[msg.what]);
                }
            }
        };
    }


    private AnimationDrawable anim;
    private AniType isAnimation = AniType.UNKNOW;
    private enum AniType {
        UNKNOW,
        START,
        PAUSE,
        STOP
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        y = event.getY();
        if(mStateTV!=null && mStateIV!=null &&y<0){
            if (isAnimation == AniType.START){
                anim.stop();
                isAnimation = AniType.PAUSE;
                mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_cancel));
                mStateTV.setText("松开手指,取消发送");
            }else if(isAnimation == AniType.STOP){
                mStateTV.setText("松开手指,取消发送");
            }
        }else if(mStateTV != null){
            if (isAnimation == AniType.PAUSE){
                mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.anmi_record));
                anim = (AnimationDrawable) mStateIV.getDrawable();
                anim.start();
                isAnimation = AniType.START;
                mStateTV.setText(recordTips);
            }else if(isAnimation == AniType.STOP){
                mStateTV.setText(recordTips);
            }
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setText("松开发送");
                initDialogAndStartRecord();
                parentLayout.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                parentLayout.requestDisallowInterceptTouchEvent(true);
                this.setText("按住录音");

                if (endTime == 0){
                    endTime = System.currentTimeMillis();
                }
                Logger.d("endTime = " + endTime + "&&& startTime - endTime = " + (endTime - startTime));
                if(y >= 0 && (endTime - startTime > MIN_INTERVAL_TIME)){
                    finishRecord();
                }else{
                    cancelRecord();
                }
                if (recordDialog != null){
                    recordDialog.dismiss();
                }

                endTime = 0;
                recordTips = "手指上滑,取消发送";
                break;
        }

        return true;
    }

    public void setFatherLayout(MsgBgView msgBgView){
        parentLayout = msgBgView;
    }



    /**
     * 初始化录音对话框 并 开始录音
     */
    private void initDialogAndStartRecord() {
        startTime = System.currentTimeMillis();
        endTime = 0;
        recordDialog = new Dialog(getContext(), R.style.like_toast_dialog_style);

        // view = new ImageView(getContext());
        view = View.inflate(getContext(), R.layout.dialog_record, null);
        mStateIV = (RoundImageView) view.findViewById(R.id.rc_audio_state_image);
        mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
        mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.anmi_record));
        anim = (AnimationDrawable) mStateIV.getDrawable();
        anim.start();
        isAnimation = AniType.START; ;
        mStateIV.setVisibility(View.VISIBLE);

        //mStateIV.setImageResource(R.drawable.ic_volume_1);
        mStateTV.setVisibility(View.VISIBLE);
        mStateTV.setText(recordTips);
        recordDialog.setContentView(view, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        recordDialog.setOnDismissListener(onDismiss);
        WindowManager.LayoutParams lp = recordDialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        startRecording();
        recordDialog.show();
    }


    /**
     * 放开手指，结束录音处理
     */
    private void finishRecord() {
        isError = false;
        long intervalTime = endTime - startTime;
        if (intervalTime < MIN_INTERVAL_TIME) {
            Logger.d("录音时间太短");
            volumeHandler.sendEmptyMessageDelayed(-100, 500);
             //view.setBackgroundResource(R.drawable.ic_voice_cancel);
            mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_wraning));
            mStateTV.setText("录音时间太短");
            anim.stop();
            isAnimation = AniType.PAUSE;
            File file = new File(mFile);
            file.delete();
            stopRecording();

        /*    stopRecording();
            recordDialog.dismiss();*/
            return;
        }else{
             stopRecording();
        }
        Logger.d("录音完成的路径:"+mFile);
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mFile);
            mediaPlayer.prepare();
            mediaPlayer.getDuration();
            Logger.d("获取到的时长:"+mediaPlayer.getDuration()/1000);
        }catch (Exception e){

        }
        if (finishedListener != null && isError == false){
            if (mediaPlayer.getDuration() > 0){
                finishedListener.onFinishedRecord(mFile,mediaPlayer.getDuration()/1000);
            }
        }
    }

    /**
     * 取消录音对话框和停止录音
     */
    public void cancelRecord() {
        stopRecording();
        File file = new File(mFile);
        file.delete();
    }

    //获取类的实例
   // ExtAudioRecorder extAudioRecorder; //压缩的录音（WAV）
    /**
     * 执行录音操作
     */
    //int num = 0 ;
    private void startRecording() {
        if (mRecorder != null) {
            mRecorder.reset();
        } else {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(8000);
        mRecorder.setAudioEncodingBitRate(64);

        File file = new File(mFile);
        Logger.d("创建文件的路径:"+mFile);
        Logger.d("文件创建成功:"+file.exists());
        mRecorder.setOutputFile(mFile);
         try {
            mRecorder.prepare();
            mRecorder.start();
        }catch (Exception e){
             Logger.d("preparestart异常,重新开始录音:"+e.toString());
             e.printStackTrace();
            mRecorder.release();
            mRecorder = null ;
            startRecording();
        }
        mThread = new  ObtainDecibelThread();
        mThread.start();
    }



    private void stopRecording() {
        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
        if (mRecorder != null) {
            try {
                mRecorder.stop();//停止时没有prepare，就会报stop failed
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException pE) {
                isError = true;
                pE.printStackTrace();
            }
        }

    }

    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
//            Logger.d("检测到的分贝001:");
            while (running) {
                if (mRecorder == null || !running) {
                    break;
                }
                // int x = recorder.getMaxAmplitude(); //振幅
                /*
                //*******检测分贝显示声音大小

                int db =   mRecorder.getMaxAmplitude() / 600;
                Logger.d("检测到的分贝002:"+mRecorder);
                if (db != 0 && y>=0) {
                    int f = (int) (db/ 5);
                    if (f ==0  )
                        volumeHandler.sendEmptyMessage(0);
                    else if (f  ==1)
                        volumeHandler.sendEmptyMessage(1);
                    else if (f  ==2)
                        volumeHandler.sendEmptyMessage(2);
                    else if (f  ==3)
                        volumeHandler.sendEmptyMessage(3);
                    else if (f  ==4)
                        volumeHandler.sendEmptyMessage(4);
                    else if (f  ==5)
                        volumeHandler.sendEmptyMessage(5);
                    else if (f  ==6)
                        volumeHandler.sendEmptyMessage(6);
                    else
                        volumeHandler.sendEmptyMessage(7);
                }
                volumeHandler.sendEmptyMessage(-1);
                */

                //****定时
                showTips();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void showTips(){
        if ((System.currentTimeMillis() - startTime) <= MAX_INTERVAL_TIME){
            final int leftTimes =  (int)((MAX_INTERVAL_TIME - (System.currentTimeMillis() - startTime)) / 1000);
            Logger.d("leftTimes = " + leftTimes);
            if (leftTimes <= 10){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordTips = "你还能说" + leftTimes + "秒";
                        mStateTV.setText(recordTips);
                    }
                });
            }

        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStateIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_wraning));
                    anim.stop();
                    isAnimation = AniType.STOP;
                    recordTips = "录音结束";
                    mStateTV.setText(recordTips);
                }
            });
            //
            endTime = System.currentTimeMillis();
            stopRecording();
        }

    }


    private DialogInterface.OnDismissListener onDismiss = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            stopRecording();
        }
    };

    public interface OnFinishedRecordListener {
        public void onFinishedRecord(String audioPath, int time);
    }



}
