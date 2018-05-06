package justita.top.timesecretary.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import justita.top.timesecretary.app.BaseApp;
import justita.top.timesecretary.service.DataSyncService;
import justita.top.timesecretary.uitl.DateUtils;
import justita.top.timesecretary.uitl.GsonUtil;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public class Affair implements DataContract.AffairColumns ,SyncDataBean {

    public static final long INVALID_ID = -1;

    //1. 普通事件  2. 普通事件 有开始提醒 3.时间轴事件 4.时间轴事件 有开始提醒 5.时间轴事件 有结束提醒
    public static final String NORMAL_TYPE = "1";
    public static final String NORMAL_TYPE_WITH_START_ALARM = "2";
    public static final String TIME_TYPE = "3";
    public static final String TIME_TYPE_WITH_START_ALARM = "4";
    public static final String TIME_TYPE_WITH_END_ALARM = "5";
    public static final String TIME_TYPE_BOTH = "6";

    private static final String DEFAULT_CATEGORY = "1";
    private static final String DEFAULT_REMARK = new JSONObject().toString();
    private static final String DEFAULT_POSITION = "default_position";
    private static final long DEFAULT_USER_ID= 1;
    public static final int DEFAULT_AFFAIR_ID= -1;//默认未同步
    private static final int DEFAULT_STATE = AFFAIR_SILENT_STATE;
    static {
        Operation.orm.put(DatabaseHelper.AFFAIRS_TABLE_NAME,Affair.class);
    }
    private static final String[] QUERY_COLUMNS = {
            _ID,
            NAME,
            TYPE,
            CATEGORY,
            REMARK,
            POSITION,
            TIME,
            STATE,
            USER_ID,
            AFFAIR_ID
    };


    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int TYPE_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;
    private static final int REMARK_INDEX = 4;
    private static final int POSITION_INDEX = 5;
    private static final int TIME_INDEX = 6;
    private static final int STATE_INDEX = 7;
    private static final int USER_ID_INDEX = 8;
    private static final int AFFAIR_ID_INDEX = 9;
    private static final int COLUMN_COUNT = AFFAIR_ID_INDEX + 1;

    public static ContentValues createContentValues(Affair affair) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (affair.mId != INVALID_ID) {
            values.put(_ID, affair.mId);
        }
        values.put(NAME, affair.mName);
        values.put(TYPE, affair.mType);
        values.put(CATEGORY, affair.mCategory);
        values.put(REMARK, affair.mRemark);
        values.put(POSITION,affair.mPosition);
        values.put(TIME, affair.mTime);
        values.put(STATE, affair.mState);
        values.put(USER_ID, affair.mUserId);
        values.put(AFFAIR_ID, affair.mAffairId);
        return values;
    }

    public static Intent createIntent(String action, long affairId) {
        return new Intent(action).setData(getUri(affairId));
    }

    public static Intent createIntent(Context context, Class<?> cls, long affairId) {
        return new Intent(context, cls).setData(getUri(affairId));
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    public static Uri getUri(long affairId) {
        return ContentUris.withAppendedId(CONTENT_URI, affairId);
    }



    public static Affair getAffair(ContentResolver contentResolver, long affairId) {
        Cursor cursor = contentResolver.query(getUri(affairId), QUERY_COLUMNS, null, null, null);
        Affair result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Affair(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static List<Affair> getAffairs(ContentResolver contentResolver, Calendar c){
        String time = DateUtils.formatDate(c,DateUtils.YEAR_MOUTH_DAY);
        String selection = Affair.TIME+ ">=? and " +Affair.TIME+"<= ?";
        return Affair.getAffairs(contentResolver,selection,new String[]{time+" 00:00",time+" 23:59"});
    }
    public static List<Affair> getAffairs(ContentResolver contentResolver,
                                                   String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<Affair> result = new LinkedList<Affair>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new Affair(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }


    /**
     * 添加一个事件，并且记录操作
     * @param contentResolver
     * @param affair
     * @return
     */
    public static Affair addAffair(ContentResolver contentResolver,
                                   Affair affair) {
        ContentValues values = createContentValues(affair);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        affair.mId = getId(uri);
        LogUtils.e(affair.toString());
        DataSyncService.addInsertOperation(BaseApp.getInstance(),Affair.class,affair.mId,Operation.NOT_SYNC);
        return affair;
    }

    /**
     * 修改一个事件，并且记录操作
     * @param contentResolver
     * @param affair
     * @return
     */
    public static boolean updateAffair(ContentResolver contentResolver, Affair affair) {
        if (affair.mId == INVALID_ID) return false;
        ContentValues values = createContentValues(affair);
        long rowsUpdated = contentResolver.update(getUri(affair.mId), values, null, null);

        DataSyncService.addUpdateOperation(BaseApp.getInstance(),Affair.class,affair.mId,Operation.NOT_SYNC);
        return rowsUpdated == 1;
    }

    /**
     * 修改一个事件，并且记录操作
     * @param contentResolver
     * @param values
     * @param affairId
     * @return
     */
    public static boolean updateAffair(ContentResolver contentResolver, ContentValues values,long affairId) {
        if (affairId == INVALID_ID) return false;
        long rowsUpdated = contentResolver.update(getUri(affairId), values, null, null);
        DataSyncService.addUpdateOperation(BaseApp.getInstance(),Affair.class,affairId,Operation.NOT_SYNC);
        return rowsUpdated == 1;
    }

    /**
     * 改变事件状态，并且记录操作
     * @param contentResolver
     * @param affair
     * @return
     */
    public static boolean changeAffairState(ContentResolver contentResolver, Affair affair){
        if (affair.mId == INVALID_ID) return false;
        ContentValues values = new ContentValues(1);
        values.put(STATE,affair.mState);
        long rowsUpdated = contentResolver.update(getUri(affair.mId), values, null, null);

        if(affair.mState == AFFAIR_DELETE_STATE){
            DataSyncService.addDeleteOperation(BaseApp.getInstance(),Affair.class,affair.mId,Operation.NOT_SYNC);
        }else{
            DataSyncService.addUpdateOperation(BaseApp.getInstance(),Affair.class,affair.mId,Operation.NOT_SYNC);
        }
        return rowsUpdated == 1;
    }



    //需要持久化的数据
    public long mId;
    public String mName;
    public String mType;
    public String mCategory;
    public String mRemark;
    public String mPosition;
    //事务时间 yyyy-MM-dd HH:mm
    public String mTime;
    public int mState;
    public long mUserId;
    public long mAffairId;

    //关联表
    public AlarmInstance mStartInstance;
    public AlarmInstance mEndInstance;
    public DaysOfWeek mDaysOfWeek;
    public int mAdvanceTime = -1;

    public Affair(String mName, String mType, String mCategory, String mRemark,String mTime, int mState) {
        mId = INVALID_ID;
        this.mName = mName;
        this.mType = mType;
        this.mCategory = mCategory;
        this.mRemark = mRemark;
        this.mPosition = DEFAULT_POSITION;
        this.mTime = mTime;
        this.mState = mState;
        this.mUserId = DEFAULT_USER_ID;
        this.mAffairId = DEFAULT_AFFAIR_ID;
    }

    public Affair(String mName, String mType, String mCategory, String mRemark, String mPosition, String mTime, int mState, long mUserId, int mAffairId) {
        this.mName = mName;
        this.mType = mType;
        this.mCategory = mCategory;
        this.mRemark = mRemark;
        this.mPosition = mPosition;
        this.mTime = mTime;
        this.mState = mState;
        this.mUserId = mUserId;
        this.mAffairId = mAffairId;
    }

    public Affair() {
        mId = INVALID_ID;
        this.mType = NORMAL_TYPE;
        this.mCategory = DEFAULT_CATEGORY;
        this.mRemark = DEFAULT_REMARK;
        this.mPosition = DEFAULT_POSITION;
        this.mTime = DateUtils.formatDate(new Date(),DateUtils.DEFAULT_FORMAT);
        this.mState = DEFAULT_STATE;
        this.mAffairId = DEFAULT_AFFAIR_ID;
        this.mDaysOfWeek = new DaysOfWeek(0);
        this.mAdvanceTime = 0;
    }

    public DaysOfWeek getDaysOfWeek(){
        if(mDaysOfWeek == null) {
            int daysOfWeek = GsonUtil.getGson().fromJson(mRemark,JsonObject.class).get("daysOfWeek").getAsInt();
            mDaysOfWeek = new DaysOfWeek(daysOfWeek);
        }
        return mDaysOfWeek;
    }

    public int getAdvanceTime(){
        if(mAdvanceTime == -1){
            JsonObject remark = GsonUtil.getGson().fromJson(mRemark,JsonObject.class);
            mAdvanceTime=  remark.get("advanceTime").getAsInt();
        }
        return mAdvanceTime;
    }


    public Affair(Cursor c) {
        mId = c.getLong(ID_INDEX);
        mName = c.getString(NAME_INDEX);
        mType = c.getString(TYPE_INDEX);
        mCategory = c.getString(CATEGORY_INDEX);
        mRemark = c.getString(REMARK_INDEX);
        mPosition = c.getString(POSITION_INDEX);
        mTime = c.getString(TIME_INDEX);
        mState = c.getInt(STATE_INDEX);
        mUserId = c.getLong(USER_ID_INDEX);
        mAffairId = c.getInt(AFFAIR_ID_INDEX);
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AlarmInstance)) return false;
        final AlarmInstance other = (AlarmInstance) o;
        return mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mId).hashCode();
    }

    @Override
    public String toString() {
        return "Affair{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mType='" + mType + '\'' +
                ", mCategory='" + mCategory + '\'' +
                ", mRemark='" + mRemark + '\'' +
                ", mPosition='" + mPosition + '\'' +
                ", mTime='" + mTime + '\'' +
                ", mState=" + mState +
                ", mUserId='" + mUserId +'\''+
                ", mAffairId='" + mAffairId +'\''+
                '}';
    }

    public boolean isTimeAffair(){
        if(mType.equals(Affair.TIME_TYPE)
                || mType.equals(Affair.TIME_TYPE_WITH_START_ALARM)
                || mType.equals(Affair.TIME_TYPE_WITH_END_ALARM)
                || mType.equals(Affair.TIME_TYPE_BOTH))
            return true;
        else
            return false;
    }

    public static Affair JsonToAffair(JsonObject jsonObject){
        Affair affair = new Affair();

        affair.mId = jsonObject.get(_ID).getAsLong();
        affair.mName = jsonObject.get(NAME).getAsString();
        affair.mUserId = PreferenceUtils.getPrefLong(BaseApp.getInstance(),
                PreferenceConstants.USERID,-1);
        affair.mType = jsonObject.get(TYPE).getAsString();
        affair.mCategory = jsonObject.get(CATEGORY).getAsString();
        affair.mRemark = jsonObject.get(REMARK).getAsJsonObject().toString();
        affair.mPosition = jsonObject.get(POSITION).getAsString();
        affair.mTime = jsonObject.get(TIME).getAsString();
        affair.mState = jsonObject.get(STATE).getAsInt();
        affair.mAffairId = jsonObject.get(AFFAIR_ID).getAsLong();
        return affair;
    }

    public String getTimeLabel(){
        String[] time = mTime.split(PreferenceConstants.TIME_SEPARATOR);
        String label = "";
        try {
            label+=DateUtils.defaultToMD_HM(time[0]);
            if(time.length>=2){
                label+= PreferenceConstants.TIME_SEPARATOR+time[1];
            }
        } catch (ParseException e) {
            e.printStackTrace();
            label+= DateUtils.formatDate(new Date(),DateUtils.MOUTH_DAY_HOUR_MINUTE);
        }
        return label;
    }

    @Override
    public String getAddData() {
        JSONObject o = new JSONObject();
        try {
            o.put(_ID,mId);
            o.put(NAME,mName);
            o.put(TYPE,mType);
            o.put(CATEGORY,mCategory);
            o.put(REMARK,mRemark);
            o.put(POSITION,mPosition);
            o.put(TIME,mTime);
            o.put(STATE,mState);
            o.put(USER_ID,mUserId);
            LogUtils.e(o.toString());
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getUpdateData() {
        JSONObject o = new JSONObject();
        try {
            o.put(_ID,mId);
            o.put(NAME,mName);
            o.put(TYPE,mType);
            o.put(CATEGORY,mCategory);
            o.put(REMARK, mRemark);
            o.put(POSITION,mPosition);
            o.put(TIME,mTime);
            o.put(STATE,mState);
            o.put(USER_ID,mUserId);
            o.put(AFFAIR_ID,mAffairId);
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDeleteData() {
        JSONObject o = new JSONObject();
        try {
            o.put(_ID,mId);
            o.put(USER_ID,mUserId);
            o.put(AFFAIR_ID,mAffairId);
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAddUrl() {
        return Utils.webUrl+"addAffair";
    }

    @Override
    public String getUpdateUrl() {
        return Utils.webUrl+"updateAffair";
    }

    @Override
    public String getDeleteUrl() {
        return Utils.webUrl+"delAffair";
    }

    @Override
    public String getSyncDataByUserUrl() {
        return Utils.webUrl+"queryByUserId";
    }

    @Override
    public void syncAddCallback(JsonObject jsonObject, long dataId) {
        ContentValues values = new ContentValues();
        values.put(AFFAIR_ID,jsonObject.get(AFFAIR_ID).getAsLong());
        BaseApp.getInstance().getContentResolver().update(getUri(dataId), values, null, null);
    }

    @Override
    public void syncUpdateCallback(JsonObject jsonObject, long dataId) {
    }

    @Override
    public void syncDeleteCallback(JsonObject jsonObject, long dataId) {
    }

    @Override
    public void syncGetDataCallback(JsonObject jsonObject) {
        JsonArray affairs = jsonObject.getAsJsonArray("affairs");
        for(int i = 0 ;i<affairs.size();i++){
            JsonObject affair = affairs.get(i).getAsJsonObject();
            long _id = affair.get(_ID).getAsLong();
            if(Affair.getAffair(BaseApp.getInstance().getContentResolver(), _id) != null){
                DataSyncService.addUpdateOperation(BaseApp.getInstance(),Affair.class,_id,Operation.NOT_SYNC);
            }else{
                Affair temp = Affair.JsonToAffair(affair);
                ContentValues values = createContentValues(temp);
                BaseApp.getInstance().getContentResolver().insert(CONTENT_URI, values);
                DataSyncService.addInsertOperation(BaseApp.getInstance(),Affair.class,_id,Operation.SYNC);
            }
        }
    }


    public static class Builder {
        private Affair affair;
        private boolean hasStartAlarm = false;
        private boolean hasEndAlarm = false;
        private int advanceTime = 0;
        private JSONObject remark;
        private boolean[] mDaysOfWeek = new boolean[7];
        public boolean isHasStartAlarm() {
            return hasStartAlarm;
        }

        public boolean isHasEndAlarm() {
            return hasEndAlarm;
        }

        public Builder() {
            affair = new Affair();
        }
        public Builder(Affair affair) {
            this.affair = affair;
        }
        public Builder setName(String name) {
            affair.mName = name;
            return this;
        }

        public Builder setUserId(long userId) {
            affair.mUserId = userId;
            return this;
        }

        public Builder setTime(String time){
            affair.mTime = time;
            return this;
        }

        public Builder setType(String type){
            affair.mType = type;
            return this;
        }
        public Builder setPosition(String position){
            affair.mPosition = position;
            return this;
        }

        public Builder setStartAlarm(boolean hasStartAlarm){
            this.hasStartAlarm =hasStartAlarm;
            return this;
        }

        public Builder setEndAlarm(boolean hasEndAlarm){

            //如果不是计时事件应抛出异常

            this.hasEndAlarm = hasEndAlarm;
            return this;
        }

        public Builder setCategory(String category){
            affair.mCategory = category;
            return this;
        }

        public Builder setAdvanceTime(int advanceTime){
            this.advanceTime = advanceTime;
            return this;
        }
        public Builder addRemark(String name ,Object object){
            if(remark == null){
                remark = new JSONObject();
            }
            try {
                remark.put(name,object);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder setDaysOfWeek(boolean[] daysOfWeek){
            for(int i =0;i<7;i++){
                mDaysOfWeek[i] = daysOfWeek[i];
            }
            return this;
        }
        public Affair create(){

            //设置类别
            if(getAffair().mType.equals(Affair.NORMAL_TYPE)){
                if(hasStartAlarm){
                    setType(Affair.NORMAL_TYPE_WITH_START_ALARM);
                    LogUtils.e("NORMAL_TYPE_WITH_START_ALARM" +getAffair().mType );
                }else{
                    LogUtils.e("NORMAL_TYPE" +getAffair().mType );
                }
            }else if(getAffair().mType.equals(Affair.TIME_TYPE)){
                if(hasStartAlarm&& !hasEndAlarm) {
                    setType(Affair.TIME_TYPE_WITH_START_ALARM);
                    LogUtils.e("TIME_TYPE_WITH_START_ALARM" +getAffair().mType );
                }else if(!hasStartAlarm && hasEndAlarm){
                    setType(Affair.TIME_TYPE_WITH_END_ALARM);
                    LogUtils.e("TIME_TYPE_WITH_END_ALARM" +getAffair().mType );
                }else if(!hasStartAlarm && !hasEndAlarm){
                    setType(Affair.TIME_TYPE);
                    LogUtils.e("TIME_TYPE" +getAffair().mType );
                }else {
                    setType(Affair.TIME_TYPE_BOTH);
                    LogUtils.e("TIME_TYPE_BOTH" +getAffair().mType );
                }
            }


            //设置重复，添加到备注中去
            DaysOfWeek daysOfWeek = new DaysOfWeek(0);
            for(int i = 0;i<7;i++){
                daysOfWeek.setDaysOfWeek(mDaysOfWeek[i],DateUtils.DAY_ORDER[i]);
            }
            addRemark("daysOfWeek",daysOfWeek.getBitSet());

            //设置提前提醒时间，并且添加到备注中去
            addRemark("advanceTime",advanceTime);
            getAffair().mRemark = remark.toString();


            return affair;
        }

        public Affair getAffair() {
            return affair;
        }
    }

}
