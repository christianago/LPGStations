package gr.stelios.lpgstations;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;


public class MapActivity extends FragmentActivity implements LocationListener, OnTaskCompleted{
	
	private GoogleMap googleMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
		
	   //Ελεγχος εαν υπαρχει το Google Play στο κινητο
	   int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
       if( status != ConnectionResult.SUCCESS ){
    	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 	    builder.setMessage("Δεν έχετε εγκατεστημένο το Google Play.")
	 	           .setCancelable(false)
	 	           .setPositiveButton("ΟΚ", new DialogInterface.OnClickListener(){
	 	               public void onClick(final DialogInterface dialog, final int id) {
	 	            	  finish();
	 	               }
	 	           });
	 	     
		 	    final AlertDialog alert = builder.create();
		 	    alert.show();
       } else{
    	   
			setContentView(R.layout.activity_map);
	
			//χρησιμοποιειται για την εμφανιση του χαρτη 
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
	        googleMap = fm.getMap();
	        
	        //Καθορισμος της θεσης του χρηστη
	        googleMap.setMyLocationEnabled(true);
	        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
	        Criteria criteria = new Criteria();
	        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        String provider = locationManager.getBestProvider(criteria, true);
	        
	        //Ληψη της τελευταιας τρεχουσας τοποθεσιας του χρηστη
	        Location location = locationManager.getLastKnownLocation(provider);
	
	        if( location != null ){
	            onLocationChanged(location);
	        }
	        locationManager.requestLocationUpdates(provider, 5000000, 0, this);
	        
	       }
        
	}

	
	//Ενημερωση τοποθεσιας του χρηστη αναλογα με τη θεση του
	@Override
	public void onLocationChanged(Location location) {
		
    	SharedPreferences prefs = this.getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
		String slat = prefs.getString("user_lat", null); 
		String slon = prefs.getString("user_lon", null); 
		double dlat = Double.parseDouble(slat);
	    double dlon = Double.parseDouble(slon);
		
	    //Ληψη των ορισματων απο την καλουσα κλαση
		String extra = getIntent().getStringExtra("list");
    	String[] part = extra.split("#");
    	
	    double tlat = Double.parseDouble(part[3].split(",")[0]);
	    double tlon = Double.parseDouble(part[3].split(",")[1]);

	    //μετατροπη σε αντικειμενο συντεταγμενων
    	LatLng from = new LatLng(dlat, dlon);
        LatLng to = new LatLng(tlat, tlon);
        
        //System.out.println(tlat+","+tlon);

        //αρχικοποιηση των 2 σημειων/markers(θεση χρηστη - προορισμος)
    	Marker m1 = googleMap.addMarker(new MarkerOptions().position(from).draggable(true).visible(true));
    	Marker m2 = googleMap.addMarker(new MarkerOptions().position(to).draggable(true).visible(true));
    	
    	LatLngBounds.Builder b = new LatLngBounds.Builder();
	    b.include(m1.getPosition());
	    b.include(m2.getPosition());
    	LatLngBounds bounds = b.build();
    	
    	//Γνωστοποιηση του πλατους του παραθυρου της εφαρμογης αναλογα με το πλατος της οθονης του κινητου
    	Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x - 100;
		
		//current camera zoom
    	CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, width, 5);
    	googleMap.animateCamera(cu);
    	
    	//Αντικειμενο της κλασης MapDirections
    	MapDirections gd = new MapDirections(from, to);
    	
    	//Ορισμος ακροατη
    	gd.setListener(this);
    	
    	//Εκτελεση των παραπανω
    	gd.execute();

	}
	
	
	//Η MapDirections τελειωσε τη λειτουργια της
	public void onTaskCompleted(ArrayList<LatLng> directionPoint) {
		
		//Αντικειμενο PolylineOptions (γκρι γραμμη στο χαρτη)
		PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.GRAY);
		
		//Σχεδιασμος της διαδρομης μεταξυ των 2 σημειων 
        for(int i = 0 ; i < directionPoint.size() ; i++){          
        	rectLine.add(directionPoint.get(i));
        }
        googleMap.addPolyline(rectLine);
	}
	

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onProviderDisabled(String provider) {}
	
	
	@Override
	public void onBackPressed(){
		finish();
	}

}
