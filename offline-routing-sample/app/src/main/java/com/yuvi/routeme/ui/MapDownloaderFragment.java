package com.yuvi.routeme.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.squareup.otto.Subscribe;
import com.yuvi.routeme.App;
import com.yuvi.routeme.map.downloader.MapDownloaderLoader;
import com.yuvi.routeme.util.event.MapDownloaderProgressEvent;
import com.yuvi.routeme.util.event.MapSuccessfulDownloadedEvent;


/**
 * A placeholder fragment containing a simple view.
 */
public class MapDownloaderFragment extends Fragment {

    private ViewSwitcher mViewSwitcher;
    private TextView mDownloadingProgressTextView;
    private ProgressBar mDownloadingProgressBar;

    private final LoaderManager.LoaderCallbacks<Boolean> mLoadManager = new LoaderManager.LoaderCallbacks<Boolean>() {

        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            return new MapDownloaderLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
            getLoaderManager().destroyLoader(drawable.com.lassana.routeme.R.id.loader_map_downloader);
            App.getApplication(getActivity()).sendOttoEvent(new MapSuccessfulDownloadedEvent());
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
        }
    };

    public MapDownloaderFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(drawable.com.lassana.routeme.R.layout.fragment_map_downloader, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(drawable.com.lassana.routeme.R.id.loader_map_downloader);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewSwitcher = (ViewSwitcher) view.findViewById(drawable.com.lassana.routeme.R.id.view_flipper);
        mDownloadingProgressTextView = (TextView) view.findViewById(drawable.com.lassana.routeme.R.id.text_view_downloading_progress);
        mDownloadingProgressBar = (ProgressBar) view.findViewById(drawable.com.lassana.routeme.R.id.progress_bar_downloading);
        view.findViewById(drawable.com.lassana.routeme.R.id.button_start_downloading).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownloading();
            }
        });
        view.findViewById(drawable.com.lassana.routeme.R.id.button_cancel_downloading).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDownloading();
            }
        });
        mViewSwitcher.setDisplayedChild(getLoaderManager().getLoader(drawable.com.lassana.routeme.R.id.loader_map_downloader) == null ? 0 : 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getApplication(getActivity()).unregisterOttoBus(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getApplication(getActivity()).registerOttoBus(this);
    }

    private void stopDownloading() {
        getLoaderManager().destroyLoader(drawable.com.lassana.routeme.R.id.loader_map_downloader);
        mViewSwitcher.setDisplayedChild(0);
    }

    private void startDownloading() {
        answerAvailable(new MapDownloaderProgressEvent(0));
        getLoaderManager().initLoader(drawable.com.lassana.routeme.R.id.loader_map_downloader, null, mLoadManager);
        mViewSwitcher.setDisplayedChild(1);
    }

    @Subscribe
    public void answerAvailable(final MapDownloaderProgressEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadingProgressTextView.setText(getString(drawable.com.lassana.routeme.R.string.text_view_downloading_progress, event.progress));
                mDownloadingProgressBar.setProgress(event.progress);
            }
        });

    }
}
