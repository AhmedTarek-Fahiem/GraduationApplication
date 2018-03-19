package com.example.ahmed_tarek.graduationapplication;

import org.json.JSONObject;

/**
 * Created by cyber on 18/01/15.
 */

public interface AsyncResponse {
    void processFinish(JSONObject output, String type);
}
