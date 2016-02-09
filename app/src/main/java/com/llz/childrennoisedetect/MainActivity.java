package com.llz.childrennoisedetect;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.llz.childrennoisedetect.config.AppConfig;
import com.llz.childrennoisedetect.widgets.ControlView;
import com.llz.childrennoisedetect.widgets.YSBNavigationBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

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

    private int noiseCount = 0; //分贝超过阈值的次数，20次，也就是2s
    private YSBNavigationBar navigationBar;
    private int volumeThreshold;
    private int volumeContinueTime;
    private String phone;
    private ControlView cvBtnOp;

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
        navigationBar.setTitle(getResources().getString(R.string.app_name));


        cvBtnOp.setBtnOpClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cvBtnOp.getBtnOpState()) {
                    startAudio();
                } else {
                    stopAudio();
                }
            }
        });

    }

    private void initviews() {
        navigationBar = (YSBNavigationBar) findViewById(R.id.nav);
        cvBtnOp = (ControlView) findViewById(R.id.cv_btnOp);
        tvVolume = (TextView) findViewById(R.id.tv_volume);

        setViews();
    }

    /*
    开始录音
     */
    private void startAudio() {
        if(TextUtils.isEmpty(phone))
        {
            cvBtnOp.setBtnOpState(false);
            postDialog();
            return;
        }

        noiseCount = 0;

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

    private void postDialog() {
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("为了便于您更好地使用该软件，请到设置界面设置要通知的手机,并检查其它设置项是否正确。");
        AlertDialog dialog = builder.create();
        dialog.setButton(DialogInterface.
                BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(TextUtils.isEmpty(phone))
                {
                    cvBtnOp.setBtnOpState(false);
                    Intent intent = new Intent(MainActivity.this, MySettingsActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    /**
     * 停止录音
     */
    private void stopAudio() {
        noiseCount = 0;
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop(); //RECORDSTATE_STOPPED
            isRecording = false;
            cvBtnOp.setBtnOpState(false);
        }

    }

    private class AudioRecordHandler extends Handler {
        private WeakReference<MainActivity> weakContext;
        private boolean flag_Begin_count = false;
        private int volume_count;


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
                    Log.d("MainActivity", "分贝值:" + volume + "noiseCount=" + activity.noiseCount + "阈值=" + activity.volumeThreshold);
                    if(volume==Float.NEGATIVE_INFINITY){
                        activity.tvVolume.setText("无法获取环境音量，请在设置中为'宝贝监护'应用打开录音权限");
                        activity.stopAudio();
                        return;
                    }

                    if(flag_Begin_count){
                        volume_count++;
                    }
                    if (((int) volume) > activity.volumeThreshold) {                                //音量超过阈值开始计数
                        flag_Begin_count = true;
                        activity.noiseCount++;

                        if(volume_count==0){
                            volume_count++;
                        }
                        //哭声次数超过activity.volumeContinueTime * 10 *0.8次且
                        if (activity.noiseCount >= activity.volumeContinueTime * 10 * 0.6 && volume_count<=activity.volumeContinueTime * 10) {

                            flag_Begin_count = false;
                            volume_count = 0;
                            activity.stopAudio();
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
                    }else {
                        if(volume_count>=activity.volumeContinueTime * 10){ //volumeContinueTime计时结束,没达到条件要重置条件
                            activity.noiseCount = 0;
                            flag_Begin_count = false;
                            volume_count = 0;
                        }
                    }

                    break;
                default:
                    Toast.makeText(weakContext.get(), "工作异常，请检查权限", Toast.LENGTH_SHORT).show();
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

        if((volumeThreshold = AppConfig.getUserDefault(AppConfig.flag_volume_threshold, int.class)) == -1)
        {
            volumeThreshold = 55;   //默认值 55db
        }

        if((volumeContinueTime = AppConfig.getUserDefault(AppConfig.flag_volume_continue_time, int.class)) == -1)
        {
            volumeContinueTime = 2; //默认2s
        }

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
