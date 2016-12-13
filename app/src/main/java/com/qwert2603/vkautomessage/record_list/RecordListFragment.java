package com.qwert2603.vkautomessage.record_list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.qwert2603.vkautomessage.R;
import com.qwert2603.vkautomessage.VkAutoMessageApplication;
import com.qwert2603.vkautomessage.base.BaseActivity;
import com.qwert2603.vkautomessage.base.BaseRecyclerViewAdapter;
import com.qwert2603.vkautomessage.base.list.ListFragment;
import com.qwert2603.vkautomessage.delete_record.DeleteRecordDialog;
import com.qwert2603.vkautomessage.model.Record;
import com.qwert2603.vkautomessage.record_details.RecordActivity;
import com.qwert2603.vkautomessage.recycler.RecyclerItemAnimator;
import com.qwert2603.vkautomessage.util.AndroidUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordListFragment extends ListFragment<Record> implements RecordListView {

    private static final String userIdKey = "userId";
    private static final String drawingStartYKey = "drawingStartY";

    public static RecordListFragment newInstance(int userId, int drawingStartY) {
        RecordListFragment recordListFragment = new RecordListFragment();
        Bundle args = new Bundle();
        args.putInt(userIdKey, userId);
        args.putInt(drawingStartYKey, drawingStartY);
        recordListFragment.setArguments(args);
        return recordListFragment;
    }

    @BindView(R.id.new_record_fab)
    FloatingActionButton mNewRecordFAB;

    @Inject
    RecordListPresenter mRecordListPresenter;

    @Inject
    RecordListAdapter mRecordListAdapter;

    @NonNull
    @Override
    protected RecordListPresenter getPresenter() {
        return mRecordListPresenter;
    }

    @NonNull
    @Override
    protected BaseRecyclerViewAdapter<Record, ?, ?> getAdapter() {
        return mRecordListAdapter;
    }

    @Override
    protected int getToolbarContentRes() {
        return R.layout.toolbar_title;
    }

    @Override
    protected int getScreenContentRes() {
        return R.layout.fragment_record_list;
    }

    @Override
    protected boolean isNavigationButtonVisible() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        VkAutoMessageApplication.getAppComponent().inject(RecordListFragment.this);
        mRecordListPresenter.setUserId(getArguments().getInt(userIdKey));
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(RecordListFragment.this, view);

        // TODO: 03.12.2016 toolbar должен состоять из отдельных:
        // * круглая ава пользователя-получателя
        // * кол-во записей (14/26) -- todo AnimatedIntegerView
        // * имя друга (android:ellipsize="marquee")
        // каждая часть должна иметь свое transitionName

        // TODO: 12.12.2016 фильтрация активных и неактивных записей

        // TODO: 13.12.2016 в альбомной ориентации -- 2 столбца

        mContentRootView.setPivotY(getArguments().getInt(drawingStartYKey));

        mNewRecordFAB.setOnClickListener(v -> mRecordListPresenter.onNewRecordClicked());

        mRecyclerItemAnimator.setEnterOrigin(RecyclerItemAnimator.EnterOrigin.LEFT);

        mRecordListAdapter.setRecordEnableChangedCallback((position, enabled) -> mRecordListPresenter.onRecordEnableChanged(position, enabled));

        return view;
    }

    @Override
    public void showUserName(String userName) {
        mToolbarTitleTextView.setText(userName);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void moveToDetailsForItem(int id, boolean withSetPressed) {
        ActivityOptions activityOptions = null;
        RecordListAdapter.RecordViewHolder viewHolder =
                (RecordListAdapter.RecordViewHolder) mRecyclerView.findViewHolderForItemId(id);
        if (viewHolder != null && AndroidUtils.isLollipopOrHigher()) {
            TextView messageTextView = viewHolder.mMessageTextView;
            TextView timeTextView = viewHolder.mTimeTextView;
            TextView periodTextView = viewHolder.mRepeatInfoTextView;
            CheckBox enableCheckBox = viewHolder.mEnableCheckBox;
            activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    Pair.create(messageTextView, messageTextView.getTransitionName()),
                    Pair.create(timeTextView, timeTextView.getTransitionName()),
                    Pair.create(periodTextView, periodTextView.getTransitionName()),
                    Pair.create(enableCheckBox, enableCheckBox.getTransitionName()),
                    Pair.create(mToolbarTitleTextView, mToolbarTitleTextView.getTransitionName()));
        }
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        intent.putExtra(RecordActivity.EXTRA_ITEM_ID, id);

        if (viewHolder != null) {
            viewHolder.itemView.setPressed(withSetPressed);

            int[] startingPoint = new int[2];
            viewHolder.itemView.getLocationOnScreen(startingPoint);
            startingPoint[0] += viewHolder.itemView.getWidth() / 2;
            startingPoint[1] -= mToolbar.getHeight();
            intent.putExtra(RecordActivity.EXTRA_DRAWING_START_X, startingPoint[0]);
            intent.putExtra(RecordActivity.EXTRA_DRAWING_START_Y, startingPoint[1]);
        }

        startActivityForResult(intent, REQUEST_DETAILS_FOT_ITEM, activityOptions != null ? activityOptions.toBundle() : null);

        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void moveToDetailsForItem(Record item, boolean withSetPressed) {
        ActivityOptions activityOptions = null;
        RecordListAdapter.RecordViewHolder viewHolder =
                (RecordListAdapter.RecordViewHolder) mRecyclerView.findViewHolderForItemId(item.getId());
        if (viewHolder != null && AndroidUtils.isLollipopOrHigher()) {
            TextView messageTextView = viewHolder.mMessageTextView;
            TextView timeTextView = viewHolder.mTimeTextView;
            TextView periodTextView = viewHolder.mRepeatInfoTextView;
            CheckBox enableCheckBox = viewHolder.mEnableCheckBox;
            activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                    Pair.create(messageTextView, messageTextView.getTransitionName()),
                    Pair.create(timeTextView, timeTextView.getTransitionName()),
                    Pair.create(periodTextView, periodTextView.getTransitionName()),
                    Pair.create(enableCheckBox, enableCheckBox.getTransitionName()),
                    Pair.create(mToolbarTitleTextView, mToolbarTitleTextView.getTransitionName()));
        }
        Intent intent = new Intent(getActivity(), RecordActivity.class);
        intent.putExtra(RecordActivity.EXTRA_ITEM, item);

        if (viewHolder != null) {
            viewHolder.itemView.setPressed(withSetPressed);

            int[] startingPoint = new int[2];
            viewHolder.itemView.getLocationOnScreen(startingPoint);
            startingPoint[0] += viewHolder.itemView.getWidth() / 2;
            startingPoint[1] -= mToolbar.getHeight();
            intent.putExtra(RecordActivity.EXTRA_DRAWING_START_X, startingPoint[0]);
            intent.putExtra(RecordActivity.EXTRA_DRAWING_START_Y, startingPoint[1]);
        }

        startActivityForResult(intent, REQUEST_DETAILS_FOT_ITEM, activityOptions != null ? activityOptions.toBundle() : null);

        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void askDeleteItem(int recordId) {
        DeleteRecordDialog deleteRecordDialog = DeleteRecordDialog.newInstance(recordId);
        deleteRecordDialog.setTargetFragment(RecordListFragment.this, REQUEST_DELETE_ITEM);
        deleteRecordDialog.show(getFragmentManager(), deleteRecordDialog.getClass().getName());
    }

    @Override
    public void scrollListToTop() {
        super.scrollListToTop();
        ObjectAnimator.ofFloat(mNewRecordFAB, "translationX", 0).start();
    }

    @Override
    public void showDontWriteToDeveloper() {
        Toast.makeText(getActivity(), R.string.toast_i_told_you, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Animator createEnterAnimator() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mContentRootView, "scaleY", 0.1f, 1);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                int toolbarIconLeftMargin = ((ViewGroup.MarginLayoutParams) mToolbarIconImageView.getLayoutParams()).leftMargin;
                mToolbarIconImageView.setTranslationX(-1 * (mToolbarIconImageView.getWidth() + toolbarIconLeftMargin));

                if (!AndroidUtils.isLollipopOrHigher()) {
                    int toolbarTitleRightMargin = ((ViewGroup.MarginLayoutParams) mToolbarTitleTextView.getLayoutParams()).rightMargin;
                    mToolbarTitleTextView.setTranslationX(mToolbarTitleTextView.getWidth() + toolbarTitleRightMargin);
                }

                int fabRightMargin = ((ViewGroup.MarginLayoutParams) mNewRecordFAB.getLayoutParams()).rightMargin;
                mNewRecordFAB.setTranslationX(mNewRecordFAB.getWidth() + fabRightMargin);

                mViewAnimator.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mViewAnimator.setVisibility(View.VISIBLE);
            }
        });
        return objectAnimator;
    }

    @Override
    protected Animator createExitAnimator() {
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mContentRootView, "scaleY", 0);
        scaleY.setDuration(300);
        scaleY.setInterpolator(new AccelerateInterpolator());
        scaleY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mNewRecordFAB.setVisibility(View.INVISIBLE);
            }
        });

        ObjectAnimator alpha = ObjectAnimator.ofFloat(mContentRootView, "alpha", 1, 0);
        alpha.setDuration(100);
        alpha.setStartDelay(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleY).with(alpha);
        return animatorSet;
    }

    @Override
    protected Animator createInAnimator(boolean withLargeDelay) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mToolbarIconImageView, "translationX", 0);
        objectAnimator.setStartDelay(withLargeDelay ? 300 : 50);
        objectAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();

        if (!AndroidUtils.isLollipopOrHigher()) {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mToolbarTitleTextView, "translationX", 0);
            objectAnimator1.setStartDelay(withLargeDelay ? 300 : 100);
            objectAnimator1.setDuration(400);
            animatorSet.play(objectAnimator1).with(objectAnimator);
        } else {
            animatorSet.play(objectAnimator);
        }

        return animatorSet;
    }

    @Override
    protected Animator createOutAnimator() {
        int toolbarIconLeftMargin = ((ViewGroup.MarginLayoutParams) mToolbarIconImageView.getLayoutParams()).leftMargin;
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mToolbarIconImageView, "translationX", -1.5f * (mToolbarIconImageView.getWidth() + toolbarIconLeftMargin));
        objectAnimator.setDuration(300);

        int fabRightMargin = ((ViewGroup.MarginLayoutParams) mNewRecordFAB.getLayoutParams()).rightMargin;
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mNewRecordFAB, "translationX", mNewRecordFAB.getWidth() + fabRightMargin);
        objectAnimator2.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();

        if (!AndroidUtils.isLollipopOrHigher()) {
            int toolbarTitleRightMargin = ((ViewGroup.MarginLayoutParams) mToolbarTitleTextView.getLayoutParams()).rightMargin;
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mToolbarTitleTextView, "translationX", mToolbarTitleTextView.getWidth() + toolbarTitleRightMargin);
            objectAnimator1.setDuration(300);
            animatorSet.play(objectAnimator).with(objectAnimator1).with(objectAnimator2);
        } else {
            animatorSet.play(objectAnimator).with(objectAnimator2);
        }

        return animatorSet;
    }

    @Override
    public void animateInNewItemButton(int delay) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mNewRecordFAB, "translationX", 0);
        objectAnimator.setStartDelay(delay);
        objectAnimator.setDuration(300);
        objectAnimator.start();
    }

    @Override
    public void performBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(BaseActivity.EXTRA_ITEM_ID, getArguments().getInt(userIdKey));
        getActivity().setResult(Activity.RESULT_OK, intent);
        super.performBackPressed();
    }
}
