package justita.top.timesecretary.app;


import android.app.Application;
import android.content.ContentValues;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import justita.top.timesecretary.alarms.AlarmStateManager;
import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.DatabaseHelper;
import justita.top.timesecretary.provider.DaysOfWeek;
import justita.top.timesecretary.service.AffairStateManageService;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;


public class BaseApp extends Application{

    private static BaseApp mApplication;

    public synchronized static BaseApp getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        LogUtils.DEBUG = true;


        //初始化AlarmInstance表的最大id
        if(!PreferenceUtils.hasKey(this,PreferenceConstants.MAX_ALARM_ID)){
            PreferenceUtils.setSettingLong(this,PreferenceConstants.MAX_ALARM_ID,1);
        }

        if(!PreferenceUtils.hasKey(this,PreferenceConstants.LAST_START_DATE)){
            Calendar calendar = Calendar.getInstance();
            PreferenceUtils.setPrefString(this,PreferenceConstants.LAST_START_DATE, DateUtils.formatDate(calendar,DateUtils.YEAR_MOUTH_DAY));
        }

        long s1 = System.currentTimeMillis();
        String processName = Utils.getProcessName(this,
                android.os.Process.myPid());
        LogUtils.e("进程名称"+processName);
        if (processName != null) {

            if (processName.equals(Utils.REAL_PACKAGE_NAME)) {
                if(DatabaseHelper.hasCreateTables) {
                    initAlarm();
                    initRepeatAffair();
                }
            }else if(processName.equals(Utils.REAL_PACKAGE_NAME+":remote")) {
                Intent startIntent = new Intent(this, AffairStateManageService.class);
                startIntent.setAction(AffairStateManageService.START_ACTION);
                startService(startIntent);
            }
        }
        LogUtils.e( "onCreate耗时" + (System.currentTimeMillis() - s1));

    }

    private void initRepeatAffair() {
        Calendar calendar = Calendar.getInstance();
        String lastDate = PreferenceUtils.getPrefString(this,PreferenceConstants.LAST_START_DATE, DateUtils.formatDate(calendar,DateUtils.YEAR_MOUTH_DAY));


        //每一天程序启动时初始化一次
        if(lastDate.equals(DateUtils.formatDate(calendar,DateUtils.YEAR_MOUTH_DAY)))
            return;

        //取出状态不为删除的所有事件
        List<Affair> allAffairs = Affair.getAffairs(getContentResolver(),Affair.STATE+"<> ?",Affair.AFFAIR_DELETE_STATE+"");

        for(Affair affair :allAffairs){
            DaysOfWeek daysOfWeek = affair.getDaysOfWeek();

            if (daysOfWeek.isBitEnabled(DaysOfWeek.convertDayToBitIndex(calendar.get(Calendar.DAY_OF_WEEK)))) {


                //yyyy-MM-dd HH:mm--HH:mm
                String[] time = affair.mTime.split(" ");
                ContentValues values = new ContentValues(2);
                values.put(Affair.TIME, DateUtils.formatDate(calendar, DateUtils.YEAR_MOUTH_DAY) + " " + time[1]);


                //如果事件状态为完成，进行中，延时 ，则改变为为开始，并且添加提醒
                //如果事件状态未开始，只改变时间，更新提醒
                if(affair.mState == Affair.AFFAIR_COMPLETE_STATE
                        || affair.mState == Affair.AFFAIR_FIRED_STATE
                        || affair.mState == Affair.AFFAIR_SNOOZE_STATE) {
                    values.put(Affair.STATE,Affair.AFFAIR_SILENT_STATE);
                    Affair.updateAffair(getContentResolver(), values, affair.mId);
                    AlarmStateManager.addAlarmByAffair(this,affair.mId);
                }else{
                    Affair.updateAffair(getContentResolver(), values, affair.mId);

                }


            }
        }

    }

    private void initAlarm() {
        AlarmStateManager.removeAllOutTimeAlarm(this);
    }
}
