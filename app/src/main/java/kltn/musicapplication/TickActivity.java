package kltn.musicapplication;

import android.app.AlertDialog;
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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.models.Effect;
import kltn.musicapplication.models.TickEffect;
import kltn.musicapplication.utils.BlurBuilder;
import kltn.musicapplication.views.ProgressBarIndeterminateDeterminate;
import kltn.musicapplication.views.ToggleButton;

/**
 * Created by UITCV on 11/29/2017.
 */

public class TickActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView listView;
    private Button btn_send;
    private int total = 10;
    private BluetoothDevice bluetoothDevice;
    private Snackbar snackbar_TurnOn;
    private Bluetooth bluetooth;
    private Handler handler;
    private Toolbar toolbar_connect;
    private TickEffect[] tickEffects;
    private ArrayAdapter<TickEffect> arrayAdapter;
    private ProgressBarIndeterminateDeterminate progressBar_connect;
    private LinearLayout coordinatorLayout_connect;
    private Button button_reconnect;
    private Button button_clear;
    private ToggleButton toggleButton_connect;
    protected boolean check = true;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tick);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        listView = (ListView) findViewById(R.id.list_view_effect);
        btn_send = (Button) findViewById(R.id.btn_send_data);


        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

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
        handler = new TickActivity.myHandler(TickActivity.this);
        bluetooth = new Bluetooth(this, handler);
        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);
        setTitle(bluetoothDevice.getName());
        bluetooth.connectToDevice(bluetoothDevice);

        if (bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton_connect.setToggleOn();
        else
            toggleButton_connect.setToggleOff();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView v = (CheckedTextView) view;
                boolean currentCheck = v.isChecked();
                TickEffect user = (TickEffect) listView.getItemAtPosition(position);
                user.setActive(!currentCheck);
            }
        });

        TickEffect[] effects = new TickEffect[total];
        for (int i = 0; i < total; i++){
            effects[i] = new TickEffect(new Effect("Effect " + (i + 1), "" + i, "Describe effect " + (i + 1), R.drawable.logo));
        }
        tickEffects = effects;

        arrayAdapter = new ArrayAdapter<TickEffect>(this, R.layout.list_effect, tickEffects);
        listView.setAdapter(arrayAdapter);
        for(int i = 0; i < tickEffects.length; i++ )  {
            listView.setItemChecked(i,tickEffects[i].isActive());
        }

        //////////

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

    public void btnsend(View view) {
        SparseBooleanArray sp = listView.getCheckedItemPositions();

        String sb = new String();

        for(int i=0;i<sp.size();i++){
            if(sp.valueAt(i)==true){
                TickEffect effects = (TickEffect) listView.getItemAtPosition(i);
                String s = effects.getEffect().getCode();
                sb = sb + s;
            }
        }

        if (bluetooth.getState() == bluetooth.STATE_CONNECTED){
            bluetooth.send(sb);
        } else {
            Toast.makeText(this, "Connect error. Please Reconnect !", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        BacktoPre();
    }

    private void BacktoPre(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Question");
        builder.setMessage("Do you want to back?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private class MyGesture extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e2.getX() - e1.getX() > MainActivity.SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > MainActivity.SWIPE_VELOCITY_THRESHOLD){
                finish();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        BacktoPre();
        return false;
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

    @Override
    protected void onRestart() {
        super.onRestart();
        reconnect();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        private final WeakReference<TickActivity> mActivity;
        public myHandler(TickActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final TickActivity activity = mActivity.get();
            if (activity.bluetooth.getState() == Bluetooth.STATE_CONNECTED && activity.check == true){
                activity.bluetooth.send("op2");
                activity.check = false;
            }
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
