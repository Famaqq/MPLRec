package com.graphhopper.example;

import org.locationtech.jts.geom.LineString;

public class WayPoint2 {
    private int num;
    private double midlat1;
    private double midlon1;
    private double distance_first;
    private double distance_second;
    private double distance;
    private int gid;
    private LineString linestring;
    private double time;
    public WayPoint2(){}

    public WayPoint2(int num, double midlat1, double midlon1, double distance_first, double distance_second, double distance, int gid, LineString linestring,double time) {
        this.num = num;
        this.midlat1 = midlat1;
        this.midlon1 = midlon1;
        this.distance_first = distance_first;
        this.distance_second = distance_second;
        this.distance = distance;
        this.gid = gid;
        this.linestring = linestring;
        this.time = time;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public double getMidlat1() {
        return midlat1;
    }

    public void setMidlat1(double midlat1) {
        this.midlat1 = midlat1;
    }

    public double getMidlon1() {
        return midlon1;
    }

    public void setMidlon1(double midlon1) {
        this.midlon1 = midlon1;
    }

    public double getDistance_first() {
        return distance_first;
    }

    public void setDistance_first(double distance_first) {
        this.distance_first = distance_first;
    }

    public double getDistance_second() {
        return distance_second;
    }

    public void setDistance_second(double distance_second) {
        this.distance_second = distance_second;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public LineString getLinestring() {
        return linestring;
    }

    public void setLinestring(LineString linestring) {
        this.linestring = linestring;
    }
}
