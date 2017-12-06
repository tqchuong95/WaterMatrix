package kltn.musicapplication;

import android.Manifest;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kltn.musicapplication.utils.BlurBuilder;

/**
 * Created by UITCV on 12/5/2017.
 */

public class SelecMusicActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private BluetoothDevice bluetoothDevice;
    private ListView listView;
    private ArrayList<String> paths, playsong;
    private ArrayAdapter<String> arrayAdapter;
    public static String pathname;
    //private String pathname;
    private int position;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectmusic);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bm = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        final Bitmap blur_bitmap = BlurBuilder.blur(this, bm);
        this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), blur_bitmap));

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        bluetoothDevice = getIntent().getExtras().getParcelable(MainActivity.EXTRA_DEVICE);
        setTitle(bluetoothDevice.getName());

        checkAndRequestPermissions();

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        paths = new ArrayList<>();
        playsong = new ArrayList<>();
        getMusicPlayer();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playsong);
        listView.setAdapter(arrayAdapter);
        listView.setBackgroundColor(Color.WHITE);

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

    private void getMusicPlayer(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()){
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String currentTitle =  songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentLocation = songCursor.getString(songLocation);
                playsong.add(currentTitle + "\n" + currentArtist);
                paths.add(currentLocation);
            } while (songCursor.moveToNext());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.position = position;
        pathname = paths.get(position);
        Intent intent = new Intent(SelecMusicActivity.this, MSEditorActivity.class);
        intent.putExtra(MainActivity.EXTRA_DEVICE, bluetoothDevice);
        startActivity(intent);
    }

}
