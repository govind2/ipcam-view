package com.github.niqdev.ipcam;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.github.niqdev.ipcam.settings.SettingsActivity;
import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_AUTH_PASSWORD;
import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_AUTH_USERNAME;
import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_FLIP_HORIZONTAL;
import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_FLIP_VERTICAL;
import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_IPCAM_URL;
import static com.github.niqdev.ipcam.settings.SettingsActivity.PREF_ROTATE_DEGREES;

public class IpCamDefaultActivity extends AppCompatActivity {

    private static final int TIMEOUT = 10;

    @BindView(R.id.mjpegViewDefault)
    MjpegView mjpegView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_ipcam_default);
            ButterKnife.bind(this);
            String videoUrl = "";

            if (getIntent().hasExtra("videoUrl")) {
                videoUrl = getIntent().getStringExtra("videoUrl");
            }
            if (TextUtils.isEmpty(videoUrl)) {
                videoUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
            }
            PREF_IPCAM_URL = videoUrl;
            Log.d("IPCAM", "playing VideoURL = " + PREF_IPCAM_URL + ", userId = " + PREF_AUTH_USERNAME + ", password = " + PREF_AUTH_PASSWORD);
            loadIpCam();

        } catch (Exception e) {
            Log.e("IPCAM", "playing VideoURL = " + PREF_IPCAM_URL + ", userId = " + PREF_AUTH_USERNAME + ", password = " + PREF_AUTH_PASSWORD + " error = "+ e);
        }
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager
                .getDefaultSharedPreferences(this);
    }

    private String getPreference(String key) {
        return getSharedPreferences()
            .getString(key, "");
    }

    private Boolean getBooleanPreference(String key) {
        return getSharedPreferences()
                .getBoolean(key, false);
    }

    private DisplayMode calculateDisplayMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE ?
            DisplayMode.FULLSCREEN : DisplayMode.BEST_FIT;
    }

    private void loadIpCam() {
        try {
            Log.d("IPCAM", " in loadIpCam , playing VideoURL = " + PREF_IPCAM_URL + ", userId = " + PREF_AUTH_USERNAME + ", password = " + PREF_AUTH_PASSWORD);
            Mjpeg.newInstance()
                    .credential(SettingsActivity.PREF_AUTH_USERNAME, SettingsActivity.PREF_AUTH_PASSWORD)
                    .open(SettingsActivity.PREF_IPCAM_URL, TIMEOUT)
                    .subscribe(
                            inputStream -> {
                                mjpegView.setSource(inputStream);
                                mjpegView.setDisplayMode(calculateDisplayMode());
//                    mjpegView.flipHorizontal(getBooleanPreference(PREF_FLIP_HORIZONTAL));
//                    mjpegView.flipVertical(getBooleanPreference(PREF_FLIP_VERTICAL));
//                    mjpegView.setRotate(Float.parseFloat(getPreference(PREF_ROTATE_DEGREES)));
                                mjpegView.showFps(true);
                            },
                            throwable -> {
                                Log.e(getClass().getSimpleName(), "mjpeg error", throwable);
                                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                            });
        } catch ( Exception e) {
            Log.e("IPCAM", "exception in loadIpCam = " + PREF_IPCAM_URL + ", userId = " + PREF_AUTH_USERNAME + ", password = " + PREF_AUTH_PASSWORD + " error = "+ e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadIpCam();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mjpegView.stopPlayback();
    }

}
