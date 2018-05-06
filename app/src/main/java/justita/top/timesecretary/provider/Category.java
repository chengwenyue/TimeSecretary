package justita.top.timesecretary.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import justita.top.timesecretary.app.BaseApp;
import justita.top.timesecretary.service.DataSyncService;
import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;
import justita.top.timesecretary.uitl.Utils;

public class Category implements DataContract.CategoryColumns,SyncDataBean {
    public static final long INVALID_ID = -1;

    private static final String[] QUERY_COLUMNS = {
            _ID,
            NAME,
            COLOR,
            USER_ID,
            CATEGORY_ID
    };
    static {
        Operation.orm.put(DatabaseHelper.CATEGORIES_TABLE_NAME,Category.class);
    }
    /**
     * These save calls to cursor.getColumnIndexOrThrow()
     * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
     */
    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int COLOR_INDEX = 2;
    private static final int USER_ID_INDEX = 3;
    private static final int CATEGORY_ID_INDEX = 4;

    private static final int COLUMN_COUNT = CATEGORY_ID_INDEX + 1;

    public Category() {

    }

    public static ContentValues createContentValues(Category category) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (category.mId != INVALID_ID) {
            values.put(_ID, category.mId);
        }

        values.put(NAME, category.mName);
        values.put(COLOR, category.mColor);
        values.put(USER_ID, category.mUser_Id);
        values.put(CATEGORY_ID, category.mCategoryId);
        return values;
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    public static Uri getUri(long categoryId) {
        return ContentUris.withAppendedId(CONTENT_URI, categoryId);
    }


    public static Category getCategory(ContentResolver contentResolver, long categoryId) {
        Cursor cursor = contentResolver.query(getUri(categoryId), QUERY_COLUMNS, null, null, null);
        Category result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new Category(cursor);

                if(DataSyncService.isDataDelete(BaseApp.getInstance(),Category.class,result.mId)){
                    return null;
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static List<Category> getCategories(ContentResolver contentResolver,
                                                   String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<Category> result = new LinkedList<Category>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    Category category =new Category(cursor);
                    if(!DataSyncService.isDataDelete(BaseApp.getInstance(),Category.class,category.mId)){
                        result.add(category);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static Category addCategory(ContentResolver contentResolver,
                                       Category category) {
        ContentValues values = createContentValues(category);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        category.mId = getId(uri);
        DataSyncService.addInsertOperation(BaseApp.getInstance(),Category.class,category.mId,Operation.NOT_SYNC);
        return category;
    }

    public static void deleteCategory(ContentResolver contentResolver, long categoryId) {
        if (categoryId == INVALID_ID) return ;
        DataSyncService.addDeleteOperation(BaseApp.getInstance(),Category.class,categoryId,Operation.NOT_SYNC);
    }

    // Public fields
    public long mId = INVALID_ID;
    public String mName;
    public int mColor;
    public long mUser_Id = INVALID_ID;
    public long mCategoryId = INVALID_ID;

    public Category(Cursor c) {
        mId = c.getLong(ID_INDEX);
        mName = c.getString(NAME_INDEX);
        mColor = c.getInt(COLOR_INDEX);
        mUser_Id = c.getLong(USER_ID_INDEX);
        mCategoryId = c.getLong(CATEGORY_ID_INDEX);
    }



    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Category)) return false;
        final Category other = (Category) o;
        return mId == other.mId;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mId).hashCode();
    }

    @Override
    public String toString() {
        return "Category {" +
                "mId=" + mId +
                ", mName=" + mName +
                ", mColor=#" + Integer.toHexString(mColor) +
                ", mUser_Id=" + mUser_Id +
                '}';
    }

    @Override
    public String getAddData() {
        JSONObject o = new JSONObject();
        try {
            o.put(_ID,mId);
            o.put(USER_ID,mUser_Id);
            o.put(NAME,mName);
            o.put(COLOR,mColor);
            LogUtils.e(o.toString());
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getUpdateData() {
        return null;
    }

    @Override
    public String getDeleteData() {
        JSONObject o = new JSONObject();
        try {
            o.put(_ID,mId);
            o.put(USER_ID,mUser_Id);
            LogUtils.e(o.toString());
            return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Category JsonToCategory(JsonObject jsonObject){
        Category category = new Category();

        category.mName = jsonObject.get(NAME).getAsString();

        category.mUser_Id = PreferenceUtils.getPrefLong(BaseApp.getInstance(),
                PreferenceConstants.USERID,-1);

        category.mColor = jsonObject.get(COLOR).getAsInt();
        category.mCategoryId = jsonObject.get(CATEGORY_ID).getAsLong();
        category.mId = jsonObject.get(_ID).getAsLong();
        return category;
    }
    @Override
    public String getAddUrl() {
        return Utils.webUrl+"category_addCategory";
    }

    @Override
    public String getUpdateUrl() {
        return null;
    }

    @Override
    public String getDeleteUrl() {
        return Utils.webUrl+"category_delCategory";
    }

    @Override
    public String getSyncDataByUserUrl() {
        return Utils.webUrl+"category_queryByUserId";
    }

    @Override
    public void syncAddCallback(JsonObject jsonObject, long dataId) {
        ContentValues values = new ContentValues();
        values.put(CATEGORY_ID,jsonObject.get(CATEGORY_ID).getAsLong());
        //只更新本地数据
        BaseApp.getInstance().getContentResolver().update(getUri(dataId), values, null, null);
    }

    @Override
    public void syncUpdateCallback(JsonObject jsonObject, long dataId) {
    }

    @Override
    public void syncDeleteCallback(JsonObject jsonObject, long dataId) {
        BaseApp.getInstance().getContentResolver().delete(getUri(dataId), "", null);
    }

    @Override
    public void syncGetDataCallback(JsonObject jsonObject) {
        JsonArray categories = jsonObject.getAsJsonArray("categories");
        for(int i = 0 ;i<categories.size();i++){
            JsonObject category = categories.get(i).getAsJsonObject();
            long _id = category.get(_ID).getAsLong();
            if(Category.getCategory(BaseApp.getInstance().getContentResolver(), _id) != null){
                DataSyncService.addUpdateOperation(BaseApp.getInstance(),Category.class,_id,Operation.NOT_SYNC);
            }else{
                Category temp = Category.JsonToCategory(category);
                ContentValues values = createContentValues(temp);
                BaseApp.getInstance().getContentResolver().insert(CONTENT_URI, values);
                DataSyncService.addInsertOperation(BaseApp.getInstance(),Category.class,_id,Operation.SYNC);
            }
        }
    }

}
