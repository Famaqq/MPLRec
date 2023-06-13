package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Waypoint_Strategy_by_link2 {
    public static void main(String[] args) throws InterruptedException, IOException {
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper_foot = createGraphHopperInstance_foot(relDir + "core/files/chengdu.osm.pbf");
        GraphHopper hopper_car = createGraphHopperInstance_car(relDir + "core/files/chengdu.osm.pbf");


        //创建Scanner对象，接受从控制台输入
        Scanner input=new Scanner(System.in);
        System.out.println("请输入目标策略的序号:");
        /*输入策略序号*/
        int Strategy_num = 3;
        // 开始时间
        long stime = System.currentTimeMillis();
        // 执行程序
        query(hopper_foot,hopper_car,Strategy_num);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d ms.", (etime - stime));
        hopper_foot.close();
        hopper_car.close();
    }
    public static void query(GraphHopper hopper_foot,GraphHopper hopper_car,Integer Strategy_num) throws IOException {
        /*数据库搜索*/
        String url = "jdbc:postgresql://localhost:5432/cdosm";
        String user = "postgres";
        String password = "880123";

        String QUERY = "SELECT * FROM link_half_point\n" +
                "\tWHERE ST_Intersects(link_half_point.point_geom,ST_Buffer(ST_GeomFromText(?,4326), 0.004))";

//        String query_lng= "104.079711";
//        String query_lat= "30.684264";
//        String query_dlng= "104.090017";
//        String query_dlat= "30.688515";

        BufferedReader reader = new BufferedReader(new FileReader("F://zhq/旧电脑/地点推荐资料/测试集24-30/wgs84_1124_reason_test.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        int h = 0;
        while((line=reader.readLine())!=null) {
            // 开始时间
            long stime = System.currentTimeMillis();


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
//            String query_lng= "104.079711";
//            String query_lat= "30.684264";
//            String query_dlng= "104.0839312";
//            String query_dlat= "30.66325915";

            double startlng = Double.parseDouble(query_lng);
            double startlat = Double.parseDouble(query_lat);
            double endlng = Double.parseDouble(query_dlng);
            double endlat = Double.parseDouble(query_dlat);

            String point_input = "POINT(" + query_lng + " " + query_lat + ")";//起始点
            /*数据库搜索nodes并存入latList、lonList中*/
            List nodes_point_lonList = new ArrayList();
            List nodes_point_latList = new ArrayList();
            List nodes_point_linkList = new ArrayList();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {

                /*传入查询的参数*/
                preparedStatement.setString(1, point_input);

                // Step 3: Execute the query or update query
                ResultSet rs = preparedStatement.executeQuery();
                int sum = 0;
                // Step 4: Process the ResultSet object.
                while (rs.next()) {
                    //接收查询结果的相应列
                    String lon = rs.getString("lng");
                    String lat = rs.getString("lat");
                    String gid = rs.getString("gid");
                    sum++;
                    nodes_point_lonList.add(lon);
                    nodes_point_latList.add(lat);
                    nodes_point_linkList.add(gid);
                }
                System.out.println("该点规定范围内搜索到的points数量为:" + sum);
            } catch (SQLException e) {

            }

            double dis_final = 0.00;
            double distance_first_final = 0.00;
            double distance_second_final = 0.00;
            String midlat1 = "";
            String midlon1 = "";
            double midlat1_final = 0.00;
            double midlon1_final = 0.00;

            double distance_first = 0.00;
            double distance_second = 0.00;
            double distance = 0.00;


            ArrayList<WayPoint2> array1 = new ArrayList<WayPoint2>();

            /*从搜索到的nodes中去遍历规划路径，获取最佳的途径点（即总路径最短）*/
            for (int k = 0; k < nodes_point_lonList.size(); k++) {

                midlat1 = String.valueOf(nodes_point_latList.get(k));
                Double midlat2 = Double.valueOf(midlat1);
                midlon1 = String.valueOf(nodes_point_lonList.get(k));
                Double midlon2 = Double.valueOf(midlon1);
                int gid = Integer.parseInt(String.valueOf(nodes_point_linkList.get(k)));

                /*第一段路径 步行*/
                GHRequest req_foot = new GHRequest(startlat, startlng, midlat2, midlon2).
                        // 需要createGraphHopperInstance中配置也修改
                                setProfile("foot").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE).
                                setSnapPreventions(Collections.singletonList("trunk"));
                GHResponse res_foot = hopper_foot.route(req_foot);
                ResponsePath path_foot = res_foot.getBest();

                /*第二段路径 车辆*/
                GHRequest req_car = new GHRequest(midlat2, midlon2, endlat, endlng).
                        // 需要createGraphHopperInstance中配置也修改
                                setProfile("car").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse res_car = hopper_car.route(req_car);
                ResponsePath path_car = res_car.getBest();

                /*排除定位在桥上问题*/
                GHRequest req_withnottrunk = new GHRequest(midlat2, midlon2, endlat, endlng).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("car").
                                setSnapPreventions(Collections.singletonList("trunk")).
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse rsp_withnottrunk = hopper_car.route(req_withnottrunk);
                ResponsePath path_withnottrunk = rsp_withnottrunk.getBest();
                /*判断两段路的长度，消除定位在桥上的影响*/
                double path_car_finall = 0.00;
                if (path_withnottrunk.getDistance() > 1.0) {
                    if (path_withnottrunk.getDistance() < path_car.getDistance()) {
                        path_car_finall = path_withnottrunk.getDistance();
                    } else path_car_finall = path_car.getDistance();
                }
                //本次计算的值
                distance_first = path_foot.getDistance();
                distance_second = path_car_finall;
                distance = distance_first + distance_second;

                //将途经点都计算出来，结果保存到Arraylist里面
                array1.add(new WayPoint2(k, midlat2, midlon2, distance_first, distance_second, distance, gid,null,0));

            }


            //Arraylist 的迭代
//        System.out.println("遍历排序前的list看看：");
//        for(int i = 0;i < array1.size(); i ++){
//            System.out.println(array1.get(i).getNum()+","+array1.get(i).getMidlat1()+","+array1.get(i).getMidlon1()+","+array1.get(i).getDistance_first()
//                    +","+array1.get(i).getDistance_second()+","+array1.get(i).getDistance());
//        }

            //根据Distance_first字段去降序排序，然后输出需要的Top-k个数
            //List<WayPoint> collect = array1.stream().sorted(Comparator.comparing(WayPoint::getDistance_first).reversed()).limit(2).collect(Collectors.toList());//降序排列
//        List<WayPoint> collect = array1.stream().sorted(Comparator.comparing(WayPoint::getDistance_first)).limit(10).collect(Collectors.toList());//升序排列排列
//        System.out.println("遍历排序后的list看看：");
//        for(int i = 0;i < collect.size(); i ++){
//            System.out.println(collect.get(i).getNum()+","+collect.get(i).getMidlat1()+","+collect.get(i).getMidlon1()+","+collect.get(i).getDistance_first()
//                    +","+collect.get(i).getDistance_second()+","+collect.get(i).getDistance());
//        }

            /*判断策略*/
            //策略一：步行距离最少
            if (Strategy_num == 1) {
                //根据Distance_first字段升序排列，然后输出步行距离最少的的Top-k个上车点
                List<WayPoint2> collect = array1.stream().sorted(Comparator.comparing(WayPoint2::getDistance_first)).limit(10).collect(Collectors.toList());//升序排列
                System.out.println("计算完成，输出步行距离最少的k个点：");
                for (int i = 0; i < collect.size(); i++) {
                    System.out.println(collect.get(i).getNum() + "," + collect.get(i).getMidlat1() + "," + collect.get(i).getMidlon1() + "," + collect.get(i).getDistance_first()
                            + "," + collect.get(i).getDistance_second() + "," + collect.get(i).getDistance() + "," + collect.get(i).getGid());
                }
            }
            //策略二：行驶距离最少
            if (Strategy_num == 2) {
                //根据Distance_second字段升序排列，然后输出行驶距离最少的的Top-k个上车点
                List<WayPoint2> collect = array1.stream().sorted(Comparator.comparing(WayPoint2::getDistance_second)).limit(10).collect(Collectors.toList());//升序排列
                System.out.println("计算完成，输出行驶距离最少的k个点：");
                for (int i = 0; i < collect.size(); i++) {
                    System.out.println(collect.get(i).getNum() + "," + collect.get(i).getMidlat1() + "," + collect.get(i).getMidlon1() + "," + collect.get(i).getDistance_first()
                            + "," + collect.get(i).getDistance_second() + "," + collect.get(i).getDistance() + "," + collect.get(i).getGid());
                }
            }
            //策略三：总距离最少 /*最短总的路程距离规划 = min(path_foot + path_car_finall)*/
            if (Strategy_num == 3) {
                //根据Distance字段升序排列，然后输出总距离最少的的Top-k个上车点
//            List<WayPoint2> collect = array1.stream().sorted(Comparator.comparing(WayPoint2::getDistance)).limit(10).collect(Collectors.toList());//升序排列
                List<WayPoint2> collect = array1.stream().sorted(Comparator.comparing(WayPoint2::getDistance)).collect(Collectors.toList());//升序排列
                System.out.println("计算完成，输出总距离最少的k个点：");
                for (int i = 0; i < collect.size(); i++) {
//                    System.out.println(collect.get(i).getNum() + "," + collect.get(i).getMidlat1() + "," + collect.get(i).getMidlon1() + "," + collect.get(i).getDistance_first()
//                            + "," + collect.get(i).getDistance_second() + "," + collect.get(i).getDistance() + "," + collect.get(i).getGid());

//                    File newcsv = new File("F://zhq/旧电脑/地点推荐资料/测试集24-30/1124_osm_result.csv");
                    File newcsv = new File("C://Users/fama/Desktop/fama/date_for_ml_test.csv");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));


                    //数据写入csv（路段id、相似od途经车数量、历史上车点数据、全部成功上车数、查询时间）
                    //bw.write(oid+ "，" +query_lng + "，" + query_lat + "," + query_dlng + "," + query_dlat + "," + key + "," + bbb_map.get(key) + "," + ddd_map.get(key) + "," + all_car_num + "," + rtime);
                    bw.write(oid+ "," + collect.get(i).getGid() + "," + collect.get(i).getMidlat1() + "," + collect.get(i).getMidlon1() + "," + collect.get(i).getDistance_first() + "," + collect.get(i).getDistance_second()+ "," +
                            collect.get(i).getDistance() );
                    bw.newLine();//换行
                    bw.close();

                }
            }

//        System.out.println("计算完成，输出结果：");
            //  System.out.println("起点坐标："+query_lat+ "," + query_lng+ ",推荐上车点坐标：" +midlat1_final+ "," +midlon1_final+ ",终点坐标：" +query_dlat+ ","+
            //  query_dlng+ ",需要步行距离：" + distance_first_final+ ",行驶距离" +distance_second_final+ ",总路程：" +dis_final);


            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf("执行时长：%d ms.", (etime - stime));

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
