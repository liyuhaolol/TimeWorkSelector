package spa.lyh.cn.time_work_selector;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
    private ResultHandler resultHandler;
    private ResultHandler cacelHandler;
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
    private int startYear, startMonth, startDay, startHour, startMininute, endYear, endMonth, endDay, endHour, endMininute;
    private int todayYear, todayMonth, todayDay,todayHour, todayMininute, minute_workStart, minute_workEnd, hour_workStart, hour_workEnd;
    private boolean spanYear, spanMon, spanDay, spanHour, spanMin;
    private Calendar selectedCalender = Calendar.getInstance();
    private String workTime;
    private final long ANIMATORDELAY = 200L;
    private final long CHANGEDELAY = 90L;
    private String workStart_str;
    private String workEnd_str;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private Calendar todayCalendar;
    private TextView tv_cancel;
    private TextView tv_select, tv_title;
    private TextView hour_text;
    private TextView minute_text;

    private int ResId;

    private String year_content;

    private String month_content;

    private String day_content;

    private String hour_content;

    private String mininute_content;

    private final String FORMAT_YEAR = "yyyy";
    private final String FORMAT_MONTH = "MM";
    private final String FORMAT_DAY= "dd";
    private final String FORMAT_HOUR= "HH";
    private final String FORMAT_MIN= "mm";

    //默认显示当日
    public final static int SHOW_TODAY = 1000;
    //默认显示开始日期
    public final static int SHOW_START_DAY = 1001;

    private int showStatus;


    public TimeWorkSelector(Context context, String startDate, String endDate, int showStatus) {
        this.todayCalendar = Calendar.getInstance();
        this.context = context;
        this.showStatus = showStatus;
        this.minute_workStart = -1;
        this.minute_workEnd = 60;
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

    //可以设置朝九晚五
    public TimeWorkSelector(Context context, String startDate, String endDate, String workStartTime, String workEndTime,int showStatus) {
        this(context, startDate, endDate,showStatus);
        this.workStart_str = workStartTime;
        this.workEnd_str = workEndTime;
    }

    public TimeWorkSelector(Context context, String startDate, String endDate,int color,int showStatus) {
        GlobelData.myColor = color;
        this.todayCalendar = Calendar.getInstance();
        this.context = context;
        this.showStatus = showStatus;
        this.minute_workStart = -1;
        this.minute_workEnd = 60;
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
    public void show(String time) {
        show(0,time);
    }

    public void show(int ResId) {
        show(ResId,"");
    }

    public void show() {
        show(0,"");
    }

    public void show(int ResId,String time) {
        this.ResId = ResId;
        if(TextUtil.isEmpty(time)){
            todayCalendar.setTime(new Date());
        }else {
            try {
                SimpleDateFormat sdf=new SimpleDateFormat(FORMAT_STR_YMD);
                Date date = sdf.parse(time);
                todayCalendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
                todayCalendar.setTime(new Date());
            }

        }

        if (startCalendar.getTime().getTime() >= endCalendar.getTime().getTime()) {
            Toast.makeText(context, "start>end", Toast.LENGTH_LONG).show();
            return;
        }

        if (!excuteWorkTime()){
            return;
        }
        switch (showStatus){
            case SHOW_TODAY:
                //当天时间不在设置的时间段里时,设置为初始时间
                if (todayCalendar.getTime().getTime() >= endCalendar.getTime().getTime() || todayCalendar.getTime().getTime() <= startCalendar.getTime().getTime()){
                    initStartParameter();
                    initStartTimer();
                }else {
                    initTodayParameter();
                    initTodayTimer();
                }
                break;
            case SHOW_START_DAY:
                initStartParameter();
                initStartTimer();
                break;
        }
        addListener();
        seletorDialog.show();
    }

    public void setResultHander(ResultHandler handler){
        this.resultHandler = handler;
    }

    public void setCancelHander(ResultHandler handler){
        this.cacelHandler = handler;
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
                if (cacelHandler != null){
                    cacelHandler.handle("",ResId);
                }
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
                if (resultHandler != null){
                    resultHandler.handle(result,ResId);
                }
                seletorDialog.dismiss();
            }
        });

    }


    private void initTodayParameter() {
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
        selectedCalender = todayCalendar;
        //添加当天的内容
        year_content = DateUtil.format(selectedCalender.getTime(), FORMAT_YEAR);
        month_content = DateUtil.format(selectedCalender.getTime(), FORMAT_MONTH);
        day_content = DateUtil.format(selectedCalender.getTime(), FORMAT_DAY);
        hour_content = DateUtil.format(selectedCalender.getTime(), FORMAT_HOUR);
        mininute_content = DateUtil.format(selectedCalender.getTime(), FORMAT_MIN);
        todayYear = selectedCalender.get(Calendar.YEAR);
        todayMonth = selectedCalender.get(Calendar.MONTH) + 1;
        todayDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
        todayHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
        todayMininute = selectedCalender.get(Calendar.MINUTE);




    }

    private void initStartParameter() {
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
    }

    private void initTodayTimer() {
        initArrayList();

        if (spanYear) {
            for (int i = startYear; i <= endYear; i++) {
                year.add(String.valueOf(i));
            }
            for (int i = 1; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
            for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
            //时间问题
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(todayHour));
            } else {
                for (int i = 0; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(todayMininute));
            } else {
                for (int i = 0; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }

        } else if (spanMon) {
            year.add(String.valueOf(todayYear));
            for (int i = 1; i <= endMonth; i++) {
                month.add(fomatTimeUnit(i));
            }
            for (int i = 1; i <= startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(todayHour));
            } else {
                for (int i = 0; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(todayMininute));
            } else {
                for (int i = 0; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
        } else if (spanDay) {
            year.add(String.valueOf(todayYear));
            month.add(fomatTimeUnit(todayMonth));
            for (int i = 1; i <= endDay; i++) {
                day.add(fomatTimeUnit(i));
            }
            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(todayHour));
            } else {
                for (int i = 0; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(todayMininute));
            } else {
                for (int i = 0; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }

        } else if (spanHour) {
            year.add(String.valueOf(todayYear));
            month.add(fomatTimeUnit(todayMonth));
            day.add(fomatTimeUnit(todayDay));

            if ((scrollUnits & SCROLLTYPE.HOUR.value) != SCROLLTYPE.HOUR.value) {
                hour.add(fomatTimeUnit(todayHour));
            } else {
                for (int i = 0; i <= endHour; i++) {
                    hour.add(fomatTimeUnit(i));
                }

            }

            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(todayMininute));
            } else {
                for (int i = 0; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }


        } else if (spanMin) {
            year.add(String.valueOf(todayYear));
            month.add(fomatTimeUnit(todayMonth));
            day.add(fomatTimeUnit(todayDay));
            hour.add(fomatTimeUnit(todayHour));


            if ((scrollUnits & SCROLLTYPE.MINUTE.value) != SCROLLTYPE.MINUTE.value) {
                minute.add(fomatTimeUnit(todayMininute));
            } else {
                for (int i = 0; i <= endMininute; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
        }

        workTime = context.getString(R.string.timeselector_up_work);

        loadTodayComponent();

    }


    private void initStartTimer() {
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

        loadStartComponent();

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
            startCalendar.set(Calendar.HOUR_OF_DAY, hour_workStart);
            startCalendar.set(Calendar.MINUTE, minute_workStart);
            endCalendar.set(Calendar.HOUR_OF_DAY, hour_workEnd);
            endCalendar.set(Calendar.MINUTE, minute_workEnd);

            int startWorkHour = startCalendar.get(Calendar.HOUR_OF_DAY);
            int startWorkMinute = startCalendar.get(Calendar.MINUTE);

            int endWorkHour = endCalendar.get(Calendar.HOUR_OF_DAY);
            int endWorkMinute = endCalendar.get(Calendar.MINUTE);

            //结束小时大于开始小时，或者结束小时等于开始小时且结束分钟大于等于开始分钟
            if (endWorkHour > startWorkHour || (endWorkHour == startWorkHour && endWorkMinute >= startWorkMinute)){
                MINHOUR = startCalendar.get(Calendar.HOUR_OF_DAY);
                MAXHOUR = endCalendar.get(Calendar.HOUR_OF_DAY);
            }else {
                Toast.makeText(context, "Wrong parames!", Toast.LENGTH_LONG).show();
                return false;
            }

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

                int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
                int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);

                if (selectedMonth == 2){
                    if (selectedDay == 29){
                        selectedDay = 28;
                        selectedCalender.set(Calendar.DAY_OF_MONTH, selectedDay);
                        selectedCalender.set(Calendar.MONTH, selectedMonth - 1);

                    }
                }

                selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
                monthChange();


            }
        });
        month_pv.setOnSelectListener(new PickerView.onSelectListener() {
            @Override
            public void onSelect(String text) {
                //取得当天
                int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
                //设置当天为1号以防跨月
                selectedCalender.set(Calendar.DAY_OF_MONTH, 1);
                //设置为选择的月份
                selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);
                //取到该月最大天数
                int maxDay = selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH);

                switch (Integer.parseInt(text)){
                    case 2:
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        if (maxDay < selectedDay){
                            selectedDay = maxDay;
                        }
                        break;
                }
                //设置回当天
                selectedCalender.set(Calendar.DAY_OF_MONTH, selectedDay);

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


    private void loadTodayComponent() {
        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        hour_pv.setData(hour);
        minute_pv.setData(minute);
        year_pv.setSelected(year_content);
        month_pv.setSelected(month_content);
        day_pv.setSelected(day_content);
        hour_pv.setSelected(hour_content);
        minute_pv.setSelected(mininute_content);
        ///
        work_pv.setData(work);
        work_pv.setSelected(0);
        excuteScroll();
    }

    private void loadStartComponent() {
        year_pv.setData(year);
        month_pv.setData(month);
        day_pv.setData(day);
        hour_pv.setData(hour);
        minute_pv.setData(minute);
        year_pv.setSelected(0);
        month_pv.setSelected(0);
        day_pv.setSelected(0);
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
        String monthChange = String.valueOf(selectedCalender.get(Calendar.MONTH) + 1);
        //清空月份数组
        month.clear();
        //按照要求重新填充月份数组
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        if (selectedYear == startYear) {
            for (int i = startMonth; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
            if (selectedCalender.get(Calendar.MONTH) + 1 < startMonth){
                selectedCalender.set(Calendar.MONTH, startMonth-1);
                monthChange = String.valueOf(selectedCalender.get(Calendar.MONTH) + 1);
            }
        } else if (selectedYear == endYear) {
            for (int i = 1; i <= endMonth; i++) {
                month.add(fomatTimeUnit(i));
            }
            if (selectedCalender.get(Calendar.MONTH) + 1 > endMonth){
                selectedCalender.set(Calendar.MONTH, endMonth-1);
                monthChange = String.valueOf(selectedCalender.get(Calendar.MONTH) + 1);
            }
        } else {
            for (int i = 1; i <= MAXMONTH; i++) {
                month.add(fomatTimeUnit(i));
            }
        }
        //设置改变值
        month_pv.setData(month);

        if (selectedCalender.get(Calendar.MONTH) + 1 <= 9){
            monthChange = "0"+monthChange;
        }

        month_pv.setSelected(monthChange);
        excuteAnimator(ANIMATORDELAY, month_pv);

        month_pv.postDelayed(new Runnable() {
            @Override
            public void run() {
                dayChange();
            }
        }, CHANGEDELAY);

    }

    private void dayChange() {

        String dayChange = String.valueOf(selectedCalender.get(Calendar.DAY_OF_MONTH));

        day.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        if (selectedYear == startYear && selectedMonth == startMonth) {
            for (int i = startDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
            if (selectedCalender.get(Calendar.DAY_OF_MONTH) < startDay){
                selectedCalender.set(Calendar.DAY_OF_MONTH, startDay);
                dayChange = String.valueOf(selectedCalender.get(Calendar.DAY_OF_MONTH));
            }
        } else if (selectedYear == endYear && selectedMonth == endMonth) {
            for (int i = 1; i <= endDay; i++) {
                day.add(fomatTimeUnit(i));
            }
            if (selectedCalender.get(Calendar.DAY_OF_MONTH) > endDay){
                selectedCalender.set(Calendar.DAY_OF_MONTH, endDay);
                dayChange = String.valueOf(selectedCalender.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            for (int i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                day.add(fomatTimeUnit(i));
            }
        }
        day_pv.setData(day);

        if (selectedCalender.get(Calendar.DAY_OF_MONTH) <= 9){
            dayChange = "0"+dayChange;
        }

        day_pv.setSelected(dayChange);

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

            String hourChange = String.valueOf(selectedCalender.get(Calendar.HOUR_OF_DAY));


            hour.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);

            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay) {
                for (int i = startHour; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.HOUR_OF_DAY) < startHour){
                    selectedCalender.set(Calendar.HOUR_OF_DAY, startHour);
                    hourChange = String.valueOf(selectedCalender.get(Calendar.HOUR_OF_DAY));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay) {
                for (int i = MINHOUR; i <= endHour; i++) {
                    hour.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.HOUR_OF_DAY) > endHour){
                    selectedCalender.set(Calendar.HOUR_OF_DAY, endHour);
                    hourChange = String.valueOf(selectedCalender.get(Calendar.HOUR_OF_DAY));
                }
            } else {
                for (int i = MINHOUR; i <= MAXHOUR; i++) {
                    hour.add(fomatTimeUnit(i));
                }

            }
            hour_pv.setData(hour);

            if (selectedCalender.get(Calendar.HOUR_OF_DAY) <= 9){
                hourChange = "0"+hourChange;
            }

            hour_pv.setSelected(hourChange);
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

            String minuteChange = String.valueOf(selectedCalender.get(Calendar.MINUTE));

            minute.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);

            if (selectedYear == startYear && selectedMonth == startMonth && selectedDay == startDay && selectedHour == startHour) {
                for (int i = startMininute; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.MINUTE) < startMininute){
                    selectedCalender.set(Calendar.MINUTE, startMininute);
                    minuteChange = String.valueOf(selectedCalender.get(Calendar.MINUTE));
                }
            } else if (selectedYear == endYear && selectedMonth == endMonth && selectedDay == endDay && selectedHour == endHour) {
                for (int i = MINMINUTE; i <= endMininute; i++) {
                    minute.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.MINUTE) > endMininute){
                    selectedCalender.set(Calendar.MINUTE, endMininute);
                    minuteChange = String.valueOf(selectedCalender.get(Calendar.MINUTE));
                }
            }else if (selectedHour == hour_workStart) {
                for (int i = minute_workStart; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.MINUTE) < minute_workStart){
                    selectedCalender.set(Calendar.MINUTE, minute_workStart);
                    minuteChange = String.valueOf(selectedCalender.get(Calendar.MINUTE));
                }
            } else if (selectedHour == hour_workEnd) {
                for (int i = MINMINUTE; i <= minute_workEnd; i++) {
                    minute.add(fomatTimeUnit(i));
                }
                if (selectedCalender.get(Calendar.MINUTE) > minute_workEnd){
                    selectedCalender.set(Calendar.MINUTE, minute_workEnd);
                    minuteChange = String.valueOf(selectedCalender.get(Calendar.MINUTE));
                }
            }else {
                for (int i = MINMINUTE; i <= MAXMINUTE; i++) {
                    minute.add(fomatTimeUnit(i));
                }
            }
            minute_pv.setData(minute);

            if (selectedCalender.get(Calendar.MINUTE) <= 9){
                minuteChange = "0"+minuteChange;
            }

            minute_pv.setSelected(minuteChange);
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
