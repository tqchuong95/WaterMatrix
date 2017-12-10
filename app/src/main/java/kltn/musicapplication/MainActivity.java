package kltn.musicapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

import kltn.musicapplication.adapters.BluetoothDevicesAdapter;
import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.utils.BlurBuilder;
import kltn.musicapplication.views.ProgressBarDeterminate;
import kltn.musicapplication.views.ToggleButton;

/**
 * Created by UITCV on 10/17/2017.
 */

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "extra_device";
    public static final String EXTRA_BLUETOOTH = "extra_ble";

    public static int SWIPE_THRESHOLD = 100;
    public static int SWIPE_VELOCITY_THRESHOLD = 100;

    private Toolbar toolbar;
    private ProgressBarDeterminate progressBar_toolbar;
    private Button button_search;
    private ListView listView_devices;
    private TextView textView_empty_devices;
    private LinearLayout coordinatorLayout;
    private BluetoothDevicesAdapter bluetoothDevicesAdapter;
    private ToggleButton toggleButton;

    private Bluetooth bluetooth;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        coordinatorLayout = (LinearLayout) findViewById(R.id.coordinator_layout_main);
        coordinatorLayout = (LinearLayout) findViewById(R.id.coordinator_layout_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressBar_toolbar = (ProgressBarDeterminate) findViewById(R.id.prog_toolbar);
        button_search = (Button) findViewById(R.id.btn_search);
        listView_devices = (ListView) findViewById(R.id.liv_devices);
        textView_empty_devices = (TextView) findViewById(R.id.txtv_nothing);
        toggleButton = (ToggleButton) findViewById(R.id.tog_main);

        Date date = new Date();
        date.toString();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        bluetooth = new Bluetooth(this);
        bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this);
//        setSupportActionBar(toolbar);
        toolbar.setSubtitle(getString(R.string.none));
        listView_devices.setAdapter(bluetoothDevicesAdapter);
        listView_devices.setEmptyView(textView_empty_devices);

        if (!bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton.setToggleOff();
        else
            toggleButton.setToggleOn();

        // Quick permission check
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }


        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
//                startActivity(intent);
                if(bluetooth.getBluetoothAdapter().isEnabled()) {
                    searchDevices();
                    button_search.setEnabled(false);
                }
                else{
                    toolbar.setSubtitle(getString(R.string.enabling_blt));
                    bluetooth.enableBluetooth();
                    toggleButton.setToggleOff();
                }

            }
        });
        listView_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toolbar.setSubtitle(getString(R.string.asking_to_connect));
                final BluetoothDevice device = bluetoothDevicesAdapter.getItem(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle(getString(R.string.connect))
                        .setMessage("Do you want to connect to: " + device.getName() + " - " + device.getAddress())
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                progressBar_toolbar.setVisibility(View.INVISIBLE);
                                bluetooth.getBluetoothAdapter().cancelDiscovery();
                                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                                intent.putExtra(EXTRA_DEVICE, device);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                toolbar.setSubtitle("Cancelled connection");
                            }
                        }).show();
            }
        });
        toggleButton.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if(on){
                    bluetooth.enableBluetooth();
                    toolbar.setSubtitle("Bluetooth turned on");
                }
                else{
                    bluetooth.disableBluetooth();
                    progressBar_toolbar.setVisibility(View.INVISIBLE);
                    toolbar.setSubtitle("Bluetooth turned off");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == bluetooth.REQUEST_ENABLE_BLT) {
            if (resultCode == RESULT_OK) {
                toggleButton.setToggleOn();
            } else {
                toolbar.setSubtitle(getString(R.string.error));
                toggleButton.setToggleOff();
                Snackbar.make(coordinatorLayout, getString(R.string.failed_to_enable_bluetooth), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.try_again), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bluetooth.enableBluetooth();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton.setToggleOff();
        else
            toggleButton.setToggleOn();

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiverScan, filter);
    }

    private void searchDevices(){
        if (bluetooth.scanDevices()){
            progressBar_toolbar.setVisibility(View.VISIBLE);
            toolbar.setSubtitle(getString(R.string.searching_devices));
        }
        else {
            toolbar.setSubtitle(getString(R.string.error));
            Snackbar.make(coordinatorLayout, getString(R.string.failed_to_search), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.try_again), new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            searchDevices();
                        }
                    }).show();
        }

    }

    private BroadcastReceiver mReceiverScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        if (bluetooth.getDiscoveryCallback() != null)
                            bluetooth.getDiscoveryCallback().onError("Bluetooth turned off");
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    context.unregisterReceiver(mReceiverScan);
                    progressBar_toolbar.setVisibility(View.INVISIBLE);
                    toolbar.setSubtitle(getString(R.string.none));
                    button_search.setEnabled(true);
                    if (bluetooth.getDiscoveryCallback() != null)
                        bluetooth.getDiscoveryCallback().onFinish();
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(bluetoothDevicesAdapter.getPosition(device) == -1) {
                        bluetoothDevicesAdapter.add(device);
                        bluetoothDevicesAdapter.notifyDataSetChanged();
                    }
                    if (bluetooth.getDiscoveryCallback() != null)
                        bluetooth.getDiscoveryCallback().onDevice(device);
                    break;
            }
        }
    };

}

