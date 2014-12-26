package gr.stelios.lpgstations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;


public class MainActivity extends Activity {

	private ArrayList<String> pratiriaList = new ArrayList<String>(); 
	private ArrayList<String> pratiriaListTemp = new ArrayList<String>(); 
	private ArrayList<String> address = new ArrayList<String>(); 
	private ArrayList<String> price = new ArrayList<String>(); 
	private PratiriaAdapter adapter;
	private ListView list;
	private double dlat, dlon;
	private ArrayList<String> sortedByDistance = new ArrayList<String>(); 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if ( !isNetworkConnected() ){
			//TextView tvMyRegion = (TextView) findViewById(R.id.tvMyRegion);
			//tvMyRegion.setText("Παρακαλώ τερματίστε την εφαρμογή και ενεργοποιήστε το Internet.");
			//tvMyRegion.setTextColor(Color.RED);
			return;
		}
		
		adapter = new PratiriaAdapter(this, address, price);
		list = (ListView) findViewById(R.id.listview_pratiria);
		list.setAdapter(adapter);
		list.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);
		
		getUserCoordinates();
		
		readPratiriaFromFile();

		adapter.notifyDataSetChanged(); 
		
		getNearest();
	}

	
	private void readPratiriaFromFile(){
		
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("pratiria.txt"), "UTF-8")); 

		    String mLine = reader.readLine();
		    int k = 0;
		    while (k < 672){
		       mLine = reader.readLine(); 
		       
		       if ( mLine.indexOf("#") != -1 ){
			       float minX = 1.0f;
			       float maxX = 5.0f;
			       Random rand = new Random();
			       float finalX = rand.nextFloat() * (maxX - minX) + minX;
			       String pr = new DecimalFormat("#.##").format(finalX);
			       pratiriaList.add(mLine+"#"+pr);
		       }
		       
		       k++;
		    }

		} catch (IOException e) {
	
		} finally { 
		    if (reader != null) {
		         try {
		             reader.close();
		         } catch (IOException e) {}
		    }
		}
	}
	
	
	private void getNearest(){
		
		for(int i = 0; i < pratiriaList.size(); i++){
			String[] part = pratiriaList.get(i).split("#");
			String[] ll = part[1].split(",");
			
			double tlat = Double.parseDouble(ll[0]);
			double tlon = Double.parseDouble(ll[1]);
			
			float[] results = new float[1];
	     	Location.distanceBetween(dlat, dlon, tlat, tlon, results);
	     	
	     	sortedByDistance.add(results[0]+"#"+part[0]+"#"+part[2]);
	     	 
	     	//System.out.println("DISTANCE: "+results[0]);
		}
		
		Collections.sort(sortedByDistance);
		
		finalizeListView();
		//System.out.println(sortedByDistance);
	}
	
	
	private void finalizeListView(){
		
		for(int i = 0; i < 20; i++){
			
			String[] part = sortedByDistance.get(i).split("#");
			
			address.add(part[1]);
		    price.add(part[2]+" €");
		}
		
	}
	
	
	
	private void getUserCoordinates(){
		 GPSTracker gpsTracker = new GPSTracker(this);
		 if ( gpsTracker.canGetLocation() ){
			String stringLatitude = String.valueOf(gpsTracker.latitude);
	        String stringLongitude = String.valueOf(gpsTracker.longitude);
	        dlat = Double.parseDouble(stringLatitude);
	        dlon = Double.parseDouble(stringLongitude);
	        //System.out.println(stringLatitude+" - "+stringLongitude);
	     }
	 }
	
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if ( ni == null ) {
			  System.out.println("no network connection");
		   return false;
		  } else
		   return true;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
