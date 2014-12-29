package gr.stelios.lpgstations;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;

public interface OnTaskCompleted{
    public void onTaskCompleted(ArrayList<LatLng> directionPoint);
}