package gr.stelios.lpgstations;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class PratiriaAdapter extends BaseAdapter{ 
	
	//������� ������������ TextView
	static class ViewHolder{
        TextView tv_address;
        TextView tv_price;
        TextView tv_distance;
    }
	
	//�� ������� �������� ��� app
	private Context ctx;
	
	//�� ����� ��� ��� ������ ��� �������
	private int width = 200;
    

	//self-explained arraylists
	private ArrayList<String> address = new ArrayList<String>();
	private ArrayList<String> price = new ArrayList<String>();
	private ArrayList<String> distance = new ArrayList<String>();

    
	//����� ��� ��������� �� ����������� ������ ���������, ��� ����� 4
    public PratiriaAdapter(Context ctx, ArrayList<String>... arg) {
    	this.ctx = ctx;
    	this.address = arg[0];
    	this.price = arg[1];
    	this.distance = arg[2];
    	
    	WindowManager wm = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	width = display.getWidth();
    }
    
    //����������� ��� ��������� ��� ������
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
    public boolean isEnabled(int position){
    	return true;
    }

    //� ������� ��� �� ����� render �� ����� �� �� �������� ��� ������
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

    	ViewHolder holder = null;
    	
    	//��� ��� ���� ������������� ����� � �����
    	if ( convertView == null ) {
    		
    		//�������� ��� layout ��� �����;�
    		LayoutInflater mInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    		
    		//������� ��� ������ ��� ������
    		convertView = mInflater.inflate(R.layout.pratiria, parent, false);
	        holder = new ViewHolder();
	        
	        //������� ��� Textview ��� �����
	        holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
	        holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
	        holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);
	        
	    	holder.tv_address.setWidth((int) (width / 1.5));
			
	        convertView.setTag(holder);
    	} else{
    		 holder = (ViewHolder) convertView.getTag();
    	}
    	
    	//������� ��� ����� �� ���� ���������������� �������� ��� ������
    	holder.tv_address.setText(address.get(position));
    	holder.tv_price.setText(price.get(position));
    	holder.tv_distance.setText(distance.get(position));
       
        return convertView;
    }

}
