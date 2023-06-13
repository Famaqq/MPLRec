package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route_recommendation_engine {
    public static void main(String[] args) throws IOException {
        String relDir = args.length == 1 ? args[0] : "";
        //GraphHopper hopper = createGraphHopperInstance(relDir + "core/files/chengdu.osm.pbf");
        //routing(hopper);
        customizableRouting(relDir + "core/files/chengdu.osm.pbf");
        //hopper.close();
    }

    public static void customizableRouting(String ghLoc) throws IOException {

        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/routing-custom-graph-cache");
        hopper.setProfiles(new CustomProfile("car_custom").setCustomModel(new CustomModel()).setVehicle("car"));

        hopper.getLMPreparationHandler().setLMProfiles(new LMProfile("car_custom"));
        hopper.importOrLoad();


        int i = 0;
        System.out.println("正在读取文件中.....");
        BufferedReader reader = new BufferedReader(new FileReader("D://gps_routing_1101_head_WSG84.csv"));//换成你的文件名
        BufferedReader reader2 = new BufferedReader(new FileReader("D://gps_routing_1101_tail_WSG84.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        reader2.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        String line2 = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        while (((line = reader.readLine()) != null) && ((line2 = reader2.readLine()) != null)) {

            i = i + 1;
            if (i % 500 == 0) {
                System.out.println("已经规划路径" + i + "条");
            }

            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String[] line2_item = line2.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String item1 = item[3];
            String item2 = item[4];
            String item3 = line2_item[3];
            String item4 = line2_item[4];

            double startlng = Double.parseDouble(item1);
            double startlat = Double.parseDouble(item2);
            double endlng = Double.parseDouble(item3);
            double endlat = Double.parseDouble(item4);


            /*数据库搜索nodes*/
            String url = "jdbc:postgresql://localhost:5432/cdosm";
            String user = "postgres";
            String password = "880123";

            String QUERY2 = " With CTE AS (SELECT *,TRUNC(ST_Distance(ST_GeomFromText(?,4326), geom  )*100000)::integer as distance FROM allwaynodes ORDER BY geom <-> ST_GeomFromText(?, 4326) LIMIT 500\n" +
                    ")SELECT * FROM CTE where distance < 500";
            String SELECT_ALL_QUERY = "select * from allwaynodes";

            List nodes_point_lonList = new ArrayList();
            List nodes_point_latList = new ArrayList();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY2);) {
                String a= String.valueOf(startlng);
                String b= String.valueOf(startlat);
                String point_input = "POINT("+a+" "+b+")";
                preparedStatement.setString(1, point_input);
                preparedStatement.setString(2, point_input);
                System.out.println(preparedStatement);
                // Step 3: Execute the query or update query
                ResultSet rs = preparedStatement.executeQuery();
                int sum = 0;
                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    String id = rs.getString("id");
                    String lon = rs.getString("lon");
                    String lat = rs.getString("lat");
                    String distance = rs.getString("distance");
                   // System.out.println(id + "," + lon + "," + lat + "," + distance);
                    sum++;
                    nodes_point_lonList.add(lon);
                    nodes_point_latList.add(lat);
                }
                System.out.println("nodes_pointList_num:"+sum);
                /*for (int k = 0; k < nodes_pointList.size(); k++) {
                    System.out.println(nodes_pointList.get(k));
                }*/
            } catch (SQLException e) {
                printSQLException(e);
            }

            /*数据库搜索nodes*/
            double dis_final = 0.00;
            String midlat1 = "";
            String midlon1 = "";
            String midlat1_final = "";
            String midlon1_final = "";

            for (int k = 0; k < nodes_point_lonList.size(); k++) {
                //System.out.println(nodes_point_lonList.get(k));
               // System.out.println(nodes_point_latList.get(k));

            midlat1 = String.valueOf(nodes_point_latList.get(k));
            Double midlat2 = Double.valueOf(midlat1);
            midlon1 = String.valueOf(nodes_point_lonList.get(k));
            Double midlon2 = Double.valueOf(midlon1);
            GHRequest req = new GHRequest().setProfile("car_custom").
                    addPoint(new GHPoint(startlat,startlng)).addPoint(new GHPoint(midlat2,midlon2)).addPoint(new GHPoint(endlat,endlng))
                    .setSnapPreventions(Collections.singletonList("trunk"));

            GHResponse res = hopper.route(req);
            System.out.println("客制化路线:" + res);
            if (res.hasErrors())
                throw new RuntimeException(res.getErrors().toString());

            ResponsePath path = res.getBest();
            System.out.println("path:" + path);
            // points, distance in meters and time in millis of the full path
            PointList pointList = path.getPoints();
            double distance = path.getDistance();
                if (k == 0) {
                    dis_final = distance;
                    midlat1_final = midlat1;
                    midlon1_final = midlon1;
                }
                /*取最小的路径*/
                if (dis_final > distance) {
                    dis_final = distance;
                    midlat1_final = midlat1;
                    midlon1_final = midlon1;
                }
            //System.out.println("distance:" + dis_final);
            }

            File newcsv=new File("D://routing_dis_20161101.csv");
            BufferedWriter bw=new BufferedWriter (new FileWriter(newcsv,true));

            String item_orderid = item[1];
            //将规划好的距离数据写入csv
            bw.write(item_orderid + "," + item1+ "," + item2+ "," +midlon1_final+ "," +midlat1_final+ ","+item3+ "," + item4+ "," + dis_final);
            bw.newLine();//换行
            bw.close();



        }
        System.out.println("OK!");
        System.out.println("一共规划路径"+i+"条");

    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e: ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}
