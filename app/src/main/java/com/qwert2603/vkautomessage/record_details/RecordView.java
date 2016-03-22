package com.qwert2603.vkautomessage.record_details;

import android.graphics.Bitmap;

import com.qwert2603.vkautomessage.base.BaseView;

public interface RecordView extends BaseView {
    void showPhoto(Bitmap photo);
    void showUserName(String userName);
    void showMessage(String message);
    void showEnabled(boolean enabled);
    void showTime(String time);
    void showChooseUser(int currentUserId);
    void showChooseTime();
}