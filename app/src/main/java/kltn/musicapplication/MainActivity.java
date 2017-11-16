package kltn.musicapplication;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kltn.musicapplication.Music.App;
import kltn.musicapplication.Music.MusicPlayer;
import kltn.musicapplication.Music.PlayListAdapter;
import kltn.musicapplication.utils.BlurBuilder;

import static kltn.musicapplication.Music.MusicPlayer.PLAYER_PLAY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MusicPlayer.OnCompletionListener, AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener {
    private ListView lvListMusic;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPrevious;
    private ImageView ivRepeat;
    private ImageView ivShuffle;
    private ImageView ivHome;
    private TextView tvTimeCurrent;
    private TextView tvTimeTotal;
    private SeekBar sbProTime;
    private ArrayList<String> paths;
    private int timeProcess;
    private int timeTotal;
    private PlayListAdapter adapter;
    private MusicPlayer musicPlayer;
    private boolean isRunning;
    private int UPDATE_TIME = 1;
    private int timeCurrent;
    private int position;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));
        // ánh xạ
        initViews();
        // sét sự kiện cho các nút cần click
        initListeners();
        // thêm nội dung cho chương trình
        initComponents();

    }

    private void initListeners() {
        lvListMusic.setOnItemClickListener(this);
        ivNext.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivShuffle.setOnClickListener(this);
        ivRepeat.setOnClickListener(this);
        ivHome.setOnClickListener(this);
        sbProTime.setOnSeekBarChangeListener(this);
    }

    private void initViews() {
        lvListMusic = (ListView) findViewById(R.id.lv_listMusic);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        ivPrevious = (ImageView) findViewById(R.id.iv_previous);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivRepeat = (ImageView) findViewById(R.id.iv_repeat);
        ivShuffle = (ImageView) findViewById(R.id.iv_shuffle);
        ivHome = (ImageView) findViewById(R.id.iv_home);
        tvTimeCurrent = (TextView) findViewById(R.id.tv_time1);
        tvTimeTotal = (TextView) findViewById(R.id.tv_time2);
        tvTitle = (TextView) findViewById(R.id.tv_song);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        sbProTime = (SeekBar) findViewById(R.id.sb_process_time);
    }

    private void initComponents() {
        initList();
        adapter = new PlayListAdapter(App.getContext(), paths);
        lvListMusic.setAdapter(adapter);
        musicPlayer = new MusicPlayer();
        musicPlayer.setOnCompletionListener(this);
    }

    private void initList() {
        paths = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Zing MP3";
        File file = new File(path);
        File[] files = file.listFiles(); // lay tat ca cac file trong thu muc. ở đây là Download
        for (int i = 0; i < files.length; i++) {
            // doc tat ca cac file co trong download them vao list nhac
            String s = files[i].getName();
            if (s.endsWith(".mp3")) {
                // thủ thuật kiểm tra nó có phải đuôi nhạc mp3 không, có thể nó
                // là tệp ảnh hoặc thư mục lúc đó sẽ gây ra lỗi, 1 số định dạng khác có thể có của nhạc là .flat(lostless), .wav, ...
                paths.add(files[i].getAbsolutePath());
            }
        }
        // đọc xong danh sách nhạc, các bạn muốn thêm đuôi nhạc khác như .flac hay .flat
        // không nhớ rõ đuôi nữa thì thêm điều kiện ở trên là được
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_TIME) {
                timeCurrent = musicPlayer.getTimeCurrent();
                tvTimeCurrent.setText(getTimeFormat(timeCurrent));
                sbProTime.setProgress(timeCurrent);
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_shuffle:
                Toast.makeText(this, "Shuffle", Toast.LENGTH_SHORT).show();
                break;

            case R.id.iv_previous:
                previousMusic();
                break;

            case R.id.iv_next:
                nextMusic();
                break;

            case R.id.iv_play:
                if (musicPlayer.getState() == PLAYER_PLAY) {
                    ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    musicPlayer.pause();
                } else {
                    ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    musicPlayer.play();
                }
                break;

            case R.id.iv_repeat:
                Toast.makeText(this, "Repeat", Toast.LENGTH_SHORT).show();
                break;

            case R.id.iv_home:
                Intent intent = new Intent(MainActivity.this, ResActivity.class);
                startActivity(intent);

            default:
                break;
        }
    }

    @Override
    public void OnEndMusic() {
        //khi kết thúc bài hát nó sẽ vào đây
        nextMusic();
        Log.d("Chương", "Ok");
        // như vậy khi kết thúc bài hát nó có thể next bài tiếp theo
        // nếu hết danh sách bài hát nó sẽ quay lại từ bài đầu tiên
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
        String path = paths.get(position);
        playMusic(path);
    }

    private void playMusic(String path) {
        if (musicPlayer.getState() == PLAYER_PLAY) {
            musicPlayer.stop();
        }
        musicPlayer.setup(path);
        musicPlayer.play();
        ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        // set up tên bài hát + ca sĩ
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(paths.get(position));
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        tvArtist.setText(artist);
        tvTitle.setText(title);
        isRunning = true;
        // set up time
        // total time
        tvTimeTotal.setText(getTimeFormat(musicPlayer.getTimeTotal()));
        // process time // set up seekbar
        sbProTime.setMax(musicPlayer.getTimeTotal());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    Message message = new Message();
                    message.what = UPDATE_TIME;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();
    }

    private String getTimeFormat(long time) {
        String tm = "";
        int s;
        int m;
        int h;
        //giây
        s = (int) (time % 60);
        m = (int) ((time - s) / 60);
        if (m >= 60) {
            h = m / 60;
            m = m % 60;
            if (h > 0) {
                if (h < 10)
                    tm += "0" + h + ":";
                else
                    tm += h + ":";
            }
        }
        if (m < 10)
            tm += "0" + m + ":";
        else
            tm += m + ":";
        if (s < 10)
            tm += "0" + s;
        else
            tm += s + "";
        return tm;
    }

    private void previousMusic() {
        position--;
        if (position < 0) {
            position = paths.size() - 1;
        }
        String path = paths.get(position);
        playMusic(path);
    }

    private void nextMusic() {
        position++;
        if (position >= paths.size()) {
            position = 0;
        }
        String path = paths.get(position);
        playMusic(path);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (timeCurrent != progress && timeCurrent != 0)
            musicPlayer.seek(sbProTime.getProgress() * 1000);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
