package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class baseline {
    private static long near_etime;
    private static long his_point_etime;
    private static long his_trj_etime;
    private static long distance_etime;
    List his_points_numList = new ArrayList();
    List his_points_numList_gid = new ArrayList();
    List his_trj_numList = new ArrayList();
    Map gid_map = new HashMap();
    Map his_points_num_map = new HashMap();
    Map his_trj_num_map = new HashMap();
    Map his_trj_num_all = new HashMap();


    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";
    private static final String QUERY = "";

    public static void main(String[] args) throws IOException {
        System.out.println("Hi,fama~");
        //clostest
        //near_fid
        //(prob 非齐次泊松分布)his_point his_trj
        //offline网格

        /*查询最短路径距离,500米内邻近路段到终点路段的距离*/
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper_foot = createGraphHopperInstance_foot(relDir + "core/files/chengdu.osm.pbf");
        GraphHopper hopper_car = createGraphHopperInstance_car(relDir + "core/files/chengdu.osm.pbf");

        //near500(hopper_foot,hopper_car);
        //his_prob();
        //closest(hopper_foot,hopper_car);
        kmeans();
        hopper_foot.close();
        hopper_car.close();
    }




    private static void near500(GraphHopper hopper_foot, GraphHopper hopper_car) throws IOException
    {
    BufferedReader reader = new BufferedReader(new FileReader("F://zhq/旧电脑/地点推荐资料/测试集24-30/wgs84_1124_reason_test.csv"));//换成你的文件名
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

            baseline example = new baseline();
            /*查询500米内邻近路段到目的地的距离*/
            Map near_gidList_result = example.get_near_gid(query_lng, query_lat, query_dlng, query_dlat);
            ArrayList<WayPoint2> distancelist = example.get_near_distance(hopper_foot, hopper_car, Arrays.asList(near_gidList_result.keySet().toArray()), near_gidList_result, query_lng, query_lat, query_dlng, query_dlat);

        }
    }


    private static void his_prob() throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("F://zhq/旧电脑/地点推荐资料/测试集24-30/wgs84_1124_reason_test.csv"));//换成你的文件名
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

            baseline example = new baseline();
            /*查询500米内邻近路段*/
            Map near_gidList_result = example.get_near_gid(query_lng, query_lat, query_dlng, query_dlat);
            /*数据扩充：查询相似轨迹在500米内邻近路段的历史途经数目*/
            Map his_trj_numList = example.search_his_trj_num(Arrays.asList(near_gidList_result.keySet().toArray()), query_lng, query_lat, query_dlng, query_dlat, time_slot, week);
            /*全部轨迹：在500米内邻近路段的历史途经数目*/
           // Map his_trj_all_numList = example.search_his_trj_num_all(Arrays.asList(near_gidList_result.keySet().toArray()), query_lng, query_lat, query_dlng, query_dlat, time_slot, week);

            int[] his_trj_numarr = new int[100];
            int[] his_trj_all_numarr = new int[100];
            double[] prob = new double[100];
            for (int i = 0; i < Arrays.asList(near_gidList_result.keySet().toArray()).size(); i++) {
                his_trj_numarr[i] = Integer.parseInt(String.valueOf(his_trj_numList.get(String.valueOf(Arrays.asList(near_gidList_result.keySet().toArray()).get(i)))));
               // his_trj_all_numarr[i] = Integer.parseInt(String.valueOf(his_trj_all_numList.get(String.valueOf(Arrays.asList(near_gidList_result.keySet().toArray()).get(i)))));

//                int c = (his_trj_all_numarr[i])*(-1);
//                double c1 = Math.pow(Math.E,c);
//                double d = Math.pow(his_trj_all_numarr[i],his_trj_numarr[i]);
//                double a=( c1 * d );
//                double b=unRecursion(his_trj_numarr[i]);
//
//                prob[i]=a/b;
//                if(a==0 || b==0){
//                    prob[i]=0;
//                }
//                System.out.println("his_trj_all_numarr[i],is_trj_numarr[i]:"+his_trj_all_numarr[i]+","+his_trj_numarr[i]);
//                System.out.println("a,b:"+a+","+b);
//                System.out.println("c1,d:"+c1+","+d);
//                System.out.println("prob:"+prob[i]);

                //计算公式：P(t)=1 - e^(-λ*t)
                double c = (his_trj_numarr[i] / 3.0)*(-1);
                double c1 = Math.pow(Math.E,c);
                prob[i] = 1 - Math.pow(Math.E,c);
                System.out.println("his_trj_numarr:"+his_trj_numarr[i]);
                System.out.println("prob:"+prob[i]);
            }
        }
    }

    private static void closest(GraphHopper hopper_foot, GraphHopper hopper_car) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("F://zhq/旧电脑/地点推荐资料/测试集24-30/wgs84_1124_reason_test.csv"));//换成你的文件名
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

            baseline example = new baseline();
            /*查询500米内邻近路段到目的地的距离*/
            Map near_gidList_result = example.get_closest_gid(query_lng, query_lat, query_dlng, query_dlat);
            System.out.println(near_gidList_result.values());
        }
    }

    private static void kmeans() throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader("F://zhq/旧电脑/地点推荐资料/测试集24-30/wgs84_1124_reason_test.csv"));//换成你的文件名
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

            baseline example = new baseline();
            /*数据扩充：查询相似轨迹在500米内邻近路段的历史途经数目*/
            Map his_trj_numList = example.search_kmeans(query_lng, query_lat, query_dlng, query_dlat, time_slot, week);


        }
    }

    /*查询500米内邻近路段*/
    public Map get_near_gid(String query_lng,String query_lat,String query_dlng,String query_dlat) {
        String QUERY = " SELECT gid,lng,lat FROM link_half_point WHERE ST_Intersects(link_half_point.point_geom,\n" +
                "\tST_Buffer(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), 0.0038))";
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
            near_etime =  (etime - stime);
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", near_etime);
            System.out.println();
            int i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
                gid_map.put(rs.getString("gid"),rs.getString("lng")+","+rs.getString("lat"));
            }
            System.out.println("查询到邻近边有："+i+"条");
            System.out.println(gid_map.keySet());
        } catch (SQLException e) {
            printSQLException(e);
        }
        return gid_map;
    }

    /*查询最短路径距离,500米内邻近路段到终点路段的距离*/
    public ArrayList<WayPoint2>  get_near_distance(GraphHopper hopper_foot,GraphHopper hopper_car,List his_points_numList_gid,Map near_gidList_result,String query_lng,String query_lat,String query_dlng,String query_dlat) {
        // 开始时间
        long stime = System.currentTimeMillis();

        /**最短路径的计算**/
        double dis_final = 0.00;
        double distance_first_final = 0.00;
        double distance_second_final = 0.00;
        String midlat = "";
        String midlon = "";
        double midlat1_final = 0.00;
        double midlon1_final = 0.00;

        double distance_first = 0.00;
        double distance_second = 0.00;
        double distance = 0.00;

        ArrayList<WayPoint2> array1 = new ArrayList<WayPoint2>();

        /*从搜索到的nodes中去遍历规划路径，获取最佳的途径点（即总路径最短）*/
        for (int k = 0; k < his_points_numList_gid.size(); k++) {
            String[] sArray= String.valueOf(near_gidList_result.get(his_points_numList_gid.get(k))).split(",");
            midlat = sArray[1];
            midlon = sArray[0];
            int gid = Integer.parseInt(String.valueOf(his_points_numList_gid.get(k)));

            /*第一段路径 步行*/
            GHRequest req_foot = new GHRequest(Double.valueOf(query_lat), Double.valueOf(query_lng), Double.valueOf(midlat), Double.valueOf(midlon)).
                    // 需要createGraphHopperInstance中配置也修改
                            setProfile("foot").
                    // define the language for the turn instructions
                            setLocale(Locale.SIMPLIFIED_CHINESE).
                            setSnapPreventions(Collections.singletonList("trunk"));
            GHResponse res_foot = hopper_foot.route(req_foot);

            ResponsePath path_foot = res_foot.getBest();

            /*第二段路径 车辆*/
            GHRequest req_car = new GHRequest(Double.valueOf(midlat), Double.valueOf(midlon), Double.valueOf(query_dlat), Double.valueOf(query_dlng)).
                    // 需要createGraphHopperInstance中配置也修改
                            setProfile("car").
                    // define the language for the turn instructions
                            setLocale(Locale.SIMPLIFIED_CHINESE);
            GHResponse res_car = hopper_car.route(req_car);
            ResponsePath path_car = res_car.getBest();

//            /*排除定位在桥上问题*/
//            GHRequest req_withnottrunk = new GHRequest(Double.valueOf(midlat), Double.valueOf(midlon),Double.valueOf(query_dlat), Double.valueOf(query_dlng)).
//                    // note that we have to specify which profile we are using even when there is only one like here
//                            setProfile("car").
//                            setSnapPreventions(Collections.singletonList("trunk")).
//                    // define the language for the turn instructions
//                            setLocale(Locale.SIMPLIFIED_CHINESE);
//            GHResponse rsp_withnottrunk = hopper_car.route(req_withnottrunk);
//            ResponsePath path_withnottrunk = rsp_withnottrunk.getBest();
//            /*判断两段路的长度，消除定位在桥上的影响*/
//            double path_car_finall = 0.00;
//            if (path_withnottrunk.getDistance() > 1.0) {
//                if (path_withnottrunk.getDistance() < path_car.getDistance()) {
//                    path_car_finall = path_withnottrunk.getDistance();
//                } else path_car_finall = path_car.getDistance();
//            }
            /*忽略定位在桥上的影响*/
            double path_car_finall = 0.00;
            path_car_finall = path_car.getDistance();
            //本次计算的值
            distance_first = path_foot.getDistance();
            distance_second = path_car_finall;
            distance = distance_first + distance_second;

            //将途经点都计算出来，结果保存到Arraylist里面
            array1.add(new WayPoint2(k, Double.valueOf(midlat), Double.valueOf(midlon), distance_first, distance_second, distance, gid,null,0));
        }
        // 结束时间
        long etime = System.currentTimeMillis();
        System.out.println("计算最短路径:"+his_points_numList_gid.size()+"条");
        // 计算执行时间
        distance_etime =  (etime - stime);
        System.out.printf("执行时长：%d ms.", distance_etime);
        System.out.println();

//        for (int i = 0; i < array1.size(); i++) {
//            System.out.println(array1.get(i).getGid()+ "," + array1.get(i).getMidlat1() + "," + array1.get(i).getMidlon1() + "," + array1.get(i).getDistance_first()
//                    + "," + array1.get(i).getDistance_second() + "," + array1.get(i).getDistance() );
//        }

        return array1;
    }

    /*相似轨迹：在500米内邻近路段的历史途经数目*/
    public Map search_his_trj_num(List near_gidList_result,String query_lng,String query_lat,String query_dlng,String query_dlat,int time_slot,int week) {
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

        int i;
        String inital_o_range = "";
        inital_o_range = "select " + "'" + near_gidList_result.get(0) + "'  as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(0) + "']) = true\n";
        for (i = 1; i < near_gidList_result.size(); i++) {
            inital_o_range = inital_o_range +
                    "union select " + "'" + near_gidList_result.get(i) + "' as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(i) + "']) = true\n";
        }

        QUERY = QUERY + inital_o_range;
        //System.out.println(QUERY);
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
            his_trj_etime =  (etime - stime);
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", his_trj_etime);
            System.out.println();
            i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
//                his_trj_numList.add(rs.getString("gid")+":"+rs.getString("link_sum"));
                his_trj_num_map.put(rs.getString("gid"),rs.getString("link_sum"));
            }
            System.out.println("查询相似OD的历史上车路段有:"+i+"条");
//            for (int k = 0; k < near_gidList.size(); k++) {
//                System.out.println(near_gidList.get(k));
//            }

        } catch (SQLException e) {
            printSQLException(e);
        }
        return his_trj_num_map;
    }

    /*全部轨迹：在500米内邻近路段的历史途经数目*/
    public Map search_his_trj_num_all(List near_gidList_result,String query_lng,String query_lat,String query_dlng,String query_dlat,int time_slot,int week) {
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
                ")\n";

        int i;
        String inital_o_range = "";
        inital_o_range = "select " + "'" + near_gidList_result.get(0) + "'  as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(0) + "']) = true\n";
        for (i = 1; i < near_gidList_result.size(); i++) {
            inital_o_range = inital_o_range +
                    "union select " + "'" + near_gidList_result.get(i) + "' as gid" + ",count(*) as link_sum from s where (string_to_array(s.cpath,',') @> array['" + near_gidList_result.get(i) + "']) = true\n";
        }

        QUERY = QUERY + inital_o_range;
        //System.out.println(QUERY);
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
            his_trj_etime =  (etime - stime);
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", his_trj_etime);
            System.out.println();
            i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
//                his_trj_numList.add(rs.getString("gid")+":"+rs.getString("link_sum"));
                his_trj_num_all.put(rs.getString("gid"),rs.getString("link_sum"));
            }
            System.out.println("查询全部轨迹的历史上车路段有:"+i+"条");
//            for (int k = 0; k < near_gidList.size(); k++) {
//                System.out.println(near_gidList.get(k));
//            }

        } catch (SQLException e) {
            printSQLException(e);
        }
        return his_trj_num_all;
    }

    public static double unRecursion(int num){
        double jiecheng = 1.0;
        if(num == 0){
            return 0;
        }
        for (int i = 1; i <= num; i++) {
            jiecheng*=i;
        }
        return jiecheng;
    }

    /*查询500米内邻近路段*/
    public Map get_closest_gid(String query_lng,String query_lat,String query_dlng,String query_dlat) {
        String QUERY = " SELECT gid,lng,lat FROM link_half_point WHERE ST_Intersects(link_half_point.point_geom,\n" +
                "\tST_Buffer(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), 0.0038)) order by  ST_Distance(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326),link_half_point.point_geom) limit 1";
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
            near_etime =  (etime - stime);
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", near_etime);
            System.out.println();
            int i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
                gid_map.put(rs.getString("gid"),rs.getString("lng")+","+rs.getString("lat"));
            }
            //System.out.println("查询到邻近边有："+i+"条");
            //System.out.println(gid_map.keySet());
        } catch (SQLException e) {
            printSQLException(e);
        }
        return gid_map;
    }

    /*kmeans*/
    public Map search_kmeans(String query_lng,String query_lat,String query_dlng,String query_dlat,int time_slot,int week) {
        String QUERY = "with st as (SELECT oddegrees,otime_slice,week,olat,olon,ogeom FROM his_point),\n" +
                "s as(\n" +
                "\tSELECT ST_ClusterKMeans(ogeom,9) over () AS cid,* from st \n" +
                "\tWHERE \n" +
                "\t\tweek =  "+week+" \n" +
                "\tand \n" +
                "\t\t  otime_slice BETWEEN "+(time_slot-1)+" and "+(time_slot+1) +
                "    and (ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326),ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - oddegrees) < 30)  \n" +
                "\tor ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - oddegrees) > 330\n" +
                "\torder by cid ),\n" +
                "ss as(select s.cid from s group by cid order BY count(cid) Desc limit 1)\n" +
                "\tselect olon,olat from(select *,ROW_NUMBER()over(PARTITION By s.cid order by ST_Distance(ST_Transform(s.ogeom,900913),\n" +
                "\t\tST_Transform(st_geometryfromtext('POINT("+query_lng+"\t"+query_lat+")',4326),900913))) as rownum from s)T where T.rownum = 1 and cid in(select s.cid from s group by cid order BY count(cid) Desc limit 1) ";
       // System.out.println(QUERY);
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
            his_point_etime =  (etime - stime);
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", his_point_etime);
            System.out.println();
            int i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
                System.out.println(rs.getString("olat"));
                System.out.println(rs.getString("olon"));
            }
           // System.out.println("查询有:"+i+"条");
        } catch (SQLException e) {
            printSQLException(e);
        }
        return his_points_num_map;
    }

    public static List<String> getGeoHashBase32Forsearch500( double searchlng,double searchlat) {
        double minLat = 0.004;
        double minLng = 0.004;
        double leftLat = searchlat - minLat;
        double rightLat = searchlat + minLat;
        double upLng = searchlng - minLng;
        double downLng = searchlng + minLng;
        List<String> base32For9 = new ArrayList<String>();
        //左侧从上到下 3个
        String leftUp = encode(leftLat, upLng).substring(0,6);
        if (!(leftUp == null || "".equals(leftUp))) {
            if (!base32For9.contains(leftUp)) {
                base32For9.add(leftUp);
            }
        }
        String leftMid = encode(leftLat, searchlng).substring(0,6);
        if (!(leftMid == null || "".equals(leftMid))) {
            if (!base32For9.contains(leftMid)) {
                base32For9.add(leftMid);
            }
        }
        String leftDown = encode(leftLat, downLng).substring(0,6);
        if (!(leftDown == null || "".equals(leftDown))) {
            if (!base32For9.contains(leftDown)) {
                base32For9.add(leftDown);
            }
        }
        //中间从上到下 3个
        String midUp = encode(searchlat, upLng).substring(0,6);
        if (!(midUp == null || "".equals(midUp))) {
            if (!base32For9.contains(midUp)) {
                base32For9.add(midUp);
            }
        }
        String midMid = encode(searchlat, searchlng).substring(0,6);
        if (!(midMid == null || "".equals(midMid))) {
            if (!base32For9.contains(midMid)) {
                base32For9.add(midMid);
            }
        }
        String midDown = encode(searchlat, downLng).substring(0,6);
        if (!(midDown == null || "".equals(midDown))) {
            if (!base32For9.contains(midDown)) {
                base32For9.add(midDown);
            }
        }
        //右侧从上到下 3个
        String rightUp = encode(rightLat, upLng).substring(0,6);
        if (!(rightUp == null || "".equals(rightUp))) {
            if (!base32For9.contains(rightUp)) {
                base32For9.add(rightUp);
            }
        }
        String rightMid = encode(rightLat, searchlng).substring(0,6);
        if (!(rightMid == null || "".equals(rightMid))) {
            if (!base32For9.contains(rightMid)) {
                base32For9.add(rightMid);
            }
        }
        String rightDown = encode(rightLat, downLng).substring(0,6);
        if (!(rightDown == null || "".equals(rightDown))) {
            if (!base32For9.contains(rightDown)) {
                base32For9.add(rightDown);
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
