package gr.stelios.lpgstations;

import java.util.Comparator;

public class DistanceComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        int v1 = Integer.parseInt(s1.split("#")[0]);
        int v2 = Integer.parseInt(s2.split("#")[0]);
        return v1 - v2;
  }
}