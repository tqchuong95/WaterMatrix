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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.utils.BlurBuilder;
import kltn.musicapplication.views.ProgressBarIndeterminateDeterminate;
import kltn.musicapplication.views.ToggleButton;

/**
 * Created by UITCV on 10/4/2017.
 */

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout relativeLayout;
    private BluetoothDevice bluetoothDevice;
    private Snackbar snackbar_TurnOn;
    private Bluetooth bluetooth;
    private Handler handler;
    private Toolbar toolbar_connect;
    private ProgressBarIndeterminateDeterminate progressBar_connect;
    private LinearLayout coordinatorLayout_connect;
    private Button button_reconnect;
    private Button button_clear;
    private ToggleButton toggleButton_connect;

    private Button btn[][];
    private Button btnStart, btnStop;
    private boolean check = true;

    private String effect;

    protected boolean option = true;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

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
        init();

        snackbar_TurnOn = Snackbar.make(coordinatorLayout_connect, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        toolbar_connect.setSubtitle(getString(R.string.enabling_blt));
                        bluetooth.enableBluetooth();
                    }
                });
        handler = new myHandler(GameActivity.this);
        bluetooth = new Bluetooth(this, handler);
        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);
        setTitle(bluetoothDevice.getName());
        bluetooth.connectToDevice(bluetoothDevice);

        if (bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton_connect.setToggleOn();
        else
            toggleButton_connect.setToggleOff();

        listenerOnClick();
        Reset();
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

    private void init() {
        relativeLayout = (RelativeLayout) findViewById(R.id.relLayout);
        btn = new Button[5][5];
        btn[0][0] = (Button) findViewById(R.id.btn11);
        btn[0][1] = (Button) findViewById(R.id.btn12);
        btn[0][2] = (Button) findViewById(R.id.btn13);
        btn[0][3] = (Button) findViewById(R.id.btn14);
        btn[0][4] = (Button) findViewById(R.id.btn15);
        btn[1][0] = (Button) findViewById(R.id.btn21);
        btn[1][1] = (Button) findViewById(R.id.btn22);
        btn[1][2] = (Button) findViewById(R.id.btn23);
        btn[1][3] = (Button) findViewById(R.id.btn24);
        btn[1][4] = (Button) findViewById(R.id.btn25);
        btn[2][0] = (Button) findViewById(R.id.btn31);
        btn[2][1] = (Button) findViewById(R.id.btn32);
        btn[2][2] = (Button) findViewById(R.id.btn33);
        btn[2][3] = (Button) findViewById(R.id.btn34);
        btn[2][4] = (Button) findViewById(R.id.btn35);
        btn[3][0] = (Button) findViewById(R.id.btn41);
        btn[3][1] = (Button) findViewById(R.id.btn42);
        btn[3][2] = (Button) findViewById(R.id.btn43);
        btn[3][3] = (Button) findViewById(R.id.btn44);
        btn[3][4] = (Button) findViewById(R.id.btn45);
        btn[4][0] = (Button) findViewById(R.id.btn51);
        btn[4][1] = (Button) findViewById(R.id.btn52);
        btn[4][2] = (Button) findViewById(R.id.btn53);
        btn[4][3] = (Button) findViewById(R.id.btn54);
        btn[4][4] = (Button) findViewById(R.id.btn55);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
    }

    private void listenerOnClick() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                btn[i][j].setOnClickListener(this);
            }
        }
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void Reset(){
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                btn[i][j].setText("");
                btn[i][j].setEnabled(true);
            }
        }
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
            default:

            if (bluetooth.getState() == Bluetooth.STATE_CONNECTED) {
                if (check) {
                    switch (v.getId()) {
                    /*case R.id.btn_clear:
                        break;
                    case R.id.btn_reconnect:
                        reconnect();
                        break;*/

                        case R.id.btnStart:
                            effect = "Start";
                            Reset();
                            break;
                        case R.id.btnStop:
                            effect = "Stop";
                            for (int i = 0; i < 5; i++) {
                                for (int j = 0; j < 5; j++) {
                                    btn[i][j].setEnabled(false);
                                }
                            }
                            break;
                        case R.id.btn11:
                            btn[0][0].setText("X");
                            btn[0][0].setEnabled(false);
                            effect = "a0";
                            break;
                        case R.id.btn12:
                            btn[0][1].setText("X");
                            btn[0][1].setEnabled(false);
                            effect = "a1";
                            break;
                        case R.id.btn13:
                            btn[0][2].setText("X");
                            btn[0][2].setEnabled(false);
                            effect = "a2";
                            break;
                        case R.id.btn14:
                            btn[0][3].setText("X");
                            btn[0][3].setEnabled(false);
                            effect = "a3";
                            break;
                        case R.id.btn15:
                            btn[0][4].setText("X");
                            btn[0][4].setEnabled(false);
                            effect = "a4";
                            break;
                        case R.id.btn21:
                            btn[1][0].setText("X");
                            btn[1][0].setEnabled(false);
                            effect = "b0";
                            break;
                        case R.id.btn22:
                            btn[1][1].setText("X");
                            btn[1][1].setEnabled(false);
                            effect = "b1";
                            break;
                        case R.id.btn23:
                            btn[1][2].setText("X");
                            btn[1][2].setEnabled(false);
                            effect = "b2";
                            break;
                        case R.id.btn24:
                            btn[1][3].setText("X");
                            btn[1][3].setEnabled(false);
                            effect = "b3";
                            break;
                        case R.id.btn25:
                            btn[1][4].setText("X");
                            btn[1][4].setEnabled(false);
                            effect = "b4";
                            break;
                        case R.id.btn31:
                            btn[2][0].setText("X");
                            btn[2][0].setEnabled(false);
                            effect = "c0";
                            break;
                        case R.id.btn32:
                            btn[2][1].setText("X");
                            btn[2][1].setEnabled(false);
                            effect = "c1";
                            break;
                        case R.id.btn33:
                            btn[2][2].setText("X");
                            btn[2][2].setEnabled(false);
                            effect = "c2";
                            break;
                        case R.id.btn34:
                            btn[2][3].setText("X");
                            btn[2][3].setEnabled(false);
                            effect = "c3";
                            break;
                        case R.id.btn35:
                            btn[2][4].setText("X");
                            btn[2][4].setEnabled(false);
                            effect = "c4";
                            break;
                        case R.id.btn41:
                            btn[3][0].setText("X");
                            btn[3][0].setEnabled(false);
                            effect = "d0";
                            break;
                        case R.id.btn42:
                            btn[3][1].setText("X");
                            btn[3][1].setEnabled(false);
                            effect = "d1";
                            break;
                        case R.id.btn43:
                            btn[3][2].setText("X");
                            btn[3][2].setEnabled(false);
                            effect = "d2";
                            break;
                        case R.id.btn44:
                            btn[3][3].setText("X");
                            btn[3][3].setEnabled(false);
                            effect = "d3";
                            break;
                        case R.id.btn45:
                            btn[3][4].setText("X");
                            btn[3][4].setEnabled(false);
                            effect = "d4";
                            break;
                        case R.id.btn51:
                            btn[4][0].setText("X");
                            btn[4][0].setEnabled(false);
                            effect = "e0";
                            break;
                        case R.id.btn52:
                            btn[4][1].setText("X");
                            btn[4][1].setEnabled(false);
                            effect = "e1";
                            break;
                        case R.id.btn53:
                            btn[4][2].setText("X");
                            btn[4][2].setEnabled(false);
                            effect = "e2";
                            break;
                        case R.id.btn54:
                            btn[4][3].setText("X");
                            btn[4][3].setEnabled(false);
                            effect = "e3";
                            break;
                        case R.id.btn55:
                            btn[4][4].setText("X");
                            btn[4][4].setEnabled(false);
                            effect = "e4";
                            break;
                        default:
                            break;
                    }
                    check = false;
                    if (checkColRow() || checkSlash()) {
                        Toast toast = Toast.makeText(GameActivity.this, "X win", Toast.LENGTH_LONG);
                        toast.show();
                        effect = "xwin";
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                btn[i][j].setEnabled(false);
                            }
                        }
                    }
                    if (aDraw()) {
                        Toast toast = Toast.makeText(GameActivity.this, "A DRAW", Toast.LENGTH_LONG);
                        toast.show();
                        effect = "adraw";
                    }

                    bluetooth.send(effect);
                } else {
                    switch (v.getId()) {
                        case R.id.btn_clear:
                            break;
                        case R.id.btn_reconnect:
                            reconnect();
                            break;

                        case R.id.btnStart:
                            effect = "Start";
                            Reset();
                            break;
                        case R.id.btnStop:
                            effect = "Stop";
                            for (int i = 0; i < 5; i++) {
                                for (int j = 0; j < 5; j++) {
                                    btn[i][j].setEnabled(false);
                                }
                            }
                            break;

                        case R.id.btn11:
                            btn[0][0].setText("O");
                            btn[0][0].setEnabled(false);
                            effect = "0a";
                            break;
                        case R.id.btn12:
                            btn[0][1].setText("O");
                            btn[0][1].setEnabled(false);
                            effect = "1a";
                            break;
                        case R.id.btn13:
                            btn[0][2].setText("O");
                            btn[0][2].setEnabled(false);
                            effect = "2a";
                            break;
                        case R.id.btn14:
                            btn[0][3].setText("O");
                            btn[0][3].setEnabled(false);
                            effect = "3a";
                            break;
                        case R.id.btn15:
                            btn[0][4].setText("O");
                            btn[0][4].setEnabled(false);
                            effect = "4a";
                            break;
                        case R.id.btn21:
                            btn[1][0].setText("O");
                            btn[1][0].setEnabled(false);
                            effect = "0b";
                            break;
                        case R.id.btn22:
                            btn[1][1].setText("O");
                            btn[1][1].setEnabled(false);
                            effect = "1b";
                            break;
                        case R.id.btn23:
                            btn[1][2].setText("O");
                            btn[1][2].setEnabled(false);
                            effect = "2b";
                            break;
                        case R.id.btn24:
                            btn[1][3].setText("O");
                            btn[1][3].setEnabled(false);
                            effect = "3b";
                            break;
                        case R.id.btn25:
                            btn[1][4].setText("O");
                            btn[1][4].setEnabled(false);
                            effect = "4b";
                            break;
                        case R.id.btn31:
                            btn[2][0].setText("O");
                            btn[2][0].setEnabled(false);
                            effect = "0c";
                            break;
                        case R.id.btn32:
                            btn[2][1].setText("O");
                            btn[2][1].setEnabled(false);
                            effect = "1c";
                            break;
                        case R.id.btn33:
                            btn[2][2].setText("O");
                            btn[2][2].setEnabled(false);
                            effect = "2c";
                            break;
                        case R.id.btn34:
                            btn[2][3].setText("O");
                            btn[2][3].setEnabled(false);
                            effect = "3c";
                            break;
                        case R.id.btn35:
                            btn[2][4].setText("O");
                            btn[2][4].setEnabled(false);
                            effect = "4c";
                            break;
                        case R.id.btn41:
                            btn[3][0].setText("O");
                            btn[3][0].setEnabled(false);
                            effect = "0d";
                            break;
                        case R.id.btn42:
                            btn[3][1].setText("O");
                            btn[3][1].setEnabled(false);
                            effect = "1d";
                            break;
                        case R.id.btn43:
                            btn[3][2].setText("O");
                            btn[3][2].setEnabled(false);
                            effect = "2d";
                            break;
                        case R.id.btn44:
                            btn[3][3].setText("O");
                            btn[3][3].setEnabled(false);
                            effect = "3d";
                            break;
                        case R.id.btn45:
                            btn[3][4].setText("O");
                            btn[3][4].setEnabled(false);
                            effect = "4d";
                            break;
                        case R.id.btn51:
                            btn[4][0].setText("O");
                            btn[4][0].setEnabled(false);
                            effect = "0e";
                            break;
                        case R.id.btn52:
                            btn[4][1].setText("O");
                            btn[4][1].setEnabled(false);
                            effect = "1e";
                            break;
                        case R.id.btn53:
                            btn[4][2].setText("O");
                            btn[4][2].setEnabled(false);
                            effect = "2e";
                            break;
                        case R.id.btn54:
                            btn[4][3].setText("O");
                            btn[4][3].setEnabled(false);
                            effect = "3e";
                            break;
                        case R.id.btn55:
                            btn[4][4].setText("O");
                            btn[4][4].setEnabled(false);
                            effect = "4e";
                            break;
                        default:
                            break;
                    }

                    check = true;
                    if (checkColRow() || checkSlash()) {
                        Toast toast = Toast.makeText(GameActivity.this, "O win", Toast.LENGTH_LONG);
                        toast.show();
                        effect = "owin";
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                btn[i][j].setEnabled(false);
                            }
                        }
                    }

                    if (aDraw()) {
                        Toast toast = Toast.makeText(GameActivity.this, "A DRAW", Toast.LENGTH_LONG);
                        toast.show();
                        effect = "adraw";
                    }
                    bluetooth.send(effect);
                }
            } else {
                Toast.makeText(this, "Connect error. Please Reconnect !", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }

    private boolean aDraw(){
        int dr = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (btn[i][j].getText().equals("X") || btn[i][j].getText().equals("O")){
                    dr++;
                }
            }
        }
        if (dr >= 25) {
            return true;
        }
        return false;
    }

    private boolean checkColRow(){
        int col = 0, row = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (btn[i][j].getText().equals("X")){
                    row++;
                }
                if (row > 4){
                    return true;
                }
            }
            row = 0;
        }
        row = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (btn[i][j].getText().equals("O")){
                    row++;
                }
                if (row > 4){
                    return true;
                }
            }
            row = 0;
        }
        for (int j = 0; j < 5; j++){
            for (int i = 0; i < 5; i++) {
                if (btn[i][j].getText().equals("X")) {
                    col++;
                }
                if (col > 4){
                    return true;
                }
            }
            col = 0;
        }
        col = 0;
        for (int j = 0; j < 5; j++){
            for (int i = 0; i < 5; i++) {
                if (btn[i][j].getText().equals("O")) {
                    col++;
                }
                if (col > 4){
                    return true;
                }
            }
            col = 0;
        }
        return false;
    }

    private boolean checkSlash() {
        int slash = 0;
        for (int i = 0; i < 5; i++) {
            if (btn[i][i].getText().equals("X")) {
                slash++;
            }
            if (slash > 4) {
                return true;
            }
        }

        slash = 0;

        for (int i = 0; i < 5; i++) {
            if (btn[i][i].getText().equals("O")) {
                slash++;
            }
            if (slash > 4) {
                return true;
            }
        }

        slash = 0;

        for (int i = 0; i < 5; i++) {
            if (btn[i][4-i].getText().equals("X")) {
                slash++;
            }
            if (slash > 4) {
                return true;
            }
        }

        slash = 0;

        for (int i = 0; i < 5; i++) {
            if (btn[i][4-i].getText().equals("O")) {
                slash++;
            }
            if (slash > 4) {
                return true;
            }
        }
        return false;
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
        private final WeakReference<GameActivity> mActivity;
        public myHandler(GameActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final GameActivity activity = mActivity.get();
            if (activity.bluetooth.getState() == Bluetooth.STATE_CONNECTED && activity.option == true){
                activity.bluetooth.send("op3");
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
