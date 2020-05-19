package com.wind.frida.xposed.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import dalvik.system.DexFile;

/**
 * @author Windysha
 */
public class AppUtils {

    private static final String TAG = "AppUtils";

    public static Context createAppContext() {

//        LoadedApk.makeApplication()
//        ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);

        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);

            Object activityThreadObj = currentActivityThreadMethod.invoke(null);

            Field boundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
            boundApplicationField.setAccessible(true);
            Object mBoundApplication = boundApplicationField.get(activityThreadObj);   // AppBindData

            Field infoField = mBoundApplication.getClass().getDeclaredField("info");   // info
            infoField.setAccessible(true);
            Object loadedApkObj = infoField.get(mBoundApplication);  // LoadedApk

            Class contextImplClass = Class.forName("android.app.ContextImpl");
            Method createAppContextMethod = contextImplClass.getDeclaredMethod("createAppContext", activityThreadClass, loadedApkObj.getClass());
            createAppContextMethod.setAccessible(true);
            Object context = createAppContextMethod.invoke(null, activityThreadObj, loadedApkObj);

            if (context instanceof Context) {
                return (Context) context;
            }

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            Log.e(TAG, " createAppContext failed, msg = " + e.getMessage());
        }
        return null;
    }

    public static boolean classNameContainedInStackTrace(String className) {
        Throwable throwable = new Throwable();
        StackTraceElement[] array = throwable.getStackTrace();
        for (StackTraceElement element : array) {
            String name = element.getClassName();
            if (name != null && name.contains(className)) {
                return true;
            }
        }
        return false;
    }

    public static String getPluginApkPath() {
        try {
            Field fieldPathList = AppUtils.class.getClassLoader().getClass().getSuperclass().getDeclaredField("pathList");
            fieldPathList.setAccessible(true);
            Object dexPathList = fieldPathList.get(AppUtils.class.getClassLoader());

            Field fieldDexElements = dexPathList.getClass().getDeclaredField("dexElements");
            fieldDexElements.setAccessible(true);
            Object[] dexElements = (Object[]) fieldDexElements.get(dexPathList);
            Object dexElement = dexElements[0];

            Field fieldDexFile = dexElement.getClass().getDeclaredField("dexFile");
            fieldDexFile.setAccessible(true);
            Object dexFile = fieldDexFile.get(dexElement);
            String apkPath = ((DexFile) dexFile).getName();

            // 8.0以及以上Element类中才有getDexPath这个接口
//            Method method = dexElement.getClass().getDeclaredMethod("getDexPath");
//            method.setAccessible(true);
//            String apkPath = (String) method.invoke(dexElement);

            return apkPath;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "getPluginApkPath failed, msg --> " + e.getMessage(), e);
        }
        return "";
    }

    public static final void ensurePathExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 获取当前进程名
     */
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void copyFile(File src, File dest) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(src);
            outputStream = new FileOutputStream(dest);
            copyLarge(inputStream, outputStream);
        } finally {
            safeClose(inputStream);
            safeClose(outputStream);
        }
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.e("FileUtil", "Empty Catch on safeClose", e);
            }
        }
    }

    public static Boolean checkIfFileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }

    public static void createDirectoryIfNeeded(String filePath) {
        if (checkIfFileExists(filePath)) {
            return;
        }
        File file = new File(filePath);
        file.mkdir();
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        if (oldPath == null || oldPath.trim().isEmpty() || newPath == null || newPath.trim().isEmpty()) {
            Log.d(TAG, "oldPath or newPath invalid: " + oldPath + " " + newPath);
            return;
        }
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            Log.d(TAG, "oldPath is not exists");
            return;
        }
        File newFile = new File(newPath);
        createDirectoryIfNeeded(newFile.getParentFile().getAbsolutePath());
        try {
            copyFile(oldFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "copyFile finished");
    }
}
