package org.servalproject.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.storage.FileSource;
//import com.mapbox.mapboxsdk.testapp.utils.FileUtils;
//import com.mapbox.mapboxsdk.maps.Style;




import org.servalproject.R;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.LibraryLoader;
import com.mapbox.mapboxsdk.MapStrictMode;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.TelemetryDefinition;
import com.mapbox.mapboxsdk.net.ConnectivityReceiver;
import com.mapbox.mapboxsdk.storage.FileSource;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
//import com.mapbox.mapboxsdk.utils.FileUtils;



public class MergeOfflineDb extends AppCompatActivity {

    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    static final String LOG_TAG = "Mapbox";

    OfflineManager offlineManager;
    MapView mapview;
    //MapboxMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_offline_regions);
        Mapbox.getInstance(this, getString(R.string.access_token));

        // want to access and list shared .db files
        // data/data/org.servalproject/files/mbgl-offline.db

        // merge the chosen one with our main database
        // should be able to open offline manager and see that the map has been added

        // TODO need to change the name of .db file that stores location

        // folder picker to access storage/self/primary/Android/data/org.servalproject/files/saved
        // 1. move file over to device
        // pick which file you want eventually, for right now, we will assume its the only file in there

        mapview = findViewById(R.id.mapView);
        mapview.onCreate(savedInstanceState);


        offlineManager = OfflineManager.getInstance(this);
        mergeOfflineRegions();

    }

    private void mergeOfflineRegions() {
        //File offlineMap = new File(Environment.getExternalStorageDirectory(), "mbgl-offline.db");
        //String path = offlineMap.getAbsolutePath();
        String path = "/storage/self/primary/Android/data/org.servalproject/files/saved/mbgl-offline.db";
        File offlineFile = new File(path);
        String path2 = offlineFile.getAbsolutePath();

        if (offlineFile.exists())
            offlineManager.mergeOfflineRegions(path, new OfflineManager.MergeOfflineRegionsCallback() {
                @Override
                public void onMerge(OfflineRegion[] offlineRegions) {
                    //showStatus(String.format("Merged: %d regions", offlineRegions.length));
                    Toast.makeText(MergeOfflineDb.this, R.string.mergeDB_progress_success, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(String error) {
                    //showStatus("Error merging regions: " + error);
                    Toast.makeText(MergeOfflineDb.this, R.string.mergeDB_error, Toast.LENGTH_LONG).show();
                }
            });
        else {
            //showStatus("File does not exist!");
            Toast.makeText(MergeOfflineDb.this, R.string.mergeDB_noFilesFound, Toast.LENGTH_LONG).show();
        }
    }


    //File newFile = new File("/storage/self/primary/Android/data/org.servalproject/files/saved/sharedLocation.db");

    //offlineFile.renameTo(newFile);


    // offlineManager.mergeOfflineRegions(String path, offlineManager.MergeOfflineRegionsCallback callback)
    /*
    path = the path to the secondary database
    callback = completion error callback if it fails
     */

}
