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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends Activity {

	//ArrayList με τα πρατηρια
	private ArrayList<String> pratiriaList = new ArrayList<String>(); 
	
	//ArrayList με τις διευθυνσεις των πρατηριων
	private ArrayList<String> address = new ArrayList<String>(); 
	
	//ArrayList με τις τιμες των πρατηριων
	private ArrayList<String> price = new ArrayList<String>(); 
	
	//ArrayList με τις αποστασεις μεταξυ των πρατηριων
	private ArrayList<String> distance = new ArrayList<String>(); 
	
	//ArrayList με τις διευθυνσεις των πρατηριων
	private ArrayList<String> sorted = new ArrayList<String>(); 
	private PratiriaAdapter adapter;
	
	//σημαια για το ταξινομηση των πρατηριων (0 - Κοντινοτερα, 1 - Φθηνοτερα)
	private int sortFlag = 0;
	
	//σημαια για το αν ο χρηστης βλεπει τα αγαπημενα πρατηρια ή οχι
	private int viewFlag = 0;
	
	
	//Πρωτη μεθοδος που εκτελειται κατα την εκκινηση της εφαρμογης
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Ελεγχος για Internet
		if ( !isNetworkConnected() ){
			
			//Εμφανιση μηνυματος αν ο χρηστης δεν εχει Internet
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
			
			//Εκκινηση του LPGApplication
			((LPGApplication) getApplication()).initialize();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Εμφανιση του αντιστοιχου layout (res/layout/*.xml)
		setContentView(R.layout.activity_main);
		
		adapter = new PratiriaAdapter(this, address, price, distance);
		
		
		//Το ListView θα εμφανιζει τη λιστα με τα πρατηρια
		ListView list = (ListView) findViewById(R.id.listview_pratiria);
		list.setAdapter(adapter);
		
		//Disable cache
		list.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);

		//Κουμπι-εικονα (a/z) για την ταξινομηση
		ImageView img_sort = (ImageView) findViewById(R.id.img_sort);
		
		//Κουμπι-εικονα για εμφανιση πρατηριων κοντα στο χρηστη
		final ImageView imgAll = (ImageView) findViewById(R.id.img_all);
		
		//Κουμπι-εικονα για εμφανιση αγαπημενων πρατηριων 
		final ImageView imgFavorite = (ImageView) findViewById(R.id.img_favs);
		
		readPratiria();
		sortNearest();
		
		
		//Ακροατης για το πατημα των πρατηριων που υπαρχουν στη λιστα που εμφανιζεται στο χρηστη
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			
			//user click
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				
				//εκκινηση της κλασης MapActivity
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				
				//με την τρεχουσα λιστα ως παραμετρο
				intent.putExtra("list", sorted.get(position));
				startActivity(intent);
			}
		});
		
		
		//Ακροατης για το πατημα της εικονας
		//=>αλλαγη του χρωματος της εικονας και κληση της αντιστοιχης μεθοδου
		img_sort.setOnClickListener(new View.OnClickListener() {
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
		
		
		//Ακροατης για το πατημα της εικονας (αγαπημενα πρατηρια)
		//=>αλλαγη του χρωματος της εικονας και κληση της αντιστοιχης μεθοδου
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
		
		
		//Ακροατης για το πατημα της εικονας (ολα τα πρατηρια)
		//=>αλλαγη του χρωματος της εικονας και κληση της αντιστοιχης μεθοδου
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
	
	
	//Αναγνωση απο ASCII αρχειο τη λιστα με τα πρατηρια
	private void readFavoritePratiria(){
		
		//εκκαθαριση της arraylist πρατηριων
		pratiriaList.clear();
		BufferedReader reader = null;
		try {
			
			//ανοιγμα του αρχειου
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("favorites.txt"), "UTF-8")); 

		    //αναγνωση των ηδη αποθηκευμενων τρεχοντων συντεταγμενων
		    SharedPreferences prefs = getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
			String slat = prefs.getString("user_lat", null); 
			String slon = prefs.getString("user_lon", null); 
			
			if ( slat == null ){
				slat = "40.4875602";
				slon = "21.212499";
	    	}
			
			
			//εμφανιση στην κονσολα
			System.out.println(slat + "---" + slon);
			
			//μετατροπη των παραπανω σε double
		    double dlat = Double.parseDouble(slat);
		    double dlon = Double.parseDouble(slon);
		    
		    //ελεγχος για null τιμες
		    if ( slat != null && slon != null ){
		    	
		    	//αναγνωση του αρχειου ανα 1 γραμμη
			    String mLine = reader.readLine();
			    int k = 0;
			    while (k < 9){ //αναγνωση μεχρι 10 γραμμες
			       mLine = reader.readLine(); 
			       
			       //συνεχισε αν βρεις το παρακατω συμβολο σε καθε αναγνωση
			       if ( mLine.indexOf("#") != -1 ){
			    	   
				       float minX = 1.0f;
				       float maxX = 5.0f;
				       Random rand = new Random();
				       
				       //τυχαιος δεκαδικος αριθμος (η τιμη του πρατηριου) απο 1 μεχρι 5
				       float finalX = rand.nextFloat() * (maxX - minX) + minX;
				       
				       //σπασιμο της γραμμης σε 2 κομματα - array με 2 values με βαση το παρακατω διαχωριστικο
				       String[] part = mLine.split("#");
				       String[] ll = part[1].split(",");
						
				       //μετατροπη σε double
				       double tlat = Double.parseDouble(ll[0]);
				       double tlon = Double.parseDouble(ll[1]);
						
				       //υπολογισμος της αποστασης του πρατηριου σε σχεση με το χρηστη
				       float[] results = new float[1];
				       Location.distanceBetween(dlat, dlon, tlat, tlon, results);
				       
				       //στρογγυλοποιηση
				       int r = Math.round(results[0]);
				       
				       //εισαγωγη των στοιχειων καθε γραμμης στην παρακατω arraylist
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
		        	 
		        	 //κλεισιμο του αρχειου
		             reader.close();
		         } catch (IOException e) {}
		    }
		}
		
	}

	
	//Εχει αναλυθει παραπανω
	private void readPratiria(){
		
		pratiriaList.clear();
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("pratiria.txt"), "UTF-8")); 
		    SharedPreferences prefs = getSharedPreferences("gr.stelios.lpgstations", Context.MODE_PRIVATE);
			String slat = prefs.getString("user_lat", null); 
			String slon = prefs.getString("user_lon", null); 
			
			if ( slat == null ){
				slat = "40.4875602";
				slon = "21.212499";
	    	}
			
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
		             
		             System.out.println(pratiriaList);
		             
		         } catch (IOException e) {}
		    }
		}
	}
	
	
	//Ταξινομηση με τα κοντινοτερα πρατηρια
	private void sortNearest(){
		
		//εκκαθαριση της τρεχουσας arraylist
		sorted.clear();
		
		//iteration της arraylist
		for(int i = 0; i < pratiriaList.size(); i++){
			String[] part = pratiriaList.get(i).split("#");
			
			//το arraylist με τα νεα δεδομενα
			sorted.add(part[1]+"#"+part[0]+"#"+part[2]+"#"+part[3]);
		}
		
		//κληση της κλαση DistanceComparator για συγκριση των τιμων στο arraylist
		Collections.sort(sorted, new DistanceComparator());
		if ( pratiriaList.size() > 20 ) finalizeListView(0);
		else finalizeListView(1);
	}
	
	
	//Ταξινομηση με τα φθηνοτερα πρατηρια
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
	
	
	//Παραμετρος mode: 0 - ολα, 1 αγαπημενα
	private void finalizeListView(int mode){
		
		address.clear();
		distance.clear();
		price.clear();
		int len = 20;
		if ( mode == 1 ){ //favorites
			len = 9;
		}
		
		//Log.d("LPGSTATIONS", sortFlag+"");
		
		
		if ( sortFlag == 0 ){
			for(int i = 0; i < len; i++){
				String[] part = sorted.get(i).split("#");
				address.add(part[1]);
				
				double temp = Double.parseDouble(part[0]);
				
				//εμφανιση χιλιομετρα αντι για μετρα
				int dis = (int) (temp / 1000);
				String d = dis + "";
				distance.add(d+" χλμ.");
				
				//μορφοποιηση της τιμης οπως θα εμφανιζεται
				String pr = new DecimalFormat("#.##").format(Float.parseFloat(part[2]));
			    price.add(pr+" ");
			}
			
			//τιτλος της εφαρμογης
			setTitle("LPGStations - Κοντινότερα:");
			
		} else{
			for(int i = 0; i < len; i++){
				String[] part = sorted.get(i).split("#");
				address.add(part[1]);
				
				double temp = Double.parseDouble(part[2]);
				int dis = (int) (temp / 1000);
				String d = dis + "";
				distance.add(d+" χλμ.");
				
				String pr = new DecimalFormat("#.##").format(Float.parseFloat(part[0]));
				price.add(pr+" ");
			}
			setTitle("LPGStations - Φθηνότερα:");
		}
		adapter.notifyDataSetChanged(); 
	}
	
	
	//Ελεγχος για active Internet
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if ( ni == null ) {
			  System.out.println("no network connection");
		   return false;
		  } else return true;
	}
	
	
	//Εκτελειται κατα την εμφανιση της εφαρμογης στο προσκηνιο
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
