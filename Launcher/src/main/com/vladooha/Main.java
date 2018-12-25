package main.com.vladooha;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            Process process = Runtime.getRuntime().exec("@echo off\n" +
                    ":: BatchGotAdmin (Run as Admin code starts)\n" +
                    "REM --> Check for permissions\n" +
                    ">nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n" +
                    "REM --> If error flag set, we do not have admin.\n" +
                    "if '%errorlevel%' NEQ '0' (\n" +
                    "echo Requesting administrative privileges...\n" +
                    "goto UACPrompt\n" +
                    ") else ( goto gotAdmin )\n" +
                    ":UACPrompt\n" +
                    "echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n" +
                    "echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n" +
                    "\"%temp%\\getadmin.vbs\"\n" +
                    "exit /B\n" +
                    ":gotAdmin\n" +
                    "if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n" +
                    "pushd \"%CD%\"\n" +
                    "CD /D \"%~dp0\"\n" +
                    ":: BatchGotAdmin (Run as Admin code ends)\n" +
                    ":: Your codes should start from the following line\n" +
                    "java -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -jar Pingovsheeque.jar");
        } catch (IOException e) {
            System.err.println("IO error occured!");
        }
    }
}
