package com.graphhopper.example;

import com.graphhopper.GraphHopper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.*;
import java.util.*;

public class DistanceSum {
    public static void main(String[] args) throws IOException {
        int i = 19;
        while(i<20) {
            BufferedReader reader = new BufferedReader(new FileReader("C://Users/fama/Desktop/0404/wtime_201611"+String.valueOf(i)+"_pro.csv"));//换成你的文件名
            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null;
            System.out.println("读取成功！");
            while ((line = reader.readLine()) != null) {
                String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                if(item[2].length() >5){
                    double lon1 = Double.valueOf(item[2]);
                    double lat1 = Double.valueOf(item[3]);
                    double lon2 = Double.valueOf(item[5]);
                    double lat2 = Double.valueOf(item[6]);
                    double ou_distance = GetDistance(lon1, lat1, lon2, lat2);
                    File newcsv = new File("C://Users/fama/Desktop/0405/11"+String.valueOf(i)+"_walk.csv");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));

                    String item_orderid = item[0];
                    //将规划好的距离数据写入csv
                    bw.write(item_orderid + "," + ou_distance);
                    bw.newLine();//换行
                    bw.close();
                }
            }
            i++;
            // System.out.println(GetDistance(104.1122068,30.6549977,104.1124706,30.6550603));
        }
    }

    // 圆周率
    public static final double PI = 3.14159265358979324;
    // 赤道半径(单位m)
    private static final  double EARTH_RADIUS = 6378137;

    /**
     * 转化为弧度(rad)
     * */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    /**
     * 基于googleMap中的算法得到两经纬度之间的距离,
     * 计算精度与谷歌地图的距离精度差不多，相差范围在0.2米以下
     * @param lon1 第一点的经度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的经度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位km
     * */
    public static double GetDistance(double lon1,double lat1,double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        //s = Math.round(s * 10000) / 10000;
        return s;
    }
}