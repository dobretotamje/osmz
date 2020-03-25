package com.example.myapp;

import android.util.Log;

import java.io.File;

public class HTMLDirectoryListingBuilder {
    public static String build(File currentDirectory) {
        File[] arrayfile = currentDirectory.listFiles();

        StringBuilder html = new StringBuilder("<html><body><h1>Files</h1>");
        if (arrayfile.length > 0) {
            for (File value : arrayfile) {
                Log.d("Files", "FileName:" + value.getName());
                html.append("+ <a href='").append(value.getName()).append("/' >").append(value.getName()).append("</a><br>");

            }
        }
        return html.toString();
    }
}
