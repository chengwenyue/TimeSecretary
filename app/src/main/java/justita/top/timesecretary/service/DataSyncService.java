package justita.top.timesecretary.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

import justita.top.timesecretary.app.TSBroadcastReceiver;
import justita.top.timesecretary.provider.Operation;
import justita.top.timesecretary.provider.SyncDataBean;
import justita.top.timesecretary.uitl.GsonUtil;
import justita.top.timesecretary.uitl.OkHttpUtil;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;

public class DataSyncService implements TSBroadcastReceiver.EventHandler{

    public static int SUCCESS = 0x01;
    public static int ERROR = 0x02;


    public static final int REQUEST_SUCCESS = 0x11;
    public static final int REQUEST_ERROR = 0x13;
    public static final int DATA_ERROR = 0x14;
    public static final int SYNC_ERROR = 0x15;
    public static final int NET_ERROR = 0x16;
    public static final int OTHER_ERROR = 0x17;

    private static DataSyncService mDataSyncService;
    private Operation mCurrentO;
    private boolean isNetConnect = false;

    public synchronized static DataSyncService getInstance(Context context) {
        if(mDataSyncService == null){
            mDataSyncService = new DataSyncService(context);
        }
        return mDataSyncService;
    }
    private HandlerThread handlerThread;
    private Handler mHandler;
    private SimpleQueue<Runnable> syncQueue;
    private Context mContext;
    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_SUCCESS :
                    notifyNextData();
                    break;
                case REQUEST_ERROR :
                case DATA_ERROR :
                case OTHER_ERROR :
                case SYNC_ERROR :{
                    mCurrentO = null;
                    notifyNextData();
                }
                break;
                case NET_ERROR :{
                    if(handlerThread != null){
                        handlerThread.quit();
                        handlerThread = null;
                    }
                }
                break;
            }
        }
    };
    private static int syncSize = 10;
    public interface OnSyncByUserListener{
        void syncFinish(int result);
    }
    
    private DataSyncService(Context context){
        this.mContext = context;
        TSBroadcastReceiver.mListeners.add(this);
        handlerThread = new HandlerThread("DataSyncService");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        syncQueue = new SimpleQueue<>(syncSize);
        if(OkHttpUtil.isNetworkAvailable(mContext)) {
            isNetConnect = true;
            notifyNextData();
        }
    }

    public void notifyNextData(){
        if(!isNetConnect)
            return;

        String select =Operation.STATE +" = ? ";
        Operation operation = Operation.getLastOperations(mContext.getContentResolver(),select,Operation.NOT_SYNC+"");

        if(operation != null) {
            if(mCurrentO!=null && mCurrentO.mId == operation.mId)
                return;

            mCurrentO = operation;
            SyncRunnable runnable = new SyncRunnable();
            runnable.setOperation(operation);
            mHandler.post(runnable);
        }
    }

    

    /**
     * 插入新增一个操作
     * @param context
     * @param c
     * @param dataId 数据id
     * @param state 是否同步
     */
    public static void addInsertOperation(Context context,Class c, long dataId,int state){
        Operation operation = new Operation();
        for(Map.Entry<String,Class> item:Operation.orm.entrySet()){
            if(item.getValue().equals(c)){
                operation.mTableName = item.getKey();
                break;
            }
        }
        operation.mUser_Id = PreferenceUtils.getPrefLong(context, PreferenceConstants.USERID,-1);
        operation.mOperation =Operation.INSERT;
        operation.mState = state;
        operation.mDataId = dataId;
        operation.mTime =System.currentTimeMillis();
        Operation.addOperation(context.getContentResolver(),operation);
        if(mDataSyncService == null){
            mDataSyncService = new DataSyncService(context);
        }
        mDataSyncService.notifyNextData();
    }

    public static void addUpdateOperation(Context context,Class c,long dataId,int state){
        Operation operation = new Operation();
        for(Map.Entry<String,Class> item:Operation.orm.entrySet()){
            if(item.getValue().equals(c)){
                operation.mTableName = item.getKey();
                break;
            }
        }
        operation.mUser_Id = PreferenceUtils.getPrefLong(context, PreferenceConstants.USERID,-1);
        operation.mOperation =Operation.UPDATE;
        operation.mState = state;
        operation.mDataId = dataId;
        operation.mTime =System.currentTimeMillis();
        Operation.addOperation(context.getContentResolver(),operation);
        if(mDataSyncService == null){
            mDataSyncService = new DataSyncService(context);
        }
        mDataSyncService.notifyNextData();
    }

    public static void addDeleteOperation(Context context,Class c,long dataId,int state){
        Operation operation = new Operation();
        for(Map.Entry<String,Class> item:Operation.orm.entrySet()){
            if(item.getValue().equals(c)){
                operation.mTableName = item.getKey();
                break;
            }
        }
        operation.mUser_Id = PreferenceUtils.getPrefLong(context, PreferenceConstants.USERID,-1);
        operation.mOperation =Operation.DELETE;
        operation.mState = state;
        operation.mDataId = dataId;
        operation.mTime =System.currentTimeMillis();
        Operation.addOperation(context.getContentResolver(),operation);
        if(mDataSyncService == null){
            mDataSyncService = new DataSyncService(context);
        }
        mDataSyncService.notifyNextData();
    }

    public static boolean isDataDelete(Context context,Class c,long dataId){
        Operation operation = new Operation();
        for(Map.Entry<String,Class> item:Operation.orm.entrySet()){
            if(item.getValue().equals(c)){
                operation.mTableName = item.getKey();
                break;
            }
        }
        operation.mUser_Id = PreferenceUtils.getPrefLong(context, PreferenceConstants.USERID,-1);
        operation.mOperation =Operation.DELETE;
        operation.mDataId = dataId;

        return operation.getDOperation(context.getContentResolver()) != null;
    }


    /**
     * 从服务器上拉取数据
     * @param context
     * @param c
     * @param onSyncByUserListener
     */
    public void syncDataByUser(final Context context, Class c, final OnSyncByUserListener onSyncByUserListener){
        try {
            final SyncDataBean syncDataBean = (SyncDataBean) c.newInstance();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = new JSONObject();
                    try {
                        object.put(Operation.USER_ID,PreferenceUtils.getPrefLong(context,PreferenceConstants.USERID,-1));

                        String s =  OkHttpUtil.postJson(syncDataBean.getSyncDataByUserUrl(),
                                "data", object.toString());
                        if(TextUtils.isEmpty(s)){
                            onSyncByUserListener.syncFinish(ERROR);
                            return;
                        }
                        JsonObject root = GsonUtil.getGson().fromJson(s,JsonObject.class);
                        if(!root.has("success")){
                            onSyncByUserListener.syncFinish(ERROR);
                            return;
                        }
                        if(root.get("success").getAsBoolean()){
                            syncDataBean.syncGetDataCallback(root);
                            onSyncByUserListener.syncFinish(SUCCESS);
                        }else{
                            onSyncByUserListener.syncFinish(ERROR);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onSyncByUserListener.syncFinish(ERROR);
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            onSyncByUserListener.syncFinish(ERROR);
        }
    }
    

    @Override
    public void onNetChange() {
        //如果网络重新连接
        if(OkHttpUtil.isNetworkAvailable(mContext)){
            //网络重新连接
            if(!isNetConnect) {
                isNetConnect = true;
                if(handlerThread == null){
                    handlerThread = new HandlerThread("DataSyncService");
                    handlerThread.start();
                    mHandler = new Handler(handlerThread.getLooper());
                }
                notifyNextData();
            }
        }
        //网络断开
        else{
            if(isNetConnect) {
                isNetConnect = false;
                handlerThread.quit();
                handlerThread = null;
                mCurrentO = null;
            }
        }
    }

    private void doSyncAdd(Operation operation){
        try {
            //已经被同步过了
            if(operation.getISOperation(mContext.getContentResolver()) != null
                    || operation.getUSOperation(mContext.getContentResolver()) != null){
                mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                return;
            }


            //查询D-N如果存在，说明数据在online下被删除
            Operation dn = operation.getDNOperation(mContext.getContentResolver());
            if(dn != null){
                SyncDataBean syncDataBean = operation.getData(mContext.getContentResolver());
                syncDataBean.syncDeleteCallback(null,operation.mDataId);

                //删除此数据所有的操作记录
                Operation.deleteOperation(mContext.getContentResolver(),operation.mTableName,operation.mDataId);
                mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                //把此数据所有的操作都改为同步状态
    //            ContentValues values = new ContentValues(1);
    //            values.put(Operation.STATE,Operation.SYNC);
    //            Operation.updateOperation(mContext.getContentResolver(),values,operation.mTableName,operation.mDataId);
            }else{
                Operation.setUpdateOperationStateSync(mContext.getContentResolver(),operation.mTableName,operation.mDataId);

                SyncDataBean syncData = operation.getData(mContext.getContentResolver());
                String result =  OkHttpUtil.postJson(syncData.getAddUrl(),"data",syncData.getAddData());
                if(TextUtils.isEmpty(result)){
                    mMainHandler.sendEmptyMessage(REQUEST_ERROR);
                    return;
                }
                JsonObject root = GsonUtil.getGson().fromJson(result,JsonObject.class);
                if(!root.has("success")){
                    mMainHandler.sendEmptyMessage(DATA_ERROR);
                    return;
                }
                if(root.get("success").getAsBoolean()){
                    //更新操作
                    Operation.setOperationStateSync(mContext.getContentResolver(),operation.mId);
                    root.remove("success");
                    syncData.syncAddCallback(root,operation.mDataId);
                    mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            mMainHandler.sendEmptyMessage(OTHER_ERROR);
        }
    }

    private void doSyncUpdate(Operation operation){
        try {

            //查询I-S如果存在，说明数据已经添加同步
            Operation is = operation.getISOperation(mContext.getContentResolver());
            if(is != null){
                //查询D-N如果存在，说明数据在online下被删除
                Operation dn = operation.getDNOperation(mContext.getContentResolver());
                if(dn != null){
                    doSyncDelete(operation);
                }else {
                    //执行更新同步
                    SyncDataBean syncData = operation.getData(mContext.getContentResolver());
                    String result = OkHttpUtil.postJson(syncData.getUpdateUrl(), "data", syncData.getUpdateData());
                    if(TextUtils.isEmpty(result)){
                        mMainHandler.sendEmptyMessage(REQUEST_ERROR);
                        return;
                    }
                    JsonObject root = GsonUtil.getGson().fromJson(result,JsonObject.class);
                    if(!root.has("success")){
                        mMainHandler.sendEmptyMessage(DATA_ERROR);
                        return;
                    }
                    if (root.get("success").getAsBoolean()) {
                        //更新操作
                        Operation.setOperationStateSync(mContext.getContentResolver(), operation.mId);
                        root.remove("success");
                        syncData.syncUpdateCallback(root,operation.mDataId);
                        mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                    }
                }
            }else{
                doSyncAdd(operation);
            }
        }catch (Exception e){
            e.printStackTrace();
            mMainHandler.sendEmptyMessage(OTHER_ERROR);
        }
    }

    public void doSyncDelete(Operation operation){

        try {
            //已经被同步过了
            if(operation.getDSOperation(mContext.getContentResolver()) != null) {
                mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                return;
            }
            Operation in = operation.getINOperation(mContext.getContentResolver());
            if(in != null){
                SyncDataBean syncDataBean = operation.getData(mContext.getContentResolver());
                syncDataBean.syncDeleteCallback(null,operation.mDataId);
                //删除此数据所有的操作记录
                Operation.deleteOperation(mContext.getContentResolver(),operation.mTableName,operation.mDataId);
                mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
    //            //把此数据所有的操作都改为同步状态
    //            ContentValues values = new ContentValues(1);
    //            values.put(Operation.STATE,Operation.SYNC);
    //            Operation.updateOperation(mContext.getContentResolver(),values,operation.mTableName,operation.mDataId);
            }else{
                SyncDataBean syncData = operation.getData(mContext.getContentResolver());
                String result =OkHttpUtil.postJson(syncData.getDeleteUrl(),"data",syncData.getDeleteData());
                if(TextUtils.isEmpty(result)){
                    mMainHandler.sendEmptyMessage(REQUEST_ERROR);
                    return;
                }
                JsonObject root = GsonUtil.getGson().fromJson(result, JsonObject.class);
                if(!root.has("success")){
                    mMainHandler.sendEmptyMessage(DATA_ERROR);
                    return;
                }
                if (root.get("success").getAsBoolean()) {
                    //更新操作

    //                //把此数据所有的操作都改为同步状态
    //                ContentValues values = new ContentValues(1);
    //                values.put(Operation.STATE,Operation.SYNC);
    //                Operation.updateOperation(mContext.getContentResolver(),values,operation.mTableName,operation.mDataId);
                    root.remove("success");
                    syncData.syncDeleteCallback(root,operation.mDataId);

                    //删除此数据所有的操作记录
                    Operation.deleteOperation(mContext.getContentResolver(),operation.mTableName,operation.mDataId);
                    mMainHandler.sendEmptyMessage(REQUEST_SUCCESS);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            mMainHandler.sendEmptyMessage(OTHER_ERROR);
        }
    }


    private class SyncRunnable implements Runnable{

        private Operation operation;
        public SyncRunnable(){
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        @Override
        public void run() {
            if(!isNetConnect){
                mMainHandler.sendEmptyMessage(NET_ERROR);
                return;
            }
            switch (operation.mOperation){
                case Operation.INSERT:
                    doSyncAdd(operation);
                    break;
                case Operation.UPDATE:
                    doSyncUpdate(operation);
                    break;
                case Operation.DELETE:
                    doSyncDelete(operation);
                    break;
            }
        }
    }

    private class SimpleQueue<E>{
        Object[] es;
        int front;
        int rear;
        int maxSize;
        public SimpleQueue(int size){
            maxSize = size;
            es =  new Object[maxSize];
            front = rear = 0;
        }

        int size(){
            return (rear - front +maxSize) % maxSize;
        }

        boolean enQueue(E e){
            if((rear + 1) % maxSize == front)
                return false;
            es[rear] = e;
            rear = (rear + 1) % maxSize;
            return true;
        }

        E DeQueue(){
            if(front == rear)
                return null;
            @SuppressWarnings("unchecked") E e = (E) es[front];
            front = (front +1) % maxSize;
            return e;
        }
    }

}
