package gr.stelios.lpgstations;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class PratiriaAdapter extends BaseAdapter{ 
	
	static class ViewHolder{
        TextView tv_address;
        TextView tv_price;
    }
	
	private Context ctx;
    

	private ArrayList<String> address = new ArrayList<String>();
	private ArrayList<String> price = new ArrayList<String>();

    
    public PratiriaAdapter(Context ctx, ArrayList<String>... arg) {
    	this.ctx = ctx;
    	this.address = arg[0];
    	this.price = arg[1];
    }
    
    @Override
    public int getCount() {
        return address.size();
    }
 
    @Override
    public Object getItem(int position) {
        return position;
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

    	ViewHolder holder = null;
    	
    	if ( convertView == null ) {
    		LayoutInflater mInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    		convertView = mInflater.inflate(R.layout.pratiria, parent, false);
	        holder = new ViewHolder();
	        holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
	        holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
	        convertView.setTag(holder);
    	} else{
    		 holder = (ViewHolder) convertView.getTag();
    	}
    	
    	holder.tv_address.setText(address.get(position));
    	holder.tv_price.setText(price.get(position));
       
        return convertView;
    }

}
