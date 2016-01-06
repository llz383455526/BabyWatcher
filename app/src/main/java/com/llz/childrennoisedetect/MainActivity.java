package com.llz.childrennoisedetect;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.llz.childrennoisedetect.config.AppConfig;
import com.llz.childrennoisedetect.widgets.YSBNavigationBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;
    private Button btnStop;
    private AudioRecord audioRecord;
    private static final int sampleRateInHz = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufferSize = 0;
    private AudioRecordThread audioRecordThread = null;

    private boolean isRecording = false;
    private java.lang.Object mLock;
    private AudioRecordHandler audioRecordHandler;
    private TextView tvVolume;

    private int NoiseCount = 0; //分贝超过阈值的次数，20次，也就是2s
    private YSBNavigationBar navigationBar;
    private int volumeThreshold;
    private int volumeContinueTime;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init
        mLock = new Object();

        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);
        //STATE_INITIALIZED
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                minBufferSize);

        audioRecordHandler = new AudioRecordHandler(MainActivity.this);

        initviews();

    }

    private void setViews() {
        navigationBar.setLeftLayoutInvisible();
        navigationBar.enableRightTextView("设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MySettingsActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        navigationBar.setTitle("婴儿监护专家");
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAudio();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
            }
        });
    }

    private void initviews() {
        navigationBar = (YSBNavigationBar) findViewById(R.id.nav);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        tvVolume = (TextView) findViewById(R.id.tv_volume);

        setViews();
    }

    /*
    开始录音
     */
    private void startAudio() {
        NoiseCount = 0;

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
    private void stopAudio() {
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop(); //RECORDSTATE_STOPPED
            isRecording = false;
        }

    }

    private class AudioRecordHandler extends Handler {
        private WeakReference<MainActivity> weakContext;


        AudioRecordHandler(MainActivity activity) {
            weakContext = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = null;
            if (weakContext != null) {
                activity = weakContext.get();
            }

            switch (msg.what) {
                case 1:
                    float volume = Float.parseFloat(msg.obj.toString());
                    activity.tvVolume.setText(volume + "");
                    Log.d("MainActivity", "分贝值:" + volume + "noiseCount=" + activity.NoiseCount + "阈值=" + activity.volumeThreshold);
                    if (((int) volume) > activity.volumeThreshold) {
                        activity.NoiseCount++;
                        //哭声持续超过2s
                        if (activity.NoiseCount >= activity.volumeContinueTime * 10) {

                            activity.stopAudio();
                            activity.NoiseCount = 0;

                            //直接拨打电话
                            Uri uri = Uri.parse("tel:" + activity.phone);
                            Intent call = new Intent(Intent.ACTION_CALL, uri); //直接播出电话
                            try {
                                activity.startActivity(call);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //发送qq/微信 信息到父母手机上
//                            String url="mqqwpa://im/chat?chat_type=wpa&uin=464392427";
////                            weakContext.get().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                            if(weakContext.get().mTencent == null){
//                                Log.e("kengle","kengle");
//                                return;
//                            }
//                            int ret = weakContext.get().mTencent.startWPAConversation(weakContext.get(), "464392427", "宝宝监护测试");
//                            weakContext.get().mTencent.startWPAConversation();


                            //网络音视频电话


                        }
                    }

                    break;
                default:
                    Toast.makeText(weakContext.get(), "error", Toast.LENGTH_SHORT).show();
            }


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
                msg.what = 1;
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

    @Override
    protected void onResume() {
        super.onResume();
        volumeThreshold = AppConfig.getUserDefault(AppConfig.flag_volume_threshold, int.class);
        volumeContinueTime = AppConfig.getUserDefault(AppConfig.flag_volume_continue_time, int.class);
        phone = AppConfig.getUserDefault(AppConfig.flag_phone, String.class);

        Log.d("llz", "shold=" + volumeThreshold + ":time=" + volumeContinueTime);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
        audioRecord.release();  //STATE_UNINITIALIZED
        audioRecordHandler.removeCallbacksAndMessages(null);
    }
}
