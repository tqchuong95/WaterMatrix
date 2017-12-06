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
import android.view.View;
import android.widget.Button;

import kltn.musicapplication.utils.BlurBuilder;

/**
 * Created by UITCV on 10/16/2017.
 */

public class HomeActivity extends AppCompatActivity {

    private Button btn_Selection1;
    private Button btn_Selection2;
    private Button btn_Selection3;
    private Button btn_Selection4;
    private Button btn_Selection5;
    private BluetoothDevice bluetoothDevice;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        btn_Selection1 = (Button) findViewById(R.id.btn_Selection1);
        btn_Selection2 = (Button) findViewById(R.id.btn_Selection2);
        btn_Selection3 = (Button) findViewById(R.id.btn_Selection3);
        btn_Selection4 = (Button) findViewById(R.id.btn_Selection4);
        btn_Selection5 = (Button) findViewById(R.id.btn_Selection5);


        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);
        btn_Selection1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ConnectActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                startActivity(intent);
            }
        });

        btn_Selection2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, TickActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                startActivity(intent);
            }
        });

        btn_Selection3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GameActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                startActivity(intent);
            }
        });

        btn_Selection4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MusicActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                startActivity(intent);
            }
        });

        btn_Selection5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SelecMusicActivity.class);
                intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
                startActivity(intent);
            }
        });

    }
}