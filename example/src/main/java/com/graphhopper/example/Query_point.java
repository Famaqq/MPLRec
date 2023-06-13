package com.graphhopper.example;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Query_point {
    public static void main(String[] args) throws InterruptedException  {
        // 开始时间
        long stime = System.currentTimeMillis();
        // 执行程序
        query();
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
       // System.out.printf("执行时长：%3s 秒.", (etime - stime)/1000.0);
        System.out.printf("执行时长：%d ms.", (etime - stime));
    }
    public static void query()  {
        /*数据库搜索*/
        String url = "jdbc:postgresql://localhost:5432/cdosm";
        String user = "postgres";
        String password = "880123";

        /*查询语句*/
      //  String QUERY1 = "insert into temp_point_select select * FROM pd_point WHERE ST_Intersects(pd_point.ogeom,ST_Buffer(ST_GeomFromText(?,4326), 0.004));select * from Get_recommend_ponit3(?,?)";
        String QUERY = "select * from Get_recommend_ponit(?,?)";

        String query_lng= "104.060022";
        String query_lat= "30.666806";
        String query_dlng= "104.068036";
        String query_dlat= "30.678115";

        String point_input = "POINT(" + query_lng + " " + query_lat + ")";//起始点
        String dpoint_input = "POINT(" + query_dlng + " " + query_dlat + ")";//目的点
        /*数据库搜索nodes并存入latList、lonList中*/
        List nodes_point_lonList = new ArrayList();
        List nodes_point_latList = new ArrayList();

//
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

//            System.out.println("point_input:"+point_input);
//            System.out.println("dpoint_input:"+dpoint_input);
            /*传入查询的参数*/
            preparedStatement.setString(1, point_input);
            preparedStatement.setString(2, dpoint_input);
          //  preparedStatement.setString(3, dpoint_input);

            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            int sum = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                //String id = rs.getString("id");
                String lon = rs.getString("olon");
                String lat = rs.getString("olat");
                //String distance = rs.getString("distance");
                sum++;
                nodes_point_lonList.add(lon);
                nodes_point_latList.add(lat);
            }
            System.out.println("该点规定范围内搜索到的points数量为:"+sum);
        } catch (SQLException e) {
            printSQLException(e);
        }


        for (int k = 0; k < nodes_point_lonList.size(); k++) {
            System.out.println("上车点"+k+"坐标为："+String.valueOf(nodes_point_lonList.get(k))+","+String.valueOf(nodes_point_latList.get(k)));
        }
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
