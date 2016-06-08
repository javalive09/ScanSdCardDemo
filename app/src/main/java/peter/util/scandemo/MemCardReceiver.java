package peter.util.scandemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

public class MemCardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            onMemcardMounted(context, intent);
        }
        else if (!Environment.getExternalStorageState().equals(Environment.MEDIA_CHECKING)){
            onMemorycardUnMounted(context, intent);
        }
    }

    private void onMemorycardUnMounted(Context context, Intent intent) {
        Toast.makeText(context,  "onMemorycardUnMounted", Toast.LENGTH_LONG).show();
    }

    private void onMemcardMounted(Context context, Intent intent) {
        Toast.makeText(context, "onMemcardMounted" + intent.toString(), Toast.LENGTH_SHORT).show();
    }

}