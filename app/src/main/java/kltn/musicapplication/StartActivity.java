package kltn.musicapplication;

import android.app.WallpaperManager;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import kltn.musicapplication.utils.BlurBuilder;

/**
 * Created by UITCV on 12/8/2017.
 */

public class StartActivity extends AppCompatActivity {

    private BluetoothDevice bluetoothDevice;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);

        Thread settime = new Thread(){
            public void run()
            {
                try {
                    sleep(3000);
                } catch (Exception e) {

                }
                finally
                {
                    Intent intent = new Intent(StartActivity.this, HomeActivity.class);
                    intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                    startActivity(intent);
                }
            }
        };
        settime.start();
    }

    protected void onPause(){
        super.onPause();
        finish();
    }

}
