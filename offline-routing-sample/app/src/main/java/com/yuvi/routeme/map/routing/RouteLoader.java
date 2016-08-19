package com.yuvi.routeme.map.routing;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.yuvi.routeme.R;
import com.yuvi.routeme.map.downloader.AbstractMap;
import com.yuvi.routeme.util.LogUtils;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.yuvi.routeme.util.LogUtils.LOGD;
import static com.yuvi.routeme.util.LogUtils.LOGE;
import static com.yuvi.routeme.util.LogUtils.LOGI;
import static com.yuvi.routeme.util.LogUtils.LOGW;


/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 5/21/2015.
 */
public class RouteLoader extends AsyncTaskLoader<RouteLoader.Result> {
    public static class Result {
        public static final Result INTERNAL_ERROR = new Result(false, true);
        public static final Result NO_ROUTE = new Result(true, false);

        private final List<GeoPoint> mGeoPoints;
        private final boolean mIsError;
        private final boolean mIsNoRoute;

        public Result(@NonNull List<GeoPoint> geoPoints) {
            mGeoPoints = geoPoints;
            mIsError = mIsNoRoute = false;
        }

        private Result(boolean isNoRoute, boolean isError) {
            mGeoPoints = null;
            mIsNoRoute = isNoRoute;
            mIsError = isError;
        }

        public List<GeoPoint> getGeoPoints() {
            return mGeoPoints;
        }

        public boolean isError() {
            return mIsError;
        }

        public boolean isNoRoute() {
            return mIsNoRoute;
        }
    }

    private static final String TAG = LogUtils.makeLogTag(RouteLoader.class);

    private final Location mStartLocation;
    private final Location mEndLocation;

    public RouteLoader(@NonNull Context context, @NonNull Location startLocation, @NonNull Location endLocation) {
        super(context);
        mStartLocation = startLocation;
        mEndLocation = endLocation;
    }

    @Override
    public Result loadInBackground() {
        LOGI(TAG, "#loadInBackground; mStartLocation = " + mStartLocation + "; mEndLocation = " + mEndLocation);
        try {


            final GraphHopper hopper = new GraphHopper().forMobile();
            hopper.setInMemory();
            final File mapsforgeFile = AbstractMap.instance().getMapsforgeFile(getContext());
            hopper.setOSMFile(mapsforgeFile.getAbsolutePath());
            hopper.setGraphHopperLocation(mapsforgeFile.getParent());
            hopper.setEncodingManager(new EncodingManager("car"));
            hopper.importOrLoad();
            final GHRequest req =
                    new GHRequest(
                            mStartLocation.getLatitude(),
                            mStartLocation.getLongitude(),
                            mEndLocation.getLatitude(),
                            mEndLocation.getLongitude())
                            .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
            req.getHints().
                    put(Parameters.Routing.INSTRUCTIONS, "false");
//            GHResponse resp = hopper.route(req);
            final GHResponse rsp = hopper.route(req);
            if (rsp.hasErrors()) {
                LOGW(TAG, "GHResponse contains errors!");
                List<Throwable> errors = rsp.getErrors();
                for (int i = 0; i < errors.size(); i++) {
                    LOGE(TAG, "Graphhopper error #" + i, errors.get(i));
                }
                return Result.INTERNAL_ERROR;
            }

//            if (!rsp.isFound()) {
//                LOGW(TAG, "Graphhopper cannot find route!");
//                return Result.NO_ROUTE;
//            }
            else {
                PathWrapper paths = rsp.getBest();
                final List<GeoPoint> geoPoints = new LinkedList<>();
                final PointList points = paths.getPoints();
                double lati, longi, alti;
                for (int i = 0; i < points.getSize(); i++) {
                    lati = points.getLatitude(i);
                    longi = points.getLongitude(i);
                    alti = points.getElevation(i);
                    geoPoints.add(new GeoPoint(lati, longi, alti));
                }
                return new Result(geoPoints);
            }
        } catch (OutOfMemoryError e) {
            LOGE(TAG, "Graphhoper OOM", e);
            return Result.INTERNAL_ERROR;
        }

    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        LOGD(TAG, "onStartLoading");
        forceLoad();
    }



    public static boolean isRunning(@NonNull LoaderManager loaderManager) {
        final Loader<Object> loader = loaderManager.getLoader(R.id.loader_find_route);
        return loader != null && loader.isStarted();
    }

}
