package com.qwert2603.vkautomessage.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.qwert2603.vkautomessage.R;
import com.qwert2603.vkautomessage.presenter.RecordPresenter;
import com.qwert2603.vkautomessage.view.RecordView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecordFragment extends Fragment implements RecordView {

    private static final String recordIdKey = "recordId";

    public static RecordFragment newInstance(int recordId) {
        RecordFragment recordFragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(recordIdKey, recordId);
        recordFragment.setArguments(args);
        return recordFragment;
    }

    private RecordPresenter mRecordPresenter;

    @Bind(R.id.photo_image_view)
    ImageView mPhotoImageView;

    @Bind(R.id.user_name_button)
    Button mUsernameButton;

    @Bind(R.id.enable_switch)
    Switch mEnableSwitch;

    @Bind(R.id.message_edit_text)
    EditText mMessageEditText;

    @Bind(R.id.time_button)
    Button mTimeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mRecordPresenter = new RecordPresenter();
        mRecordPresenter.setModelId(getArguments().getInt(recordIdKey));
        mRecordPresenter.bindView(this);
    }

    @Override
    public void onDestroy() {
        mRecordPresenter.unbindView();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        ButterKnife.bind(this, view);
        mUsernameButton.setOnClickListener(v -> mRecordPresenter.onChooseUserClicked());
        mEnableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mRecordPresenter.onEnableClicked(isChecked));
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecordPresenter.onMessageEdited(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTimeButton.setOnClickListener(v -> mRecordPresenter.onChooseTimeClicked());
        return view;
    }

    @Override
    public void showPhoto(Bitmap photo) {
        mPhotoImageView.setImageBitmap(photo);
    }

    @Override
    public void showUserName(String userName) {
        mUsernameButton.setText(userName);
    }

    @Override
    public void showMessage(String message) {
        mMessageEditText.setText(message);
    }

    @Override
    public void showEnabled(boolean enabled) {
        mEnableSwitch.setChecked(enabled);
    }

    @Override
    public void showTime(String time) {
        mTimeButton.setText(time);
    }

    @Override
    public void showChooseUser() {
        // TODO: 18.03.2016
    }

    @Override
    public void showChooseTime() {
        // TODO: 18.03.2016
    }
}
