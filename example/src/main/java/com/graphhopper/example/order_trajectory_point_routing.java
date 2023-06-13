package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Translation;

import java.io.*;
import java.util.Collections;
import java.util.Locale;

public class order_trajectory_point_routing {
    public static void main(String[] args) {
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper = createGraphHopperInstance(relDir + "core/files/chengdu.osm.pbf");
        routing(hopper);
        hopper.close();
    }

    static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache-foot2");

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("foot").setVehicle("foot").setWeighting("shortest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();
        return hopper;
    }

    public static void routing(GraphHopper hopper) {
        int i = 0;
        try {
            System.out.println("正在读取文件中.....");
            BufferedReader reader = new BufferedReader(new FileReader("D://order_trajectory_start_point.csv"));//换成你的文件名
            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null;
            System.out.println("读取成功！");
            System.out.println("正在规划路径.....");
            while((line=reader.readLine())!=null){

                i=i+1;
                if(i%500==0) {
                    System.out.println("已经规划路径"+i+"条");
                }

                String[] item = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分

                /*下单点位置*/
                String item1 = item[2];
                String item2 = item[1];
                /*上车点位置*/
                String item3 = item[4];
                String item4 = item[3];

                double startlng = Double.parseDouble(item1);
                double startlat = Double.parseDouble(item2);
                double endlng = Double.parseDouble(item3);
                double endlat = Double.parseDouble(item4);

                // simple configuration of the request object
                GHRequest req = new GHRequest(startlat,startlng,endlat,endlng).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("foot").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse rsp = hopper.route(req);

                Double finall_path = 0.00;
                if (rsp.hasErrors())
                {
                    //throw new RuntimeException(rsp.getErrors().toString());
                    finall_path = 9999.999;
                }else {

                    // use the best path, see the GHResponse class for more possibilities.
                    ResponsePath path = rsp.getBest();
                    finall_path = path.getDistance();
                }

                File newcsv=new File("D://order_trajectory_routing_dis_20161101.csv");
                BufferedWriter bw=new BufferedWriter (new FileWriter(newcsv,true));

                String item_orderid = item[0];
                //将规划好的距离数据写入csv
                bw.write(item_orderid + "," + item2+ "," + item1+ "," + item4+ "," + item3+ "," + finall_path);
                bw.newLine();//换行
                bw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("OK!");
        System.out.println("一共规划路径"+i+"条");



    }

}
