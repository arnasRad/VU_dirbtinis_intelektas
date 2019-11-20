package com.arnasrad.fbchaining.utility;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String getListString(List<String> list, String delimeter) {

        if (list == null || list.size() == 0) {

            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String string : list) {
            sb.append(string).append(delimeter);
        }

        String resultString = sb.toString();
        return resultString.substring(0, resultString.length() - delimeter.length());
    }

    public static ArrayList<String> getListsDifference(ArrayList<String> leftList, ArrayList<String> rightList) {

        ArrayList<String> difference = new ArrayList<>();

        for(String element : leftList) {
            if (!rightList.contains(element)) {
                difference.add(element);
            }
        }

        return difference;
    }

    public static ArrayList<String> getListsDifference(ArrayList<String> leftList, String element) {

        ArrayList<String> difference = new ArrayList<>(leftList);
        difference.remove(element);

        return difference;
    }
}
