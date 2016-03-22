package com.qwert2603.vkautomessage.record_details;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
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
import com.qwert2603.vkautomessage.user_list.UserListDialog;

public class RecordFragment extends Fragment implements RecordView {

    private static final String recordIdKey = "recordId";

    private static final int REQUEST_CHOOSE_USER = 1;
    private static final int REQUEST_CHOOSE_TIME = 2;

    public static RecordFragment newInstance(int recordId) {
        RecordFragment recordFragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(recordIdKey, recordId);
        recordFragment.setArguments(args);
        return recordFragment;
    }

    private RecordPresenter mRecordPresenter;

    private ImageView mPhotoImageView;
    private Button mUsernameButton;
    private Switch mEnableSwitch;
    private EditText mMessageEditText;
    private Button mTimeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mRecordPresenter = new RecordPresenter(getArguments().getInt(recordIdKey));
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

        mPhotoImageView = (ImageView) view.findViewById(R.id.photo_image_view);
        mUsernameButton = (Button) view.findViewById(R.id.user_name_button);
        mEnableSwitch = (Switch) view.findViewById(R.id.enable_switch);
        mMessageEditText = (EditText) view.findViewById(R.id.message_edit_text);
        mTimeButton = (Button) view.findViewById(R.id.time_button);

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

        mRecordPresenter.onViewReady();

        return view;
    }

    @Override
    public void onDestroyView() {
        mRecordPresenter.onViewNotReady();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CHOOSE_USER:
                int userId = data.getIntExtra(UserListDialog.EXTRA_SELECTED_USER_ID, 0);
                mRecordPresenter.onUserChosen(userId);
                break;
        }
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
    public void showChooseUser(int currentUserId) {
        UserListDialog userListDialog = UserListDialog.newInstance(currentUserId);
        userListDialog.setTargetFragment(RecordFragment.this, REQUEST_CHOOSE_USER);
        userListDialog.show(getFragmentManager(), userListDialog.getClass().getName());
    }

    @Override
    public void showChooseTime() {
        // TODO: 18.03.2016
    }
}