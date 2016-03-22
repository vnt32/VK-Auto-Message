package com.qwert2603.vkautomessage.user_details;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.qwert2603.vkautomessage.base.BasePresenter;
import com.qwert2603.vkautomessage.model.DataManager;
import com.qwert2603.vkautomessage.user_list.UserListPresenter;
import com.qwert2603.vkautomessage.util.LogUtils;
import com.qwert2603.vkautomessage.util.StringUtils;
import com.vk.sdk.api.model.VKApiUserFull;

import java.lang.ref.WeakReference;

public class UserPresenter extends BasePresenter<VKApiUserFull, UserView> {

    private WeakReference<UserListPresenter> mUserListPresenter;

    public UserPresenter(VKApiUserFull user, @Nullable UserListPresenter userListPresenter) {
        setModel(user);
        if (userListPresenter != null) {
            mUserListPresenter = new WeakReference<>(userListPresenter);
        }
    }

    @Override
    protected void onUpdateView(@NonNull UserView view) {
        VKApiUserFull user = getModel();
        if (user == null) {
            return;
        }
        view.showName(StringUtils.getUserName(user));
        DataManager.getInstance()
                .getPhotoByUrl(user.photo_100)
                .subscribe(
                        view::showPhoto,
                        LogUtils::e
                );
        view.showSelected(false);
        if (mUserListPresenter != null) {
            UserListPresenter userListPresenter = mUserListPresenter.get();
            if (userListPresenter != null) {
                view.showSelected(userListPresenter.getSelectedUserId() == getModel().id);
            }
        }
    }

    public VKApiUserFull getUser() {
        return getModel();
    }
}