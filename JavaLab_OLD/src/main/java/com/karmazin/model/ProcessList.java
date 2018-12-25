package com.karmazin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The class stores a list of ProcessData processes. *
 * Do not use at the moment
 */
public class ProcessList {
    private List<ProcessData> processDataList;

    public ProcessList() {
        processDataList = getProcesses();
    }

    private List<ProcessData> getProcesses() {
        try {
            Process p = Runtime.getRuntime().exec("tasklist /V /FO \"CSV\"");

            List<ProcessData> data = new ArrayList<>();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
                input.readLine();
                String line;
                while ((line = input.readLine()) != null) {
                    data.add(new ProcessData(line));
                }
                return data;
            }

        } catch (IOException e) {
            System.err.println("Ошибка потока записи/вывода!");
            return null;
        }
    }

    public void ShowProcesses() {
        for (ProcessData processData : processDataList) {
            System.out.println(processData);
        }
    }
}
