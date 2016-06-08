package peter.util.scandemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 0; // 请求码
    PermissionsChecker mPermissionsChecker;

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan:
                final String sdPath = Environment.getExternalStorageDirectory().getPath();
                if (!TextUtils.isEmpty(sdPath)) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            ArrayList<String> allPath = new ArrayList<String>();
                            allPath.add(sdPath);
                            allPath.add("/storage/9016-4EF8");

                            new ScanMusicFile().startScan(allPath, new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    String result = msg.getData().getString("msg");
                                    ((TextView) findViewById(R.id.result)).setText(result);
                                    ConcurrentHashMap<String, String> musics = (ConcurrentHashMap<String, String>) msg.getData().getSerializable("music");

                                    StringBuilder strB = new StringBuilder();
                                    int count = 0;
                                    for (Map.Entry<String, String> e : musics.entrySet()) {
                                        count++;
                                        strB.append(count + ") " + e.getValue() + "\n");
                                    }
                                    TextView musicList = ((TextView) findViewById(R.id.music_list));
                                    ;
                                    musicList.setText(strB.toString());
                                }
                            });
                        }
                    }).start();
                    ((TextView) findViewById(R.id.result)).setText("开始扫描...");
                }
                break;

            case R.id.ext_sd_path:
//                File[] files = new FileManager().getAllAvailableSDCards(this);
//                StringBuilder strB = new StringBuilder();
//                for(File f: files) {
//                    strB.append(f.toString() + "\n");
//                }

                File file = FileManager.getExternalSDCard(MainActivity.this);

                if(file != null) {
                    String path = file.toString();

//                    Uri uri = Uri.parse("https://www.baidu.com/");
//                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                    startActivity(intent);
                    Toast.makeText(MainActivity.this, path, Toast.LENGTH_LONG).show();
                }
                break;
        }

    }

    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase().contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase().contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    public static String getFirstExterPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static List<String> getAllExterSdcardPath() {
        List<String> SdList = new ArrayList<String>();

        String firstPath = getFirstExterPath();

        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // LogUtil.i("peak", "line-->"+line);
                // 将常见的linux分区过滤掉
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("media"))
                    continue;
                if (line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data")
                        || line.contains("tmpfs") || line.contains("shell")
                        || line.contains("root") || line.contains("acct")
                        || line.contains("proc") || line.contains("misc")
                        || line.contains("obb")) {
                    continue;
                }

                if (line.contains("fat") || line.contains("fuse")
                        || line.contains("ntfs")) {

                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        String path = columns[1].toLowerCase(Locale.getDefault());
                        if (path != null && !SdList.contains(path)
                                && path.contains("sd"))
                            SdList.add(columns[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!SdList.contains(firstPath)) {
            SdList.add(firstPath);
        }
        // LogUtil.i("peak", "size-->" + SdList.size());
        return SdList;
    }

}
