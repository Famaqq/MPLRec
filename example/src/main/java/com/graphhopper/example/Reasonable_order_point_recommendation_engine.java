package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Reasonable_order_point_recommendation_engine {
    public static void main(String[] args) throws IOException {
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper_foot = createGraphHopperInstance_foot(relDir + "core/files/chengdu.osm.pbf");
        GraphHopper hopper_car = createGraphHopperInstance_car(relDir + "core/files/chengdu.osm.pbf");
        routing(hopper_foot,hopper_car);
        hopper_foot.close();
        hopper_car.close();
    }

    public static void routing(GraphHopper hopper_foot,GraphHopper hopper_car) throws IOException{
        int route_num = 0;
        System.out.println("正在读取文件中.....");
        BufferedReader reader = new BufferedReader(new FileReader("D://Reasonable_order_point_routing.csv"));//换成你的文件名
       // BufferedReader reader2 = new BufferedReader(new FileReader("D://PyTest/pythonProject1/gps_routing_1101_tail_WSG84.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息
        //reader2.readLine();//第一行信息，为标题信息
        String line = null;
       // String line2 = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        while ((line = reader.readLine()) != null)  {

            route_num = route_num + 1;
            if (route_num % 500 == 0) {
                System.out.println("已经规划路径" + route_num + "条");
            }

            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分


            String item1 = item[1];
            String item2 = item[2];
            String item3 = item[3];
            String item4 = item[4];

            double startlng = Double.parseDouble(item2);
            double startlat = Double.parseDouble(item1);
            double endlng = Double.parseDouble(item4);
            double endlat = Double.parseDouble(item3);

            String query_lng= String.valueOf(startlng);
            String query_lat= String.valueOf(startlat);

            /*数据库搜索nodes*/
            String url = "jdbc:postgresql://localhost:5432/cdosm";
            String user = "postgres";
            String password = "880123";

            /*查询语句*/
            String QUERY = " With CTE AS (SELECT *,TRUNC(ST_Distance(ST_GeomFromText(?,4326), geom  )*100000)::integer as distance FROM allwaynodes ORDER BY geom <-> ST_GeomFromText(?, 4326) LIMIT 500\n" +
                    ")SELECT * FROM CTE where distance < 400";

            /*数据库搜索nodes并存入latList、lonList中*/
            List nodes_point_lonList = new ArrayList();
            List nodes_point_latList = new ArrayList();

            /*将查询点本身也添加到latList、lonList中去规划，因为有可能目前所在的点就是最佳规划路径的起点,放在第一位也能初始化mid point*/
            nodes_point_lonList.add(query_lng);
            nodes_point_latList.add(query_lat);

            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
            // Step 2:Create a statement using connection object
            PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

                String point_input = "POINT("+query_lng+" "+query_lat+")";
                preparedStatement.setString(1, point_input);
                preparedStatement.setString(2, point_input);
                System.out.println(preparedStatement);
                // Step 3: Execute the query or update query
                ResultSet rs = preparedStatement.executeQuery();
                int sum = 0;
                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    //String id = rs.getString("id");
                    String lon = rs.getString("lon");
                    String lat = rs.getString("lat");
                    //String distance = rs.getString("distance");
                    sum++;
                    nodes_point_lonList.add(lon);
                    nodes_point_latList.add(lat);
                }
                //System.out.println("该点规定范围内搜索到的nodes数量为:"+sum);
            } catch (SQLException e) {
                printSQLException(e);
            }


            double dis_final = 0.00;
            double distance_first_final = 0.00;
            double distance_second_final = 0.00;
            String midlat1 = "";
            String midlon1 = "";
            String midlat1_final = "";
            String midlon1_final = "";

            /*从搜索到的nodes中去遍历规划路径，获取最佳的途径点（即总路径最短）*/
            for (int k = 0; k < nodes_point_lonList.size(); k++)
            {

                midlat1 = String.valueOf(nodes_point_latList.get(k));
                Double midlat2 = Double.valueOf(midlat1);
                midlon1 = String.valueOf(nodes_point_lonList.get(k));
                Double midlon2 = Double.valueOf(midlon1);

                /*第一段路径 步行*/
                GHRequest req_foot = new GHRequest(startlat,startlng,midlat2,midlon2).
                        // 需要createGraphHopperInstance中配置也修改
                                setProfile("foot").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE).
                                setSnapPreventions(Collections.singletonList("trunk"));
                GHResponse res_foot = hopper_foot.route(req_foot);
                ResponsePath path_foot = res_foot.getBest();
               // System.out.println("path_foot:" + path_foot);

                /*第二段路径 车辆*/
                GHRequest req_car = new GHRequest(midlat2,midlon2,endlat,endlng).
                        // 需要createGraphHopperInstance中配置也修改
                                setProfile("car").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse res_car = hopper_car.route(req_car);
                ResponsePath path_car = res_car.getBest();

                /*排除定位在桥上问题*/
                GHRequest req_withnottrunk = new GHRequest(midlat2,midlon2,endlat,endlng).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("car").
                                setSnapPreventions(Collections.singletonList("trunk")).
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse rsp_withnottrunk = hopper_car.route(req_withnottrunk);
                ResponsePath path_withnottrunk = rsp_withnottrunk.getBest();
                /*判断两段路的长度，消除定位在桥上的影响*/
                double path_car_finall = 0.00;
                if( path_withnottrunk.getDistance() > 1.0 ){
                    if(path_withnottrunk.getDistance() < path_car.getDistance()){
                        path_car_finall = path_withnottrunk.getDistance();}
                    else  path_car_finall = path_car.getDistance();
                }

                /*最短总的路程距离规划 = min(path_foot + path_car_finall)*/
                double distance_first = path_foot.getDistance();
                double distance_second = path_car_finall;
                double distance= distance_first + distance_second;
                if (k == 0) {
                    dis_final = distance;
                    midlat1_final = midlat1;
                    midlon1_final = midlon1;
                    distance_first_final = distance_first;
                    distance_second_final = distance_second;
                }

                /*取最短的路径*/
                if (dis_final > distance) {
                    dis_final = distance;
                    midlat1_final = midlat1;
                    midlon1_final = midlon1;
                    distance_first_final = distance_first;
                    distance_second_final = distance_second;
                }

            }

            File newcsv=new File("D://Reasonable_order_point_routing_dis_20161101.csv");
            BufferedWriter bw=new BufferedWriter (new FileWriter(newcsv,true));

            String item_orderid = item[0];
            //将规划好的距离数据写入csv
            bw.write(item_orderid + "," + item2+ "," + item1+ "," +midlon1_final+ "," +midlat1_final+ ","+item4+ ","+item3+ "," + distance_first_final+ ","+ distance_second_final+ "," + dis_final);
            bw.newLine();//换行
            bw.close();

        }
        System.out.println("OK!");
        System.out.println("一共规划路径"+route_num+"条");

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

    static GraphHopper createGraphHopperInstance_foot(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache-foot");

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("foot").setVehicle("foot").setWeighting("shortest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();
        return hopper;
    }

    static GraphHopper createGraphHopperInstance_car(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache-car");

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("shortest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();
        return hopper;
    }
}
