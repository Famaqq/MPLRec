package com.graphhopper.example;

public class Link_rank {
    private int num;
    private double midlat1;
    private double midlon1;
    private double distance_first;
    private double distance_second;
    private double distance;
    private int gid;
    private int his_point_num;
    private int his_trj_num;
    public Link_rank(){}

    public Link_rank(int gid, double distance_first, double distance_second, double distance, int his_point_num, int his_trj_num) {
        this.distance_first = distance_first;
        this.distance_second = distance_second;
        this.distance = distance;
        this.gid = gid;
        this.his_point_num = his_point_num;
        this.his_trj_num = his_trj_num;
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

    public int getHis_point_num() {
        return his_point_num;
    }

    public void setHis_point_num(int his_point_num) {
        this.his_point_num = his_point_num;
    }

    public int getHis_trj_num() {
        return his_trj_num;
    }

    public void setHis_trj_num(int his_trj_num) {
        this.his_trj_num = his_trj_num;
    }

    @Override
    public String toString() {
        return "Link_rank{" +
                "gid=" + gid +
                ", distance_first=" + distance_first +
                ", distance_second=" + distance_second +
                ", distance=" + distance +
                ", his_point_num=" + his_point_num +
                ", his_trj_num=" + his_trj_num +
                '}';
    }
}
