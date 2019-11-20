package com.arnasrad.fbchaining.utility;

import java.util.List;

public class Utils {

    public static String getListString(List<String> list) {

        if (list == null || list.size() == 0) {

            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String string : list) {
            sb.append(string).append(", ");
        }

        String resultString = sb.toString();
        return resultString.substring(0, resultString.length() - 2);
    }
}
