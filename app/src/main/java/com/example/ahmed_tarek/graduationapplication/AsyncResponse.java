package com.example.ahmed_tarek.graduationapplication;

import org.json.JSONArray;

/**
 * Created by cyber on 18/01/15.
 */

public interface AsyncResponse {
    void processFinish(JSONArray output, String type);
}
