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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * <p>
 * The contract between the clock provider and desk clock. Contains
 * definitions for the supported URIs and data columns.
 * </p>
 * <h3>Overview</h3>
 * <p>
 * DataContract defines the data model of clock related information.
 * This data is stored in a number of tables:
 * </p>
 * <ul>
 * <li>The {@link InstancesColumns} table holds the current state of each
 * alarm in the AlarmsColumn table.
 */
public final class DataContract {
    /**
     * This authority is used for writing to or querying from the clock
     * provider.
     */
    public static final String AUTHORITY = "top.justita.timesecretary";

    /**
     * This utility class cannot be instantiated
     */
    private DataContract() {}

    /**
     * Constants for tables with AlarmSettings.
     */
    private interface AlarmSettingColumns extends BaseColumns {
        /**
         * This string is used to indicate no ringtone.
         */
        public static final Uri NO_RINGTONE_URI = Uri.EMPTY;

        /**
         * This string is used to indicate no ringtone.
         */
        public static final String NO_RINGTONE = NO_RINGTONE_URI.toString();



        /**
         * 某个用户
         */
        public static final String USER_ID = "userId";

        /**
         * True if alarm should vibrate
         * <p>Type: BOOLEAN</p>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Alarm label.
         *
         * <p>Type: STRING</p>
         */
        public static final String LABEL = "label";

        /**
         * Audio alert to play when alarm triggers. Null entry
         * means use system default and entry that equal
         * Uri.EMPTY.toString() means no ringtone.
         *
         * <p>Type: STRING</p>
         */
        public static final String RINGTONE = "ringtone";
    }
    public interface AffairColumns extends  AlarmSettingColumns ,BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/affairs");

        /**
         * 未开始状态
         */
        public static final int AFFAIR_SILENT_STATE = 1;
        /**
         * 进行中状态
         */
        public static final int AFFAIR_FIRED_STATE = 2;
        /**
         * 推迟状态
         */
        public static final int AFFAIR_SNOOZE_STATE = 3;

        /**
         * 完成状态
         */
        public static final int AFFAIR_COMPLETE_STATE = 4;

        /**
         * 删除状态
         */
        public static final int AFFAIR_DELETE_STATE = 5;

        public static final String NAME = "affairName";

        /**
         * 1. 普通事件  2. 普通事件 有开始提醒 3.时间轴事件 4.时间轴事件 有开始提醒 5.时间轴事件 有结束提醒
         */
        public static final String TYPE = "affairType";

        /**
         * 事件分类 例如 个人事务 杂货列表 工作项目等
         */
        public static final String CATEGORY = "affairCgy";

        /**
         * 备注 将多段附加信息 变成json字符串
         */
        public static final String REMARK = "affairRemark";

        /**
         * 位置
         */
        public static final String POSITION = "affairPsn";



        /**
         * 时间格式 yyyy-MM-dd HH:mm--HH:mm 时间分隔符前为开始时间，之后的为结束时间
         */
        public static final String TIME = "affairTime";

        /**状态
         *
         */
        public static final String STATE = "affairState";

        /**同步状态 -1 未同步 其他 已同步
         *
         */
        public static final String AFFAIR_ID = "affairId";
    }

    public interface CategoryColumns extends  AlarmSettingColumns ,BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/categories");

        public static final String NAME = "categoryName";

        public static final String COLOR = "categoryColor";

        public static final String USER_ID = "userId";

        /**
         * 同步状态 -1 未同步 其他 已同步
         */
        public static final String CATEGORY_ID = "categoryId";
    }

    public interface OperationsColumns extends  AlarmSettingColumns ,BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/operations");


        /**
         * 操作的表名
         */
        public static final String TABLE_NAME = "tableName";

        /**
         * 记录id
         */
        public static final String DATA_ID = "dataId";

        /**
         * 操作的方式
         * Insert ,Update ,Delete
         */
        public static final String OPERATION = "operationName";

        /**
         * 同步状态
         * Sync ,Not-Sync
         */
        public static final String STATE = "operationState";


        public static final String TIME = "operationTime";
    }

    /**
     * Constants for the Instance table, which contains the state of each alarm.
     */
    protected interface InstancesColumns extends AlarmSettingColumns, BaseColumns {
        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/instances");


        /**
         * Alarm year.
         *
         * <p>Type: INTEGER</p>
         */
        public static final String YEAR = "year";

        /**
         * Alarm month in year.
         *
         * <p>Type: INTEGER</p>
         */
        public static final String MONTH = "month";

        /**
         * Alarm day in month.
         *
         * <p>Type: INTEGER</p>
         */
        public static final String DAY = "day";

        /**
         * Alarm hour in 24-hour localtime 0 - 23.
         * <p>Type: INTEGER</p>
         */
        public static final String HOUR = "hour";

        /**
         * Alarm minutes in localtime 0 - 59
         * <p>Type: INTEGER</p>
         */
        public static final String MINUTES = "minutes";

        /**
         * Foreign key to Alarms table
         * <p>Type: INTEGER (long)</p>
         */
        public static final String AFFAIR_ID = "affair_id";

    }
}
