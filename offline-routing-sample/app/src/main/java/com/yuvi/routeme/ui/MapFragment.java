package com.yuvi.routeme.ui;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.yuvi.routeme.map.MapsConfig;
import com.yuvi.routeme.map.downloader.AbstractMap;
import com.yuvi.routeme.map.marker.CustomMarker;
import com.yuvi.routeme.map.marker.CustomMarkerModel;
import com.yuvi.routeme.map.marker.MyLocationOverlayItem;
import com.yuvi.routeme.map.routing.RouteLoader;
import com.yuvi.routeme.map.view.MapsforgeMapView;
import com.yuvi.routeme.util.LogUtils;

import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.cache.TileCache;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.PathOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.yuvi.routeme.util.LogUtils.LOGD;


/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 4/28/2015.
 */
public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LogUtils.makeLogTag(MapFragment.class);

    @Override
    public void onConnected(Bundle bundle) {
        if (mLastUserPosition == null) {
            mLastUserPosition = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            LOGD(TAG, String.format("%s: %f", "latitude",
//                    mLastUserPosition.getLatitude()));
//            LOGD(TAG, String.format(String.format("%s: %f", "Longitude",
//                    mLastUserPosition.getLongitude())));
            updateUserPosition(true);
//            saveLocationInPreference(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LOGD(TAG, "onLocationChanged: " + location);
        mLastUserPosition = location;
        updateUserPosition(true);
//            destroyLocationManager();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private interface SavedState {
        String ZOOM_LVL = "zoom_lvl";
        String LATI = "lati";
        String LONGI = "longi";
        String LOCATION = "location";
        String TARGET = "target";
        String ROUTE = "route";
    }

    private TileCache tileCache;
    private MapsforgeMapView mMapView;
    private DefaultResourceProxyImpl mDefaultResourceProxy;
    private ItemizedIconOverlay<MyLocationOverlayItem> mMyLocationOverlayItem;

    private Location mLastUserPosition;
    private CustomMarkerModel mTarget;
    private PathOverlay mPathOverlay;
    private ArrayList<GeoPoint> mCurrentRouteGeoPoints;

    private LocationManager mLocationManager;
    //    private MyLocationListener mLocationListener;
    protected GoogleApiClient mGoogleApiClient;
    //protected Location mLastLocation;
    LocationRequest mLocationRequest;

    private View mOverviewLayout;
    private Button mFindRouteButton;
    private ImageView mMarkerImageView;
    private TextView mMarkerTextView;
    private TextView mMarkerDescriptionTextView;


    private Marker.OnMarkerClickListener mOnMarkerClickListener = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            LOGD(TAG, "marker is clicked");
            if (marker instanceof CustomMarker) {
                final CustomMarker cMarker = (CustomMarker) marker;
                mTarget = cMarker.model;
                LOGD(TAG, "mTarget = " + mTarget.getTitle());
                updateSelectedMarker(true);
                return true;
            } else {
                return false;
            }
        }
    };

    private ItemizedIconOverlay.OnItemGestureListener<MyLocationOverlayItem> onUserItemGestureListener =
            new ItemizedIconOverlay.OnItemGestureListener<MyLocationOverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int i, MyLocationOverlayItem overlayItem) {
                    Toast.makeText(getActivity(), overlayItem.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }

                @Override
                public boolean onItemLongPress(int i, MyLocationOverlayItem overlayItem) {
                    return false;
                }
            };


//    private class MyLocationListener implements LocationListener {
//
//        public void onLocationChanged(Location location) {
//            LOGD(TAG, "onLocationChanged: " + location);
//            mLastUserPosition = location;
//            updateUserPosition(true);
//            destroyLocationManager();
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            LOGD(TAG, "onStatusChanged: " + provider + "; status: " + status);
//        }
//
//        public void onProviderEnabled(String provider) {
//            LOGD(TAG, "onProviderEnabled: " + provider);
//        }
//
//        public void onProviderDisabled(String provider) {
//            LOGD(TAG, "onProviderDisabled: " + provider);
//        }
//
//    }

    private final LoaderManager.LoaderCallbacks<RouteLoader.Result> mRouteLoadManager
            = new LoaderManager.LoaderCallbacks<RouteLoader.Result>() {
        @Override
        public Loader<RouteLoader.Result> onCreateLoader(int id, Bundle args) {
            return new RouteLoader(getActivity(), mLastUserPosition, mTarget.getLocation());
        }

        @Override
        public void onLoadFinished(Loader<RouteLoader.Result> loader, RouteLoader.Result data) {
            mCurrentRouteGeoPoints = new ArrayList<>(data.getGeoPoints());
            getLoaderManager().destroyLoader(drawable.com.lassana.routeme.R.id.loader_find_route);
            mMapView.post(new Runnable() {
                @Override
                public void run() {
                    mFindRouteButton.setEnabled(true);
                    updateCurrentRoute();
                }
            });
        }

        @Override
        public void onLoaderReset(Loader<RouteLoader.Result> loader) {

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rvalue = inflater.inflate(drawable.com.lassana.routeme.R.layout.fragment_map, container, false);
        tileCache = AndroidUtil.createTileCache(getActivity(), MapsConfig.TILE_CACHE_ID, MapsConfig.TILE_SIZE, MapsConfig.SCREEN_RATION, MapsConfig.OVERDRAW);
        final File mapFile = AbstractMap.instance().getMapsforgeFile(getActivity());
        mMapView = new MapsforgeMapView(getActivity(), tileCache, mapFile.getAbsolutePath());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout forMap = (RelativeLayout) rvalue.findViewById(drawable.com.lassana.routeme.R.id.layout_map);
        forMap.addView(mMapView, 0, params);
        buildGoogleApiClient();
        return rvalue;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView.setMultiTouchControls(true);
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(false);
        mMapView.setUseDataConnection(false);

        view.findViewById(drawable.com.lassana.routeme.R.id.img_btn_zoom_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mMapView.getController().zoomIn();
            }
        });
        view.findViewById(drawable.com.lassana.routeme.R.id.img_btn_zoom_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mMapView.getController().zoomOut();
            }
        });
        view.findViewById(drawable.com.lassana.routeme.R.id.btn_get_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "My location button clicked; mLocationManager is" + (mGoogleApiClient == null ? " " : " NOT ") + "null");
                if (mGoogleApiClient == null) buildGoogleApiClient();
            }
        });

        mOverviewLayout = view.findViewById(drawable.com.lassana.routeme.R.id.layout_route);
        mFindRouteButton = (Button) view.findViewById(drawable.com.lassana.routeme.R.id.button_find_route);
        mMarkerImageView = (ImageView) view.findViewById(drawable.com.lassana.routeme.R.id.image_view_marker_overview);
        mMarkerTextView = (TextView) view.findViewById(drawable.com.lassana.routeme.R.id.text_view_marker_title);
        mMarkerDescriptionTextView = (TextView) view.findViewById(drawable.com.lassana.routeme.R.id.text_view_marker_description);

        mFindRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTarget != null
                        && mTarget.getLocation() != null
                        && mLastUserPosition != null) {
                    getLoaderManager().initLoader(drawable.com.lassana.routeme.R.id.loader_find_route, null, mRouteLoadManager);
                    mFindRouteButton.setEnabled(false);
                }
            }
        });

        final GeoPoint initialCenter;
        if (savedInstanceState != null) {
            mMapView.getController().setZoom(savedInstanceState.getInt(SavedState.ZOOM_LVL));
            initialCenter = new GeoPoint(savedInstanceState.getDouble(SavedState.LATI), savedInstanceState.getDouble(SavedState.LONGI));
            mLastUserPosition = savedInstanceState.getParcelable(SavedState.LOCATION);
            mTarget = savedInstanceState.getParcelable(SavedState.TARGET);
            mCurrentRouteGeoPoints = savedInstanceState.getParcelableArrayList(SavedState.ROUTE);
            updateUserPosition(false);
        } else {
            mMapView.getController().setZoom(AbstractMap.instance().getDefaultZoom());
            initialCenter = AbstractMap.instance().getCenterGeoPoint();
            /*
            mLastUserPosition = new Location("");
            mLastUserPosition.setLatitude(35.159722);
            mLastUserPosition.setLongitude(33.377778);
            updateUserPosition(false);
            */
        }
        updateSelectedMarker(false);
        updateFindRouteButtonState();
        updateCurrentRoute();

        mFindRouteButton.setEnabled(!RouteLoader.isRunning(getLoaderManager()));

        mMapView.getController().setCenter(initialCenter);
        mMapView.setCenter(initialCenter);

        mMapView.getOverlays().add(AbstractMap.instance().createMarkersCluster(mMapView, getDefaultResourceProxyImpl(), mOnMarkerClickListener));
        mMapView.invalidate();
    }


    private void updateFindRouteButtonState() {
        if (getLoaderManager().getLoader(drawable.com.lassana.routeme.R.id.loader_find_route) != null) {
            mFindRouteButton.setEnabled(false);
        } else {
            if (mLastUserPosition != null && mTarget != null) {
                mFindRouteButton.setEnabled(true);
            } else {
                mFindRouteButton.setEnabled(false);
            }
        }
    }

    private DefaultResourceProxyImpl getDefaultResourceProxyImpl() {
        return mDefaultResourceProxy == null
                ? mDefaultResourceProxy = new DefaultResourceProxyImpl(getActivity())
                : mDefaultResourceProxy;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LOGD(TAG, "onSaveInstanceState; mMapView is" + (mMapView == null ? " " : " NOT ") + "null");
        if (mMapView != null) {
            final IGeoPoint mapCenter = mMapView.getMapCenter();
            outState.putDouble(SavedState.LATI, mapCenter.getLatitude());
            outState.putDouble(SavedState.LONGI, mapCenter.getLongitude());
            outState.putInt(SavedState.ZOOM_LVL, mMapView.getZoomLevel());
        }
        outState.putParcelable(SavedState.LOCATION, mLastUserPosition);
        outState.putParcelable(SavedState.TARGET, mTarget);
        outState.putParcelableArrayList(SavedState.ROUTE, mCurrentRouteGeoPoints);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tileCache != null) tileCache.destroy();
    }

//    private void initLocationManager() {
//        LOGD(TAG, "initLocationManager");
//        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        mLocationListener = new MyLocationListener();
//        final Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
//        final String usedProvider = mLocationManager.getBestProvider(criteria, true);
//        mLocationManager.requestLocationUpdates(usedProvider, 0, 1, mLocationListener);
//        LOGD(TAG, "Location manager will be use " + usedProvider + " as location provider");
//    }


//    private void destroyLocationManager() {
//        LOGD(TAG, "destroyLocationManager");
//        if (mLocationManager != null) {
//            mLocationManager.removeUpdates(mLocationListener);
//            mLocationListener = null;
//            mLocationManager = null;
//        }
//    }


    private void updateUserPosition(boolean moveToCenter) {
        if (isDetached()) return;

        if (mMyLocationOverlayItem != null) {
            mMapView.getOverlays().remove(mMyLocationOverlayItem);
        }
        if (mLastUserPosition != null) {
            final List<MyLocationOverlayItem> mMyLocationOverlayItemArray = new ArrayList<>();
            MyLocationOverlayItem object =
                    new MyLocationOverlayItem(new GeoPoint(mLastUserPosition.getLatitude(), mLastUserPosition.getLongitude()),
                            getResources(),
                            drawable.com.lassana.routeme.R.string.title_map_my_location,
                            drawable.com.lassana.routeme.R.drawable.image_my_location);
            mMyLocationOverlayItemArray.add(object);
            mMyLocationOverlayItem =
                    new ItemizedIconOverlay<>(mMyLocationOverlayItemArray,
                            onUserItemGestureListener,
                            getDefaultResourceProxyImpl());
            mMapView.getOverlays().add(mMyLocationOverlayItem);
            if (moveToCenter) mMapView.setCenter(mLastUserPosition);
            updateDistanceToTarget();
            updateFindRouteButtonState();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i("MainActivity", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void updateSelectedMarker(boolean moveToCenter) {
        if (mTarget != null) {
            mOverviewLayout.setVisibility(View.VISIBLE);
            mMarkerImageView.setImageDrawable(mTarget.getDrawable(getResources()));
            mMarkerTextView.setText(mTarget.getTitle());
            updateDistanceToTarget();

            if (moveToCenter) mMapView.setCenter(mTarget.getLocation());
            updateFindRouteButtonState();
        } else {
            mOverviewLayout.setVisibility(View.GONE);
        }
    }

    private void updateDistanceToTarget() {
        if (mLastUserPosition != null && mTarget != null && mTarget.getLocation() != null) {
            float distance = mLastUserPosition.distanceTo(mTarget.getLocation());
            mMarkerDescriptionTextView.setText(String.format("Distance: %.2f km", distance / 1000));
        }
    }

    private void updateCurrentRoute() {
        if (mPathOverlay != null) {
            mMapView.getOverlays().remove(mPathOverlay);
        }
        if (mCurrentRouteGeoPoints != null) {
            final int color = getResources().getColor(android.R.color.holo_blue_dark);
            mPathOverlay = new PathOverlay(color, 15.0f, getDefaultResourceProxyImpl());
            for (GeoPoint geoPoint : mCurrentRouteGeoPoints) {
                mPathOverlay.addPoint(geoPoint);
            }
            mMapView.getOverlays().add(mPathOverlay);
            mMapView.setCenter(mLastUserPosition);
            mMapView.getController().setZoom(16);
            mMapView.invalidate();
        }
    }
}
