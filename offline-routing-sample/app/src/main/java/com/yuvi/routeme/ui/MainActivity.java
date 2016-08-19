package com.yuvi.routeme.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.squareup.otto.Subscribe;
import com.yuvi.routeme.App;
import com.yuvi.routeme.R;
import com.yuvi.routeme.map.downloader.AbstractMap;
import com.yuvi.routeme.util.event.MapSuccessfulDownloadedEvent;

public class MainActivity extends ActionBarActivity {

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            updateContent(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getApplication(this).registerOttoBus(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getApplication(this).unregisterOttoBus(this);
    }

    @Subscribe
    public void onMapDownloaded(MapSuccessfulDownloadedEvent event) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                updateContent(true);
            }
        });
    }

    private void updateContent(boolean force) {
        if (mCurrentFragment == null || force) {
            mCurrentFragment = AbstractMap.instance().exist(this)
                    ? new MapFragment()
                    : new MapDownloaderFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, mCurrentFragment).commit();
        }
    }

}
