package com.karmazin.model;

import javafx.scene.image.*;
import java.io.*;
import java.net.URL;

/**
 * Class for obtaining a static image from Yandex maps
 *  
 * There are restrictions on the size of the image and the number of requests per day (25 000 per day)
 */
public class YandexMapsAPI {

    private int width;
    private int height;

    public YandexMapsAPI(int width, int height) {
        // Max value is 650x450
        this.width = (width > 650) ? 650 : width;
        this.height = (height > 450) ? 450 : height;
    }

    /**
     * Method for getting picture by coordinates
     *
     * @param latitude широта
     * @param longitude долгота
     * @return
     */
    public Image getMapImageIcon(float latitude, float longitude) {
        // TODO Нужно создавать временную папку через консоль с атрибутом хидден
        (new File(".temp")).mkdir();
        String destinationFile = ".temp/tempmap-" + String.valueOf(longitude) + ","
                + String.valueOf(latitude);// + ".png";

        //System.out.println(destinationFile);
        Image image;
        try {
            String imageUrl = "https://static-maps.yandex.ru/1.x/?l=map"
                    + "&ll=" + String.valueOf(longitude) + "," + String.valueOf(latitude) // longitude/Latitude
                    + "&spn=0.005,0.005" // The length of the map display area
                    + "&size=" + width + "," + height // Size of picture
                    + "&scale=1.2" //The rate of increase of objects on the map [1.0, 4.0]
                    + "&pt=" + String.valueOf(longitude) + "," + String.valueOf(latitude) + ",comma" // Sign
                    + "&lang=ru_RU"; // Language

            // Get response from Yandex and save time map
            URL url = new URL(imageUrl);
            try (InputStream is = url.openStream()) {
                try (OutputStream os = new FileOutputStream(destinationFile)) {
                    byte[] b = new byte[2048];
                    int length;
                    while ((length = is.read(b)) != -1) {
                        os.write(b, 0, length);
                    }
                }
            } catch (Exception e) { }

            image = new Image(new File(destinationFile).toURI().toString());

        } catch (IOException e) {
            e.printStackTrace();
            // Do we need exit from program?
            System.exit(1);
            return null;
        }

        return image;
    }
}


