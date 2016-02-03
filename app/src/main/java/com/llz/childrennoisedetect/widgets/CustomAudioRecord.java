package com.llz.childrennoisedetect.widgets;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by ysbang on 2016/2/3.
 */
public class CustomAudioRecord {


    private static final int WHAT_VOLUME = 1000;
    private final Context mContext;
    private int minBufferSize = 0;
    private static final int sampleRateInHz = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private AudioRecordThread audioRecordThread;
    private AudioRecordHandler audioRecordHandler;
    private Object mLock;

    private onVolumeChangeListener listener;


    public CustomAudioRecord(Context context) {
        this.mContext = context;

        init();
        //STATE_INITIALIZED
        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    minBufferSize);
        }catch (IllegalArgumentException e){
            return ;
        }
    }

    private void init(){
        mLock = new Object();

        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);

        audioRecordHandler = new AudioRecordHandler(mContext);

    }

    public void startAudio(onVolumeChangeListener l) throws Exception {

        if(l==null){
            throw new Exception("参数不能为空");
        }else {
            listener = l;
        }
        //已经在录音，什么也不做
        if(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
        {
            return;
        }

        /*开始录音*/
        audioRecord.startRecording();   //RECORDSTATE_RECORDING
        isRecording = true;
        audioRecordThread = new AudioRecordThread();
        audioRecordThread.start();
    }

    /**
     * 停止录音
     */
    public void stopAudio() {

        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop(); //RECORDSTATE_STOPPED
            isRecording = false;
        }

    }

    private class AudioRecordThread extends Thread {
        @Override
        public void run() {
            super.run();
            short[] buffer = new short[minBufferSize * 10];
            while (isRecording) {

                //r是实际读取的数据长度，一般而言r会小于buffersize
                int r = audioRecord.read(buffer, 0, minBufferSize);
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                // 平方和除以数据总长度，得到音量大小。
                double mean = v / (double) r;
                double volume = 10 * Math.log10(mean);


                Message msg = Message.obtain();
                msg.what = WHAT_VOLUME;
                msg.obj = volume;
                audioRecordHandler.sendMessage(msg);
                // 大概一秒十次
                synchronized (mLock) {
                    try {
                        mLock.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private class AudioRecordHandler extends Handler {
        private WeakReference<Context> weakContext;


        AudioRecordHandler(Context context) {
            weakContext = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = null;
            if (weakContext != null) {
                activity = (Activity) weakContext.get();
            }

            switch (msg.what) {
                case WHAT_VOLUME:
                    float volume = Float.parseFloat(msg.obj.toString());

                    if(volume==Float.NEGATIVE_INFINITY){
                        stopAudio();
                        return;
                    }

                    listener.onVolumeChange(volume);


                    break;
                default:
                    Toast.makeText(weakContext.get(), "工作异常，请检查权限", Toast.LENGTH_SHORT).show();
            }


        }
    }

    public interface onVolumeChangeListener{
        public void onVolumeChange(float volume);
    }
}
