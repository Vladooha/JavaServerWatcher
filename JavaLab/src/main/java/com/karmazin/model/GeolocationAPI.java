package com.karmazin.model;

import org.json.JSONException;

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
public class GeolocationAPI {
    private static final LoggerAPI logger = new LoggerAPI(GeolocationAPI.class.getName());


    public GeolocationAPIData sendRequest(String IP) {
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
                GeolocationAPIData data = new GeolocationAPIData(response.toString());
                //System.out.println(data);

                logger.log(Level.INFO,"Geolocation recieved!");

                return data;
            }
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Wrong URL!", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Input/output stream error!", e);
        } catch (JSONException e) {
            logger.log(Level.SEVERE,e.getMessage(), e);
        }
        return null;
    }
}

