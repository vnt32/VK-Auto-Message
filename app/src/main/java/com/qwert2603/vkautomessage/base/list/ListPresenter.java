package com.qwert2603.vkautomessage.base.list;

import android.support.annotation.NonNull;

import com.qwert2603.vkautomessage.base.in_out_animation.AnimationPresenter;
import com.qwert2603.vkautomessage.base.in_out_animation.ShouldCheckIsInningOrInside;
import com.qwert2603.vkautomessage.model.Identifiable;
import com.qwert2603.vkautomessage.recycler.RecyclerItemAnimator;

import java.util.List;

/**
 * Презентер для view списка с поддержкой in/out анимации и появление списка.
 * <p>
 * По умолчанию:
 * - при нажатии на элемент происходит out-анимация и последующий переход к подробностям об этом элементе.
 * <p>
 * - при долгом нажатии и свайпе запрашивается подтверждение на удаление.
 * после запроса ListView должно вызвать {@link #onItemDeleteSubmitted(int)} или {@link #onItemDeleteCanceled(int)}
 * элемент для удаления выделяется пока не будет получен результат запроса на удаление.
 *
 * @param <T> тип элемента списка
 * @param <M> тип модели
 * @param <V> тип представления
 */
public abstract class ListPresenter<T extends Identifiable, M, V extends ListView<T>> extends AnimationPresenter<M, V> {

    private enum AnimationState {
        WAITING_FOR_TRIGGER,
        SHOULD_START,
        STARTED
    }

    private AnimationState mListEnterAnimationState = AnimationState.WAITING_FOR_TRIGGER;

    protected abstract List<T> getList();

    protected abstract boolean isError();

    protected abstract void doLoadList();

    protected abstract void doLoadItem(int id);

    @Override
    protected void onUpdateView(@NonNull V view) {
        if (getModel() == null) {
            if (isError()) {
                view.showError();
            } else {
                view.showLoading();
            }
        } else {
            if (mListEnterAnimationState != AnimationState.WAITING_FOR_TRIGGER) {
                List<T> list = getList();
                if (mListEnterAnimationState == AnimationState.SHOULD_START) {
                    mListEnterAnimationState = AnimationState.STARTED;
                    if (list == null || list.isEmpty()) {
                        view.showEmpty();
                        view.animateInNewItemButton(0);
                    } else {
                        view.animateAllItemsEnter(true);
                        view.delayEachItemEnterAnimation(true);
                        view.showListEnter(list);

                        int delay = (int) (0.75 * Math.min(RecyclerItemAnimator.MAX_ENTER_DURATION, list.size() * RecyclerItemAnimator.ENTER_EACH_ITEM_DELAY));
                        view.animateInNewItemButton(delay);
                    }
                } else {
                    if (list == null || list.isEmpty()) {
                        view.showEmpty();
                    } else {
                        view.showList(list);
                    }
                }
            }
        }
    }

    @Override
    public void onReadyToAnimate() {
        if (isOutside()) {
            getView().animateInNewItemButton(50);
        }
        super.onReadyToAnimate();
    }

    @Override
    public void onAnimateInFinished() {
        super.onAnimateInFinished();
        if (mListEnterAnimationState == AnimationState.WAITING_FOR_TRIGGER) {
            mListEnterAnimationState = AnimationState.SHOULD_START;
            updateView();
        }
    }

    @Override
    public void onAnimateOutFinished(int id) {
        super.onAnimateOutFinished(id);
        if (id != AnimationPresenter.ON_BACK_PRESSED_ANIMATE_OUT_ID) {
            getView().moveToDetailsForItem(id);
        }
    }

    @ShouldCheckIsInningOrInside
    public void onItemAtPositionClicked(int position) {
        if (!isInningOrInside()) {
            return;
        }
        List<T> list = getList();
        if (list == null) {
            return;
        }
        getView().scrollToPosition(position);
        animateOut(list.get(position).getId());
    }

    @ShouldCheckIsInningOrInside
    public void onItemAtPositionLongClicked(int position) {
        // TODO: 29.11.2016 начинать множественное выделение на longClick (чтобы удалять сразу несколько потом)
        if (!isInningOrInside()) {
            return;
        }
        askDeleteItem(position);
    }

    @ShouldCheckIsInningOrInside
    public void onItemDismissed(int position) {
        if (!isInningOrInside()) {
            return;
        }
        askDeleteItem(position);
    }

    @ShouldCheckIsInningOrInside
    public void onItemDeleteSubmitted(int id) {
        getView().showItemSelected(-1);
    }

    public void onItemDeleteCanceled(int id) {
        getView().showItemSelected(-1);
    }

    public final void onReloadList() {
        doLoadList();
        updateView();
    }

    public final void onReloadItem(int id) {
        doLoadItem(id);
        updateView();
    }

    @ShouldCheckIsInningOrInside
    public void onToolbarClicked() {
        if (!isInningOrInside()) {
            return;
        }
        getView().scrollListToTop();
    }

    private void askDeleteItem(int position) {
        List<T> list = getList();
        if (list == null) {
            return;
        }
        getView().askDeleteItem(list.get(position).getId());
        getView().showItemSelected(position);
    }

}