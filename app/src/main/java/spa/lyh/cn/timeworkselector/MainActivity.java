package spa.lyh.cn.timeworkselector;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import spa.lyh.cn.time_work_selector.TimeWorkSelector;

public class MainActivity extends AppCompatActivity {
    private TimeWorkSelector timeworkSelector;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar startC = Calendar.getInstance();
    Calendar endC = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startC.setTime(new Date());
        startC.add(Calendar.YEAR, -1);
        Date startDate = startC.getTime();
        endC.setTime(new Date());
        endC.add(Calendar.YEAR, +1);
        Date endDate = endC.getTime();
        timeworkSelector = new TimeWorkSelector(this, new TimeWorkSelector.ResultHandler() {
            @Override
            public void handle(String time, int ResId) {
                Toast.makeText(getApplicationContext(), time+" ResId:"+ResId, Toast.LENGTH_LONG).show();
            }
        }, format.format(startDate), format.format(endDate));
        timeworkSelector.setIsLoop(true);
    }

    public void show(View v) {
        switch (v.getId()){
            case R.id.YMD:
                timeworkSelector.setMode(TimeWorkSelector.MODE.YMD);
                timeworkSelector.show(R.id.YMD);
                break;
            case R.id.YMDHM:
                timeworkSelector.setMode(TimeWorkSelector.MODE.YMDHM);
                timeworkSelector.show(R.id.YMDHM);
                break;
            case R.id.YMDW:
                timeworkSelector.setMode(TimeWorkSelector.MODE.YMDW);
                timeworkSelector.show(R.id.YMDW);
                break;
            case R.id.YMDHMW:
                timeworkSelector.setMode(TimeWorkSelector.MODE.YMDHMW);
                timeworkSelector.show(R.id.YMDHMW);
                break;
        }
        //timeworkSelector.show();
    }
}
