package gr.stelios.lpgstations;

import java.util.Comparator;

/*Custom class για να συγκρινει τις αποστασεις που δινονται συμφωνα με τα ορισματα και να επιστρεψει τη μεγαλυτερη μεταξυ των 2*/
public class DistanceComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        int v1 = Integer.parseInt(s1.split("#")[0]);
        int v2 = Integer.parseInt(s2.split("#")[0]);
        return v1 - v2;
  }
}