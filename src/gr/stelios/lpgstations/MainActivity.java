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
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;


public class MainActivity extends Activity {

	private ArrayList<String> pratiriaList = new ArrayList<String>(); 
	private ArrayList<String> address = new ArrayList<String>(); 
	private ArrayList<String> price = new ArrayList<String>(); 
	private ArrayList<String> distance = new ArrayList<String>(); 
	private ArrayList<String> sorted = new ArrayList<String>(); 
	private PratiriaAdapter adapter;
	private double dlat, dlon;
	private int sortFlag = 0;
	
	
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
		
		adapter = new PratiriaAdapter(this, address, price, distance);
		ListView list = (ListView) findViewById(R.id.listview_pratiria);
		list.setAdapter(adapter);
		list.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);

		
		getUserCoordinates();
		readPratiriaFromFile();
		sortNearest();
		
		
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("mylat", dlat);
				intent.putExtra("mylon", dlon);
				intent.putExtra("list", sorted.get(position));
		
				//System.out.println("LIST CLICK "+sorted.get(position));
				
				startActivity(intent);
			}

		});
		
		
		ImageView imgFavorite = (ImageView) findViewById(R.id.img_sort);
		imgFavorite.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v){
		    	if ( sortFlag == 0 ){
		    		((ImageView) v).setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
		    		sortFlag = 1;
		    		sortCheapest();
		    	} else{
		    		((ImageView) v).setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
		    		sortFlag = 0;
		    		sortNearest();
		    	}
		    }
		});


	}

	
	private void readPratiriaFromFile(){
		
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("pratiria.txt"), "UTF-8")); 

		    String mLine = reader.readLine();
		    int k = 0;
		    while (k < 674){
		       mLine = reader.readLine(); 
		       
		       if ( mLine.indexOf("#") != -1 ){
		    	   
			       float minX = 1.0f;
			       float maxX = 5.0f;
			       Random rand = new Random();
			       float finalX = rand.nextFloat() * (maxX - minX) + minX;
			       
			       String[] part = mLine.split("#");
			       String[] ll = part[1].split(",");
					
			       double tlat = Double.parseDouble(ll[0]);
			       double tlon = Double.parseDouble(ll[1]);
					
			       float[] results = new float[1];
			       Location.distanceBetween(dlat, dlon, tlat, tlon, results);
			       int r = Math.round(results[0]);
			       
			       pratiriaList.add(part[0]+"#"+r+"#"+finalX+"#"+part[1]);
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
	
	
	private void sortNearest(){
		sorted.clear();
		for(int i = 0; i < pratiriaList.size(); i++){
			String[] part = pratiriaList.get(i).split("#");
			sorted.add(part[1]+"#"+part[0]+"#"+part[2]+"#"+part[3]);
		}
		Collections.sort(sorted, new DistanceComparator());
		finalizeListView();
	}
	
	
	private void sortCheapest(){
		sorted.clear();
		for(int i = 0; i < pratiriaList.size(); i++){
			String[] part = pratiriaList.get(i).split("#");
			sorted.add(part[2]+"#"+part[0]+"#"+part[1]+"#"+part[3]);
		}
		Collections.sort(sorted, new PriceComparator());
		finalizeListView();
	}
	
	
	private void finalizeListView(){
		
		address.clear();
		distance.clear();
		price.clear();
		
		if ( sortFlag == 0 ){
			for(int i = 0; i < 20; i++){
				String[] part = sorted.get(i).split("#");
				address.add(part[1]);
				distance.add(part[0]+" μ.");
				String pr = new DecimalFormat("#.##").format(Float.parseFloat(part[2]));
			    price.add(pr+" €");
			}
			setTitle("LPGStations - Κοντινότερα:");
			
		} else{
			for(int i = 0; i < 20; i++){
				String[] part = sorted.get(i).split("#");
				address.add(part[1]);
				distance.add(part[2]+" μ.");
				String pr = new DecimalFormat("#.##").format(Float.parseFloat(part[0]));
			    price.add(pr+" €");
			}
			setTitle("LPGStations - Φθηνότερα:");
		}
		adapter.notifyDataSetChanged(); 
	}
	
	
	private void getUserCoordinates(){
		 MyLocation gpsTracker = new MyLocation(this);
		 if ( gpsTracker.canGetLocation() ){
			String stringLatitude = String.valueOf(gpsTracker.latitude);
	        String stringLongitude = String.valueOf(gpsTracker.longitude);
	        
	        dlat = Double.parseDouble(stringLatitude);
	        dlon = Double.parseDouble(stringLongitude);
	        
	        //dlat = 40.5169088;
	        //dlon = 21.2631571;
	        //System.out.println(stringLatitude+" - "+stringLongitude);
	     }
	 }
	
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if ( ni == null ) {
			  System.out.println("no network connection");
		   return false;
		  } else return true;
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
