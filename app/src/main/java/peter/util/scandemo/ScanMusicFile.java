package peter.util.scandemo;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanMusicFile {
    private static final String TAG = "ScanMusicFile";
    private static final Pattern mPattern = Pattern.compile("([^\\.]*)\\.([^\\.]*)");
    private ExecutorService service = Executors.newFixedThreadPool(128);
    private ConcurrentHashMap<String, String> filterFiles_music = new ConcurrentHashMap<String, String>(1024);
    private final AtomicLong counter = new AtomicLong();
    private long startTime;

    private void scanDir(final String path) {
        counter.incrementAndGet();
        service.execute(new Runnable() {
            @Override
            public void run() {
                scanFile(path);
            }
        });
    }

    private void scanFile(final String path) {
        try {
            File file = new File(path);
            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                if (fileList != null) {
                    for (File currFile : fileList) {
                        if (currFile.isFile()) {
                            checkFile(currFile);
                        } else {
                            scanDir(currFile.getAbsolutePath());
                        }
                    }
                }
            } else {
                checkFile(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            counter.decrementAndGet();
        }
    }

    private void checkFile(File file) {
        Matcher matcher = mPattern.matcher(file.getName());
        if(matcher.matches()) {
            String fileExtension = matcher.group(2);
            if(isMusic(fileExtension)) {
                filterFiles_music.put(file.getName(), file.getAbsolutePath());
            }
        }
    }

    private boolean isMusic(String extension) {
        if (extension == null) {
            return false;
        }
        final String ext = extension.toLowerCase();
        if (ext.equals("mp3") || ext.equals("wav") || ext.equals("3gp") || ext.equals("ota") ||
                ext.equals("aac") || ext.equals("mid") || ext.equals("midi") || ext.equals("ogg") ||
                ext.equals("wma") || ext.equals("ra") || ext.equals("mka") || ext.equals("mkv") ||
                ext.equals("m4a") || ext.equals("flac") || ext.equals(".rtx")) {
            return true;
        }
        return false;
    }

    public void startScan(String path, Handler handler) {
        startTime = System.currentTimeMillis();
        try {
            scanDir(path);
            while (counter.get() > 0) {
                Thread.sleep(100);
            }

            Log.i(TAG, "end >>>>>>>>");
            Log.i(TAG, "cost time >>>>>>>>" + (System.currentTimeMillis() - startTime)/ 1000.0 + " sed");
            Log.i(TAG, "scan file count = " + filterFiles_music.size());

            handler.sendEmptyMessage(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            service.shutdown();
        }
    }

}
