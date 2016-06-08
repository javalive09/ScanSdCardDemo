package peter.util.scandemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FileManager {

    public static boolean isAvailable(String state) {
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static File getAvailableRoot(File file) {
        if (file == null)
            return null;

        File root = file;
        while (isAvailable(Environment.getStorageState(root.getParentFile())))
            root = root.getParentFile();

        return root;
    }

    // Get All Available External Storage on your device
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static File[] getAllAvailableSDCards(Context context) {
        File[] files = context.getExternalCacheDirs();
        for (int i = 0; i < files.length; ++i) {
            files[i] = getAvailableRoot(files[i]);
        }
        return files;
    }

    public static File getExternalSDCard(Context context) {
        File files[] = getAllAvailableSDCards(context);
        if (files == null)
            return null;

        if (files.length == 1)
//            return files[0]; //Internal SDCard
            return null;
        else if (files.length == 2)
            return files[1]; //External SDCard
        return null;
    }
}