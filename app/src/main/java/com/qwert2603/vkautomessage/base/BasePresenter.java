package com.qwert2603.vkautomessage.base;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Базовый презентер для шаблона MVP.
 *
 * @param <M> тип модели, с которой работает презентер.
 * @param <V> тип представления, которым управляет презентер.
 */
public abstract class BasePresenter<M, V extends BaseView> {

    private M mModel;
    private WeakReference<V> mView;

    /**
     * Готово ли представление отображать данные.
     */
    private boolean mIsViewReady = false;

    /**
     * Назначить модель.
     *
     * @param model объект модели.
     */
    protected void setModel(M model) {
        mModel = model;
        updateView();
    }

    /**
     * @return модель, с которой работает презентер.
     */
    protected M getModel() {
        return mModel;
    }

    /**
     * @return представление, которым управляет презентер.
     */
    protected V getView() {
        return mView == null ? null : mView.get();
    }

    /**
     * Привязать представление.
     *
     * @param view представление для привязки.
     */
    public void bindView(V view) {
        mView = new WeakReference<>(view);
//        VkAutoMessageApplication.sRefWatcher.watch(this);
    }

    /**
     * Отвязать представление.
     */
    public void unbindView() {
        if (mIsViewReady) {
            onViewNotReady();
        }
        mView = null;
    }

    /**
     * Уведомить о готовности представления отображать данные.
     */
    public void onViewReady() {
        mIsViewReady = true;
        updateView();
    }

    /**
     * Уведомить о том, что представление не готово отображать данные.
     */
    public void onViewNotReady() {
        mIsViewReady = false;
    }

    /**
     * Обновить представление.
     * Если представление привязано и готово к отображению, будет вызван метод {@link #onUpdateView(BaseView)}.
     */
    @MainThread
    protected final void updateView() {
        if (canUpdateView()) {
            onUpdateView(mView.get());
        }
    }

    /**
     * @return можно ли сейчас обновлять представление.
     */
    protected boolean canUpdateView() {
        return mView != null && mView.get() != null && mIsViewReady;
    }

    /**
     * Действия, выполняемые при обновлении всего представления.
     *
     * @param view представление, которым управляет презентер.
     */
    @MainThread
    protected abstract void onUpdateView(@NonNull V view);
}