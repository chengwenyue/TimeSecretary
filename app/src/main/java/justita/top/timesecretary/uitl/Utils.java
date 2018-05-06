/*
 * Copyright (C) 2012 The Android Open Source Project
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

package justita.top.timesecretary.uitl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.util.List;


public class Utils {

    public static final String webUrl = "http://121.42.140.230:8080/TimeSecretaryWeb/";
    public static int deviceHeigth;
    public static int deviceWidth;

    public final static String REAL_PACKAGE_NAME = "justita.top.timesecretary";

    /**
     * Help URL query parameter key for the app version.
     */
    private final static String PARAM_VERSION = "version";

    /**
     * Cached version code to prevent repeated calls to the package manager.
     */
    private static String sCachedVersionCode = null;

    /**
     * Array of single-character day of week symbols {'S', 'M', 'T', 'W', 'T', 'F', 'S'}
     */
    private static String[] sShortWeekdays = null;

    /** Types that may be used for clock displays. **/
    public static final String CLOCK_TYPE_DIGITAL = "digital";
    public static final String CLOCK_TYPE_ANALOG = "analog";

    /** The background colors of the app, it changes thru out the day to mimic the sky. **/
    public static final String[] BACKGROUND_SPECTRUM = { "#212121", "#27232e", "#2d253a",
            "#332847", "#382a53", "#3e2c5f", "#442e6c", "#393a7a", "#2e4687", "#235395", "#185fa2",
            "#0d6baf", "#0277bd", "#0d6cb1", "#1861a6", "#23569b", "#2d4a8f", "#383f84", "#433478",
            "#3d3169", "#382e5b", "#322b4d", "#2c273e", "#272430" };

    /**
     * Returns whether the SDK is KitKat or later
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static void initDeviceScreenSize(Activity activity) {
        WindowManager m = activity.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        deviceHeigth = d.getHeight();
        deviceWidth = d.getWidth();
    }
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

    }
}
