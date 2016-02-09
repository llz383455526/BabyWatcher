package com.llz.childrennoisedetect;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.llz.childrennoisedetect.config.AppConfig;
import com.llz.childrennoisedetect.widgets.CustomAudioRecord;
import com.llz.childrennoisedetect.widgets.YSBNavigationBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ysbang on 2015/12/31.
 */
public class MySettingsActivity extends Activity {

    private ViewHolder holder;
    private int volumeThreshold = 0;
    private int volumeContinueTime = 0;
    private String phone;
    private AlertDialog.Builder builder;
    private Integer currentVolumeMax;
    private AlertDialog detectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ButterKnife.bind(this);

        //读sp
        holder = new ViewHolder(this);
        if((volumeThreshold = AppConfig.getUserDefault(AppConfig.flag_volume_threshold, int.class)) == -1)
        {
            volumeThreshold = 55;   //默认值 55db
        }

        if((volumeContinueTime = AppConfig.getUserDefault(AppConfig.flag_volume_continue_time, int.class)) == -1)
        {
            volumeContinueTime = 3; //默认2s
        }

        phone = AppConfig.getUserDefault(AppConfig.flag_phone, String.class);
        if(!TextUtils.isEmpty(phone))
        {
            holder.settingEtPhone.setText(phone);
        }


        holder.settingNpVolume.setMinValue(20);
        holder.settingNpVolume.setMaxValue(75);
        holder.settingNpVolume.setWrapSelectorWheel(false);
        holder.settingNpVolume.setValue(volumeThreshold);
        holder.settingTvVolume.setText(volumeThreshold + "");

        holder.settingEtTime.setText(volumeContinueTime + "");

        setViews();
    }

    private void setViews() {
        holder.nav.setTitle("设置");
        holder.nav.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MySettingsActivity.this.finish();
            }
        });
        holder.nav.enableRightTextView("保存设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退出设置时，将设置内容存入sp
                saveSetting();
            }
        });
        /**
         * 音量阈值调节
         */

        holder.settingTvVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 先进行一次自动检测，给用户建议
                 */
                postAutoDetectDialog();
                holder.settingNpVolume.setVisibility(View.VISIBLE);
            }
        });
        holder.settingNpVolume.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                holder.settingTvVolume.setText(newVal + "");
            }
        });


        holder.settingEtTime.setSelection(holder.settingEtTime.getText().toString().length());

    }

    private void postAutoDetectDialog() {

        View view = LayoutInflater.from(MySettingsActivity.this).inflate(R.layout.auto_detect_dialog_view,null);
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);

        //init view
        Button btnStartDetect = (Button) view.findViewById(R.id.btn_start_detect);
        final Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final Button btnToSet = (Button) view.findViewById(R.id.btn_to_set);
        final ProgressBar pb = (ProgressBar) view.findViewById(R.id.progressbar);
        final TextView tvDetectResult = (TextView) view.findViewById(R.id.tv_detect_result);

        pb.setMax(50);

        //set view
        tvDetectResult.setText("点击\"开始检测\"可以对房间声场状态进行自动检测，持续5秒。如果不需要自动检测，点击\"取消\"。");
        btnStartDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final CustomAudioRecord customAudioRecord = new CustomAudioRecord(MySettingsActivity.this);
                try {
                    customAudioRecord.startAudio(new CustomAudioRecord.onVolumeChangeListener() {

                        public int count; //自动检测5s，也就是50次
                        public ArrayList<Integer> list = new ArrayList<Integer>();

                        @Override
                        public void onVolumeChange(float volume) {
                            list.add((int) volume);
                            count++;
                            pb.setProgress(count);
                            if (count == 50) {
                                customAudioRecord.stopAudio();

                                Collections.sort(list, new Comparator<Integer>() {
                                    @Override
                                    public int compare(Integer lhs, Integer rhs) {
                                        return lhs - rhs;
                                    }
                                });

                                currentVolumeMax = list.get(list.size() - 1);
                                pb.setVisibility(View.INVISIBLE);
                                ((Button) v).setVisibility(View.GONE);
                                btnCancel.setVisibility(View.GONE);
                                btnToSet.setVisibility(View.VISIBLE);
                                String str = "系统检测到你房间的音量范围是[" + list.get(0) + "," + currentVolumeMax + "] dB," + "建议您设置的音量阈值大于" + currentVolumeMax + "。";
                                tvDetectResult.setVisibility(View.VISIBLE);
                                tvDetectResult.setText(str);

                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnToSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.settingTvVolume.setText(currentVolumeMax + "");
                holder.settingNpVolume.setValue(currentVolumeMax);
                detectDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectDialog.dismiss();
            }
        });

        detectDialog = builder.show();
    }

    private void postSuggestionDialog(ArrayList list) {
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs - rhs;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("系统检测到你房间的音量范围是["+list.get(0)+","+list.get(list.size()-1)+"]。");
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        saveSetting();
    }

    private void saveSetting() {
        String time = holder.settingEtTime.getText().toString();
        if(!TextUtils.isEmpty(time)){
            AppConfig.setUserDefault(AppConfig.flag_volume_continue_time, Integer.parseInt(time));
        }else {
            Toast.makeText(MySettingsActivity.this, "哭声持续时间不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        int volume = holder.settingNpVolume.getValue();
        AppConfig.setUserDefault(AppConfig.flag_volume_threshold, volume);

        String phone = holder.settingEtPhone.getText().toString();
        if(!TextUtils.isEmpty(phone))
        {
            AppConfig.setUserDefault(AppConfig.flag_phone, phone);
        }else {
            Toast.makeText(MySettingsActivity.this, "要通知的手机不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        MySettingsActivity.this.finish();
        Toast.makeText(MySettingsActivity.this, "设置已保存", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    static class ViewHolder {
        @Bind(R.id.nav)
        YSBNavigationBar nav;
        @Bind(R.id.setting_tv_time_title)
        TextView settingTvTimeTitle;
        @Bind(R.id.setting_et_time)
        EditText settingEtTime;
        @Bind(R.id.setting_tv_phone_title)
        TextView settingTvPhoneTitle;
        @Bind(R.id.setting_et_phone)
        EditText settingEtPhone;
        @Bind(R.id.setting_np_volume)
        NumberPicker settingNpVolume;
        @Bind(R.id.setting_tv_volume)
        TextView settingTvVolume;
        @Bind(R.id.rl_volume)
        RelativeLayout rlVolume;

        ViewHolder(Activity view) {
            ButterKnife.bind(this, view);
        }
    }


}
