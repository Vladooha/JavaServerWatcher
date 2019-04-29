package com.karmazin.model;

import com.karmazin.controller.SimplePopup;
import com.karmazin.controller.WorkScreenTest;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.List;

public class SelfTester {
    private static final LoggerAPI logger = new LoggerAPI(SelfTester.class.getName());

    public static XYChart.Series<Integer, Long> startTabsTest(final int windowsCount, List<Integer> tabsCount, final int testCount) {
        List<Integer> buffTabList = new ArrayList<>();
        buffTabList.addAll(tabsCount);
        for (int i = 0; i < (tabsCount.size() - 1); ++i) {
            int toInsert = (tabsCount.get(i) + tabsCount.get(i + 1)) / 2;
            if (!buffTabList.contains(toInsert)) {
                buffTabList.add(toInsert);
            }
        }
        tabsCount = buffTabList;

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

        XYChart.Series<Integer, Long> graphSeria = new XYChart.Series<>();
        System.out.println("[" + new Date() + "]: " + "Test begins");

//        SimplePopup alertWindow = new SimplePopup();
//        alertWindow.setupWindow("Производится тестирование...\n" +
//                        "(Могут ненадолго открываться новые окна, " +
//                        "это необходимо для тестирования)",
//                false);

        Map<Integer, Long> midTime = new HashMap<>();
        for (int i = 0; i < buffTabList.size(); ++i)
            midTime.put(i, 0L);

        for (int k = 0; k < testCount; ++k) {
            for (int i = 0; i < buffTabList.size(); ++i) {
                Long buff = midTime.get(i);
                buff += singleSetTabTest(windowsCount, buffTabList.get(i));
                //alertWindow.toFront();
                midTime.put(i, buff);
            }
        }

        String seriaName = windowsCount + " окон(-на); " + testCount + " тестов(-а)";
        graphSeria.setName(seriaName);

        for (int i = 0; i < buffTabList.size(); ++i){
            Long buff = midTime.get(i);
            buff /= testCount;
            graphSeria.getData().add(new XYChart.Data<>(buffTabList.get(i), buff));
        }

        logger.muteMode(!ConfigWrapper.getDebug());

        System.out.println("[" + new Date() + "]: " + "Test ends");

        return graphSeria;
    }

    private static long singleSetTabTest(int windowsCount, int tabsCount) {
        try {
            List<WorkScreenTest> workScreens = new ArrayList<>();

            long begin = System.currentTimeMillis();
            for (int i = 0; i < windowsCount; ++i) {
                WorkScreenTest workScreen = new WorkScreenTest();
                workScreens.add(workScreen);

                Scanner scan = new Scanner(SelfTester.class.getResourceAsStream("/tests/sites_ip.csv"));
                for (int j = 0; j < tabsCount; ++j) {
                    if (!scan.hasNext()) {
                        scan.close();
                        scan = new Scanner(SelfTester.class.getResourceAsStream("/tests/sites_ip.csv"));
                    }

                    String next = scan.nextLine();
                    String adress = next.split("\\,")[1];

                    workScreen.adressServerTextFieldTest.setText(adress);
                    workScreen.addServerButtonTest.fire();

                    workScreen.close();
                }
            }

            for (WorkScreenTest workScreen : workScreens) {
                workScreen.close();
            }
            workScreens.clear();

            long end = System.currentTimeMillis();

            return end - begin;
        } catch (Exception e) {
            new SimplePopup().setupWindow("Ошибка при проведении теста!");

            return -1;
        }
    }
}
