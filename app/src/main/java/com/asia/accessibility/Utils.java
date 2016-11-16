package com.asia.accessibility;

import java.io.File;

/**
 * Created by asia on 10/12/16.
 */

public class Utils {
    public static final String TAG = "Utils";

    public static boolean checkRooted() {
        boolean result = false;
        try {
            result = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
