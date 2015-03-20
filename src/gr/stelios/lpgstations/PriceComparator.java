package gr.stelios.lpgstations;

import java.util.Comparator;

/*Custom class για να συγκρινει τις τιμες που δινονται συμφωνα με τα ορισματα και να επιστρεψει τη μεγαλυτερη μεταξυ των 2*/
public class PriceComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        float v1 = Float.parseFloat(s1.split("#")[0]);
        float v2 = Float.parseFloat(s2.split("#")[0]);
        if ( v1 < v2 ) return -1;
        if ( v1 > v2 ) return 1;
		return 0;
    }
}