package com.graphhopper.example;

public class Ktest {
    public static void main(String[] args) {
        String path = "D:\\dataset.txt";
        KMeans kMeans = new KMeans(path, 9);
        kMeans.doKMeans();
    }
}