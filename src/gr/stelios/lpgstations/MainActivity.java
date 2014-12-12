package gr.stelios.lpgstations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

	ArrayList<String> pratiriaList = new ArrayList<String>(); 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		readPratiriaFromFile();
	}

	
	private void readPratiriaFromFile(){
		
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new InputStreamReader(getAssets().open("pratiria.txt"), "UTF-8")); 

		    // do reading, usually loop until end of file reading 
		    String mLine = reader.readLine();
		    while (mLine != null) {
		       mLine = reader.readLine(); 
		       
		       float minX = 1.0f;
		       float maxX = 5.0f;

		       Random rand = new Random();

		       float finalX = rand.nextFloat() * (maxX - minX) + minX;
		       
		       String sr = new DecimalFormat("#.##").format(finalX);
		       
		       pratiriaList.add(mLine+"#"+sr);
		    }
		    
		    System.out.println(pratiriaList.get(0));
		} catch (IOException e) {
		    //log the exception
		} finally {
		    if (reader != null) {
		         try {
		             reader.close();
		         } catch (IOException e) {
		             //log the exception
		         }
		    }
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
