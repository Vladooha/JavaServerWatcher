package com.karmazin.model;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Here data  stored from the response api
 *
 * Parse Json from http://ip-api.com
 */
public class GeolocationAPIData {

    /** Status of request success/fail */
    private String status;

    /** Country */
    private String country;

    /** City */
    private String city;

    /** Latitude */
    private float lat;

    /** Longitude */
    private float lon;

    /** Name of organization */
    private String org;

    /** IP of request */
    private String query;

    /** Error message (if have) */
    private String message;

    public GeolocationAPIData(String response) throws JSONException {

        JSONObject json = new JSONObject(response);

        status = json.getString("status");
        if (status.equals("fail")) {
            message = json.getString("message");
            throw new JSONException("Статус запроса fail, " + message);
        }

        city = json.getString("city");
        country = json.getString("country");
        lat = json.getFloat("lat");
        lon = json.getFloat("lon");
        org = json.getString("org");
        query = json.getString("query");
    }

    @Override
    public String toString() {
        return
            "IP: " + query + "\n" +
            "Страна: " + country + "\n" +
            "Город: " + city + "\n" +
            "Организация: " + org + "\n" +
            "Широта: " + lat + "\n" +
            "Долгота: " + lon;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}



























