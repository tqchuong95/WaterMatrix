package kltn.musicapplication;

import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kltn.musicapplication.adapters.AdapterEffect;
import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.models.Effect;
import kltn.musicapplication.utils.BlurBuilder;
import kltn.musicapplication.views.ProgressBarIndeterminateDeterminate;
import kltn.musicapplication.views.ToggleButton;

/**
 * Created by UITCV on 15/09/2017.
 */

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothDevice bluetoothDevice;
    private Snackbar snackbar_TurnOn;
    private Bluetooth bluetooth;
    private Handler handler;
    private Toolbar toolbar_connect;
    private RecyclerView recyclerView_effect;
    private FloatingActionButton floatingActionButton_add;
    private ArrayList<Effect> effects;
    private AdapterEffect adapter_effects;
    private ProgressBarIndeterminateDeterminate progressBar_connect;
    private LinearLayout coordinatorLayout_connect;
    private Button button_reconnect;
    private Button button_clear;
    private ToggleButton toggleButton_connect;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));
//        BlurBehind.getInstance()
//                .withAlpha(90)
//                .withFilterColor(Color.parseColor("#FFFFFF"))
//                .setBackground(this);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        recyclerView_effect = (RecyclerView) findViewById(R.id.recv_effect);
        toolbar_connect = (Toolbar) findViewById(R.id.toolbar_connect);
        progressBar_connect = (ProgressBarIndeterminateDeterminate) findViewById(R.id.prog_toolbar_connect);
        coordinatorLayout_connect = (LinearLayout) findViewById(R.id.coordinator_layout_connect);
        button_clear = (Button) findViewById(R.id.btn_clear);
        button_reconnect = (Button) findViewById(R.id.btn_reconnect);
        toggleButton_connect = (ToggleButton) findViewById(R.id.tog_connect);

        setSupportActionBar(toolbar_connect);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        button_clear.setOnClickListener(this);
        button_reconnect.setOnClickListener(this);

        snackbar_TurnOn = Snackbar.make(coordinatorLayout_connect, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        toolbar_connect.setSubtitle(getString(R.string.enabling_blt));
                        bluetooth.enableBluetooth();
                    }
                });
        handler = new myHandler(ConnectActivity.this);
        bluetooth = new Bluetooth(this, handler);
        bluetoothDevice = getIntent().getExtras().getParcelable(ResActivity.EXTRA_DEVICE);
        setTitle(bluetoothDevice.getName());
        bluetooth.connectToDevice(bluetoothDevice);

        if (bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton_connect.setToggleOn();
        else
            toggleButton_connect.setToggleOff();

        effects = new ArrayList<>();
        recyclerView_effect.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        effects.add(new Effect("Effect 1", "0", "Describe effect 1", R.drawable.logo));
        effects.add(new Effect("Effect 2", "1", "Describe effect 2", R.drawable.logo));
        effects.add(new Effect("Effect 3", "2", "Describe effect 3", R.drawable.logo));
        effects.add(new Effect("Effect 4", "3", "Describe effect 4", R.drawable.logo));
        effects.add(new Effect("Effect 5", "4", "Describe effect 5", R.drawable.logo));
        effects.add(new Effect("Effect 6", "5", "Describe effect 6", R.drawable.logo));
        effects.add(new Effect("Effect 7", "6", "Describe effect 7", R.drawable.logo));
        effects.add(new Effect("Effect 8", "7", "Describe effect 8", R.drawable.logo));
        effects.add(new Effect("Effect 9", "8", "Describe effect 9", R.drawable.logo));
        effects.add(new Effect("Effect 10", "9", "Describe effect 10", R.drawable.logo));

        adapter_effects = new AdapterEffect(effects, ConnectActivity.this, bluetooth, bluetoothDevice, recyclerView_effect);
        recyclerView_effect.setAdapter(adapter_effects);

        toggleButton_connect.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on)
                    bluetooth.enableBluetooth();
                else{
                    bluetooth.disableBluetooth();
                    toolbar_connect.setSubtitle("Bluetooth turned off");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.disconnect();
        unregisterReceiver(mReceiver);
    }

    public void reconnect(){
        button_reconnect.setEnabled(false);
        bluetooth.disconnect();
        bluetooth.connectToDevice(bluetoothDevice);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_clear:
                break;
            case R.id.btn_reconnect:
                reconnect();
                break;
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        snackbar_TurnOn.show();
                        toolbar_connect.setSubtitle(getString(R.string.none));
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        if (snackbar_TurnOn.isShownOrQueued())
                            snackbar_TurnOn.dismiss();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        toolbar_connect.setSubtitle(getString(R.string.enabling_blt));
                        bluetooth.enableBluetooth();
                        break;
                }
            }
        }
    };
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Bluetooth.REQUEST_ENABLE_BLT) {
            if (resultCode == RESULT_OK) {
                toggleButton_connect.setToggleOn();
                toolbar_connect.setSubtitle("None");
                reconnect();
            } else {
                toggleButton_connect.setToggleOff();
                toolbar_connect.setSubtitle("Error");
                Snackbar.make(coordinatorLayout_connect, getString(R.string.failed_to_enable_bluetooth), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.try_again), new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                toolbar_connect.setSubtitle(getString(R.string.enabling_blt));
                                bluetooth.enableBluetooth();
                            }
                        }).show();
            }
        }
    }
    private static class myHandler extends Handler {
        private final WeakReference<ConnectActivity> mActivity;
        public myHandler(ConnectActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final ConnectActivity activity = mActivity.get();
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Bluetooth.STATE_CONNECTED:
                            activity.toolbar_connect.setSubtitle("Connected");
                            activity.button_reconnect.setEnabled(false);
                            activity.progressBar_connect.setVisibility(View.INVISIBLE);
                            break;
                        case Bluetooth.STATE_CONNECTING:
                            activity.toolbar_connect.setSubtitle("Connecting");
                            activity.progressBar_connect.setVisibility(View.VISIBLE);
                            break;
                        case Bluetooth.STATE_NONE:
                            activity.toolbar_connect.setSubtitle("Not Connected");
                            activity.progressBar_connect.setVisibility(View.INVISIBLE);
                            break;
                        case Bluetooth.STATE_ERROR:
                            activity.toolbar_connect.setSubtitle("Error");
                            activity.button_reconnect.setEnabled(true);
                            activity.progressBar_connect.setVisibility(View.INVISIBLE);
                            break;
                    }
                    break;
                case Bluetooth.MESSAGE_SNACKBAR:
                    Snackbar.make(activity.coordinatorLayout_connect, msg.getData().getString(Bluetooth.SNACKBAR), Snackbar.LENGTH_LONG)
                            .setAction("Connect", new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    activity.reconnect();
                                }
                            }).show();

                    break;
            }
        }


    }
}
