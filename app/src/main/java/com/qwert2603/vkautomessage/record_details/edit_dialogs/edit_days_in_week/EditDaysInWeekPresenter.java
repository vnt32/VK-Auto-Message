package com.qwert2603.vkautomessage.record_details.edit_dialogs.edit_days_in_week;

import android.support.annotation.NonNull;

import com.qwert2603.vkautomessage.Const;
import com.qwert2603.vkautomessage.base.BasePresenter;

public class EditDaysInWeekPresenter extends BasePresenter<Integer, EditDaysInWeekView> {

    public EditDaysInWeekPresenter() {
    }

    public void setDaysInWeek(int daysInWeek) {
        setModel(daysInWeek);
    }

    @Override
    protected void onUpdateView(@NonNull EditDaysInWeekView view) {
    }

    public boolean[] getSelectedDaysInWeek() {
        Integer model = getModel();
        if (model == null) {
            return null;
        }
        boolean[] res = new boolean[Const.DAYS_PER_WEEK];
        for (int i = 1; i < Const.DAYS_PER_WEEK + 1; i++) {
            res[i - 1] = (model & (1 << i)) != 0;
        }
        return res;
    }

    public void onDayInWeekEnableChanged(int dayInWeek, boolean enable) {
        // Because {@link Calendar#SUNDAY} == 1.
        dayInWeek += 1;
        Integer model = getModel();
        int daysInWeek = model != null ? model : 0;
        if (((daysInWeek & (1 << dayInWeek)) != 0) == enable) {
            return;
        }
        if (enable) {
            daysInWeek |= 1 << dayInWeek;
        } else {
            daysInWeek &= ~(1 << dayInWeek);
        }
        setModel(daysInWeek);
    }

    void onSubmitClicked() {
        Integer model = getModel();
        if (model == null) {
            return;
        }
        getView().submitDone(model);
    }

}
