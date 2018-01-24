package kltn.musicapplication;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.models.Effect;
import kltn.musicapplication.music.Segment;
import kltn.musicapplication.soundfile.CheapSoundFile;
import kltn.musicapplication.utils.BlurBuilder;
import kltn.musicapplication.views.MarkerView;
import kltn.musicapplication.views.ProgressBarIndeterminateDeterminate;
import kltn.musicapplication.views.ToggleButton;
import kltn.musicapplication.views.WaveformView;

/**
 * Created by UITCV on 12/7/2017.
 */

public class WaveformActivity extends AppCompatActivity implements View.OnClickListener, MarkerView.MarkerListener, WaveformView.WaveformListener {

    private PowerManager.WakeLock wl;
    private int total = 10;
    private int nexttotal;
    private int pos;
    private int UPDATE_TIME = 1;
    private BluetoothDevice bluetoothDevice;
    private Bluetooth bluetooth;
    private Snackbar snackbar_TurnOn;
    private Handler handler;
    private Toolbar toolbar_connect;
    private FloatingActionButton floatingActionButton_add;
    private ProgressBarIndeterminateDeterminate progressBar_connect;
    private LinearLayout coordinatorLayout_connect;
    private Button button_reconnect;
    private Button button_clear;
    private ToggleButton toggleButton_connect;
    private RadioButton[] rdbtn;
    private RadioGroup radioGroup;
    private Button btn_setms;
    private Button btn_savems;
    private Button btn_loadms;
    private int[] rd;
    private ArrayList<Integer> mSetTimeStart;
    private ArrayList<Integer> mSetTimeEnd;
    private ArrayList<Effect> mEffects;
    private String senddata = "";
    private TextView txtsettime, txttime;
    private boolean checkID = false;

    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private ProgressDialog mProgressDialog;
    private CheapSoundFile mSoundFile;
    private File mFile;
    private String mFilename = SelecMusicActivity.pathname;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private TextView mStartText;
    private TextView mEndText;
    private TextView mInfo;
    private ImageButton mPlayButton;
    private ImageButton mRewindButton;
    private ImageButton mFfwdButton;
    private boolean mKeyDown;
    private String mCaption = "";
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayStartOffset;
    private int mPlayEndMsec;
    private Handler mHandler;
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;

    protected boolean check = true;
    
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waveform);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        toolbar_connect = (Toolbar) findViewById(R.id.toolbar_connect);
        progressBar_connect = (ProgressBarIndeterminateDeterminate) findViewById(R.id.prog_toolbar_connect);
        coordinatorLayout_connect = (LinearLayout) findViewById(R.id.linearMusic);
        button_clear = (Button) findViewById(R.id.btn_clear);
        button_reconnect = (Button) findViewById(R.id.btn_reconnect);
        toggleButton_connect = (ToggleButton) findViewById(R.id.tog_connect);
        rdbtn = new RadioButton[total];
        rd = new int[total];
        rd[0] = R.id.rd_1; rd[1] = R.id.rd_2; rd[2] = R.id.rd_3; rd[3] = R.id.rd_4; rd[4] = R.id.rd_5;
        rd[5] = R.id.rd_6; rd[6] = R.id.rd_7; rd[7] = R.id.rd_8; rd[8] = R.id.rd_9; //rd[9] = R.id.rd_10;
        for (int i = 0; i < total; i++){
            rdbtn[i] = (RadioButton) findViewById(rd[i]);
        }
        radioGroup = (RadioGroup) findViewById(R.id.radioButton);
        btn_setms = (Button) findViewById(R.id.set_active);
        btn_savems = (Button) findViewById(R.id.btn_save);
        btn_loadms = (Button) findViewById(R.id.btn_load);
        txtsettime = (TextView) findViewById(R.id.txtsettime);
        txttime = (TextView) findViewById(R.id.idtxtv);

        setSupportActionBar(toolbar_connect);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        button_clear.setOnClickListener(this);
        button_reconnect.setOnClickListener(this);
        btn_setms.setOnClickListener(this);
        btn_savems.setOnClickListener(this);
        btn_loadms.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rdg, int i) {
                doOnGroupRadioButton(rdg, i);
            }
        });

        snackbar_TurnOn = Snackbar.make(coordinatorLayout_connect, "Bluetooth turned off", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        toolbar_connect.setSubtitle(getString(R.string.enabling_blt));
                        bluetooth.enableBluetooth();
                    }
                });
        handler = new myHandler(this);
        bluetooth = new Bluetooth(this, handler);
        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);
        setTitle(bluetoothDevice.getName());
        bluetooth.connectToDevice(bluetoothDevice);

        if (bluetooth.getBluetoothAdapter().isEnabled())
            toggleButton_connect.setToggleOn();
        else
            toggleButton_connect.setToggleOff();

        //PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        //wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");

        loadGui();
        //mFilename = SelecMusicActivity.pathname;
        mSetTimeStart = new ArrayList<>();
        mSetTimeEnd = new ArrayList<>();
        mEffects = new ArrayList<>();
        txtsettime.setText("");
        pos = -1;
        nexttotal = -1;
        mPlayer = null;
        mIsPlaying = false;
        mKeyDown = false;
        mHandler = new Handler();
        mHandler.postDelayed(mTimerRunnable, 100);


        mSoundFile = null;
        if (mSoundFile == null) {
            loadFromFile();
        } else {
            mHandler.post(() -> finishOpeningSoundFile());
        }
    }

    @Override
    protected void onDestroy() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        pos = -1;
        mSoundFile = null;
        mWaveformView = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_clear:
                break;
            case R.id.btn_reconnect:
                reconnect();
                break;
            case R.id.set_active:
                if (nexttotal <= -1){
                    Toast.makeText(this, "Please choose the effect...!", Toast.LENGTH_LONG).show();
                } else if(mStartPos < mMaxPos){
                    pos++;
                    mEffects.add(new Effect("Effect " + (nexttotal + 1), "" + nexttotal,
                            "Describe effect " + (nexttotal + 1), R.drawable.logo));
                    mSetTimeStart.add(new Integer(mStartPos));
                    mSetTimeEnd.add(new Integer(mEndPos));
                    txtsettime.append(formatTime(mStartPos) + "-" + formatTime(mEndPos) + ":eff" +
                            (Integer.parseInt(mEffects.get(pos).getCode()) + 1) + "   ");
                    mStartPos = mEndPos;
                    EditText ed = (EditText) findViewById(R.id.starttext);
                    ed.setEnabled(false);
                    MarkerView mk = (MarkerView) findViewById(R.id.startmarker);
                    mk.setVisibility(View.INVISIBLE);
                    mStartVisible = false;
                    updateDisplay();
                } else {
                    Toast.makeText(this, "Complete! You will save!", Toast.LENGTH_LONG).show();
                }
                    break;
            case R.id.btn_save:
                if (pos >= 0 && !checkID) {
                    pos = 0;
                    checkID = true;
                    MarkerView mk = (MarkerView) findViewById(R.id.startmarker);
                    mk.setVisibility(View.VISIBLE);
                    mStartPos = 0;
                    mEndPos = mMaxPos;
                    EditText ed = (EditText) findViewById(R.id.starttext);
                    ed.setEnabled(true);
                    mStartText.setText(formatTime(mStartPos));
                    mEndText.setText(formatTime(mEndPos));
                    ed = (EditText) findViewById(R.id.starttext);
                    ed.setEnabled(false);
                    ed = (EditText) findViewById(R.id.endtext);
                    ed.setEnabled(false);
                    updateDisplay();
                } else {
                    Toast.makeText(this, "You are not effect!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_load:
                BacktoLoad();
                break;
        }
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

    private void BacktoLoad(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Question");
        builder.setMessage("Do you want to load?");
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

    private void doOnGroupRadioButton(RadioGroup rdg, int id){
        int checkRadio = rdg.getCheckedRadioButtonId();
        switch (checkRadio){
            case R.id.rd_1:
                nexttotal = 0;
                break;
            case R.id.rd_2:
                nexttotal = 1;
                break;
            case R.id.rd_3:
                nexttotal = 2;
                break;
            case R.id.rd_4:
                nexttotal = 3;
                break;
            case R.id.rd_5:
                nexttotal = 4;
                break;
            case R.id.rd_6:
                nexttotal = 5;
                break;
            case R.id.rd_7:
                nexttotal = 6;
                break;
            case R.id.rd_8:
                nexttotal = 7;
                break;
            case R.id.rd_9:
                nexttotal = 8;
                break;
            case R.id.rd_10:
                nexttotal = 8;
                break;
            default:
                nexttotal = -1;
                break;
        }
    }

    private void outEffect(){
        String data = "";
        while (mSetTimeStart.size() > pos) {
            data = data + mEffects.get(pos).getCode() + "-" +
                    formatDecimal(mWaveformView.pixelsToSeconds(mSetTimeEnd.get(pos) - mSetTimeStart.get(pos))) + ";";
            /*try {
                if (bluetooth.getState() == Bluetooth.STATE_CONNECTED) {
                    bluetooth.send(data);
                    data = "";
                } else {
                    reconnect();
                    Toast.makeText(this, "Connected error. Please reconnect...!", Toast.LENGTH_LONG).show();
                }

            } catch (NullPointerException e) {
                //Nothing
                Toast.makeText(this, "Try again!", Toast.LENGTH_LONG).show();
            }*/
            pos++;
        }
        try {
            if (bluetooth.getState() == Bluetooth.STATE_CONNECTED) {
                bluetooth.send(data);
                //data = "";
            } else {
                reconnect();
                Toast.makeText(this, "Connected error. Please reconnect...!", Toast.LENGTH_LONG).show();
            }

        } catch (NullPointerException e) {
            //Nothing
            Toast.makeText(this, "Try again!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        BacktoPre();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //wl.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //wl.release();
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

    private static class myHandler extends Handler {
        private final WeakReference<WaveformActivity> mActivity;
        public myHandler(WaveformActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final WaveformActivity activity = mActivity.get();
            if (activity.bluetooth.getState() == Bluetooth.STATE_CONNECTED && activity.check == true){
                activity.bluetooth.send("op5");
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void loadGui() {
        DisplayMetrics metrics = new DisplayMetrics();
        mDensity = metrics.density;

        mMarkerLeftInset = (int) (46 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);

        mStartText = (TextView) findViewById(R.id.starttext);
        mStartText.addTextChangedListener(mTextWatcher);
        mEndText = (TextView) findViewById(R.id.endtext);
        mEndText.addTextChangedListener(mTextWatcher);

        mPlayButton = (ImageButton) findViewById(R.id.play);
        mPlayButton.setOnClickListener(mPlayListener);
        mRewindButton = (ImageButton) findViewById(R.id.rew);
        mRewindButton.setOnClickListener(getRewindListener());
        mFfwdButton = (ImageButton) findViewById(R.id.ffwd);
        mFfwdButton.setOnClickListener(getFwdListener());

        TextView markStartButton = (TextView) findViewById(R.id.mark_start);
        markStartButton.setOnClickListener(mMarkStartListener);
        TextView markEndButton = (TextView) findViewById(R.id.mark_end);
        markEndButton.setOnClickListener(mMarkEndListener);

        enableDisableButtons();

        mWaveformView = (WaveformView) findViewById(R.id.waveform);
        mWaveformView.setListener(this);
        mWaveformView.setSegments(getSegments());

        mInfo = (TextView) findViewById(R.id.info);
        mInfo.setText(mCaption);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);
            mMaxPos = mWaveformView.maxPos();
        }

        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setImageAlpha(255);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setImageAlpha(255);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;

        updateDisplay();
    }

    protected void loadFromFile() {
        mFile = new File(mFilename);
        mLoadingLastUpdateTime = System.currentTimeMillis();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener((DialogInterface dialog) -> mLoadingKeepGoing = false);
        mProgressDialog.show();

        final CheapSoundFile.ProgressListener listener = (double fractionComplete) -> {
            long now = System.currentTimeMillis();
            if (now - mLoadingLastUpdateTime > 100) {
                mProgressDialog.setProgress(
                        (int) (mProgressDialog.getMax() * fractionComplete));
                mLoadingLastUpdateTime = now;
            }
            return mLoadingKeepGoing;
        };

        // Create the MediaPlayer in a background thread
        new Thread() {
            public void run() {
                try {
                    MediaPlayer player = new MediaPlayer();
                    player.setDataSource(mFile.getAbsolutePath());
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.prepare();
                    mPlayer = player;
                } catch (final java.io.IOException e) {
                }
            }
        }.start();

        // Load the sound file in a background thread
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            public void run() {
                try {
                    mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(), listener);
                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    mInfo.setText(e.toString());
                    return;
                }
                if (mLoadingKeepGoing) {
                    mHandler.post(() -> finishOpeningSoundFile());
                }
            }
        }.start();
    }

    protected void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();

        mCaption = mSoundFile.getFiletype() + ", " +
                mSoundFile.getSampleRate() + " Hz, " +
                mSoundFile.getAvgBitrateKbps() + " kbps, " +
                formatTime(mMaxPos) + " " + getResources().getString(R.string.time_seconds);
        mInfo.setText(mCaption);
        mProgressDialog.dismiss();
        updateDisplay();
    }

    //
    // WaveformListener
    //

    /**
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = System.currentTimeMillis();
    }

    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = System.currentTimeMillis() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs((int) (mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec && seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec - mPlayStartOffset);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        //enableZoomButtons();
        updateDisplay();
    }

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mOffset = mWaveformView.getOffset();
        mOffsetGoal = mOffset;
        enableZoomButtons();
        updateDisplay();
    }

    private void enableZoomButtons() {

    }

    //
    // MarkerListener
    //

    public void markerDraw() {
    }

    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerRight(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    public void markerEnter(MarkerView marker) {
    }

    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler.postDelayed(() -> updateDisplay(), 100);
    }

    @SuppressLint("NewApi")
    private synchronized void updateDisplay() {
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
            txttime.setText("Effect:       " + formatDecimal(now));
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                float saveVel = mFlingVelocity;

                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
                offsetDelta = mOffsetGoal - mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mOffset += offsetDelta;
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(getResources().getText(R.string.start_marker) + " " + formatTime(mStartPos));
        mEndMarker.setContentDescription(getResources().getText(R.string.end_marker) + " " + formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(() -> {
                    mStartVisible = true;
                    mStartMarker.setImageAlpha(255);
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setImageAlpha(0);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(() -> {
                    mEndVisible = true;
                    mEndMarker.setImageAlpha(255);
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setImageAlpha(0);
                mEndVisible = false;
            }
            endX = 0;
        }

        mStartMarker.setLayoutParams(
                new AbsoluteLayout.LayoutParams(
                        AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        startX, mMarkerTopOffset));

        mEndMarker.setLayoutParams(
                new AbsoluteLayout.LayoutParams(
                        AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                        endX, mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset));
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    protected void resetPositions() {
        mStartPos = 0;
        mEndPos = mMaxPos;
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }



    private void enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayButton.setContentDescription(getResources().getText(R.string.stop));
        } else {
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mPlayButton.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private void setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    ///Format time
    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    protected List<Segment> getSegments() {
        return Arrays.asList(
                new Segment(55.2, 55.8, Color.rgb(238, 23, 104)),
                new Segment(56.2, 56.6, Color.rgb(238, 23, 104)),
                new Segment(58.4, 59.9, Color.rgb(184, 92, 184)));
    }

    protected View.OnClickListener getFwdListener() {
        return mFfwdListener;
    }

    protected View.OnClickListener getRewindListener() {
        return mRewindListener;
    }

    protected int getStep() {
        int maxSeconds = (int) mWaveformView.pixelsToSeconds(mWaveformView.maxPos());
        if (maxSeconds / 3600 > 0) {
            return 600;
        } else if (maxSeconds / 1800 > 0) {
            return 300;
        } else if (maxSeconds / 300 > 0) {
            return 60;
        }
        return 5;
    }

    protected View.OnClickListener mPlayListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (checkID){
                outEffect();
                playMusic(mStartPos);
            } else {
                onPlay(mStartPos);
            }
        }
    };

    protected View.OnClickListener mRewindListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = mPlayer.getCurrentPosition() - 5000;
                if (newPos < mPlayStartMsec)
                    newPos = mPlayStartMsec;
                mPlayer.seekTo(newPos);
            } else {
                mStartPos = trap(mStartPos - mWaveformView.secondsToPixels(getStep()));
                updateDisplay();
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);
            }
        }
    };

    protected View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                int newPos = 5000 + mPlayer.getCurrentPosition();
                if (newPos > mPlayEndMsec)
                    newPos = mPlayEndMsec;
                mPlayer.seekTo(newPos);
            } else {
                mStartPos = trap(mStartPos + mWaveformView.secondsToPixels(getStep()));
                updateDisplay();
                mStartMarker.requestFocus();
                markerFocus(mStartMarker);
            }
        }
    };

    protected View.OnClickListener mMarkStartListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mStartPos = mWaveformView.millisecsToPixels(mPlayer.getCurrentPosition() + mPlayStartOffset);
                updateDisplay();
            }
        }
    };

    protected View.OnClickListener mMarkEndListener = new View.OnClickListener() {
        public void onClick(View sender) {
            if (mIsPlaying) {
                mEndPos = mWaveformView.millisecsToPixels(mPlayer.getCurrentPosition() + mPlayStartOffset);
                updateDisplay();
                handlePause();
            }
        }
    };

    protected TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        public void afterTextChanged(Editable s) {
            if (mStartText.hasFocus()) {
                try {
                    mStartPos = mWaveformView.secondsToPixels(Double.parseDouble(mStartText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
            if (mEndText.hasFocus()) {
                try {
                    mEndPos = mWaveformView.secondsToPixels(Double.parseDouble(mEndText.getText().toString()));
                    updateDisplay();
                } catch (NumberFormatException e) {
                }
            }
        }
    };

    private Runnable mTimerRunnable = new Runnable() {
        public void run() {
            // Updating an EditText is slow on Android.  Make sure
            // we only do the update if the text has actually changed.
            if (mStartPos != mLastDisplayedStartPos && !mStartText.hasFocus()) {
                mStartText.setText(formatTime(mStartPos));
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos && !mEndText.hasFocus()) {
                mEndText.setText(formatTime(mEndPos));
                mLastDisplayedEndPos = mEndPos;
            }

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            // Not initialized yet
            return;
        }

        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }

            mPlayStartOffset = 0;

            int startFrame = mWaveformView.secondsToFrames(mPlayStartMsec * 0.001);
            int endFrame = mWaveformView.secondsToFrames(mPlayEndMsec * 0.001);
            int startByte = mSoundFile.getSeekableFrameOffset(startFrame);
            int endByte = mSoundFile.getSeekableFrameOffset(endFrame);
            if (startByte >= 0 && endByte >= 0) {
                try {
                    mPlayer.reset();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    FileInputStream subsetInputStream = new FileInputStream(mFile.getAbsolutePath());
                    mPlayer.setDataSource(subsetInputStream.getFD(),startByte, endByte - startByte);
                    mPlayer.prepare();
                    mPlayStartOffset = mPlayStartMsec;
                } catch (Exception e) {
                    mPlayer.reset();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setDataSource(mFile.getAbsolutePath());
                    mPlayer.prepare();
                    mPlayStartOffset = 0;
                }
            }

            mPlayer.setOnCompletionListener((MediaPlayer mediaPlayer) -> handlePause());
            mIsPlaying = true;

            if (mPlayStartOffset == 0) {
                mPlayer.seekTo(mPlayStartMsec);
            }
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
        }
    }

    private synchronized void playMusic(int startPosition) {
        onPlay(startPosition);
        //Send code bluetooth
    }
}

