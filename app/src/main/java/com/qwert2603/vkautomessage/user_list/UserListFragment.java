package com.qwert2603.vkautomessage.user_list;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qwert2603.vkautomessage.R;
import com.qwert2603.vkautomessage.VkAutoMessageApplication;
import com.qwert2603.vkautomessage.base.BaseRecyclerViewAdapter;
import com.qwert2603.vkautomessage.base.list.ListFragment;
import com.qwert2603.vkautomessage.choose_user.ChooseUserDialog;
import com.qwert2603.vkautomessage.delete_user.DeleteUserDialog;
import com.qwert2603.vkautomessage.model.User;
import com.qwert2603.vkautomessage.record_list.RecordListActivity;
import com.qwert2603.vkautomessage.recycler.RecyclerItemAnimator;
import com.qwert2603.vkautomessage.util.AndroidUtils;
import com.qwert2603.vkautomessage.util.LogUtils;
import com.qwert2603.vkautomessage.util.TransitionUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListFragment extends ListFragment<User> implements UserListView {

    public static UserListFragment newInstance() {
        return new UserListFragment();
    }

    private static final int REQUEST_CHOOSE_USER = 3;

    @BindView(R.id.toolbar_title_text_view)
    protected TextView mToolbarTitleTextView;

    @BindView(R.id.choose_user_fab)
    FloatingActionButton mChooseUserFAB;

    @Inject
    UserListPresenter mUserListPresenter;

    @Inject
    UserListAdapter mUserListAdapter;

    private Slide mSlideList;

    private Slide mSlideToolbar;

    private boolean mEnterAnimationPlayed = false;

    @NonNull
    @Override
    protected UserListPresenter getPresenter() {
        return mUserListPresenter;
    }

    @NonNull
    @Override
    protected BaseRecyclerViewAdapter<User, ?, ?> getAdapter() {
        return mUserListAdapter;
    }

    @Override
    protected int getToolbarContentRes() {
        return R.layout.toolbar_title;
    }

    @Override
    protected int getScreenContentRes() {
        return R.layout.fragment_user_list;
    }

    @Override
    protected boolean isNavigationButtonVisible() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        VkAutoMessageApplication.getAppComponent().inject(UserListFragment.this);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(UserListFragment.this, view);

        mChooseUserFAB.setOnClickListener(v -> mUserListPresenter.onChooseUserClicked());

        // TODO: 26.12.2016 in action mode ask user delete users and records or records only

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.user_list_divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerItemAnimator.setEnterOrigin(RecyclerItemAnimator.EnterOrigin.BOTTOM);
        mRecyclerItemAnimator.setAddDuration(600);
        mRecyclerItemAnimator.setMinEnterDelay(600);
        mSimpleOnItemTouchHelperCallback.setBackColor(getResources().getColor(R.color.swipe_back_user_list));

        TransitionUtils.setSharedElementTransitions(getActivity(), R.transition.shared_element);

        mSlideList = new Slide(Gravity.BOTTOM);
        mSlideList.addTarget(mChooseUserFAB);
        mSlideList.addListener(new TransitionUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                mRecyclerView.invalidateItemDecorations();
            }
        });

        mSlideToolbar = new Slide(Gravity.TOP);
        mSlideToolbar.addTarget(mToolbarTitleTextView);
        mSlideToolbar.addTarget(mToolbarIconImageView);

        int duration = getResources().getInteger(R.integer.transition_duration);
        TransitionSet transitionSet = new TransitionSet()
                .addTransition(mSlideToolbar)
                .addTransition(mSlideList)
                .setDuration(duration);

        getActivity().getWindow().setExitTransition(transitionSet);
        getActivity().getWindow().setEnterTransition(transitionSet);
        getActivity().getWindow().setReenterTransition(transitionSet);
        getActivity().getWindow().setReturnTransition(transitionSet);

        if (!mEnterAnimationPlayed) {
            mToolbarIconImageView.setVisibility(View.INVISIBLE);
            mToolbarTitleTextView.setVisibility(View.INVISIBLE);
            mChooseUserFAB.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHOOSE_USER:
                if (resultCode == Activity.RESULT_OK) {
                    int userId = data.getIntExtra(ChooseUserDialog.EXTRA_SELECTED_USER_ID, 0);
                    mUserListPresenter.onUserChosen(userId);
                } else {
                    mUserListPresenter.onUserChosen(-1);
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.user_list, menu);
        menu.findItem(R.id.sort).setActionView(R.layout.menu_item_sort);

        View actionView = menu.findItem(R.id.sort).getActionView();
        mSlideToolbar.addTarget(actionView);
        actionView.setOnClickListener(v -> {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sort_user_list, null);
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.sort_radio_group);
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.sort_default:
                        mUserListPresenter.onSortStateChanged(UserListPresenter.SortState.DEFAULT);
                        break;
                    case R.id.sort_first_name:
                        mUserListPresenter.onSortStateChanged(UserListPresenter.SortState.FIRST_NAME);
                        break;
                    case R.id.sort_last_name:
                        mUserListPresenter.onSortStateChanged(UserListPresenter.SortState.LAST_NAME);
                        break;
                    case R.id.sort_records_count:
                        mUserListPresenter.onSortStateChanged(UserListPresenter.SortState.RECORDS_COUNT);
                        break;
                    case R.id.sort_enabled_records_count:
                        mUserListPresenter.onSortStateChanged(UserListPresenter.SortState.ENABLED_RECORDS_COUNT);
                        break;
                }
            });
            UserListPresenter.SortState sortState = mUserListPresenter.getSortState();
            switch (sortState) {
                case DEFAULT:
                    radioGroup.check(R.id.sort_default);
                    break;
                case FIRST_NAME:
                    radioGroup.check(R.id.sort_first_name);
                    break;
                case LAST_NAME:
                    radioGroup.check(R.id.sort_last_name);
                    break;
                case RECORDS_COUNT:
                    radioGroup.check(R.id.sort_records_count);
                    break;
                case ENABLED_RECORDS_COUNT:
                    radioGroup.check(R.id.sort_enabled_records_count);
                    break;
            }
            PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setElevation(8.0f);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            popupWindow.showAsDropDown(actionView);
        });

        if (!mEnterAnimationPlayed) {
            mEnterAnimationPlayed = true;
            AndroidUtils.setActionOnPreDraw(actionView, () -> {
                mToolbarIconImageView.setTranslationY(-mToolbar.getHeight() * 1.5f);
                mToolbarIconImageView.setVisibility(View.VISIBLE);
                mToolbarIconImageView.animate().setStartDelay(300).setDuration(400).translationY(0);

                mToolbarTitleTextView.setTranslationY(-mToolbar.getHeight() * 1.5f);
                mToolbarTitleTextView.setVisibility(View.VISIBLE);
                mToolbarTitleTextView.animate().setStartDelay(400).setDuration(400).translationY(0);

                actionView.setTranslationY(-mToolbar.getHeight() * 1.5f);
                actionView.animate().setStartDelay(500).setDuration(400).translationY(0);

                mChooseUserFAB.setTranslationY(mContentRootView.getHeight() - mChooseUserFAB.getTop());
                mChooseUserFAB.setVisibility(View.VISIBLE);
                mChooseUserFAB.animate().setStartDelay(1200).setDuration(400).translationY(0);
            });
        }
    }

    @Override
    public void moveToDetailsForItem(int itemId, boolean newItem, int itemPosition) {
        mRecyclerItemAnimator.setEnterOrigin(RecyclerItemAnimator.EnterOrigin.LEFT);
        mRecyclerItemAnimator.setAddDuration(500);
        super.moveToDetailsForItem(itemId, newItem, itemPosition);
    }

    @Override
    protected void moveToDetailsForItem(int itemId) {
        LogUtils.d("moveToDetailsForItem " + itemId);
        prepareRecyclerViewForTransition();

        mSlideList.getTargets().clear();
        mSlideList.addTarget(mChooseUserFAB);
        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
            mSlideList.addTarget(mRecyclerView.getChildAt(i));
        }

        ActivityOptions activityOptions = null;
        UserListAdapter.UserViewHolder viewHolder =
                (UserListAdapter.UserViewHolder) mRecyclerView.findViewHolderForItemId(itemId);

        if (viewHolder != null) {
            activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    Pair.create(viewHolder.mAvatarView, viewHolder.mAvatarView.getTransitionName())
            );
        }
        Intent intent = new Intent(getActivity(), RecordListActivity.class);
        intent.putExtra(RecordListActivity.EXTRA_ITEM_ID, itemId);

        startActivityForResult(intent, REQUEST_DETAILS_FOT_ITEM, activityOptions != null ? activityOptions.toBundle() : null);
    }

    @Override
    public void showChooseUser() {
        // TODO: 22.12.2016  animate dialog appear/disappear (circular reveal)
        ChooseUserDialog userListDialog = ChooseUserDialog.newInstance();
        userListDialog.setTargetFragment(UserListFragment.this, REQUEST_CHOOSE_USER);
        userListDialog.show(getFragmentManager(), userListDialog.getClass().getName());
    }

    @Override
    public void askDeleteItem(int id) {
        DeleteUserDialog deleteUserDialog = DeleteUserDialog.newInstance(id);
        deleteUserDialog.setTargetFragment(UserListFragment.this, REQUEST_DELETE_ITEM);
        deleteUserDialog.show(getFragmentManager(), deleteUserDialog.getClass().getName());
    }

    @Override
    public void scrollToTop() {
        super.scrollToTop();
        mChooseUserFAB.animate().translationY(0);
    }
}
