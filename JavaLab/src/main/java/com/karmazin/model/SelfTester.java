package com.karmazin.model;

import com.karmazin.controller.OneElementScreen;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.awt.*;
import java.io.Console;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelfTester {
    private static LoggerAPI logger = new LoggerAPI(SelfTester.class.getName());
    private static XYChart chart;
    private static OneElementScreen graphScreen;

    public static boolean startTabsTest(int windowsCount, List<Integer> tabsCount) {

        List<Integer> buffList = new ArrayList<>();
        buffList.addAll(tabsCount);
        for (int i = 0; i < (tabsCount.size() - 1); ++i) {
            buffList.add((tabsCount.get(i) + tabsCount.get(i + 1)) / 2);
        }
        tabsCount = buffList;

        tabsCount.sort((a, b) -> {
            if (a > b) {
                return 1;
            } else if (a == b) {
                return 0;
            } else {
                return -1;
            }
        });


        logger.muteMode(true);

        List<Long> midTime = new ArrayList<>();

        for (int i = 0; i < tabsCount.size(); ++i) {
            midTime.add(singleSetTabTest(windowsCount, tabsCount.get(i)));
        }

        if (chart == null) {
            chart = new XYChartBuilder()
                    .width(600)
                    .height(400)
                    .title("Test graph for " + windowsCount + " windows")
                    .xAxisTitle("Tabs count")
                    .yAxisTitle("Time (ms)")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        }

        if (graphScreen != null) {
            Platform.runLater(() -> graphScreen.close());
        }

        chart.addSeries("Windows: " + windowsCount +
                ". Tabs: " + tabsCount.get(0) + " - " + tabsCount.get(tabsCount.size() - 1),
                tabsCount, midTime);

        //new Thread(() -> new SwingWrapper(chart).displayChart()).start();

        final SwingNode swingNode = new SwingNode();
        graphScreen = new OneElementScreen();
        swingNode.setContent(new XChartPanel<XYChart>(chart));
        Platform.runLater(() -> graphScreen.setupWindow(swingNode));
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    // Create and set up the window.
//                    JFrame frame = new JFrame("Advanced grapheeque");
//                    frame.setLayout(new BorderLayout());
//                    //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//
//                    // chart
//                    JPanel chartPanel = new XChartPanel<XYChart>(chart);
//                    frame.add(chartPanel, BorderLayout.CENTER);
//
//                    // Display the window.
//                    frame.pack();
//                    frame.setVisible(true);
//                }
//            });

        logger.muteMode(false);

        return true;
    }

    private static long singleSetTabTest(int windowsCount, int tabsCount) {
        try {
            Scanner scan = new Scanner(new File("src/main/resources/tests/sites_ip.csv"));

            ExecutorService ex = Executors.newFixedThreadPool(windowsCount);
            List<Callable<Object>> tasks = new ArrayList<>();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < windowsCount; ++i) {
                tasks.add(() -> {
                    /*
                    Map<String, ServerPingWatcher> serversMap = new HashMap<>();
                    Map<String, HttpWatcher> httpsMap = new HashMap<>();
                    for (int j = 0; j < tabsCount; ++j) {
                        String next = scan.nextLine();
                        //System.err.println("NEXT: " + next);
                        String adress = next.split("\\,")[1];
                        serversMap.put(String.valueOf(j), new ServerPingWatcher(adress, false));
                        httpsMap.put(String.valueOf(j), new HttpWatcher());

                        //serversMap.get(String.valueOf(j)).getTime();
                        //httpsMap.get(String.valueOf(j)).httpCodeLabel(adress, 100);
                    }
                    */

                    List<ServerPingWatcher> serversMap = new ArrayList<>();
                    List<HttpWatcher> httpsMap = new ArrayList<>();
                    for (int j = 0; j < tabsCount; ++j) {
                        String next = scan.nextLine();
                        //System.err.println("NEXT: " + next);
                        String adress = next.split("\\,")[1];
                        serversMap.add(new ServerPingWatcher(adress, false));
                        httpsMap.add(new HttpWatcher());

                        //serversMap.get(String.valueOf(j)).getTime();
                        //httpsMap.get(String.valueOf(j)).httpCodeLabel(adress, 100);
                    }

                    return null;
                });
            }
            ex.invokeAll(tasks);
            long end = System.currentTimeMillis();
            ex.shutdownNow();

            return end - begin;
        } catch (Exception e) {
            System.out.println("Test error!");
            return -1;
        }
    }
}
