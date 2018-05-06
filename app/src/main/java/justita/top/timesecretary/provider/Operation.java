package justita.top.timesecretary.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Operation implements DataContract.OperationsColumns{
    public static final long INVALID_ID = -1;
    public static final int INSERT = 0x01;
    public static final int UPDATE = 0x02;
    public static final int DELETE = 0x03;

    public static final int SYNC = 0x11;
    public static final int NOT_SYNC = 0x12;

    public static Map<String,Class> orm = new HashMap<>();
    private static final String[] QUERY_COLUMNS = {
            _ID,
            TABLE_NAME,
            DATA_ID,
            USER_ID,
            OPERATION,
            STATE,
            TIME
    };

    static {
        Operation.orm.put(DatabaseHelper.CATEGORIES_TABLE_NAME,Category.class);
        Operation.orm.put(DatabaseHelper.AFFAIRS_TABLE_NAME,Affair.class);
    }
    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    private static final int ID_INDEX = 0;
    private static final int TABLE_NAME_INDEX = 1;
    private static final int DATA_ID_INDEX = 2;
    private static final int USER_ID_INDEX = 3;
    private static final int OPERATION_INDEX = 4;
    private static final int STATE_INDEX = 5;
    private static final int TIME_INDEX = 6;

    private static final int COLUMN_COUNT = TIME_INDEX + 1;

    public Operation() {

    }

    public static ContentValues createContentValues(Operation operation) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (operation.mId != INVALID_ID) {
            values.put(_ID, operation.mId);
        }

        values.put(TABLE_NAME, operation.mTableName);
        values.put(DATA_ID, operation.mDataId);
        values.put(USER_ID, operation.mUser_Id);
        values.put(OPERATION, operation.mOperation);
        values.put(STATE, operation.mState);
        values.put(TIME, operation.mTime);
        return values;
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    public static Uri getUri(long operationId) {
        return ContentUris.withAppendedId(CONTENT_URI, operationId);
    }

    public Uri getDataUri() {
        return ContentUris.withAppendedId(Uri.parse("content://" + DataContract.AUTHORITY +"/"+ mTableName), mDataId);
    }


    public static Operation getOperation(ContentResolver contentResolver, long operationId) {
        Cursor cursor = contentResolver.query(getUri(operationId), QUERY_COLUMNS, null, null, null);
        Operation result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Operation(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    private Operation getOperation(ContentResolver contentResolver, String ... selectionArgs ) {
        StringBuilder builder = new StringBuilder();
        builder.append(Operation.TABLE_NAME)
                .append(" = ? and ")
                .append(Operation.DATA_ID)
                .append(" = ? and ")
                .append(Operation.OPERATION)
                .append(" = ? and ")
                .append(Operation.STATE)
                .append(" = ? ");
        Cursor cursor = contentResolver.query(CONTENT_URI, QUERY_COLUMNS, builder.toString(), selectionArgs, null);
        Operation result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Operation(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    public Operation getDOperation(ContentResolver contentResolver){
        StringBuilder builder = new StringBuilder();
        builder.append(Operation.TABLE_NAME)
                .append(" = ? and ")
                .append(Operation.DATA_ID)
                .append(" = ? and ")
                .append(Operation.OPERATION)
                .append(" = ? ");
        Cursor cursor = contentResolver.query(CONTENT_URI,QUERY_COLUMNS,builder.toString(),new String[]{mTableName,mDataId+"",DELETE+""},null);
        Operation result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Operation(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }
    public Operation getDNOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",DELETE+"",NOT_SYNC+"");
    }

    public Operation getDSOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",DELETE+"",SYNC+"");
    }
    public Operation getINOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",INSERT+"",NOT_SYNC+"");
    }
    public Operation getISOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",INSERT+"",SYNC+"");
    }
    public Operation getUSOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",UPDATE+"",SYNC+"");
    }

    public Operation getUNOperation(ContentResolver contentResolver){
        return getOperation(contentResolver,mTableName,mDataId+"",UPDATE+"",NOT_SYNC+"");
    }


    public static Operation getLastOperations(ContentResolver contentResolver,
                                                String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, TIME+" asc");
        Operation result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Operation(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static List<Operation> getOperations(ContentResolver contentResolver,
                                                String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, TIME+" desc");
        List<Operation> result = new LinkedList<Operation>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new Operation(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static Operation addOperation(ContentResolver contentResolver,
                                        Operation operation) {
        ContentValues values = createContentValues(operation);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        operation.mId = getId(uri);
        return operation;
    }

    public static boolean updateOperation(ContentResolver contentResolver, ContentValues values,long operationId ) {
        if (operationId == INVALID_ID) return false;
        long rowsUpdated = contentResolver.update(getUri(operationId), values, null, null);
        return rowsUpdated == 1;
    }

    public static boolean setOperationStateSync(ContentResolver contentResolver, long operationId){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Operation.STATE,Operation.SYNC );
        return Operation.updateOperation(contentResolver,contentValues,operationId);
    }

    public static void setUpdateOperationStateSync(ContentResolver contentResolver,String tableName,long dataId){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Operation.STATE,Operation.SYNC );

        StringBuilder builder = new StringBuilder();
        builder.append(TABLE_NAME)
                .append(" = ? and ")
                .append(DATA_ID)
                .append(" = ? and ")
                .append(OPERATION)
                .append(" = ? and " )
                .append(TIME)
                .append(" <= ? ");
        contentResolver.update(CONTENT_URI,contentValues,builder.toString(),
                new String[]{tableName,dataId+"",Operation.UPDATE+"",System.currentTimeMillis()+""});
    }

    public static void updateOperation(ContentResolver contentResolver,ContentValues contentValues, String tableName,long dataId) {
        if (dataId == INVALID_ID) return ;
        contentResolver.update(CONTENT_URI, contentValues,
                DATA_ID +" = ? and "+TABLE_NAME +" = ? ", new String[]{dataId +"",tableName});
    }

    public static boolean deleteOperation(ContentResolver contentResolver, long operationId) {
        if (operationId == INVALID_ID) return false;
        int deletedRows = contentResolver.delete(getUri(operationId), "", null);
        return deletedRows == 1;
    }

    public static void deleteOperation(ContentResolver contentResolver, String tableName,long dataId) {
        if (dataId == INVALID_ID) return;
        contentResolver.delete(CONTENT_URI, DATA_ID +" = ? and "+TABLE_NAME+" = ? ", new String[]{dataId +"",tableName});
    }


    // Public fields
    public long mId = INVALID_ID;
    public String mTableName;
    public long mDataId;
    public long mUser_Id = INVALID_ID;
    public int mOperation;
    public int mState;
    public long mTime;


    public Operation(Cursor c) {
        mId = c.getLong(ID_INDEX);
        mTableName = c.getString(TABLE_NAME_INDEX);
        mDataId = c.getLong(DATA_ID_INDEX);
        mUser_Id = c.getLong(USER_ID_INDEX);
        mOperation = c.getInt(OPERATION_INDEX);
        mState = c.getInt(STATE_INDEX);
        mTime = c.getLong(TIME_INDEX);
    }



    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Operation)) return false;
        final Operation other = (Operation) o;
        return mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mId).hashCode();
    }

    @Override
    public String toString() {
        return "Operation{" +
                "mId=" + mId +
                ", mTableName=" + mTableName +
                ", mDataId=" + mDataId +
                ", mUser_Id=" + mUser_Id +
                ", mOperation=" + mOperation +
                ", mState=" + mState +
                ", mTime=" + mTime +
                '}';
    }

    public SyncDataBean getData(ContentResolver contentResolver){

        try {
            Class c = orm.get(mTableName);
            Field f = c.getDeclaredField("QUERY_COLUMNS");
            f.setAccessible(true);
            String[] strings = (String[]) f.get(c);
            Cursor cursor = contentResolver.query(getDataUri(),strings,null,new String[0],null);
            if(cursor != null && !cursor.moveToFirst())
                return null;

            @SuppressWarnings("unchecked")
            Constructor<SyncDataBean> con = c.getConstructor(Cursor.class);
            SyncDataBean t = con.newInstance(cursor);

            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
