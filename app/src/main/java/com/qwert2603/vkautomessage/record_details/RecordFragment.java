package com.qwert2603.vkautomessage.record_details;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.qwert2603.vkautomessage.R;
import com.qwert2603.vkautomessage.edit_message.EditMessageDialog;
import com.qwert2603.vkautomessage.user_list.UserListDialog;

public class RecordFragment extends Fragment implements RecordView {

    private static final String recordIdKey = "recordId";

    private static final int REQUEST_CHOOSE_USER = 1;
    private static final int REQUEST_EDIT_MESSAGE = 2;
    private static final int REQUEST_CHOOSE_TIME = 3;

    public static RecordFragment newInstance(int recordId) {
        RecordFragment recordFragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putInt(recordIdKey, recordId);
        recordFragment.setArguments(args);
        return recordFragment;
    }

    private RecordPresenter mRecordPresenter;

    private ImageView mPhotoImageView;
    private TextView mUsernameTextView;
    private Switch mEnableSwitch;
    private TextView mMessageTextView;
    private TextView mTimeTextView;

    private CardView mUserCardView;
    private CardView mMessageCardView;
    private CardView mTimeCardView;

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
        mUsernameTextView = (TextView) view.findViewById(R.id.user_name_text_view);
        mEnableSwitch = (Switch) view.findViewById(R.id.enable_switch);
        mMessageTextView = (TextView) view.findViewById(R.id.message_text_view);
        mTimeTextView = (TextView) view.findViewById(R.id.time_text_view);

        mUserCardView = (CardView) view.findViewById(R.id.user_card);
        mMessageCardView = (CardView) view.findViewById(R.id.message_card);
        mTimeCardView = (CardView) view.findViewById(R.id.time_card);

        mUserCardView.setOnClickListener(v -> mRecordPresenter.onChooseUserClicked());
        mEnableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mRecordPresenter.onEnableClicked(isChecked));
        mMessageCardView.setOnClickListener(v -> mRecordPresenter.onEditMessageClicked());
        mTimeCardView.setOnClickListener(v -> mRecordPresenter.onChooseTimeClicked());

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
            case REQUEST_EDIT_MESSAGE:
                String message = data.getStringExtra(EditMessageDialog.EXTRA_MESSAGE);
                mRecordPresenter.onMessageEdited(message);
                break;
        }
    }

    @Override
    public void showPhoto(Bitmap photo) {
        mPhotoImageView.setImageBitmap(photo);
    }

    @Override
    public void showUserName(String userName) {
        mUsernameTextView.setText(userName);
    }

    @Override
    public void showMessage(String message) {
        mMessageTextView.setText(message);
    }

    @Override
    public void showEnabled(boolean enabled) {
        mEnableSwitch.setChecked(enabled);
    }

    @Override
    public void showTime(String time) {
        mTimeTextView.setText(time);
    }

    @Override
    public void showChooseUser(int currentUserId) {
        UserListDialog userListDialog = UserListDialog.newInstance(currentUserId);
        userListDialog.setTargetFragment(RecordFragment.this, REQUEST_CHOOSE_USER);
        userListDialog.show(getFragmentManager(), userListDialog.getClass().getName());
    }

    @Override
    public void showEditMessage(String message) {
        EditMessageDialog editMessageDialog = EditMessageDialog.newInstance(message);
        editMessageDialog.setTargetFragment(RecordFragment.this, REQUEST_EDIT_MESSAGE);
        editMessageDialog.show(getFragmentManager(), editMessageDialog.getClass().getName());
    }

    @Override
    public void showChooseTime() {
        // TODO: 18.03.2016
    }
}
