package peter.util.scandemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
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
    private ExecutorService mService = Executors.newFixedThreadPool(128);
    private ConcurrentHashMap<String, String> mAllMusic = new ConcurrentHashMap<String, String>(128);
    private final AtomicLong mTaskCounter = new AtomicLong();
    private final AtomicLong mFileCounter = new AtomicLong();
    private long mStartTime;

    private static final String filterDirs = "dcim,extracted,documents,lost.dir,screenshot,"
            + "shootme,vpn,system,sysdata,sohu,tencent,wandoujia,taobao,sina,pictures,"
            + "netease,jingdong,mishop,immomo,duokan,dccache,didi,"
            + "baidumap,baidunavisdk,"
            + "miui,xiami,image,images,miliao,logs,cacheimages,apk,video,tempvideo,"
            + "wallpaper,imagecache,imagescache,amap,alipay,ringtones,youku,iqiyi,"
            + "libs,soufun,wuba,msc,youdao,jdim,icbc,ctrip,cmb,autohomemain,"
            + "autonvi,gkoudai,haodou,harvestfund,jtyh,lufax,meituanwaimai,wmlogger,"
            + "mtklog,aliyun,sdk,videocache,ripple,iamge_cache,netlog,qiyivideo,"
            + "tianqitong,36kr";

    private void scanDir(final String path) {
        mTaskCounter.incrementAndGet();
        mService.execute(new Runnable() {
            @Override
            public void run() {
//                Log.i(TAG, "path =" + path);
                scanFile(path);
            }
        });
    }

    private synchronized boolean pass(String str) {
        boolean result = !filterDirs.contains(str);
        return result;
    }

    private void scanFile(final String path) {
        try {
            File file = new File(path);
            if (file.isDirectory()) {
                if(pass(file.getName().toLowerCase())) {
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
                }
            } else {
                checkFile(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mTaskCounter.decrementAndGet();
        }
    }

    private void checkFile(File file) {
        mFileCounter.incrementAndGet();
        Matcher matcher = mPattern.matcher(file.getName());
        if (matcher.matches()) {
            String fileExtension = matcher.group(2);
            if (isMusic(fileExtension)) {
                mAllMusic.put(file.getName(), file.getAbsolutePath());
            }
        }
    }

    private boolean isMusic(String extension) {
        if (extension == null) {
            return false;
        }
        final String ext = extension.toLowerCase();
        if (ext.equals("mp3") || ext.equals("wav") || ext.equals("3gp") || ext.equals("wma") || ext.equals("flac")) {
            return true;
        }
        return false;
    }

    public void startScan(ArrayList<String> paths, Handler handler) {
        mStartTime = System.currentTimeMillis();
        try {
            for (String path : paths) {
                scanDir(path);
            }
            while (mTaskCounter.get() > 0) {
                Thread.sleep(100);
            }

            String costTime = "cost time >>>>>>>>" + (System.currentTimeMillis() - mStartTime) / 1000.0 + " sed";
            String scanCount = "scan file count = " + mFileCounter.get();
            String musicCount = "music count = " + mAllMusic.size();
            Log.i(TAG, costTime);
            Log.i(TAG, scanCount);
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putString("msg", costTime + "\n" + scanCount + "\n" + musicCount);
            data.putSerializable("music", mAllMusic);
            msg.setData(data);
            handler.sendMessage(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mService.shutdown();
        }
    }

}
