package spa.lyh.cn.time_work_selector.utils;

import android.os.Build;
import android.view.View;
import android.view.Window;


public class NavBarFontColorControler {

    public static void setNavBarMode(Window window, boolean darkFont){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setSystemUiVisibility(window.getDecorView(), View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR,darkFont);
        }
    }

    /**
     * 设置显示的样式
     * @param decorView
     * @param visibility
     * @param isAddVisibility 是否添加这个属性，true添加，false移除
     */
    private static void setSystemUiVisibility(View decorView,int visibility,boolean isAddVisibility){
        int oldVis = decorView.getSystemUiVisibility();
        int newVis = oldVis;
        if (isAddVisibility){
            newVis |= visibility;
        }else {
            newVis &= ~visibility;
        }
        if (newVis != oldVis) {
            decorView.setSystemUiVisibility(newVis);
        }
    }
}
