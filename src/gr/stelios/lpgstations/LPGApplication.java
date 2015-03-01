package gr.stelios.lpgstations;

import java.io.IOException;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class LPGApplication extends Application implements LocationListener{

	public LocationManager locationManager = null;
	public String lat = null;
	public String lon = null;
	
	
    public void initialize() throws NumberFormatException, IOException{
    	if ( locationManager == null )
    	initLocationManager();
    }

    
    @Override
	public void onProviderEnabled(String provider){
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
	}
    
    
    private void initLocationManager(){
    	
    	Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        
        if ( provider != null ){
	        Location location = locationManager.getLastKnownLocation(provider);
	        if( location != null ){
	            onLocationChanged(location);
	        }
	        
	    	if( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ){
	    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this, null);
	        }
	        else{
	        	locationManager.requestLocationUpdates(provider, 10000, 0, this);
	        }

        }
    	
    }
    
    
    public void onLocationChanged(Location location) {
    	
    	if ( !String.valueOf(location.getLatitude()).equals(lat) && !String.valueOf(location.getLongitude()).equals(lon) ){
	 	    lat = String.valueOf(location.getLatitude());
	 	    lon = String.valueOf(location.getLongitude());
    	}
    	
    	//lat = "40.5169088";
    	//lon = "21.2631571";
    	
    	
    	SharedPreferences prefs = this.getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
		prefs.edit().putString("user_lat", lat).apply();
		prefs.edit().putString("user_lon", lon).apply();
    	System.out.println(lat + "," + lon);
		//Toast.makeText(this, lat + "," + lon, Toast.LENGTH_LONG).show();
	}
	

	@Override
	public void onProviderDisabled(String provider){
		if ( locationManager != null )
		locationManager.removeUpdates(this);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
