/*
 * Copyright (C) 2008 The Android Open Source Project
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

/**
 * package-level logging flag
 */

package justita.top.timesecretary.uitl;

import android.util.Log;

public class LogUtils {

    public final static String LOGTAG = "LogUtils";
    public static boolean DEBUG = true;

    public static void v(String message, Object... args) {
        if (DEBUG) {
            Log.v(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void v(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.v(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }

    public static void d(String message, Object... args) {
        if (DEBUG) {
            Log.d(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void d(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.d(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }

    public static void i(String message, Object... args) {
        if (DEBUG) {
            Log.i(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void i(String tag, String message, Object... args) {
        if (DEBUG || Log.isLoggable(LOGTAG, Log.INFO)) {
            Log.i(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }

    public static void w(String message, Object... args) {
        if (DEBUG ) {
            Log.w(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void w(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.w(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }

    public static void e(String message, Object... args) {
        if (DEBUG) {
            Log.e(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void e(String tag, String message, Object... args) {
        if (DEBUG) {
            Log.e(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }

    public static void e(String message, Exception e) {
        if (DEBUG || Log.isLoggable(LOGTAG, Log.ERROR)) {
            Log.e(LOGTAG, message, e);
        }
    }

    public static void e(String tag, String message, Exception e) {
        if (DEBUG ) {
            Log.e(LOGTAG + "/" + tag, message, e);
        }
    }

    public static void wtf(String message, Object... args) {
        if (DEBUG ) {
            Log.wtf(LOGTAG, args == null ? message : String.format(message, args));
        }
    }

    public static void wtf(String tag, String message, Object... args) {
        if (DEBUG ) {
            Log.wtf(LOGTAG + "/" + tag, args == null ? message : String.format(message, args));
        }
    }
}
