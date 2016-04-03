package com.qwert2603.vkautomessage.navigation;

import android.support.annotation.NonNull;

import com.qwert2603.vkautomessage.base.BasePresenter;
import com.qwert2603.vkautomessage.model.DataManager;
import com.qwert2603.vkautomessage.util.LogUtils;
import com.vk.sdk.api.model.VKApiUser;

import rx.Subscription;

import static com.qwert2603.vkautomessage.util.StringUtils.getUserName;

public class NavigationPresenter extends BasePresenter<VKApiUser, NavigationView> {

    private Subscription mSubscription;

    public NavigationPresenter() {
        loadMyselfUser();
    }

    @Override
    protected void onUpdateView(@NonNull NavigationView view) {
        VKApiUser user = getModel();
        if (user != null) {
            view.showUserName(getUserName(user));
            view.showUserPhoto(null);
            DataManager.getInstance()
                    .getPhotoByUrl(user.photo_100)
                    .subscribe(
                            photo -> {
                                NavigationView navigationView = getView();
                                if (navigationView != null) {
                                    navigationView.showUserPhoto(photo);
                                }
                            },
                            LogUtils::e
                    );
        } else {
            view.showLoading();
        }
    }

    @Override
    public void unbindView() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        super.unbindView();
    }

    public void onSettingsClicked() {
        getView().showSettings();
    }

    public void onLogOutClicked() {
        DataManager.getInstance().logOutVk();
        getView().showLogOut();
    }

    private void loadMyselfUser() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        mSubscription = DataManager.getInstance()
                .getVkUserMyself()
                .subscribe(
                        user -> NavigationPresenter.this.setModel(user),
                        throwable -> {
                            if (mSubscription != null) {
                                mSubscription.unsubscribe();
                                mSubscription = null;
                            }
                            updateView();
                            LogUtils.e(throwable);
                        }
                );
    }
}
