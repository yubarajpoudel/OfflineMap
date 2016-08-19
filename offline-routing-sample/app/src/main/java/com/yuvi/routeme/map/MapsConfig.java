package com.yuvi.routeme.map;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 4/28/2015.
 */
public abstract class MapsConfig {

    private MapsConfig() {
    }

    public static final int TILE_SIZE = 256;
    public static final float SCREEN_RATION = 1.0f;
    public static final float OVERDRAW = 1.5f;
    public static final int GRID_SIZE = 150;
    public static final String TILE_CACHE_ID = "mapcache";

}
