package gr.stelios.lpgstations;

import gr.stelios.lpgstations.LPGApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
	private int sortFlag = 0;
	private int viewFlag = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if ( !isNetworkConnected() ){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 	    builder.setMessage("Παρακαλώ ενεργοποιήστε το Internet.")
	 	           .setCancelable(false)
	 	           .setPositiveButton("ΟΚ", new DialogInterface.OnClickListener(){
	 	               public void onClick(final DialogInterface dialog, final int id) {
	 	            	   dialog.cancel();
	 	               }
	 	           });
	 	     
		 	    final AlertDialog alert = builder.create();
		 	    alert.show();
			return;
		}
		
		try {
			((LPGApplication) getApplication()).initialize();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setContentView(R.layout.activity_main);
		
		adapter = new PratiriaAdapter(this, address, price, distance);
		ListView list = (ListView) findViewById(R.id.listview_pratiria);
		list.setAdapter(adapter);
		list.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);

		ImageView img_sort = (ImageView) findViewById(R.id.img_sort);
		final ImageView imgAll = (ImageView) findViewById(R.id.img_all);
		final ImageView imgFavorite = (ImageView) findViewById(R.id.img_favs);
		
		readPratiria();
		sortNearest();
		
		
		list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("list", sorted.get(position));
		
				//System.out.println("LIST CLICK "+sorted.get(position));
				
				startActivity(intent);
			}

		});
		
		
		img_sort.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v){
		    	if ( sortFlag == 0 ){
		    		((ImageView) v).setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
		    		sortFlag = 1;
		    	} else{
		    		((ImageView) v).setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
		    		sortFlag = 0;
		    	}
		    	sortCheapest();
		    }
		});
		
		
		imgFavorite.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v){
		    	if ( viewFlag == 0 ){
		    		imgAll.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
			    	((ImageView) v).setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
			    	viewFlag = 1;
			    	readFavoritePratiria();
			    	sortNearest();
		    	}
		    }
		});
		
		
		imgAll.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v){
		    	if ( viewFlag == 1 ){
		    		imgFavorite.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
			    	((ImageView) v).setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
			    	viewFlag = 0;
			    	readPratiria();
			    	sortNearest();
		    	}
		    }
		});
	}
	
	
	private void readFavoritePratiria(){
		
		pratiriaList.clear();
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("favorites.txt"), "UTF-8")); 

		    SharedPreferences prefs = getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
			String slat = prefs.getString("user_lat", null); 
			String slon = prefs.getString("user_lon", null); 
			
			System.out.println(slat + "---" + slon);
			
		    double dlat = Double.parseDouble(slat);
		    double dlon = Double.parseDouble(slon);
		    
		    if ( slat != null && slon != null ){	
			    String mLine = reader.readLine();
			    int k = 0;
			    while (k < 9){
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
		    }
		    
		    //System.out.println(pratiriaList);

		} catch (IOException e) {
	
		} finally { 
		    if (reader != null) {
		         try {
		             reader.close();
		         } catch (IOException e) {}
		    }
		}
		
	}

	
	private void readPratiria(){
		
		pratiriaList.clear();
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("pratiria.txt"), "UTF-8")); 
		    SharedPreferences prefs = getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
			String slat = prefs.getString("user_lat", null); 
			String slon = prefs.getString("user_lon", null); 
			System.out.println(slat + "---" + slon);
			

			if ( slat != null && slon != null ){	
				
			    double dlat = Double.parseDouble(slat);
			    double dlon = Double.parseDouble(slon);
	
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
		if ( pratiriaList.size() > 20 ) finalizeListView(0);
		else finalizeListView(1);
	}
	
	
	private void sortCheapest(){
		sorted.clear();
		for(int i = 0; i < pratiriaList.size(); i++){
			String[] part = pratiriaList.get(i).split("#");
			sorted.add(part[2]+"#"+part[0]+"#"+part[1]+"#"+part[3]);
		}
		Collections.sort(sorted, new PriceComparator());
		if ( pratiriaList.size() > 20 ) finalizeListView(0);
		else finalizeListView(1);
	}
	
	
	private void finalizeListView(int mode){
		
		address.clear();
		distance.clear();
		price.clear();
		int len = 20;
		if ( mode == 1 ){ //favorites
			len = 9;
		}
		
		if ( sortFlag == 0 ){
			for(int i = 0; i < len; i++){
				String[] part = sorted.get(i).split("#");
				address.add(part[1]);
				distance.add(part[0]+" μ.");
				String pr = new DecimalFormat("#.##").format(Float.parseFloat(part[2]));
			    price.add(pr+" €");
			}
			setTitle("LPGStations - Κοντινότερα:");
			
		} else{
			for(int i = 0; i < len; i++){
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
	
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if ( ni == null ) {
			  System.out.println("no network connection");
		   return false;
		  } else return true;
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			((LPGApplication) getApplication()).initialize();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
