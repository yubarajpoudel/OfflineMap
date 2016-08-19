package com.yuvi.routeme.map.downloader;

import com.yuvi.routeme.R;
import com.yuvi.routeme.map.marker.CustomMarkerModel;

import org.osmdroid.util.GeoPoint;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 4/28/2015.
 */
public class NepalMap extends AbstractMap {

    @Override
    protected String getDirectoryName() {
        return "kathmandu_map";
    }

    @Override
    protected long getMapSize() {
        // size of total nepal map
        return 167143014;
    }

    @Override
    protected String getMapFileUrl() {
        return "http://192.168.1.124:8000/nepal.map";
    }

    @Override
    protected String getEdgesUrl() {
        return "http://192.168.1.124:8000/edges";
    }

    @Override
    protected String getGeometryUrl() {
        return "http://192.168.1.124:8000/geometry";
    }

    @Override
    protected String getLocationIndexUrl() {
        return "http://192.168.1.124:8000/location_index";
    }

    @Override
    protected String getNamesUrl() {
        return "http://192.168.1.124:8000/names";
    }

    @Override
    protected String getNodesUrl() {
        return "http://192.168.1.124:8000/nodes";
    }

    @Override
    protected String getPropertiesUrl() {
        return "http://192.168.1.124:8000/properties";
    }

    @Override
    protected String getShorcutFastestCar() {
        return "http://192.168.1.124:8000/nodes_ch_fastest_car";
    }

    @Override
    protected String getNodesFastestCar() {
        return "http://192.168.1.124:8000/shortcuts_fastest_car";
    }

    @Override
    public GeoPoint getCenterGeoPoint() {
        return new GeoPoint(27.6946843, 85.3310636, 15);
    }

    @Override
    protected CustomMarkerModel[] getCustomMarkerModels() {
        return new CustomMarkerModel[]{
                new CustomMarkerModel("Sankhamul", R.drawable.pin_a_blue, 27.6804, 85.3306),
                new CustomMarkerModel("Kalanki", R.drawable.pin_a_blue, 27.6931, 85.2806),
                new CustomMarkerModel("Kalimati", R.drawable.pin_a_blue, 27.7322, 85.2891),
                new CustomMarkerModel("Slash Plus", R.drawable.pin_a_blue, 27.6706527, 85.3181116),
                new CustomMarkerModel("Nepal tourism board", R.drawable.pin_a_blue, 27.7018335, 85.3148257)
//                new CustomMarkerModel("Гродно", R.drawable.pin_a_green, 53.686791, 23.848546),
//                new CustomMarkerModel("Витебск", R.drawable.pin_a_orange, 55.19611, 30.18500),
//                new CustomMarkerModel("Минский железнодорожный вокзал", R.drawable.pin_a_red, 53.890667, 27.55111),
//                new CustomMarkerModel("Могилёв 1-на-Днепре", R.drawable.pin_a_viola, 53.92611, 30.33833),
//                new CustomMarkerModel("Гомель-Пассажирский", R.drawable.pin_a_blue, 52.43083, 30.99111),
//                new CustomMarkerModel("Автовокзал г. Бреста", R.drawable.pin_a_green, 52.098363, 23.691269),
//                new CustomMarkerModel("Автовокзал Гродно", R.drawable.pin_a_orange, 53.677871, 23.843805),
//                new CustomMarkerModel("Автовокзал \"Витебск\"", R.drawable.pin_a_red, 55.196350, 30.187946),
//                new CustomMarkerModel("Центральный автовокзал", R.drawable.pin_a_viola, 53.890068, 27.554977),
//                new CustomMarkerModel("Восточный автовокзал", R.drawable.pin_a_blue, 53.87722, 27.59889),
//                new CustomMarkerModel("Московский автовокзал", R.drawable.pin_a_green, 53.928603, 27.636870),
//                new CustomMarkerModel("Автовокзал Могилев", R.drawable.pin_a_orange, 53.913231, 30.347587),
//                new CustomMarkerModel("Гомельский объединённый автовокзал", R.drawable.pin_a_red, 52.434200, 30.993193),
        };
    }

    @Override
    public int getDefaultZoom() {
        return 12;
    }
}
