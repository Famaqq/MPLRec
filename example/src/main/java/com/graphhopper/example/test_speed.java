package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import java.io.*;
import java.util.Locale;

public class test_speed {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:/Users/fama/Desktop/司机仿真/sim10_12_closest.csv"));//换成你的文件名
        reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
        String line = null;
        System.out.println("读取成功！");
        System.out.println("正在规划路径.....");
        int num = 0;

        while ((line = reader.readLine()) != null) {
            num = num + 1;
            if (num % 500 == 0) {
                System.out.println("已经规划路径" + num + "条");
            }
            String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

            String orderid = item[0];
            /*下单点位置*/
            String olat = item[2];
            String olon = item[3];
            /*上车点位置*/
            String dlat = item[4];
            String dlon = item[5];

            //计算司机当前位置到达推荐上车点的数据（时间、距离），更新司机状态
            GraphHopper hopper = new GraphHopper();
            String map_file = "C:/Users/fama/Desktop/24speed/map2/graph_big.osm (2)_24_96_speed_30kmh.xml";
            hopper.setOSMFile(map_file);
            // specify where to store graphhopper files
            String map_file_cache = "target/routing-graph-cache-fama-speed_test_30kmh";
            hopper.setGraphHopperLocation(map_file_cache);

            // see docs/core/profiles.md to learn more about profiles
            hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false));

            // this enables speed mode for the profile we called car
            hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

            // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
            hopper.importOrLoad();
            GHRequest req_car = new GHRequest(Double.valueOf(olat), Double.valueOf(olon), Double.valueOf(dlat), Double.valueOf(dlon)).
                    // 需要createGraphHopperInstance中配置也修改
                            setProfile("car").
                    // define the language for the turn instructions
                            setLocale(Locale.SIMPLIFIED_CHINESE);
            GHResponse res_car = hopper.route(req_car);

            System.out.println(orderid + ";" + res_car.getBest().getDistance() + ";" + res_car.getBest().getTime()/1000);
            //将订单调度信息输出
            File newcsv = new File("D://trj_test/sim/test_speed.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));
            bw.write(orderid + ";" + res_car.getBest().getDistance() + ";" + res_car.getBest().getTime()/1000);
            bw.newLine();//换行
            bw.close();
        }
    }
}
