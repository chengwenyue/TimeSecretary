/*
 * Copyright (C) 2013 The Android Open Source Project
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

package justita.top.timesecretary.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import justita.top.timesecretary.uitl.LogUtils;
import justita.top.timesecretary.uitl.PreferenceConstants;
import justita.top.timesecretary.uitl.PreferenceUtils;


public class DataProvider extends ContentProvider {
    private DatabaseHelper mOpenHelper;

    private static final int INSTANCES = 1;
    private static final int INSTANCES_ID = 2;

    private static final int AFFAIRS = 3;
    private static final int AFFAIRS_ID = 4;

    private static final int CATEGORY = 5;
    private static final int CATEGORY_ID = 6;

    private static final int OPERATION = 7;
    private static final int OPERATION_ID = 8;

    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI(DataContract.AUTHORITY, "instances", INSTANCES);
        sURLMatcher.addURI(DataContract.AUTHORITY, "instances/#", INSTANCES_ID);

        sURLMatcher.addURI(DataContract.AUTHORITY, "affairs", AFFAIRS);
        sURLMatcher.addURI(DataContract.AUTHORITY, "affairs/#", AFFAIRS_ID);

        sURLMatcher.addURI(DataContract.AUTHORITY, "categories", CATEGORY);
        sURLMatcher.addURI(DataContract.AUTHORITY, "categories/#", CATEGORY_ID);

        sURLMatcher.addURI(DataContract.AUTHORITY, "operations", OPERATION);
        sURLMatcher.addURI(DataContract.AUTHORITY, "operations/#", OPERATION_ID);
    }

    public DataProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs,
            String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(uri);
        switch (match) {
            case INSTANCES:
                qb.setTables(DatabaseHelper.INSTANCES_TABLE_NAME);
                qb.appendWhere(DataContract.InstancesColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            case INSTANCES_ID:
                qb.setTables(DatabaseHelper.INSTANCES_TABLE_NAME);
                qb.appendWhere(DataContract.InstancesColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                qb.appendWhere(" and "+ DataContract.InstancesColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            case AFFAIRS:
                qb.setTables(DatabaseHelper.AFFAIRS_TABLE_NAME);
                qb.appendWhere(DataContract.AffairColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            case AFFAIRS_ID:
                qb.setTables(DatabaseHelper.AFFAIRS_TABLE_NAME);
                qb.appendWhere(DataContract.AffairColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                qb.appendWhere(" and "+ DataContract.AffairColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            case CATEGORY:
                qb.setTables(DatabaseHelper.CATEGORIES_TABLE_NAME);
                break;
            case CATEGORY_ID:
                qb.setTables(DatabaseHelper.CATEGORIES_TABLE_NAME);
                qb.appendWhere(DataContract.CategoryColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                break;
            case OPERATION:
                qb.setTables(DatabaseHelper.OPERATIONS_TABLE_NAME);
                qb.appendWhere(DataContract.OperationsColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            case OPERATION_ID:
                qb.setTables(DatabaseHelper.OPERATIONS_TABLE_NAME);
                qb.appendWhere(DataContract.OperationsColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                qb.appendWhere(DataContract.OperationsColumns.USER_ID + "=");
                qb.appendWhere(PreferenceUtils.getPrefLong(getContext(), PreferenceConstants.USERID,-1)+"");
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();


        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);

        if (ret == null) {
            LogUtils.e("query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return ret;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURLMatcher.match(uri);
        switch (match) {
            case INSTANCES:
                return "vnd.android.cursor.dir/instances";
            case INSTANCES_ID:
                return "vnd.android.cursor.item/instances";
            case AFFAIRS:
                return "vnd.android.cursor.dir/affairs";
            case AFFAIRS_ID:
                return "vnd.android.cursor.item/affairs";
            case CATEGORY:
                return "vnd.android.cursor.dir/categories";
            case CATEGORY_ID:
                return "vnd.android.cursor.item/categories";
            case OPERATION:
                return "vnd.android.cursor.dir/operations";
            case OPERATION_ID:
                return "vnd.android.cursor.item/operations";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count;
        String id;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case INSTANCES_ID:
                id = uri.getLastPathSegment();
                count = db.update(DatabaseHelper.INSTANCES_TABLE_NAME, values,
                        DataContract.InstancesColumns._ID + "=" + id,
                        null);
                break;
            case AFFAIRS_ID:
                id = uri.getLastPathSegment();
                count = db.update(DatabaseHelper.AFFAIRS_TABLE_NAME, values,
                        DataContract.AffairColumns._ID + "=" + id,
                        null);
                break;
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                count = db.update(DatabaseHelper.CATEGORIES_TABLE_NAME, values,
                        DataContract.CategoryColumns._ID + "=" + id,
                        null);
                break;
            case OPERATION_ID:
                id = uri.getLastPathSegment();
                count = db.update(DatabaseHelper.OPERATIONS_TABLE_NAME, values,
                        DataContract.OperationsColumns._ID + "=" + id,
                        null);
                break;
            case OPERATION:
                id = uri.getLastPathSegment();
                count = db.update(DatabaseHelper.OPERATIONS_TABLE_NAME, values,where, whereArgs);
                break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        LogUtils.v("*** notifyChange() id: " + id + " url " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        long rowId;
        Uri uriResult;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case INSTANCES:
                rowId = db.insert(DatabaseHelper.INSTANCES_TABLE_NAME, null, initialValues);
                uriResult = ContentUris.withAppendedId(DataContract.InstancesColumns.CONTENT_URI, rowId);
                break;
            case AFFAIRS:
                rowId = db.insert(DatabaseHelper.AFFAIRS_TABLE_NAME, null, initialValues);
                uriResult = ContentUris.withAppendedId(DataContract.AffairColumns.CONTENT_URI, rowId);
                break;
            case CATEGORY:
                rowId = db.insert(DatabaseHelper.CATEGORIES_TABLE_NAME, null, initialValues);
                uriResult = ContentUris.withAppendedId(DataContract.CategoryColumns.CONTENT_URI, rowId);
                break;
            case OPERATION:
                rowId = db.insert(DatabaseHelper.OPERATIONS_TABLE_NAME, null, initialValues);
                uriResult = ContentUris.withAppendedId(DataContract.OperationsColumns.CONTENT_URI, rowId);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL: " + uri);
        }
        getContext().getContentResolver().notifyChange(uriResult, null);
        return uriResult;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int count;
        String primaryKey;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case INSTANCES:
                count = db.delete(DatabaseHelper.INSTANCES_TABLE_NAME, where, whereArgs);
                break;
            case INSTANCES_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataContract.InstancesColumns._ID + "=" + primaryKey;
                } else {
                    where = DataContract.InstancesColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(DatabaseHelper.INSTANCES_TABLE_NAME, where, whereArgs);
                break;
            case AFFAIRS:
                count = db.delete(DatabaseHelper.AFFAIRS_TABLE_NAME, where, whereArgs);
                break;
            case AFFAIRS_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataContract.AffairColumns._ID + "=" + primaryKey;
                } else {
                    where = DataContract.AffairColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(DatabaseHelper.AFFAIRS_TABLE_NAME, where, whereArgs);
                break;
            case CATEGORY:
                count = db.delete(DatabaseHelper.CATEGORIES_TABLE_NAME, where, whereArgs);
                break;
            case CATEGORY_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataContract.CategoryColumns._ID + "=" + primaryKey;
                } else {
                    where = DataContract.CategoryColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(DatabaseHelper.CATEGORIES_TABLE_NAME, where, whereArgs);
                break;

            case OPERATION:
                count = db.delete(DatabaseHelper.OPERATIONS_TABLE_NAME, where, whereArgs);
                break;
            case OPERATION_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(where)) {
                    where = DataContract.OperationsColumns._ID + "=" + primaryKey;
                } else {
                    where = DataContract.OperationsColumns._ID + "=" + primaryKey +
                            " AND (" + where + ")";
                }
                count = db.delete(DatabaseHelper.OPERATIONS_TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
