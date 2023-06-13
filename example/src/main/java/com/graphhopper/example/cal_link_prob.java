package com.graphhopper.example;

import com.graphhopper.GraphHopper;

import java.io.*;
import java.sql.*;
import java.util.*;

public class cal_link_prob {

    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";
    private static final String QUERY = "";
    Map gid_map = new HashMap();
    Map his_trj_num_map = new HashMap();

    public static void main(String[] args) throws IOException {
        his_prob();
    }

    private static void his_prob() throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/for_exp_lt500_wtime_20161124_pro.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        int h = 0;
        while((line=reader.readLine())!=null)
        {
            h = h + 1;
            if (h % 500 == 0) {
                System.out.println("已经规划路径" + h + "条");
            }
            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String oid = item[0];
            /*下单点位置*/
            String query_lat = item[1];
            String query_lng = item[2];
            /*上车点位置*/
            String query_dlat = item[3];
            String query_dlng = item[4];

            String time_slot_string = item[5];
            String week_string = item[6];
            String weekend_string = item[7];

            int time_slot = Integer.parseInt(time_slot_string);
            int week = Integer.parseInt(week_string);
            int weekend = Integer.parseInt(weekend_string);

            cal_link_prob example = new cal_link_prob();
            /*查询500米内邻近路段*/
            Map near_gidList_result = example.get_near_gid(query_lng, query_lat, query_dlng, query_dlat);
            Map his_trj_numList = null;
            ArrayList<WayPoint2> distancelist = null;
            int[] distancelistarrIndex = new int[0];
            if(Arrays.asList(near_gidList_result.keySet().toArray()).size()>0) {
                /*数据扩充：查询相似轨迹在500米内邻近路段的历史途经数目*/
                his_trj_numList = example.search_his_trj_num(Arrays.asList(near_gidList_result.keySet().toArray()), query_lng, query_lat, query_dlng, query_dlat, time_slot, week);
//                /*计算概率最大路段*/
//                System.out.println("gid:"+ his_trj_numList.keySet().toString());
//                System.out.println("num:"+ his_trj_numList.values().toString());
            }
            System.out.println("a");

        }
    }

    /*查询500米内邻近路段*/
    public Map get_near_gid(String query_lng,String query_lat,String query_dlng,String query_dlat) {
        String QUERY = " SELECT gid,lng,lat FROM link_half_point WHERE ST_Intersects(link_half_point.point_geom,\n" +
                "\tST_Buffer(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), 0.0038))";
        //System.out.println(QUERY);
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            // 计算执行时间
            //System.out.printf("执行时长：%d ms.", near_etime);
            // System.out.println();
            int i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
                gid_map.put(rs.getString("gid"),rs.getString("lng")+","+rs.getString("lat"));
            }
//            System.out.println("查询到邻近边有："+i+"条");
//            System.out.println(gid_map.keySet());
        } catch (SQLException e) {
            printSQLException(e);
        }
        return gid_map;
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


    /*相似轨迹：在500米内邻近路段的历史途经数目*/
    public Map search_his_trj_num(List near_gidList_result, String query_lng, String query_lat, String query_dlng, String query_dlat, int time_slot, int week) {
        String weekname = "";
        String tablename = "";
        switch(week){
            case 1:
                weekname = "monday";
                break;
            case 2:
                weekname = "tuesday";
                break;
            case 3:
                weekname = "wednesday";
                break;
            case 4:
                weekname = "thursday";
                break;
            case 5:
                weekname = "friday";
                break;
            case 6:
                weekname = "saturday";
                break;
            case 7:
                weekname = "sunday";
                break;
        }
        if(time_slot < 36){
            tablename = weekname+"_0_36";
        }else if(36 <= time_slot && time_slot <= 43) {
            tablename = weekname+"_35_44";
        }else if(44 <= time_slot && time_slot <= 51) {
            tablename = weekname+"_43_52";
        }else if(52 <= time_slot && time_slot <= 59) {
            tablename = weekname+"_51_60";
        }else if(60 <= time_slot && time_slot <= 67) {
            tablename = weekname+"_59_68";
        }else if(68 <= time_slot && time_slot <= 75) {
            tablename = weekname+"_67_76";
        }else if(76 <= time_slot && time_slot <= 83) {
            tablename = weekname+"_75_84";
        }else if(84 <= time_slot && time_slot <= 96) {
            tablename = weekname+"_83_96";
        }

        String QUERY = " with s as (\n" +
                "SELECT DISTINCT(cpath) FROM "+tablename+" \n" +
                "WHERE\n" +
                " time_slot >= "+( time_slot-1 )+" \n" +
                "  and \n" +
                "\ttime_slot <= "+( time_slot+1 )+" \n" +
                "  and \n" +
                "ST_Intersects("+tablename+".ogeom,ST_Buffer(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), 0.004)) \n" +
                "  and \n" +
                "ST_Intersects("+tablename+".ogeom,ST_Buffer(ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326), 0.004)) \n" +
                ")\n";
        String QUERY2 = "";
        int i;
        String inital_o_range = "";
        inital_o_range = "select * from(select " + "'" + near_gidList_result.get(0) + "'  as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(0) + "']) = true\n";
        for (i = 1; i < near_gidList_result.size(); i++) {
            inital_o_range = inital_o_range +
                    "union select " + "'" + near_gidList_result.get(i) + "' as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(i) + "']) = true\n";
        }

        QUERY = QUERY + inital_o_range+") foo  order by link_sum desc limit 10";
        //System.out.println(QUERY);

        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            // 计算执行时间
//            System.out.printf("执行时长：%d ms.", his_trj_etime);
//            System.out.println();
            i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
//                his_trj_numList.add(rs.getString("gid")+":"+rs.getString("link_sum"));
//                his_trj_num_map.put(rs.getString("gid"),rs.getString("link_sum"));
//                QUERY2 = "select gid,lng,lat from link_half_point where gid = " + rs.getString("gid");
                System.out.println("gid:"+rs.getString("gid")+","+"link_sum:"+rs.getString("link_sum"));
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return his_trj_num_map;
    }
}
