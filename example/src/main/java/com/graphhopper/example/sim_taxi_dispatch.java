package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import org.locationtech.jts.geom.LineString;

import java.io.*;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;


public class sim_taxi_dispatch {
    private static String url = "jdbc:postgresql://localhost:5432/cdosm";
    private static String user = "postgres";
    private static String password = "880123";
    private static Map gid_map = new HashMap();
    private static int inital_time = 10*3600+5*60;

    //设置原始订单数据表

    //按照时间升序，读取OD订单(10:18:43,30.668246999999997,104.07576879999999,30.675205300000002,104.0553365)
    //order:(order_time,olng,olat,dlng,dlat)-》(order_time,ogeom,dgeom)
    //空车数据表
    //订单-司机分配表


    //为订单OD推荐最佳上车点

    //对订单时间查询，寻找离上车点位置(2000m内)最近且状态为空车的司机(drive_geom)进行分配(建立空车表列表中能查到的司机状态都为空)

    //分配完成后，更新该司机的状态（driver_id,drive_geom_update,work_time_update）
    //司机位置更新还有个方法就是，每隔1分钟更新一次司机的位置（将其移到邻近500概率最大的路段，模拟司机最大概率寻客路径）
    public static void main(String[] args) throws IOException, SQLException {
//        String orderid = "1";
//        String olat ="30.668246999999997";
//        String olon ="104.07576879999999";
//        String dlat ="30.675205300000002";
//        String dlon = "104.0553365";
//        String order_time = "10:50:43";

//        //推荐合适上车点（直接读取baseline方法的结果文件）
//        String recommand_lat = "30.6633386335327";
//        String recommand_lon = "104.090684071149";
//        String order_travel_time = "230.465";
//        int inital_time = 10*3600+10*60;

        //BufferedReader reader = new BufferedReader(new FileReader("C:/Users/fama/Desktop/sim_update/sim10_12_our_withspeed_fastest.csv"));
       // BufferedReader reader = new BufferedReader(new FileReader("C:/Users/fama/Desktop/sim_update/sim10_12_shortest.csv"));//换成你的文件名
        //BufferedReader reader = new BufferedReader(new FileReader("C:/Users/fama/Desktop/sim_update/sim10_12_prob.csv"));//换成你的文件名
       BufferedReader reader = new BufferedReader(new FileReader("D://trj_test/sim_wait_time_0320/sim10_12_prob.csv"));//换成你的文件名
        //BufferedReader reader = new BufferedReader(new FileReader("C:/Users/fama/Desktop/sim_update/sim10_12_closest.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        int num = 0;


        while((line=reader.readLine())!=null) {
            num = num + 1;
            if (num % 500 == 0) {
                System.out.println("已经规划路径" + num + "条");
            }
            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String orderid = item[0];
            String order_time = item[1];
            /*下单点位置*/
            String olat = item[2];
            String olon = item[3];
            /*上车点位置*/
            String dlat = item[4];
            String dlon = item[5];

            String walk_distance = item[13];
            double walk_time = Double.valueOf(walk_distance) / 1.5 ;

            //推荐合适上车点（直接读取baseline方法的结果文件）
            String recommand_lat = item[10];
            String recommand_lon = item[11];
            String order_travel_time = item[15];

            int weekend = 0;

            String strArray2[]=order_time.split(":");
            int time_slot_string = (Integer.parseInt(strArray2[0])*3600 +Integer.parseInt(strArray2[1])*60 + Integer.parseInt(strArray2[2]))/900 + 1;

            int flag = 0;
            int later_min = 0;
            String driverid = "";
            String driver_lon = "";
            String driver_lat = "";
            String available_time = "";
            int search_range_mile = 1000;
            int add_num = 0;

            while(flag ==0) {
            //查找寻找离上车点位置最近且状态为空车的司机进行分配
            String QUERY = "SELECT *,st_distance(driver.driver_geom,st_geometryfromtext('POINT(" + recommand_lon + "\t" + recommand_lat + ")',4326)) FROM driver\n" +
                    "\tWHERE st_dwithin(\n" +
                    "\t\tST_Transform(driver.driver_geom,900913),\n" +
                    "\t\tST_Transform(st_geometryfromtext('POINT(" + recommand_lon + "\t" + recommand_lat + ")',4326),900913),"+search_range_mile+")\n" +
                    "\tand\n" +
                    "\tavailable_time < '" + order_time +
                    "'\torder by st_distance(driver.driver_geom,st_geometryfromtext('POINT(" + recommand_lon + "\t" + recommand_lat + ")',4326)) limit 1";

            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                // 开始时间
                long stime = System.currentTimeMillis();
                ResultSet rs = preparedStatement.executeQuery();
                // 结束时间
                long etime = System.currentTimeMillis();
                // 计算执行时间
                //System.out.printf("执行时长：%d ms.", (etime - stime));
//                System.out.println();
                // Step 4: Process the ResultSet object.
                if (rs.next()) {
                    driverid = rs.getString("driverid");
                    driver_lon = rs.getString("driver_lon");
                    driver_lat = rs.getString("driver_lat");
                    available_time = rs.getString("available_time");
                    System.out.println(driverid + "," + driver_lon + "," + driver_lat + "," + available_time);
                    flag = 1;
                }else{
                    String strArray[]=order_time.split(":");
                    later_min = later_min +1;
                    order_time = strArray[0]+":"+(Integer.parseInt(strArray[1])+later_min)+":"+strArray[2];
                    if((Integer.parseInt(strArray[1])+later_min)< 10 ){
                        order_time = strArray[0]+":0"+(Integer.parseInt(strArray[1])+later_min)+":"+strArray[2];
                    }
                    search_range_mile = search_range_mile + 1000;//扩大司机搜索范围
                    add_num++;
                    System.out.println("扩大搜索范围次数: "+add_num);
                }
            } catch (SQLException e) {
//            printSQLException(e);
            }
            if(add_num > 3){
                System.out.println("当前4km内缺少可用车辆: "+add_num);
                flag = 2;
            }

        }
        //计算司机当前位置到达推荐上车点的数据（时间、距离），更新司机状态
        double wait_time = 0.0;
        if(flag == 1){
            GraphHopper hopper = new GraphHopper();
            String map_file = "C:/Users/fama/Desktop/24speed/map_15/graph_big_update.osm (2)_24_"+time_slot_string+"_speed_update_low.xml";
            hopper.setOSMFile(map_file);
            // specify where to store graphhopper files
            String map_file_cache = "target/routing-graph-cache-fama-weight_shortest_update_map_update_15_"+time_slot_string;
            hopper.setGraphHopperLocation(map_file_cache);

            // see docs/core/profiles.md to learn more about profiles
            hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("shortest").setTurnCosts(false));


            // this enables speed mode for the profile we called car
            hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

            // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
            hopper.importOrLoad();
            GHRequest req_car = new GHRequest(Double.valueOf(driver_lat), Double.valueOf(driver_lon), Double.valueOf(recommand_lat), Double.valueOf(recommand_lon)).
                    // 需要createGraphHopperInstance中配置也修改
                            setProfile("car").
                    // define the language for the turn instructions
                            setLocale(Locale.SIMPLIFIED_CHINESE);
            GHResponse res_car = hopper.route(req_car);
            wait_time = res_car.getBest().getTime()/ 1000.0;
//        System.out.println("Wait_time:"+wait_time+","+"distance:"+res_car.getBest().getDistance());

            //将时间转换为秒，再换算成时间
            double change_time = Double.valueOf(order_travel_time) + Double.valueOf(wait_time);
            String strArray[]=order_time.split(":");
            int update_hour = Integer.parseInt(strArray[0])*3600;
            int update_min = Integer.parseInt(strArray[1])*60;
            int update_second = Integer.parseInt(strArray[2]);
            change_time = change_time +update_hour+update_min+update_second;
            int h = (int) change_time / 3600;
            int m = (int) (Double.valueOf(change_time) % 3600) / 60;
            int s = (int) (Double.valueOf(change_time) % 3600) % 60;
            String update_available_time = "";
            String min = "";String second = "";
            if(m<10){
                min = "0"+m;
            }else{
                min = String.valueOf(m);
            }
            if(s<10){
                second = "0"+s;
            }else{
                second = String.valueOf(s);
            }
            update_available_time =h+":"+min+":"+second;
            System.out.println("Wait_time:"+wait_time+","+"distance:"+res_car.getBest().getDistance()+","+"update_available_time:"+update_available_time);

            //将订单调度信息输出
           // File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_Our.csv");
            //File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_closest.csv");
            //File newcsv = new File("D://trj_test/sim_wait_time_0320/sim_10_12_driver_dispatch_baseline_Our.csv");
            File newcsv = new File("D://trj_test/sim_wait_time_0320/sim_10_12_driver_dispatch_baseline_prob.csv");
           // File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_prob.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));
            bw.write(orderid + "," + recommand_lat + "," + recommand_lon + "," +driverid+","+ driver_lat+ "," + driver_lon+ ","+available_time+ ","+res_car.getBest().getDistance()+"," + (later_min)+ ","+walk_time+ "," + (later_min*60+wait_time)+ "," +order_travel_time+ "," + dlat+ "," +dlon+ "," +update_available_time);
            bw.newLine();//换行
            bw.close();

            //分配订单成功后，更新司机位置一次
            String QUERY2 = "update driver set driver_lon = '"+dlon+"' ,driver_lat = '"+dlat+"' ,available_time = '" +update_available_time +"' ,driver_geom= ST_GeomFromText ( 'POINT("+dlon+" "+dlat+")',4326) where driverid = '" +driverid + "'";
            System.out.println(QUERY2);
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY2)) {
                preparedStatement.executeQuery();
            }catch (SQLException e) {
                ;
            }
            System.out.println("司机调度且更新成功！");

            //一定时间间隔更新司机位置状态
            if((update_hour+update_min+update_second) > inital_time) {
                System.out.println(order_time+"==> 定时激活更新司机状态!");
                System.out.println("调整司机寻客路线中...");
                // 开始时间
                long update_driver_location_stime = System.currentTimeMillis();
                update_driver_location(order_time);
                // 结束时间
                long update_driver_location_etime = System.currentTimeMillis();
                System.out.println("更新用时（单位秒）:" + (update_driver_location_etime - update_driver_location_stime)/1000);
                inital_time = inital_time + 300;//十分钟更新一次司机状态
            }
        }else{
            //将订单调度信息输出
            //File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_Our.csv");
            //File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_closest.csv");
            //File newcsv = new File("D://trj_test/sim_wait_time_0320/sim_10_12_driver_dispatch_baseline_Our.csv");
            File newcsv = new File("D://trj_test/sim_wait_time_0320/sim_10_12_driver_dispatch_baseline_prob.csv");
            //File newcsv = new File("D://trj_test/sim_update_wait_time/sim_10_12_driver_dispatch_baseline_prob.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));
            bw.write(orderid + "," + recommand_lat + "," + recommand_lon + "," +0+","+ 0+ "," + 0+ ","+0+ ","+0+"," + 0+ ","+0+ "," + 0+ "," +0+ "," + 0+ "," +0+ "," +0);
            bw.newLine();//换行
            bw.close();
        }

        }
    }

    //每隔5mins更新全部司机的位置
    private static void update_driver_location(String order_time) throws SQLException {
        String strArray3[]=order_time.split(":");
        int time_slot_string = (Integer.parseInt(strArray3[0])*3600 +Integer.parseInt(strArray3[1])*60 + Integer.parseInt(strArray3[2]))/900 + 1;
        //查询遍历全部司机（可用时间小于等于当前时间的）
        String QUERY3 ="select * from driver where available_time < '"+order_time+"' ";
        System.out.println(QUERY3);
        ResultSet rs ;
        int i =0;
        String QUERY_update[] = new String[1000];
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY3)) {

            // Step 3: Execute the query or update query
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                if(rs.getString("driver_lon")!=null&&rs.getString("driver_lat")!=null){
                    java.util.Random r=new java.util.Random();
                    double range = Double.valueOf(0.0038) + Double.valueOf(r.nextInt(62))/10000;

                    //查询该司机附近历史订单多的路段（范围为200-1000之间）（选择路段也是随机选择一个）
                    Map near_gidList_result = get_near_gid(rs.getString("driver_lon"), rs.getString("driver_lat"), range);
                    if(near_gidList_result.size() > 0){
                        String gps[] = search_his_points_num2(Arrays.asList(near_gidList_result.keySet().toArray()), rs.getString("driver_lon"), rs.getString("driver_lat"), time_slot_string, 0);
                        // System.out.println(gps[0]+","+gps[1]);
                        QUERY_update[i] = "update driver set driver_lon = "+gps[0]+" ,driver_lat = "+gps[1] +" ,driver_geom= ST_GeomFromText ( 'POINT("+gps[0]+" "+gps[1]+")',4326) where driverid = '" +rs.getString("driverid")+"'";
                        i++;
                    }
                }
               // System.out.println("在更新");
            }
        }
        for(int j = 0;j<i;j++){
            //System.out.println(QUERY_update[j]);
            try (Connection connection2 = DriverManager.getConnection(url, user, password);
                 PreparedStatement preparedStatement2 = connection2.prepareStatement(QUERY_update[j])) {
                preparedStatement2.executeQuery();
            }catch (SQLException e) {
                ;
            }
        }
            System.out.println("本次更新司机位置数目："+i);
    }



    private static void printSQLException(SQLException ex) {
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

    /*查询500米内邻近路段*/
    public static Map get_near_gid(String query_lng, String query_lat, double search_range) {
        String QUERY = " SELECT gid,lng,lat FROM link_half_point WHERE ST_Intersects(link_half_point.point_geom,\n" +
                "\tST_Buffer(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), " +search_range+ "))";
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

            // Step 3: Execute the query or update query
            // 开始时间
            long stime = System.currentTimeMillis();
            ResultSet rs = preparedStatement.executeQuery();
            // 结束时间
            long etime = System.currentTimeMillis();
            int i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
                gid_map.put(rs.getString("gid"),rs.getString("lng")+","+rs.getString("lat"));
            }
            //System.out.println("查询到邻近边有："+i+"条");
            //System.out.println(gid_map.keySet());
        } catch (SQLException e) {
//            printSQLException(e);
        }
        return gid_map;
    }


    /*查询500米内邻近路段的历史上车点数目*/
    public static String[] search_his_points_num(List near_gidList_result, String query_lng, String query_lat, int time_slot, int weekend) {
        String QUERY_t ="";
        //System.out.println(query_lng+","+query_lat);
        /*计算查询点500m矩形范围相交的网格编号*/
        List search_geohash = getGeoHashBase32Forsearch500(Double.valueOf(query_lng),Double.valueOf(query_lat));
        //System.out.println(search_geohash);
        String QUERY = null;
        String[] gps = new String[2];
        if(search_geohash.size()>0){
            String inital_search_geohash = "with st as (SELECT otime_slice,near_fid_o,weekend FROM "+search_geohash.get(0);
            for (int i = 1; i < search_geohash.size(); i++) {
                inital_search_geohash = inital_search_geohash +
                        "\n union select otime_slice,near_fid_o,weekend FROM " +search_geohash.get(i);
            }
            QUERY = inital_search_geohash +")" +
                    ",s as\n" +
                    "(SELECT otime_slice,near_fid_o,weekend FROM st where near_fid_o = any (array\t" +
                    near_gidList_result + "\t)),\n" +
                    "ss as(select near_fid_o from s where weekend = "+weekend+" and otime_slice BETWEEN "+(time_slot-1)+" and "+(time_slot+1)+" ),\n" +
                    "sss as (select near_fid_o,count(near_fid_o) from ss group by near_fid_o order by count(near_fid_o) desc limit 10) " +
                    "select near_fid_o from sss order by random() LIMIT 1";
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

                // Step 3: Execute the query or update query
                ResultSet rs = preparedStatement.executeQuery();
                // 计算执行时间
//            System.out.printf("执行时长：%d ms.", his_point_etime);
            //System.out.println(QUERY);
                int i = 0;
                // Step 4: Process the ResultSet object.
               if(rs.next()) {
                   QUERY_t = "select gid,lng,lat from link_half_point where gid = " + rs.getString("near_fid_o");
               }else{
                   QUERY_t = "select '104.0807956'as lng,'30.69711622'as lat ";
                   System.out.println(query_lat+","+query_lng+"更新不成功.");
               }


            } catch (SQLException e) {
               printSQLException(e);
            }

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement2 = connection.prepareStatement(QUERY_t);) {
                ResultSet rs2 = preparedStatement2.executeQuery();
                rs2.next();
                gps[0] = rs2.getString("lng");
                gps[1] = rs2.getString("lat");


            }catch (SQLException e) {
                printSQLException(e);
            }
        }else{
            gps[0] = "104.0807956";
            gps[1] = "30.69711622";
        }

        return gps;
    }

    /*查询500米内邻近路段的车辆热度*/
    public static String[] search_his_points_num2(List near_gidList_result, String query_lng, String query_lat, int time_slot, int weekend) {
        String QUERY_t ="";
        String QUERY = null;
        String[] gps = new String[2];
        if(near_gidList_result.size()>0){
            QUERY = "with s as\n" +
                    "(select gid from path_num_10_12 where gid = any (array\t" +
                    near_gidList_result + "\t) and time_slot = "+time_slot+" order by car_num desc limit 20) " +
                    "select gid from s order by random() LIMIT 1";
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                ResultSet rs = preparedStatement.executeQuery();
                // Step 4: Process the ResultSet object.
                if(rs.next()) {
                    QUERY_t = "select lng,lat from link_half_point where gid = " + rs.getString("gid");
                }else{
                    QUERY_t = "select '104.0807956'as lng,'30.69711622'as lat ";
                    System.out.println(query_lat+","+query_lng+"更新不成功.");
                }
            } catch (SQLException e) {
                //printSQLException(e);
            }
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement2 = connection.prepareStatement(QUERY_t);) {
                ResultSet rs2 = preparedStatement2.executeQuery();
                rs2.next();
                gps[0] = rs2.getString("lng");
                gps[1] = rs2.getString("lat");

            }catch (SQLException e) {
               // printSQLException(e);
            }
        }else{
            gps[0] = "104.0807956";
            gps[1] = "30.69711622";
        }

        return gps;
    }

    public static List<String> getGeoHashBase32Forsearch500( double searchlng,double searchlat) {
        double minLat = 0.004;
        double minLng = 0.004;
        double leftLat = searchlat - minLat;
        double rightLat = searchlat + minLat;
        double upLng = searchlng - minLng;
        double downLng = searchlng + minLng;
        List<String> base32For9 = new ArrayList<String>();
        List<String> all_geohash =  Arrays.asList("wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
                "wm6n87","wm3yry","wm3yxc","wm6n3q","wm6n94","wm6n8u","wm3yxv","wm3yrw",
                "wm6n2p","wm3yx3","wm6n3n","wm3yrm","wm6nc1","wm6n84","wm3yzf","wm6n2n",
                "wm3yz9","wm3yxg","wm3yx7","wm3yrq","wm6nbf","wm6n9h","wm6nc4","wm6n89",
                "wm6nc3","wm6n3h","wm6nb4","wm6nb6","wm6n8p","wm6n92","wm6n2m","wm6nc0",
                "wm3yxd","wm6n3r","wm6n2x","wm6n8v","wm6n96","wm3yrr","wm6n8x","wm6n2q",
                "wm6n2y","wm3yzc","wm3yz8","wm6n9j","wm6nb1","wm6n88","wm6n2z","wm3yx9",
                "wm3yzb","wm6n8c","wm6n9r","wm3yxy","wm6n9q","wm6n8d","wm3yrs","wm3yxt",
                "wm6n90","wm3yxq","wm3yxx","wm6n3p","wm6n3m","wm6nb9","wm6n8b","wm3yxk",
                "wm6n2r","wm6n8r","wm6nb8","wm6n2k","wm6n2s","wm3yxe","wm3yx2","wm3yxs",
                "wm6n9p","wm3yru","wm3yrx","wm6n9n","wm6nbb","wm6nb2","wm6n8g","wm6n3j",
                "wm3yx6","wm6n8y","wm3yrv","wm3yxw","wm3yrt","wm6n2h","wm6n8k","wm3yxb",
                "wm6n8m","wm6nc2","wm6n9m","wm3yrz","wm6n2u","wm6n8n","wm6n2t","wm6n8s",
                "wm6n91","wm3yx8","wm6n8w","wm6n93","wm6n97","wm3yxf","wm6n95","wm6nb3",
                "wm6nc6","wm6n82","wm6nbd","wm6n80","wm3yrk","wm3yxm","wm6n2w","wm6n81",
                "wm6n8q","wm6nb0","wm3yxz","wm6n2j","wm3yzd","wm6nbc","wm6n8e","wm6n8j",
                "wm6n8h","wm6n2v","wm6n8f");
        //左侧从上到下 3个
        String leftUp = encode(leftLat, upLng).substring(0,6);
        if (!(leftUp == null || "".equals(leftUp))) {
            if(all_geohash.contains(leftUp)){
                if (!base32For9.contains(leftUp)) {
                    base32For9.add(leftUp);
                }
            }
        }
        String leftMid = encode(leftLat, searchlng).substring(0,6);
        if (!(leftMid == null || "".equals(leftMid))) {
            if(all_geohash.contains(leftMid)){
                if (!base32For9.contains(leftMid)) {
                    base32For9.add(leftMid);
                }
            }
        }
        String leftDown = encode(leftLat, downLng).substring(0,6);
        if (!(leftDown == null || "".equals(leftDown))) {
            if(all_geohash.contains(leftDown)) {
                if (!base32For9.contains(leftDown)) {
                    base32For9.add(leftDown);
                }
            }
        }
        //中间从上到下 3个
        String midUp = encode(searchlat, upLng).substring(0,6);
        if (!(midUp == null || "".equals(midUp))) {
            if(all_geohash.contains(midUp)) {
                if (!base32For9.contains(midUp)) {
                    base32For9.add(midUp);
                }
            }
        }
        String midMid = encode(searchlat, searchlng).substring(0,6);
        if (!(midMid == null || "".equals(midMid))) {
            if(all_geohash.contains(midMid)) {
                if (!base32For9.contains(midMid)) {
                    base32For9.add(midMid);
                }
            }
        }
        String midDown = encode(searchlat, downLng).substring(0,6);
        if (!(midDown == null || "".equals(midDown))) {
            if(all_geohash.contains(midDown)) {
                if (!base32For9.contains(midDown)) {
                    base32For9.add(midDown);
                }
            }
        }
        //右侧从上到下 3个
        String rightUp = encode(rightLat, upLng).substring(0,6);
        if (!(rightUp == null || "".equals(rightUp))) {
            if(all_geohash.contains(rightUp)) {
                if (!base32For9.contains(rightUp)) {
                    base32For9.add(rightUp);
                }
            }
        }
        String rightMid = encode(rightLat, searchlng).substring(0,6);
        if (!(rightMid == null || "".equals(rightMid))) {
            if(all_geohash.contains(rightMid)) {
                if (!base32For9.contains(rightMid)) {
                    base32For9.add(rightMid);
                }
            }
        }
        String rightDown = encode(rightLat, downLng).substring(0,6);
        if (!(rightDown == null || "".equals(rightDown))) {
            if(all_geohash.contains(rightDown)) {
                if (!base32For9.contains(rightDown)) {
                    base32For9.add(rightDown);
                }
            }
        }
        return base32For9;
    }
    //对经纬度进行编码
    public static String encode(double lat, double lon) {
        BitSet latbits = getBits(lat, -90, 90);
        BitSet lonbits = getBits(lon, -180, 180);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numbits; i++) {
            buffer.append( (lonbits.get(i))?'1':'0');
            buffer.append( (latbits.get(i))?'1':'0');
        }
        return base32(Long.parseLong(buffer.toString(), 2));
    }
    //根据经纬度和范围，获取对应二进制
    private static BitSet getBits(double lat, double floor, double ceiling) {
        BitSet buffer = new BitSet(numbits);
        for (int i = 0; i < numbits; i++) {
            double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }
    private static int numbits = 6 * 5; //经纬度单独编码长度
    //32位编码对应字符
    final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    //定义编码映射关系
    final static HashMap<Character, Integer> lookup = new HashMap<Character, Integer>();
    //初始化编码映射内容
    static {
        int i = 0;
        for (char c : digits)
            lookup.put(c, i++);
    }
    //将经纬度合并后的二进制进行指定的32位编码
    private static String base32(long i) {
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative)
            i = -i;
        while (i <= -32) {
            buf[charPos--] = digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = digits[(int) (-i)];

        if (negative)
            buf[--charPos] = '-';
        return new String(buf, charPos, (65 - charPos));
    }
}
