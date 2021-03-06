package com.qwert2603.vkautomessage.base.navigation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.qwert2603.floating_action_mode.FloatingActionMode;
import com.qwert2603.vkautomessage.avatar_view.AvatarView;
import com.qwert2603.vkautomessage.R;
import com.qwert2603.vkautomessage.RxBus;
import com.qwert2603.vkautomessage.VkAutoMessageApplication;
import com.qwert2603.vkautomessage.base.BaseActivity;
import com.qwert2603.vkautomessage.base.BaseFragment;
import com.qwert2603.vkautomessage.base.BasePresenter;
import com.qwert2603.vkautomessage.errors_show.ErrorsShowDialog;
import com.qwert2603.vkautomessage.login.MainActivity;
import com.qwert2603.vkautomessage.util.LogUtils;
import com.qwert2603.vkautomessage.avatar_view.RoundedTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

public abstract class NavigationFragment<P extends BasePresenter> extends BaseFragment<P> implements NavigationView {

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigation_view)
    android.support.design.widget.NavigationView mNavigationView;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @BindView(R.id.app_bar_layout)
    protected AppBarLayout mAppBarLayout;

    protected ImageView mToolbarIconImageView;

    @BindView(R.id.coordinator)
    CoordinatorLayout mCoordinatorLayout;

    private AvatarView mAvatarView;
    private TextView mMyselfNameTextView;

    @BindView(R.id.floating_action_mode)
    protected FloatingActionMode mFloatingActionMode;

    @ToolbarIconState
    private int mIconState;

    /**
     * Потому что Dagger не может инжектить в NavigationPresenter, который generic.
     */
    public static final class InjectionsHolder {
        @Inject
        RxBus mRxBus;

        @Inject
        NavigationPresenter mNavigationPresenter;

        InjectionsHolder() {
            VkAutoMessageApplication.getAppComponent().inject(NavigationFragment.InjectionsHolder.this);
        }
    }

    private InjectionsHolder mInjectionsHolder;

    private Subscription mRxBusSubscription = Subscriptions.unsubscribed();

    private Target mPicassoTarget;

    protected abstract boolean isNavigationButtonVisible();

    @LayoutRes
    protected abstract int getToolbarContentRes();

    @LayoutRes
    protected abstract int getScreenContentRes();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInjectionsHolder = new InjectionsHolder();
        mInjectionsHolder.mNavigationPresenter.bindView(NavigationFragment.this);

        mRxBusSubscription = mInjectionsHolder.mRxBus.toObservable()
                .filter(event -> event.mEvent == RxBus.Event.EVENT_MODE_SHOW_ERRORS_CHANGED)
                .subscribe(event -> {
                    if (event.mObject instanceof Boolean) {
                        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.show_errors);
                        menuItem.setVisible((Boolean) event.mObject);
                    }
                }, LogUtils::e);
    }

    @Override
    public void onDestroy() {
        mRxBusSubscription.unsubscribe();
        mInjectionsHolder.mNavigationPresenter.unbindView();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mInjectionsHolder.mNavigationPresenter.onViewReady();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mDrawerLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    NavigationFragment.this.onDrawerSlide(mNavigationView.getWidth(), 1.0f);
                    return true;
                }
            });
        }
    }

    @Override
    public void onPause() {
        mInjectionsHolder.mNavigationPresenter.onViewNotReady();
        super.onPause();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        inflater.inflate(getToolbarContentRes(), (ViewGroup) view.findViewById(R.id.toolbar), true);
        inflater.inflate(getScreenContentRes(), (ViewGroup) view.findViewById(R.id.coordinator), true);

        ButterKnife.bind(NavigationFragment.this, view);

        ((BaseActivity) getActivity()).setSupportActionBar(mToolbar);

        mNavigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            switch (item.getItemId()) {
                case R.id.log_out:
                    mInjectionsHolder.mNavigationPresenter.onLogOutClicked();
                    return true;
                case R.id.show_errors:
                    ErrorsShowDialog.newInstance().show(getFragmentManager(), "");
                    return true;
            }
            return false;
        });

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                NavigationFragment.this.onDrawerSlide(drawerView.getWidth(), slideOffset);
            }
        });

        mToolbar.setNavigationOnClickListener(v -> {
            if (mFloatingActionMode.getOpened()) {
                stopActionMode();
                return;
            }
            if (isNavigationButtonVisible()) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            } else {
                onBackPressed();
            }
        });

        View headerNavigationView = inflater.inflate(R.layout.header_navigation, null);
        mNavigationView.addHeaderView(headerNavigationView);

        mAvatarView = (AvatarView) headerNavigationView.findViewById(R.id.avatar_view);
        mPicassoTarget = new AvatarView.PicassoTarget(mAvatarView) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                Palette.from(bitmap).generate(palette -> headerNavigationView.setBackgroundColor(0x80_FF_FF_FF & palette.getDominantColor(Color.TRANSPARENT)));
            }
        };

        mMyselfNameTextView = (TextView) headerNavigationView.findViewById(R.id.user_name_text_view);

        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationIcon(R.drawable.toolbar_icon);

            int size = mToolbar.getChildCount();
            for (int i = 0; i < size; i++) {
                View child = mToolbar.getChildAt(i);
                if (child instanceof ImageButton) {
                    ImageButton btn = (ImageButton) child;
                    if (btn.getDrawable() == mToolbar.getNavigationIcon()) {
                        mToolbarIconImageView = btn;
                        break;
                    }
                }
            }

            if (isNavigationButtonVisible()) {
                setToolbarIconState(R.attr.state_burger, true);
            } else {
                setToolbarIconState(R.attr.state_back_arrow, true);
            }
        }

        if (mFloatingActionMode.getOpened()) {
            setToolbarIconState(R.attr.state_close, true);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Picasso.with(getActivity()).cancelRequest(mPicassoTarget);
    }

    @Override
    public void performLogOut() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showMyselfName(String userName) {
        mMyselfNameTextView.setText(userName);
    }

    @Override
    public void showMyselfPhoto(String url, String initials) {
        mAvatarView.showInitials(initials);
        Picasso.with(getActivity())
                .load(url)
                .transform(new RoundedTransformation())
                .into(mPicassoTarget);
    }

    @Override
    public void showLoadingMyself() {
        mMyselfNameTextView.setText(R.string.loading);
        mAvatarView.showInitials("");
    }

    protected void startActionMode(@LayoutRes int actionContentRes) {
        mFloatingActionMode.open();
        setToolbarIconState(R.attr.state_close, false);
    }

    protected void stopActionMode() {
        mFloatingActionMode.close();
        if (isNavigationButtonVisible()) {
            setToolbarIconState(R.attr.state_burger, false);
        } else {
            setToolbarIconState(R.attr.state_back_arrow, false);
        }

        onActionModeCancelling();
    }

    /**
     * Method-callback for derived classes.
     * Will be called, when action mode is stopping.
     */
    protected void onActionModeCancelling() {
    }

    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (mFloatingActionMode.getOpened()) {
            stopActionMode();
            return;
        }
        performBackPressed();
    }

    public void onMenuPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    protected void performBackPressed() {
        ((BaseActivity) getActivity()).performOnBackPressed();
    }

    protected void setToolbarIconState(@ToolbarIconState int state, boolean withJump) {
        mIconState = state;
        int[] newState = new int[ToolbarIconState.STATES.length];
        for (int i = 0; i < ToolbarIconState.STATES.length; i++) {
            if (state == ToolbarIconState.STATES[i]) {
                newState[i] = ToolbarIconState.STATES[i];
            } else {
                newState[i] = -1 * ToolbarIconState.STATES[i];
            }
        }
        mToolbarIconImageView.setImageState(newState, true);
        if (withJump) {
            mToolbarIconImageView.jumpDrawablesToCurrentState();
        }
    }

    @ToolbarIconState
    @SuppressWarnings("unused")
    protected int getIconState() {
        return mIconState;
    }

    private void onDrawerSlide(int width, float slideOffset) {
        mCoordinatorLayout.setTranslationX(width * slideOffset / 2);
    }
}
