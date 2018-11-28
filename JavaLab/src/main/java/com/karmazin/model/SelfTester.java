package com.karmazin.model;

import com.karmazin.controller.OneElementScreen;
import com.karmazin.controller.SimplePopup;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
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
    private static class RunnableWindow implements Runnable {
        private static SimplePopup alert = new SimplePopup();
        OneElementScreen screen;
        Node node;

        RunnableWindow(OneElementScreen screen, Node node) {
            this.screen = screen;
            this.node = node;
        }

        public static void loadAlert() {
            Platform.runLater(() -> alert.setupWindow("Тест вполняется, ожидайте...", false));
        }

        @Override
        public void run() {
            screen.setupWindow(node);
            alert.close();
        }

        public void stop() {
            Platform.runLater(() -> screen.close());
        }
    }

    private LoggerAPI logger = new LoggerAPI(SelfTester.class.getName());
    private XYChart chart;
    private RunnableWindow window;
    private XChartPanel<XYChart> chartNode;

    public boolean startTabsTest(int windowsCount, List<Integer> tabsCount) {
        String seriaName = "Windows: " + windowsCount +
                ". Tabs: " + tabsCount.get(0) + " - " + tabsCount.get(tabsCount.size() - 1);
        if (chart == null || !chart.getSeriesMap().containsKey(seriaName)) {

            RunnableWindow.loadAlert();

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

            Map<Integer, Long> midTime = new HashMap<>();
            for (int i = 0; i < tabsCount.size(); ++i)
                midTime.put(i, 0L);
            //midTime.add(singleSetTabTest(windowsCount, tabsCount.get(i)));
            for (int k = 0; k < 9; ++k) {
                //List<Long> buff = new ArrayList<>();
                for (int i = 0; i < tabsCount.size(); ++i) {
                    Long buff = midTime.get(i);
                    buff += singleSetTabTest(windowsCount, tabsCount.get(i));
                    midTime.put(i, buff);
                }
            }

            for (int i = 0; i < tabsCount.size(); ++i){
                Long buff = midTime.get(i);
                buff /= 9;
                midTime.put(i, buff);
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

            if (window != null) {
                window.stop();
            }

            List<Long> buff = new ArrayList<Long>();
            buff.addAll(midTime.values());
            try {
                chart.addSeries(seriaName,
                        tabsCount, buff);

            } catch (Exception e) {

            }

            //new Thread(() -> new SwingWrapper(chart).displayChart()).start();


            final SwingNode swingNode = new SwingNode();
            chartNode = new XChartPanel<XYChart>(chart);
            swingNode.setContent(chartNode);
            if (window != null) {
                window.stop();
            }
            window = new RunnableWindow(new OneElementScreen(), swingNode);
            Platform.runLater(window);

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
        } else {
            Platform.runLater(() -> new SimplePopup().setupWindow("Данный тест уже имеется в выборке.\n" +
                    "Откройте вкладку \"Тестирование\" заного,\n" +
                    " чтобы провести этот тест ещё раз"));
            return false;
        }
    }

    private static long singleSetTabTest(int windowsCount, int tabsCount) {
        try {
            Scanner scan = new Scanner(new File("src/main/resources/tests/sites_ip.csv"));

            ExecutorService ex = Executors.newFixedThreadPool(windowsCount);
            List<Callable<Object>> tasks = new ArrayList<>();
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

                        serversMap.get(j).getTime();
                        HttpWatcher http = httpsMap.get(j);
                        http.httpCodeLabel(adress, 100);
                        http.shutdown();
                    }

                    for (HttpWatcher http : httpsMap) {
                        http.shutdown();
                    }

                    return null;
                });
            }
            long begin = System.currentTimeMillis();
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
