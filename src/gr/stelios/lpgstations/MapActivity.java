package gr.stelios.lpgstations;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;


public class MapActivity extends FragmentActivity implements LocationListener{
	
	private GoogleMap googleMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        if( location != null ){
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 5000000, 0, this);
        
	}

	
	@Override
	public void onLocationChanged(Location location) {

		double myLat;
    	double myLong;
    	
    	myLat = getIntent().getDoubleExtra("mylat", 0.0);
    	myLong = getIntent().getDoubleExtra("mylon", 0.0);
    	
    	String extra = getIntent().getStringExtra("list");
    	String[] part = extra.split("#");
    	
    	double tlat = Double.parseDouble(part[3].split(",")[0]);
	    double tlon = Double.parseDouble(part[3].split(",")[1]);

    	LatLng from = new LatLng(myLat, myLong);
        LatLng to = new LatLng(tlat, tlon);
        
        //System.out.println(tlat+","+tlon);

    	Marker m1 = googleMap.addMarker(new MarkerOptions().position(from).draggable(true).visible(true));
    	Marker m2 = googleMap.addMarker(new MarkerOptions().position(to).draggable(true).visible(true));
    	
    	LatLngBounds.Builder b = new LatLngBounds.Builder();
	    b.include(m1.getPosition());
	    b.include(m2.getPosition());
    	LatLngBounds bounds = b.build();
    	
    	Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x - 100;
    	CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, width, 5);
    	googleMap.animateCamera(cu);

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