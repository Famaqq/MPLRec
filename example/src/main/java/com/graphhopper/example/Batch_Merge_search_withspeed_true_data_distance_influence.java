package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import org.locationtech.jts.geom.LineString;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Batch_Merge_search_withspeed_true_data_distance_influence {
    private static final String ghLoc = "./target/tmp/ghosm";
    private static long near_etime;
    private static long his_point_etime;
    private static long his_trj_etime;
    private static long distance_etime;

    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";

    private static final String QUERY = "";
    List his_points_numList = new ArrayList();
    List his_points_numList_gid = new ArrayList();
    List his_trj_numList = new ArrayList();
    Map gid_map = new HashMap();
    Map his_points_num_map = new HashMap();
    Map his_trj_num_map = new HashMap();

    public static void main(String[] args) throws IOException {

        /*查询最短路径距离,500米内邻近路段到终点路段的距离*/
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper_foot = createGraphHopperInstance_foot(relDir + "core/files/chengdu.osm.pbf");

        search_rank(hopper_foot, null);

        hopper_foot.close();
    }

    private static void search_rank(GraphHopper hopper_foot, GraphHopper hopper_car) throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/sim10_12_new2142.csv"));//换成你的文件名
        BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/true_data_26.csv"));//换成你的文件名
//        BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/sim10_12_old_updatespeed.csv"));//换成你的文件名
//        BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/24_order_file.csv"));//换成你的文件名
       // BufferedReader reader = new BufferedReader(new FileReader("D://reason_order24_30/sim10_12.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        int h = 0;
        while((line=reader.readLine())!=null) {
            h = h + 1;
            if (h % 500 == 0) {
                System.out.println("已经规划路径" + h + "条");
            }
            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String oid = item[0];
            /*下单点位置*/
            String query_lat = item[7];
            String query_lng = item[6];
            /*上车点位置*/
            String query_dlat = item[10];
            String query_dlng = item[9];

            String time_slot_string = item[5];

            int time_slot = Integer.parseInt(time_slot_string);
            int week = 5;//修改日期
            int weekend = 0;

            Batch_Merge_search_withspeed_true_data_distance_influence example = new Batch_Merge_search_withspeed_true_data_distance_influence();
            /*查询500米内邻近路段Map near_gidList_result <gid,mid_point> */
            Map near_gidList_result = example.get_near_gid(query_lng, query_lat, query_dlng, query_dlat);
            /*查询500米内邻近路段的历史上车点数目Map his_points_numList_gid <near_fid_o,count> */
            Map his_points_numList_gid = example.search_his_points_num(Arrays.asList(near_gidList_result.keySet().toArray()), query_lng, query_lat, query_dlng, query_dlat, time_slot, weekend);
            if(his_points_numList_gid.size()>0){
                /*数据扩充：查询相似轨迹在500米内邻近路段的历史途经数目*/
                Map his_trj_numList = example.search_his_trj_num(Arrays.asList(his_points_numList_gid.keySet().toArray()), query_lng, query_lat, query_dlng, query_dlat, time_slot, week);
                if(his_trj_numList.size()>0){
                    /*计算路径距离*/
                    ArrayList<WayPoint2> distancelist = example.get_near_distance(hopper_foot, hopper_car, Arrays.asList(his_points_numList_gid.keySet().toArray()), near_gidList_result, query_lng, query_lat, query_dlng, query_dlat,time_slot_string);

                    int[] his_points_numarr = new int[100];
                    int[] his_trj_numarr = new int[100];
//                    double[] distancelistarr = new double[100];
//                    double[] trj_timelistarr = new double[100];

                    /*将上述各查询结果合并排名*/
                    for (int i = 0; i < distancelist.size(); i++) {
                        his_points_numarr[i] = Integer.parseInt(String.valueOf(his_points_numList_gid.get(String.valueOf(distancelist.get(i).getGid()))));
                        his_trj_numarr[i] = Integer.parseInt(String.valueOf(his_trj_numList.get(String.valueOf(distancelist.get(i).getGid()))));
//                        distancelistarr[i] = distancelist.get(i).getDistance();
//                        trj_timelistarr[i] = distancelist.get(i).getTime();
                    }


                    File newcsv = new File("D://trj_test/true_data_26/true_data_our_26_5w.csv");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));
                    File newcsv2 = new File("D://trj_test/true_data_26/true_data_our_26_5w_Linestring.csv");
                    BufferedWriter bw2 = new BufferedWriter(new FileWriter(newcsv2, true));

                    for (int i = 0; i < distancelist.size(); i++) {
                        bw.write(oid + "," + query_lat + "," + query_lng + "," + query_dlat + "," + query_dlng + "," + distancelist.get(i).getGid() + "," + time_slot_string + "," + week + "," + weekend + "," +
                                distancelist.get(i).getMidlat1() + "," + distancelist.get(i).getMidlon1() + "," +
                                distancelist.get(i).getDistance()+","+
                                distancelist.get(i).getDistance_first() + "," + distancelist.get(i).getDistance_second() + "," +
                                distancelist.get(i).getTime()/1000+","+his_points_numarr[i]+","+his_trj_numarr[i]);
                        bw.newLine();//换行
                        bw2.write(oid + ";" + time_slot_string + ";" + week + ";" + weekend + ";" + distancelist.get(i).getLinestring());
                        bw2.newLine();//换行
                    }
                    bw.close();
                    bw2.close();

                }

            }

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
//            System.out.printf("执行时长：%d ms.", near_etime);
//            System.out.println();
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

    /*查询500米内邻近路段的历史上车点数目*/
    public Map search_his_points_num(List near_gidList_result,String query_lng,String query_lat,String query_dlng,String query_dlat,int time_slot,int weekend) {

        /*计算查询点500m矩形范围相交的网格编号*/
       List search_geohash = getGeoHashBase32Forsearch500(Double.valueOf(query_lng),Double.valueOf(query_lat));
        String QUERY = null;
        if(search_geohash.size()>0){
           String inital_search_geohash = "with st as (SELECT oddegrees,otime_slice,near_fid_o,weekend FROM "+search_geohash.get(0);
           for (int i = 1; i < search_geohash.size(); i++) {
               inital_search_geohash = inital_search_geohash +
                       "\n union select oddegrees,otime_slice,near_fid_o,weekend FROM " +search_geohash.get(i);
           }
           QUERY = inital_search_geohash +")" +
                   ",s as\n" +
                   "(SELECT oddegrees,otime_slice,near_fid_o,weekend FROM st where near_fid_o = any (array\t" +
                   near_gidList_result + "\t)),\n" +
                   "ss as(select oddegrees,near_fid_o from s where weekend = "+weekend+" and otime_slice BETWEEN "+(time_slot-1)+" and "+(time_slot+1)+" ),\n" +
                   "sss as(select near_fid_o from ss where (ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - ss.oddegrees) < 30)  or\n" +
                   "   ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - ss.oddegrees) > 330 )\n" +
                   "select near_fid_o,count(near_fid_o) from sss group by near_fid_o";
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
//            System.out.printf("执行时长：%d ms.", his_point_etime);
//            System.out.println();
                int i = 0;
                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    i++;
                    his_points_numList.add(rs.getString("near_fid_o")+':'+rs.getString("count"));
                    his_points_numList_gid.add(rs.getString("near_fid_o"));
                    his_points_num_map.put(rs.getString("near_fid_o"),rs.getString("count"));
                }
//            System.out.println("查询邻近历史上车路段有:"+i+"条");
//            System.out.println(his_points_numList);
            } catch (SQLException e) {
                printSQLException(e);
            }
        }
       // System.out.println(QUERY);
//        QUERY = " with st as (SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2m union \n" +
//                "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2t union \n" +
//                "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2v union \n" +
//                "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2q union \n" +
//                "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2w union\n" +
//                "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2y " +
//                "\t  )\n" +
//                ",s as\n" +
//                "(SELECT oddegrees,otime_slice,near_fid_o,weekend FROM st where near_fid_o = any (array\t" +
//                near_gidList_result + "\t)),\n" +
//                "ss as(select oddegrees,near_fid_o from s where weekend = 0 and otime_slice BETWEEN 59 and 61 ),\n" +
//                "sss as(select near_fid_o from ss where (ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - ss.oddegrees) < 30)  or\n" +
//                "   ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT("+query_lng+"\t"+query_lat+")',4326), ST_GeomFromText('POINT("+query_dlng+"\t"+query_dlat+")',4326))) - ss.oddegrees) > 330 )\n" +
//                "select near_fid_o,count(near_fid_o) from sss group by near_fid_o";
        // Step 1: Establishing a Connection

        return his_points_num_map;
    }

    /*数据扩充：查询相似轨迹在500米内邻近路段的历史途经数目*/
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
//            System.out.printf("执行时长：%d ms.", his_trj_etime);
//            System.out.println();
            i = 0;
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                i++;
//                his_trj_numList.add(rs.getString("gid")+":"+rs.getString("link_sum"));
                his_trj_num_map.put(rs.getString("gid"),rs.getString("link_sum"));
            }
//            System.out.println("查询相似OD的历史上车路段有:"+i+"条");
//            for (int k = 0; k < near_gidList.size(); k++) {
//                System.out.println(near_gidList.get(k));
//            }

        } catch (SQLException e) {
            printSQLException(e);
        }
        return his_trj_num_map;
    }
    /*查询最短路径距离,500米内邻近路段到终点路段的距离*/
    public ArrayList<WayPoint2>  get_near_distance(GraphHopper hopper_foot,GraphHopper hopper_car,List his_points_numList_gid,Map near_gidList_result,String query_lng,String query_lat,String query_dlng,String query_dlat,String time_slot_string) {
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
            GraphHopper hopper = new GraphHopper().
                    setProfiles(new CustomProfile("car").setCustomModel(new CustomModel().setDistanceInfluence(330)).setVehicle("car").setWeighting("custom")).
                    setStoreOnFlush(true).
                    setGraphHopperLocation(ghLoc).
                    setOSMFile("C:/Users/fama/Desktop/24speed/map_15/graph_big_update.osm (2)_24_"+time_slot_string+"_speed_update_low.xml");
            // specify where to store graphhopper files
            String map_file_cache = "target/routing-graph-cache-fama-weight_fastest_update_map_update_DistanceInfluence_15_"+time_slot_string;
            hopper.setGraphHopperLocation(map_file_cache);
            hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
            hopper.importOrLoad();//输出整个地图的边信息数据

            GHRequest req_car = new GHRequest(Double.valueOf(midlat), Double.valueOf(midlon), Double.valueOf(query_dlat), Double.valueOf(query_dlng)).
                    // 需要createGraphHopperInstance中配置也修改
                            setProfile("car").
                    // define the language for the turn instructions
                            setLocale(Locale.SIMPLIFIED_CHINESE);
//            GHResponse res_car = hopper_car.route(req_car);
            GHResponse res_car = hopper.route(req_car);
            ResponsePath path_car = res_car.getBest();
            LineString linestring = path_car.getPoints().getCachedLineString(true);
            double trj_time = res_car.getBest().getTime();

            /*忽略定位在桥上的影响*/
            double path_car_finall = 0.00;
            path_car_finall = path_car.getDistance();
            //本次计算的值
            distance_first = path_foot.getDistance();
            distance_second = path_car_finall;
            distance = distance_first + distance_second;

            //将途经点都计算出来，结果保存到Arraylist里面
            array1.add(new WayPoint2(k, Double.valueOf(midlat), Double.valueOf(midlon), distance_first, distance_second, distance, gid,linestring,trj_time));
        }
        // 结束时间
        long etime = System.currentTimeMillis();
        distance_etime =  (etime - stime);

        return array1;
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

    public static int[] Arraysort(int[]arr)
    {
        //double[] arr = {5.5,2,66,3,7,5};
        int temp;
        int index;
        int k=arr.length;
        int[]Index= new int[k];
        for(int i=0;i<k;i++)
        {
            Index[i]=i;
        }

        for(int i=0;i<arr.length;i++)
        {
            for(int j=0;j<arr.length-i-1;j++)
            {
                if(arr[j]<arr[j+1])
                {
                    temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;

                    index=Index[j];
                    Index[j] = Index[j+1];
                    Index[j+1] = index;
                }
            }
        }
        return Index;
    }

    public static int[] Arraysort2(double[]arr)
    {
        double temp;
        int index;
        int k=arr.length;
        int[]Index= new int[k];
        for(int i=0;i<k;i++)
        {
            Index[i]=i;
        }

        for(int i=0;i<arr.length;i++)
        {
            for(int j=0;j<arr.length-i-1;j++)
            {
                if(arr[j+1]>arr[j])
                {
                    temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;

                    index=Index[j];
                    Index[j] = Index[j+1];
                    Index[j+1] = index;
                }
            }
        }
        return Index;
    }
    public static int[] Arraysort3(int[]arr)
    {
        //double[] arr = {5.5,2,66,3,7,5};
        int temp;
        int index;
        int k=arr.length;
        int[]Index= new int[k];
        for(int i=0;i<k;i++)
        {
            Index[i]=i;
        }

        for(int i=0;i<arr.length;i++)
        {
            for(int j=0;j<arr.length-i-1;j++)
            {
                if(arr[j]>arr[j+1])
                {
                    temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;

                    index=Index[j];
                    Index[j] = Index[j+1];
                    Index[j+1] = index;
                }
            }
        }
        return Index;
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
