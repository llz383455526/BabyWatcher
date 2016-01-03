package com.llz.childrennoisedetect.config;

/**
 * Created by ysbang on 2015/12/31.
 */
public class UserSetting {

    public  void setVolumeThreshold(float volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
    }

    public float getVolumeThreshold() {
        return volumeThreshold;
    }
    public void setNotiMotherPhone(String notiMotherPhone) {
        NotiMotherPhone = notiMotherPhone;
    }

    public void setNotiFatherPhone(String notiFatherPhone) {
        NotiFatherPhone = notiFatherPhone;
    }

    public void setNofiMotherEchatId(String nofiMotherEchatId) {
        NofiMotherEchatId = nofiMotherEchatId;
    }

    public void setNofiFatherEchatId(String nofiFatherEchatId) {
        NofiFatherEchatId = nofiFatherEchatId;
    }

    private float volumeThreshold = 0;

    public String getNofiFatherEchatId() {
        return NofiFatherEchatId;
    }



    public String getNotiMotherPhone() {
        return NotiMotherPhone;
    }

    public String getNotiFatherPhone() {
        return NotiFatherPhone;
    }

    public String getNofiMotherEchatId() {
        return NofiMotherEchatId;
    }

    private String NotiMotherPhone = "";        //要通知的手机
    private String NotiFatherPhone = "";        //要通知的手机
    private String NofiMotherEchatId = "";      //要音视频通话的id
    private String NofiFatherEchatId = "";      //要音视频通话的id
}
