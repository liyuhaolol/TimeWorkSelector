package spa.lyh.cn.time_work_selector;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import spa.lyh.cn.time_work_selector.utils.DateUtil;
import spa.lyh.cn.time_work_selector.utils.ScreenUtil;
import spa.lyh.cn.time_work_selector.utils.TextUtil;
import spa.lyh.cn.time_work_selector.view.PickerView;

/**
 * Created by liyuhao on 2017/7/25.
 */

public class TimeWorkSelector {
    public interface ResultHandler {
        void handle(String time,int ResId);
    }

    public enum SCROLLTYPE {

        HOUR(1),
        MINUTE(2);

        SCROLLTYPE(int value) {
            this.value = value;
        }

        public int value;

    }

    public enum MODE {

        YMD(1),
        YMDHM(2),
        YMDW(3),
        YMDHMW(4);

        MODE(int value) {
            this.value = value;
        }

        public int value;

    }


    private int scrollUnits = SCROLLTYPE.HOUR.value + SCROLLTYPE.MINUTE.value;
    private ResultHandler handler;
    private Context context;
    private final String FORMAT_STR_YMDHM = "yyyy-MM-dd HH:mm";
    private final String FORMAT_STR_YMD = "yyyy-MM-dd";
    private String RESULT_FORMAT_STR;
    private Dialog seletorDialog;
    private PickerView year_pv;
    private PickerView month_pv;
    private PickerView day_pv;
    private PickerView hour_pv;
    private PickerView minute_pv;
    private PickerView work_pv;

    private final int MAXMINUTE = 59;
    private int MAXHOUR = 23;
    private final int MINMINUTE = 0;
    private int MINHOUR = 0;
    private final int MAXMONTH = 12;

    private ArrayList<String> year, month, day, hour, minute,work;
    private int startYear, startMonth, startDay, startHour, startMininute, endYear, endMonth, endDay, endHour, endMininute, minute_workStart, minute_workEnd, hour_workStart, hour_workEnd;
    private boolean spanYear, spanMon, spanDay, spanHour, spanMin;
    private Calendar selectedCalender = Calendar.getInstance();
    private String workTime;
    private final long ANIMATORDELAY = 200L;
    private final long CHANGEDELAY = 90L;
    private String workStart_str;
    private String workEnd_str;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private TextView tv_cancel;
    private TextView tv_select, tv_title;
    private TextView hour_text;
    private TextView minute_text;

    private int ResId;

    private String year_content;

    private String month_content;

    private String day_content;

    private final String FORMAT_YEAR = "yyyy";
    private final String FORMAT_MONTH = "MM";
    private final String FORMAT_DAY= "dd";


    public TimeWorkSelector(Context context, String startDate, String endDate) {
        this.context = context;
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        startCalendar.setTime(DateUtil.parse(startDate, FORMAT_STR_YMDHM));
        endCalendar.setTime(DateUtil.parse(endDate, FORMAT_STR_YMDHM));
        work = new ArrayList<>();
        work.add(context.getString(R.string.timeselector_up_work));
        work.add(context.getString(R.string.timeselector_down_work));
        initDialog();
        initView();
    }

    public TimeWorkSelector(Context context, String startDate, String endDate, String workStartTime, String workEndTime) {
        this(context, startDate, endDate);
        this.workStart_str = workStartTime;
        this.workEnd_str = workEndTime;
    }

    public void show() {
        show(0);
    }

    public void show(int ResId) {
        this.ResId = ResId;
        if (startCalendar.getTime().getTime() >= endCalendar.getTime().getTime()) {
            Toast.makeText(context, "start>end", Toast.LENGTH_LONG).show();
            return;
        }

        if (!excuteWorkTime()){
            return;
        }
        initParameter();
        initTimer();
        addListener();
        seletorDialog.show();
    }

    public void setResultHander(ResultHandler resultHandler){
        this.handler = resultHandler;
    }

    private void initDialog() {
        if (seletorDialog == null) {
            seletorDialog = new Dialog(context, R.style.time_dialog);
            seletorDialog.setCancelable(true);
            seletorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            seletorDialog.setContentView(R.layout.dialog_selector);
            Window window = seletorDialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.dialogWindowAnim);
            WindowManager.LayoutParams lp = window.getAttributes();
            int width = ScreenUtil.getInstance(context).getScreenWidth();
            lp.width = width;
            window.setAttributes(lp);
        }
    }

    private void initView() {
        year_pv = (PickerView) seletorDialog.findViewById(R.id.year_pv);
        month_pv = (PickerView) seletorDialog.findViewById(R.id.month_pv);
        day_pv = (PickerView) seletorDialog.findViewById(R.id.day_pv);
        hour_pv = (PickerView) seletorDialog.findViewById(R.id.hour_pv);
        minute_pv = (PickerView) seletorDialog.findViewById(R.id.minute_pv);
        work_pv = (PickerView) seletorDialog.findViewById(R.id.work_pv);
        tv_cancel = (TextView) seletorDialog.findViewById(R.id.tv_cancel);
        tv_select = (TextView) seletorDialog.findViewById(R.id.tv_select);
        tv_title = (TextView) seletorDialog.findViewById(R.id.tv_title);
        hour_text = (TextView) seletorDialog.findViewById(R.id.hour_text);
        minute_text = (TextView) seletorDialog.findViewById(R.id.minute_text);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletorDialog.dismiss();
            }
        });
        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result;
                if (work_pv.getVisibility() == View.VISIBLE){
                    result = DateUtil.format(selectedCalender.getTime(), RESULT_FORMAT_STR)+" "+workTime;
                }else {
                    result = DateUtil.format(selectedCalender.getTime(), RESULT_FORMAT_STR);
                }
                if (handler != null){
                    handler.handle(result,ResId);
                }
                seletorDialog.dismiss();
            }
        });

    }

    private void initParameter() {
        startYear = startCalendar.get(Calendar.YEAR);
        startMonth = startCalendar.get(Calendar.MONTH) + 1;
        startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        startMininute = startCalendar.get(Calendar.MINUTE);
        endYear = endCalendar.get(Calendar.YEAR);
        endMonth = endCalendar.get(Calendar.MONTH) + 1;
        endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
        endMininute = endCalendar.get(Calendar.MINUTE);
        spanYear = startYear != endYear;
        spanMon = (!spanYear) && (startMonth != endMonth);
        spanDay = (!spanMon) && (startDay != endDay);
        spanHour = (!spanDay) && (startHour != endHour);
        spanMin = (!spanHour) && (startMininute != endMininute);
        selectedCalender.setTime(startCalendar.getTime());
        selectedCalender.add(Calendar.YEAR, +1);
        year_content = DateUtil.format(selectedCalender.getTime(), FORMAT_YEAR);
        month_content = DateUtil.format(selectedCalender.getTime(), FORMAT_MONTH);
        day_content = DateUtil.format(selectedCalender.getTime(), FORMAT_DAY);
    }

    private void initTimer() {
        initArrayList();

        if (spanYear) {
            for (int i = startYear; i <= endYear; i++) {
                year.add(String.valueOf(i));
            }
            for (int i = startMonth; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(startMininute));
            } else {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }

        } else if (spanMon) {
            year.add(String.valueOf(startYear));
            for (int i = startMonth; i <= endMonth; i++) {
                month.add(fomatTimeUnit(i));
            }
            for (int i = startDay; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(startMininute));
            } else {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
        } else if (spanDay) {
            year.add(String.valueOf(startYear));
            month.add(fomatTimeUnit(startMonth));
            for (int i = startDay; i <= endDay; i++) {
                day.add(fomatTimeUnit(i));
            }
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(startMininute));
            } else {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }

        } else if (spanHour) {
            year.add(String.valueOf(startYear));
            month.add(fomatTimeUnit(startMonth));
            day.add(fomatTimeUnit(startDay));

            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(startHour));
            } else {
                for (int i = startHour; i <= endHour; i++) {
                    hour.add(fomatTimeUnit(i));
                }

            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(startMininute));
            } else {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }


        } else if (spanMin) {
            year.add(String.valueOf(startYear));
            month.add(fomatTimeUnit(startMonth));
            day.add(fomatTimeUnit(startDay));
            hour.add(fomatTimeUnit(startHour));


            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(startMininute));
            } else {
                for (int i = startMininute; i <= endMininute; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
        }

        workTime = context.getString(R.string.timeselector_up_work);

        loadComponent();

    }

    private boolean excuteWorkTime() {
        boolean res = true;
        if (!TextUtil.isEmpty(workStart_str) && !TextUtil.isEmpty(workEnd_str)) {
            String[] start = workStart_str.split(":");
            String[] end = workEnd_str.split(":");
            hour_workStart = Integer.parseInt(start[0]);
            minute_workStart = Integer.parseInt(start[1]);
            hour_workEnd = Integer.parseInt(end[0]);
            minute_workEnd = Integer.parseInt(end[1]);
            Calendar workStartCalendar = Calendar.getInstance();
            Calendar workEndCalendar = Calendar.getInstance();
            workStartCalendar.setTime(startCalendar.getTime());
            workEndCalendar.setTime(endCalendar.getTime());
            workStartCalendar.set(Calendar.HOUR_OF_DAY, hour_workStart);
            workStartCalendar.set(Calendar.MINUTE, minute_workStart);
            workEndCalendar.set(Calendar.HOUR_OF_DAY, hour_workEnd);
            workEndCalendar.set(Calendar.MINUTE, minute_workEnd);


            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            Calendar startWorkTime = Calendar.getInstance();
            Calendar endWorkTime = Calendar.getInstance();

            startTime.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
            startTime.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
            endTime.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY));
            endTime.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE));

            startWorkTime.set(Calendar.HOUR_OF_DAY, workStartCalendar.get(Calendar.HOUR_OF_DAY));
            startWorkTime.set(Calendar.MINUTE, workStartCalendar.get(Calendar.MINUTE));
            endWorkTime.set(Calendar.HOUR_OF_DAY, workEndCalendar.get(Calendar.HOUR_OF_DAY));
            endWorkTime.set(Calendar.MINUTE, workEndCalendar.get(Calendar.MINUTE));


            if (startTime.getTime().getTime() == endTime.getTime().getTime() || (startWorkTime.getTime().getTime() < startTime.getTime().getTime() && endWorkTime.getTime().getTime() < startTime.getTime().getTime())) {
                Toast.makeText(context, "Wrong parames!", Toast.LENGTH_LONG).show();
                return false;
            }
            startCalendar.setTime(startCalendar.getTime().getTime() < workStartCalendar.getTime().getTime() ? workStartCalendar.getTime() : startCalendar.getTime());
            endCalendar.setTime(endCalendar.getTime().getTime() > workEndCalendar.getTime().getTime() ? workEndCalendar.getTime() : endCalendar.getTime());
            MINHOUR = workStartCalendar.get(Calendar.HOUR_OF_DAY);
            MAXHOUR = workEndCalendar.get(Calendar.HOUR_OF_DAY);

        }
        return res;


    }

    private String fomatTimeUnit(int unit) {
        return unit < 10 ? "0" + String.valueOf(unit) : String.valueOf(unit);
    }

    private void initArrayList() {
        if (year == null) year = new ArrayList<>();
        if (month == null) month = new ArrayList<>();
        if (day == null) day = new ArrayList<>();
        if (hour == null) hour = new ArrayList<>();
        if (minute == null) minute = new ArrayList<>();
        year.clear();
        month.clear();
        day.clear();
        hour.clear();
        minute.clear();
    }


    private void addListener() {
        year_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
                monthChange();


            }
        });
        month_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, 1);
                selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);
                dayChange();


            }
        });
        day_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                hourChange();

            }
        });
        hour_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
                minuteChange();


            }
        });
        minute_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                selectedCalender.set(Calendar.MINUTE, Integer.parseInt(text));


            }
        });
        work_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                workTime = text;
            }
        });

    }

    private void loadComponent() {
        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        hour_pv.setData(hour);
        minute_pv.setData(minute);
        year_pv.setSelected(year_content);
        month_pv.setSelected(month_content);
        day_pv.setSelected(day_content);
        hour_pv.setSelected(0);
        minute_pv.setSelected(0);
        ///
        work_pv.setData(work);
        work_pv.setSelected(0);
        excuteScroll();
    }

    //设置是否可以滑动
    private void excuteScroll() {
        year_pv.setCanScroll(year.size() > 1);
        month_pv.setCanScroll(month.size() > 1);
        day_pv.setCanScroll(day.size() > 1);
        hour_pv.setCanScroll(hour.size() > 1 && (scrollUnits & SCROLLTYPE.HOUR.value) == SCROLLTYPE.HOUR.value);
        minute_pv.setCanScroll(minute.size() > 1 && (scrollUnits & SCROLLTYPE.MINUTE.value) == SCROLLTYPE.MINUTE.value);
    }

    private void monthChange() {

        month.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        if (selectedYear == startYear) {
            for (int i = startMonth; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
        } else if (selectedYear == endYear) {
            for (int i = 1; i <= endMonth; i++) {
                month.add(fomatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
        }
        selectedCalender.set(Calendar.MONTH, Integer.parseInt(month.get(0)) - 1);
        month_pv.setData(month);
        month_pv.setSelected(0);
        excuteAnimator(ANIMATORDELAY, month_pv);

        month_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                dayChange();
            }
        }, CHANGEDELAY);

    }

    private void dayChange() {

        day.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        if (selectedYear == startYear && selectedMonth == startMonth) {
            for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
        } else if (selectedYear == endYear && selectedMonth == endMonth) {
            for (int i = 1; i <= endDay; i++) {
                day.add(fomatTimeUnit(i));
            }
        } else {
            for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
        }
        selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day.get(0)));
        day_pv.setData(day);
        day_pv.setSelected(0);
        excuteAnimator(ANIMATORDELAY, day_pv);

        day_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                hourChange();
            }
        }, CHANGEDELAY);
    }

    private void hourChange() {
        if ((scrollUnits & SCROLLTYPE.HOUR.value) == SCROLLTYPE.HOUR.value) {
            hour.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);

            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay) {
                for (int i = startHour; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay) {
                for (int i = MINHOUR; i <= endHour; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            } else {

                for (int i = MINHOUR; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }

            }
            selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour.get(0)));
            hour_pv.setData(hour);
            hour_pv.setSelected(0);
            excuteAnimator(ANIMATORDELAY, hour_pv);
        }
        hour_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                minuteChange();
            }
        }, CHANGEDELAY);

    }

    private void minuteChange() {
        if ((scrollUnits & SCROLLTYPE.MINUTE.value) == SCROLLTYPE.MINUTE.value) {
            minute.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);

            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay && selectedHour == startHour) {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay && selectedHour == endHour) {
                for (int i = MINMINUTE; i <= endMininute; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            } else if (selectedHour == hour_workStart) {
                for (int i = minute_workStart; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            } else if (selectedHour == hour_workEnd) {
                for (int i = MINMINUTE; i <= minute_workEnd; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            } else {
                for (int i = MINMINUTE; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
            selectedCalender.set(Calendar.MINUTE, Integer.parseInt(minute.get(0)));
            minute_pv.setData(minute);
            minute_pv.setSelected(0);
            excuteAnimator(ANIMATORDELAY, minute_pv);

        }
        excuteScroll();


    }

    private void excuteAnimator(long ANIMATORDELAY, View view) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f,
                0f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f,
                1.3f, 1f);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f,
                1.3f, 1f);
        ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY, pvhZ).setDuration(ANIMATORDELAY).start();
    }


    public void setNextBtTip(String str) {
        tv_select.setText(str);
    }

    public void setTitle(String str) {
        tv_title.setText(str);
    }

    public int disScrollUnit(SCROLLTYPE... scrolltypes) {
        if (scrolltypes == null || scrolltypes.length == 0)
            scrollUnits = SCROLLTYPE.HOUR.value + SCROLLTYPE.MINUTE.value;
        for (SCROLLTYPE scrolltype : scrolltypes) {
            scrollUnits ^= scrolltype.value;
        }
        return scrollUnits;
    }

    public void setMode(MODE mode) {
        switch (mode.value) {
            case 1:
                RESULT_FORMAT_STR = FORMAT_STR_YMD;
                setTextLevel(7f);
                disScrollUnit(SCROLLTYPE.HOUR, SCROLLTYPE.MINUTE);
                hour_pv.setVisibility(View.GONE);
                minute_pv.setVisibility(View.GONE);
                hour_text.setVisibility(View.GONE);
                minute_text.setVisibility(View.GONE);
                work_pv.setVisibility(View.GONE);
                break;
            case 2:
                RESULT_FORMAT_STR = FORMAT_STR_YMDHM;
                setTextLevel(7f);
                disScrollUnit();
                hour_pv.setVisibility(View.VISIBLE);
                minute_pv.setVisibility(View.VISIBLE);
                hour_text.setVisibility(View.VISIBLE);
                minute_text.setVisibility(View.VISIBLE);
                work_pv.setVisibility(View.GONE);
                break;
            case 3:
                RESULT_FORMAT_STR = FORMAT_STR_YMD;
                setTextLevel(7f);
                work_pv.setTextSizeLevel(7f);
                disScrollUnit(SCROLLTYPE.HOUR, SCROLLTYPE.MINUTE);
                hour_pv.setVisibility(View.GONE);
                minute_pv.setVisibility(View.GONE);
                hour_text.setVisibility(View.GONE);
                minute_text.setVisibility(View.GONE);
                work_pv.setVisibility(View.VISIBLE);
                break;
            case 4:
                RESULT_FORMAT_STR = FORMAT_STR_YMDHM;
                setTextLevel(10f);
                disScrollUnit();
                hour_pv.setVisibility(View.VISIBLE);
                minute_pv.setVisibility(View.VISIBLE);
                hour_text.setVisibility(View.VISIBLE);
                minute_text.setVisibility(View.VISIBLE);
                work_pv.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void setTextLevel(float no){
        year_pv.setTextSizeLevel(no);
        month_pv.setTextSizeLevel(no);
        day_pv.setTextSizeLevel(no);
        hour_pv.setTextSizeLevel(no);
        minute_pv.setTextSizeLevel(no);
        work_pv.setTextSizeLevel(no);
    }

    public void setIsLoop(boolean isLoop) {
        this.year_pv.setIsLoop(false);
        this.month_pv.setIsLoop(isLoop);
        this.day_pv.setIsLoop(isLoop);
        this.hour_pv.setIsLoop(isLoop);
        this.minute_pv.setIsLoop(isLoop);
        this.work_pv.setIsLoop(false);
    }
}
