package com.karmazin.model;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ServerDataContainer {
    private static final LoggerAPI logger = new LoggerAPI(ServerDataContainer.class.getName());

    ImageView map;
    LineChart chart;
    String geodata;

    public ServerDataContainer(ImageView _map, LineChart _chart, String _geodata) {
        map = _map;
        chart = _chart;
        geodata = _geodata;
    }

    public byte[] getMap() {
        ArrayList<Byte> byteList = new ArrayList<>();
        AtomicBoolean jobDone = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try (ByteArrayOutputStream mapImageStream = new ByteArrayOutputStream()) {
                BufferedImage mapImage = SwingFXUtils.fromFXImage(map.getImage(), null);
                ImageIO.write(mapImage, "png", mapImageStream);
                for (byte singleByte : mapImageStream.toByteArray()) {
                    byteList.add(singleByte);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Can't get server chart's stream");
            } finally {
                jobDone.set(true);
            }
        });

        while (!jobDone.get()) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

        byte[] byteArr = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); ++i) {
            byteArr[i] = byteList.get(i);
        }

        return byteArr;
    }

    public byte[] getChart() {
        ArrayList<Byte> byteList = new ArrayList<>();
        AtomicBoolean jobDone = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try (ByteArrayOutputStream chartImageStream = new ByteArrayOutputStream()) {
                WritableImage writeableImage = new WritableImage(1200, 400);
                WritableImage chartWritableImage = chart.snapshot(new SnapshotParameters(), writeableImage);
                BufferedImage chartImage = SwingFXUtils.fromFXImage(chartWritableImage, null);
                ImageIO.write(chartImage, "png", chartImageStream);
                for (byte singleByte : chartImageStream.toByteArray()) {
                    byteList.add(singleByte);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Can't get server chart's stream");
            } finally {
                jobDone.set(true);
            }
        });

        while (!jobDone.get()) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

        byte[] byteArr = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); ++i) {
            byteArr[i] = byteList.get(i);
        }

        return byteArr;
    }

    public String getGeodata() {
        return geodata;
    }
}