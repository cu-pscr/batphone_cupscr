/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.ui;

import org.servalproject.R;

import android.app.Activity;
import android.os.Bundle;
import android.location.Location;
import android.support.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.util.List;

/**
 * activity to show message if maps isn't installed
 *
 * Cathleen Note: first map goes here
 */
public class MapsActivity extends Activity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	private MapView mapView;
	private MapboxMap map;
	private PermissionsManager permissionsManager;
	private LocationEngine locationEngine;
	private LocationLayerPlugin locationLayerPlugin;
	private Location originLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		Mapbox.getInstance(this, getString(R.string.access_token));
		setContentView(R.layout.maps);
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);

		// callback
		mapView.getMapAsync(this);
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		this.map = mapboxMap;
		enableLocation();


	}

	private void enableLocation(){ // whether or not the user has granted permissions
		if (PermissionsManager.areLocationPermissionsGranted(this)){
			initializeLocationEngine();
			initializeLocationLayer();
		}
		else{
			permissionsManager = new PermissionsManager(this);
			permissionsManager.requestLocationPermissions(this);
		}
	}

	@SuppressWarnings("MissingPermission") // already checked permissions with permissions manager
	private void initializeLocationEngine(){
		// PROBLEM: only showing last location
		//LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
		//locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
		locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
		locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
		locationEngine.activate();
		locationEngine.addLocationEngineListener(this);
		locationEngine.setInterval(8);
		//locationEngine.setFastestInterval(1000);

		Location lastLocation = locationEngine.getLastLocation();
		if (lastLocation != null){
			originLocation = lastLocation;
			setCameraPosition(lastLocation);
		}
		else{
			locationEngine.addLocationEngineListener(this);
		}
	}

	@SuppressWarnings("MissingPermission") // already checked permissions with permissions manager
	private void initializeLocationLayer(){ // what user sees
		LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView, map);
		locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
		locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
		//getLifecycle().addObserver(locationLayerPlugin); // from website
	}

	private void setCameraPosition(Location location){
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
				location.getLongitude()), 13.0)); // zoom to new location
	}



	// adding user location
	@Override
	@SuppressWarnings("MissingPermission") // already checked permissions with permissions manager
	public void onConnected() { // have provider start sending updates
		locationEngine.requestLocationUpdates();;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null){
			originLocation = location;
			setCameraPosition(location);
		}
	}

	@Override
	public void onExplanationNeeded(List<String> permissionsToExplain) { // user denies access, second time can present a dialogue on why they need to grant access
		//TODO
		// Present toast or dialogue to explain why they need to grant permissions
		// Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPermissionResult(boolean granted) {
		if (granted) {
			enableLocation();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	// for the map
	@SuppressWarnings("MissingPermission")
	@Override
	public void onStart(){
		super.onStart();
		mapView.onStart();
		if (locationEngine != null){
			locationEngine.requestLocationUpdates();
		}
		if (locationLayerPlugin != null) {
			locationLayerPlugin.onStart();
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause(){
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mapView.onStop();
		if (locationEngine != null){
			locationEngine.removeLocationUpdates();
		}
		if (locationLayerPlugin != null) {
			//locationLayerPlugin.onStop();
			locationLayerPlugin.onStart();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (locationEngine != null){
			locationEngine.deactivate();
		}
		mapView.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

}