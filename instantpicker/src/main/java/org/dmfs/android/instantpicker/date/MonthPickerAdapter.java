/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dmfs.android.instantpicker.date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import org.dmfs.android.instantpicker.PickerContext;
import org.dmfs.rfc5545.DateTime;

import java.util.HashMap;


/**
 * An adapter for a list of {@link MonthView} items.
 */
public abstract class MonthPickerAdapter extends BaseAdapter implements MonthView.OnDayClickListener
{

    private static final String TAG = "SimpleMonthAdapter";


    public interface OnDateClickListener
    {
        void onDateClick(DateTime dateTime);
    }


    private final Context mContext;
    protected final PickerContext mPickerContext;

    private DateTime mSelectedDay;

    protected static int WEEK_7_OVERHANG_HEIGHT = 7;
    protected static final int MONTHS_IN_YEAR = 12;

    private OnDateClickListener mOnDateClickListener;


    public MonthPickerAdapter(Context context, PickerContext pickerContext)
    {
        mContext = context;
        mPickerContext = pickerContext;
        init();
        setSelectedDay(DateTime.nowAndHere());
    }


    /**
     * Updates the selected day and related parameters.
     *
     * @param day
     *         The day to highlight
     */
    public void setSelectedDay(DateTime day)
    {
        mSelectedDay = day;
        notifyDataSetChanged();
    }


    public DateTime getSelectedDay()
    {
        return mSelectedDay;
    }


    /**
     * Set up the gesture detector and selected time
     */
    protected void init()
    {
        mSelectedDay = DateTime.nowAndHere();
    }


    @Override
    public int getCount()
    {
        return ((mPickerContext.maxDateTime().getYear() - mPickerContext.minDateTime().getYear()) + 1) * MONTHS_IN_YEAR;
    }


    @Override
    public Object getItem(int position)
    {
        return null;
    }


    @Override
    public long getItemId(int position)
    {
        return position;
    }


    @Override
    public boolean hasStableIds()
    {
        return true;
    }


    public void setOnDayClickListener(OnDateClickListener onDateClickListener)
    {
        this.mOnDateClickListener = onDateClickListener;
    }


    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        MonthView v;
        HashMap<String, Integer> drawingParams = null;
        if (convertView != null)
        {
            v = (MonthView) convertView;
            // We store the drawing parameters in the view so it can be recycled
            drawingParams = (HashMap<String, Integer>) v.getTag();
        }
        else
        {
            v = createMonthView(mContext);
            // Set up the new view
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            v.setLayoutParams(params);
            v.setClickable(true);
            v.setOnDayClickListener(this);
        }
        if (drawingParams == null)
        {
            drawingParams = new HashMap<String, Integer>();
        }
        drawingParams.clear();

        final int month = position % MONTHS_IN_YEAR;
        final int year = position / MONTHS_IN_YEAR + mPickerContext.minDateTime().getYear();

        int selectedDay = -1;
        if (isSelectedDayInMonth(year, month))
        {
            selectedDay = mSelectedDay.getDayOfMonth();
        }

        // Invokes requestLayout() to ensure that the recycled view is set with the appropriate
        // height/number of weeks before being displayed.
        v.reuse();

        drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(MonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(MonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mPickerContext.firstDayOfWeek().ordinal());
        v.setMonthParams(drawingParams);
        v.invalidate();
        return v;
    }


    public abstract MonthView createMonthView(Context context);


    private boolean isSelectedDayInMonth(int year, int month)
    {
        return mSelectedDay.getYear() == year && mSelectedDay.getMonth() == month;
    }


    @Override
    public void onDayClick(MonthView view, DateTime day)
    {
        if (day != null)
        {
            onDayTapped(day);
        }
    }


    /**
     * Maintains the same hour/min/sec but moves the day to the tapped day.
     *
     * @param day
     *         The day that was tapped
     */
    protected void onDayTapped(DateTime day)
    {
        // mPickerContext.tryVibrate();
        // mPickerContext.onDayOfMonthSelected(day);
        if (mOnDateClickListener != null)
        {
            if (!mSelectedDay.isAllDay())
            {
                day = new DateTime(mSelectedDay.getCalendarMetrics(), mSelectedDay.getTimeZone(), day.getYear(), day.getMonth(), day.getDayOfMonth(),
                        mSelectedDay.getHours(), mSelectedDay.getMinutes(), mSelectedDay.getSeconds());
            }
            mOnDateClickListener.onDateClick(day);
        }
        setSelectedDay(day);
    }
}