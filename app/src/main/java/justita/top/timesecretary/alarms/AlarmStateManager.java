package justita.top.timesecretary.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import justita.top.timesecretary.provider.Affair;
import justita.top.timesecretary.provider.AlarmInstance;
import justita.top.timesecretary.service.AffairStateManageService;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public final class AlarmStateManager{
    public static final String ALARM_FIRE_ACTION = "ALARM_FIRE_ACTION";
    public static final String CANCEL_ALARM_ACTION = "CANCEL_ALARM_ACTION";

    // 最近的AlarmInstance的id;
    private static final String ALARM_GLOBAL_ID_EXTRA = "intent.extra.alarm.global.id";

    // Intent action to trigger an instance state change.
    public static final String CHANGE_STATE_ACTION = "change_state";



    // Intent action for an AlarmManager alarm serving only to set the next alarm indicators
    public static final String INDICATOR_ACTION = "indicator";

    // Extra key to set the desired state change.
    public static final String ALARM_STATE_EXTRA = "intent.extra.alarm.state";


    // Intent category tag used when schedule state change intents in alarm manager.
    public static final String ALARM_MANAGER_TAG = "ALARM_MANAGER";


    public static long getGlobalIntentId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(ALARM_GLOBAL_ID_EXTRA, -1);
    }

    public static void updateGloablIntentId(Context context,long instanceId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(ALARM_GLOBAL_ID_EXTRA, instanceId).commit();
        LogUtils.e("更新 updateGloablIntentId : " + instanceId);
    }


    public static Intent createStateChangeIntent(Context context, String tag,
            AlarmInstance instance, Integer state) {
        Intent intent = AlarmInstance.createIntent(context, AlarmStateManager.class, instance.mId);
        intent.setAction(CHANGE_STATE_ACTION);
        intent.addCategory(tag);
        if (state != null) {
            intent.putExtra(ALARM_STATE_EXTRA, state.intValue());
        }
        return intent;
    }


    private static void cancelScheduledInstance(Context context, AlarmInstance instance) {
        LogUtils.e("取消提醒： " + instance.toString());

        Intent intent = AlarmInstance.createIntent(context, AffairStateManageService.class, instance.mId);
        intent.setAction(ALARM_FIRE_ACTION);

        // AlarmManager的取消：（其中需要注意的是取消的Intent必须与启动Intent保持绝对一致才能支持取消AlarmManager）
        PendingIntent pendingIntent = PendingIntent.getService(context, (int) instance.mId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }


    public static void setSilentState(Context context, AlarmInstance instance) {
        LogUtils.e("提醒：" +instance.toString());
        long timeInMillis = instance.getAlarmTime().getTimeInMillis();


        Intent intent = AlarmInstance.createIntent(context, AffairStateManageService.class, instance.mId);
        intent.setAction(ALARM_FIRE_ACTION);


        PendingIntent pendingIntent2 = PendingIntent.getService(context, (int) instance.mId,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Utils.isKitKatOrLater()) {
            am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent2);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent2);
        }
    }




    public static void unregisterInstance(Context context, AlarmInstance instance) {
        // Stop alarm if this instance is firing it


        //要取消注册的提醒为当前提醒，则取消提醒
        if(instance.mId == getGlobalIntentId(context)) {
            cancelScheduledInstance(context, instance);
            updateGloablIntentId(context,-1);
        }
        AlarmInstance.deleteInstance(context.getContentResolver(),instance.mId
        );
    }

    public static void registerLastInstance(Context context) {

        //取出最近的alarmInstance 并且注册

        List<AlarmInstance> alarmInstances = AlarmInstance.getInstances(context.getContentResolver(),null,new String[0]);
        if(alarmInstances.isEmpty()) {
            updateGloablIntentId(context, -1);
            return;
        }

        int i ,j= alarmInstances.size();
        boolean isChanged ;

        for(;j>0;j--){
            isChanged = false;
            for(i =0 ;i<j -1;i++){
                AlarmInstance first = alarmInstances.get(i);
                AlarmInstance second = alarmInstances.get(i+1);
                if(first.getAlarmTime().getTimeInMillis() > second.getAlarmTime().getTimeInMillis()){

                    alarmInstances.set(i,second);

                    alarmInstances.set(i+1, first);

                    isChanged = true;

                }
            }

            if(!isChanged)
                break;
        }

        LogUtils.e("数据库中的提醒 ========================");
        for(AlarmInstance alarmInstance1 : alarmInstances){
            LogUtils.e(alarmInstance1.toString());
        }
        //把在当前时间之后的所有提醒删除
        i =0;
        for( ; i< alarmInstances.size() ;i++){
            AlarmInstance instance = alarmInstances.get(i);
            if(instance.getAlarmTime().getTimeInMillis() <= System.currentTimeMillis()){
                AlarmInstance.deleteInstance(context.getContentResolver(),instance.mId);
            }else {
                break;
            }
        }


        //所有的提醒都超时了
        if(i>=alarmInstances.size()){
            updateGloablIntentId(context, -1);
            return;
        }

        AlarmInstance lastInstance = alarmInstances.get(i);

        //注册
        setSilentState(context,lastInstance);

        //更新global
        updateGloablIntentId(context, lastInstance.mId);
    }
    public static void registerInstance(Context context,AlarmInstance instance) {



        //要注册的提醒超时，不进行注册
        if(instance.getAlarmTime().getTimeInMillis() <= System.currentTimeMillis())
            return;

        //如果当前无提醒,则直接添加
        if(getGlobalIntentId(context) == -1){
            registerLastInstance(context);
            return;
        }


        //判断当前的提醒 是否是 要注册的提醒
        if(instance.mId == getGlobalIntentId(context))
            return;

        List<AlarmInstance> alarmInstances = AlarmInstance.getInstances(context.getContentResolver(),AlarmInstance._ID +"=?",getGlobalIntentId(context)+"");

        //当前提醒被删除
        if(alarmInstances.isEmpty()){
            registerLastInstance(context);
            return;
        }

        //当前提醒
        AlarmInstance alarmInstance = alarmInstances.get(0);


        //要注册的提醒超时当前提醒
        if(alarmInstance.getAlarmTime().getTimeInMillis() <= instance.getAlarmTime().getTimeInMillis())
            return;


        //取消当前提醒
        cancelScheduledInstance(context,alarmInstance);
        //注册提醒
        setSilentState(context,instance);

        //更新global
        updateGloablIntentId(context, instance.mId);


    }


    public static void deleteAllInstances(Context context, long affairId) {
        ContentResolver cr = context.getContentResolver();
        List<AlarmInstance> instances = AlarmInstance.getInstancesByAffairId(cr, affairId);
        for (AlarmInstance instance : instances) {

            unregisterInstance(context, instance);
            AlarmInstance.deleteInstance(context.getContentResolver(), instance.mId);
        }
    }

    public static void deleteInstance(Context context, AlarmInstance instance) {
        unregisterInstance(context, instance);
        AlarmInstance.deleteInstance(context.getContentResolver(), instance.mId);
    }


    public static AlarmInstance addAlarmInstance(Context mContext,Affair affair, Calendar calendar) {
        AlarmInstance alarmInstance = new AlarmInstance(calendar,affair.mUserId);
        alarmInstance.mId = PreferenceUtils.getPrefLong(mContext, PreferenceConstants.MAX_ALARM_ID,-1);
        PreferenceUtils.setSettingLong(mContext,PreferenceConstants.MAX_ALARM_ID,alarmInstance.mId+1);
        alarmInstance.mAffairId = affair.mId;
        AlarmInstance.addInstance(mContext.getContentResolver(),alarmInstance);


        LogUtils.e("数据库拥有的提醒 ===============");
        List<AlarmInstance> alarmInstances = AlarmInstance.getInstances(mContext.getContentResolver(),null,new String[0]);
        for(AlarmInstance alarmInstance1 : alarmInstances){
            LogUtils.e(alarmInstance1.toString());
        }
        return alarmInstance;
    }


    /**
     * 为事件添加提醒但不注册
     * @param mContext
     * @param affairId
     */
    public static void addAlarmByAffair(Context mContext,long affairId){
        try {
            Affair affair = Affair.getAffair(mContext.getContentResolver(),affairId);

            //事件未设置提醒时
            if(affair.mType.equals(Affair.TIME_TYPE) || affair.mType.equals(Affair.NORMAL_TYPE)){
                return;
            }

            String[] time = affair.mTime.split(PreferenceConstants.TIME_SEPARATOR);
            Date startDate = DateUtils.formatDate(time[0],DateUtils.DEFAULT_FORMAT);
            Calendar calendar = Calendar.getInstance();

            //添加开始提醒
            if(affair.mType.equals(Affair.NORMAL_TYPE_WITH_START_ALARM)
                    || affair.mType.equals(Affair.TIME_TYPE_WITH_START_ALARM)
                    || affair.mType.equals(Affair.TIME_TYPE_BOTH)){

                calendar.setTime(startDate);

                //设置提前提醒
                calendar.add(Calendar.MINUTE,-affair.getAdvanceTime());

                AlarmInstance alarmInstance = AlarmStateManager.addAlarmInstance(mContext,affair, calendar);
                AlarmStateManager.registerInstance(mContext,alarmInstance);

            }

            //添加结束提醒
            if(affair.mType.equals(Affair.TIME_TYPE_WITH_END_ALARM)||
                    affair.mType.equals(Affair.TIME_TYPE_BOTH)){

                Date date = DateUtils.formatDate(DateUtils.formatDate(startDate,DateUtils.YEAR_MOUTH_DAY)
                        +" "+time[1],DateUtils.DEFAULT_FORMAT);
                calendar.setTime(date);

                AlarmInstance alarmInstance = AlarmStateManager.addAlarmInstance( mContext,affair, calendar);

                AlarmStateManager.registerInstance(mContext,alarmInstance);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeAlarmByAffair(Context context,long affairId,boolean isRegisterNext){
        List<AlarmInstance> alarmInstances = AlarmInstance.getInstancesByAffairId(context.getContentResolver(),affairId);


        //如果当前事件提醒为空
        if(alarmInstances.isEmpty())
            return;
        for(AlarmInstance instance :alarmInstances) {
            AlarmStateManager.unregisterInstance(context,instance);
        }
        if(isRegisterNext)
            registerLastInstance(context);
    }

    public static void removeAllOutTimeAlarm(Context context){
        List<AlarmInstance> alarmInstances = AlarmInstance.getInstances(context.getContentResolver(),null,new String[0]);
        if(alarmInstances.isEmpty()) {
            return;
        }
        //把在当前时间之后的所有提醒删除

        for(int i =0 ; i< alarmInstances.size() ;i++){
            AlarmInstance instance = alarmInstances.get(i);
            if(instance.getAlarmTime().getTimeInMillis() <= System.currentTimeMillis()){
                unregisterInstance(context,instance);
            }
        }
    }
}
