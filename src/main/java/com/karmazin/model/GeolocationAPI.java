package com.karmazin.model;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Class for getting the Longitude and Latitude of the server by IP
 *
 * Using service http://ip-api.com
 * Есть ограничение на 150 запросов в минуту
 * Unban IP here http://ip-api.com/docs/unban
 */
@Nullable
public class GeolocationAPI {
    private static final LoggerAPI logger = new LoggerAPI(GeolocationAPI.class.getName());

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

    public synchronized static GeolocationAPI sendRequest(String IP) {
        logger.log(Level.INFO, "Recieving server's [" + IP + "] geolocation...");

        // Фильтр для ответа
        String fields = "?fields=country,city,lat,lon,org,query,status,message";
        String url = "http://ip-api.com/json/" + IP + fields;

        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                StringBuffer response = new StringBuffer();

                while ((line = input.readLine()) != null) {
                    response.append(line);
                }

                GeolocationAPI data = new GeolocationAPI(response.toString());

                logger.log(Level.INFO,"Geolocation recieved!");

                return data;
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Wrong URL!", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Input/output stream error!", e);
        } catch (JSONException e) {
            logger.log(Level.SEVERE,e.getMessage(), e);
        } finally {
            // GeoAPI anti-DDoS requerement
            try {
                Thread.sleep(450);
            } catch (InterruptedException e) { }
        }
        return null;
    }

    private GeolocationAPI(String response) throws JSONException {

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

