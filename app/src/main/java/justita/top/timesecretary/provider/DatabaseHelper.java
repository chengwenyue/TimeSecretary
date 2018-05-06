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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import justita.top.timesecretary.R;
import justita.top.timesecretary.uitl.LogUtils;

/**
 * Helper class for opening the database from multiple providers.  Also provides
 * some common functionality.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static boolean  hasCreateTables = false;
    /**
     * Original Clock Database.
     **/
    private static final int VERSION_1 = 1;

    /**
     * Introduce:
     * Added alarm_instances table
     * Added selected_cities table
     * Added DELETE_AFTER_USE column to alarms table
     */
    private static final int VERSION_2 = 2;

    /**
     * Added alarm settings to instance table.
     */
    private static final int VERSION_3 = 3;

    // Database and table names
    public static final String DATABASE_NAME = "affairs.db";
    public static final String INSTANCES_TABLE_NAME = "alarm_instances";
    public static final String AFFAIRS_TABLE_NAME = "affairs";
    public static final String CATEGORIES_TABLE_NAME = "categories";
    public static final String OPERATIONS_TABLE_NAME = "operations";


    private static void createInstanceTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + INSTANCES_TABLE_NAME + " (" +
                DataContract.InstancesColumns._ID + " INTEGER PRIMARY KEY," +
                DataContract.InstancesColumns.YEAR + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.MONTH + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.DAY + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.HOUR + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.MINUTES + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.AFFAIR_ID + " INTEGER NOT NULL, " +
                DataContract.InstancesColumns.USER_ID +" INTEGER NOT NULL" +
                ");");
        LogUtils.i("Instance table created");
    }

    private static void createAffairTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + AFFAIRS_TABLE_NAME + " (" +
                DataContract.AffairColumns._ID + " INTEGER PRIMARY KEY ," +
                DataContract.AffairColumns.NAME + " VARCHAR(20) NOT NULL, " +
                DataContract.AffairColumns.TYPE + " VARCHAR(1) NOT NULL, " +
                DataContract.AffairColumns.CATEGORY + " VARCHAR(20) NOT NULL, " +
                DataContract.AffairColumns.REMARK + " VARCHAR(50) NOT NULL, " +
                DataContract.AffairColumns.POSITION + " VARCHAR(50) NOT NULL, " +
                DataContract.AffairColumns.TIME + " VARCHAR(20) NOT NULL, " +
                DataContract.AffairColumns.STATE + " INTEGER NOT NULL," +
                DataContract.AffairColumns.USER_ID + " INTEGER NOT NULL," +
                DataContract.AffairColumns.AFFAIR_ID + " INTEGER NOT NULL DEFAULT -1" +
                ");");
        LogUtils.i("affair table created");
    }

    private static void createCategoryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CATEGORIES_TABLE_NAME + " (" +
                DataContract.CategoryColumns._ID + " INTEGER PRIMARY KEY," +
                DataContract.CategoryColumns.NAME + " VARCHAR(20) NOT NULL, " +
                DataContract.CategoryColumns.COLOR + " INTEGER NOT NULL," +
                DataContract.CategoryColumns.USER_ID + " INTEGER NOT NULL ," +
                DataContract.CategoryColumns.CATEGORY_ID + " INTEGER NOT NULL " +
                ");");
        LogUtils.i("category table created");
    }

    private static void createOperationTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + OPERATIONS_TABLE_NAME + " (" +
                DataContract.OperationsColumns._ID + " INTEGER PRIMARY KEY," +
                DataContract.OperationsColumns.TABLE_NAME + " VARCHAR(15) NOT NULL, " +
                DataContract.OperationsColumns.DATA_ID + " INTEGER NOT NULL," +
                DataContract.OperationsColumns.USER_ID + " INTEGER NOT NULL ," +
                DataContract.OperationsColumns.OPERATION + " INTEGER NOT NULL ," +
                DataContract.OperationsColumns.STATE + " INTEGER NOT NULL ," +
                DataContract.OperationsColumns.TIME + " INTEGER NOT NULL " +
                ");");
        LogUtils.i("operation table created");
    }
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createInstanceTable(db);
        createAffairTable(db);
        createCategoryTable(db);
        createOperationTable(db);

        String cs = ", "; //comma and space
        String insertCategory  = "INSERT INTO " + CATEGORIES_TABLE_NAME + " (" +
                DataContract.CategoryColumns._ID + cs +
                DataContract.CategoryColumns.NAME + cs +
                DataContract.CategoryColumns.COLOR +cs+
                DataContract.CategoryColumns.USER_ID+cs+
                DataContract.CategoryColumns.CATEGORY_ID+
                ") VALUES ";

        //插入默认的分类
        db.execSQL(insertCategory + "(1,'默认',"+mContext.getResources().getColor(R.color.category_default)+",0,-1);");
        db.execSQL(insertCategory + "(2,'工作',"+mContext.getResources().getColor(R.color.category_work)+",0,-1);");
        hasCreateTables = true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
    }
}
