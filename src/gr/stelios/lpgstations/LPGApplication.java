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

/*Ειδικη Κλαση του app που instance-αντικειμενο της οποιας βρισκεται συνεχως στη μνημη του κινητου για λογους που θα εξηγηθουν παρακατω*/
public class LPGApplication extends Application implements LocationListener{

	//Ο LocationManager ειναι υπευθυνο για να λαβει την τρεχουσα τοποθεσια του χρηστη
	public LocationManager locationManager = null;
	
	//το γεωγραφικο μηκος
	public String lat = null;
	
	//το γεωγραφικο πλατος
	public String lon = null;
	
	
	//Εκκινηση του LocationManager
    public void initialize() throws NumberFormatException, IOException{
    	if ( locationManager == null )
    	initLocationManager();
    }

    
   //Ενημερωση του LocationManager για τον παραχο τηλεπικοινωνιας
    @Override
	public void onProviderEnabled(String provider){
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
	}
    
    
    
    private void initLocationManager(){
    	
    	Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        String provider = locationManager.getBestProvider(criteria, true);
        
        //Θα παρει τις συντεταγμενες με βαση το GPS αν ειναι active, αλλιως το 3G/WiFi 
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
    
    
    //Ενημερωση των συντεταγμενων καθε φορα που αλλαζει η θεση του κινητου
    public void onLocationChanged(Location location) {
    	
    	if ( !String.valueOf(location.getLatitude()).equals(lat) && !String.valueOf(location.getLongitude()).equals(lon) ){
	 	    lat = String.valueOf(location.getLatitude());
	 	    lon = String.valueOf(location.getLongitude());
    	}
    	
    	//Θα αποθηκευσει τις συντεταγμενες στο φακελο του app
    	SharedPreferences prefs = this.getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
    	
    	if ( lat == null ){
    		lat = "40.4875602";
    		lon = "21.212499";
    	}
    		
		prefs.edit().putString("user_lat", lat).apply();
		prefs.edit().putString("user_lon", lon).apply();
    	System.out.println(lat + "," + lon);
		//Toast.makeText(this, lat + "," + lon, Toast.LENGTH_LONG).show();
	}
	

    //Τερματισμος του LocationManager
	@Override
	public void onProviderDisabled(String provider){
		if ( locationManager != null )
		locationManager.removeUpdates(this);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

}
