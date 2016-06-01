package peter.util.scandemo;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.go).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
//        EditText path = (EditText) findViewById(R.id.path);
        final String sdPath = Environment.getExternalStorageDirectory().getPath();
//        final String sdPath = path.getText().toString().trim();
        if(!TextUtils.isEmpty(sdPath)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    new ScanFileTypeList().startScan(sdPath, new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            Toast.makeText(MainActivity.this, "扫描完成!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
            Toast.makeText(MainActivity.this, "开始扫描", Toast.LENGTH_SHORT).show();
        }

    }

}
