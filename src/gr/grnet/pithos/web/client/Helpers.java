package gr.grnet.pithos.web.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Helper methods.
 */
public final class Helpers {
    private Helpers() {}

    public static boolean isEmptySafe(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static <K, V> HashMap<K, V> copyHashMap(HashMap<K, V> map) {
        assert map != null;
        return new HashMap<K, V>(map);
    }

    public static <T> List<T> safeList(List<T> list) {
        if(list == null) {
            return new ArrayList<T>();
        }
        return list;
    }

    public static <T> List<T> toList(T ...items) {
        final List<T> list = new ArrayList<T>();
        Collections.addAll(list, items);
        return list;
    }

    public static JSONArray listToJSONArray(List<String> list) {
        final JSONArray jsonArray = new JSONArray();
        if(list == null) {
            return jsonArray;
        }

        for(int i = 0; i < list.size(); i++) {
            final JSONString jsonString = new JSONString(list.get(i));
            jsonArray.set(i, jsonString);
        }

        return jsonArray;
    }

    public static String stripTrailing(String s, String trailing) {
        while(s.endsWith(trailing)) {
            s = s.substring(0, s.length() - trailing.length());
        }
        return s;
    }

    public static String upToIncludingLastPart(String s, String part) {
        int index = s.lastIndexOf(part);
        if(index == -1) {
            return s;
        }
        return s.substring(0, index + part.length());
    }
}
