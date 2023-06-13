package com.graphhopper.example;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;

import java.io.*;
import java.util.*;

import static com.graphhopper.json.Statement.If;
import static com.graphhopper.json.Statement.Op.LIMIT;
import static com.graphhopper.json.Statement.Op.MULTIPLY;

public class Allrouting {
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
        hopper.setGraphHopperLocation("target/routing-graph-cache_car_shortest");

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("shortest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();
        return hopper;
    }

    public static void routing(GraphHopper hopper) {
        int i = 0;
        try {
            System.out.println("正在读取文件中.....");
            BufferedReader reader = new BufferedReader(new FileReader("D://zhq/Available_gps_routing_1101_head_WSG84.csv"));//换成你的文件名
            BufferedReader reader2 = new BufferedReader(new FileReader("D://zhq/Available_gps_routing_1101_tail_WSG84.csv"));//换成你的文件名
            reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            reader2.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
            String line = null;
            String line2 = null;
            System.out.println("读取成功！");
            System.out.println("正在规划路径.....");
            while(((line=reader.readLine())!=null)&&((line2=reader2.readLine())!=null)){

                i=i+1;
                if(i%500==0) {
                    System.out.println("已经规划路径"+i+"条");
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

                // simple configuration of the request object
                GHRequest req = new GHRequest(startlat,startlng,endlat,endlng).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("car").
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse rsp = hopper.route(req);

                GHRequest req_withnottrunk = new GHRequest(startlat,startlng,endlat,endlng).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("car").
                                setSnapPreventions(Collections.singletonList("trunk")).
                        // define the language for the turn instructions
                                setLocale(Locale.SIMPLIFIED_CHINESE);
                GHResponse rsp_withnottrunk = hopper.route(req_withnottrunk);

                // handle errors
                if (rsp.hasErrors())
                    throw new RuntimeException(rsp.getErrors().toString());

                // use the best path, see the GHResponse class for more possibilities.
                ResponsePath path = rsp.getBest();
                 // points, distance in meters             System.out.println("path:" + path);
                Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
                InstructionList il = path.getInstructions();
                // iterate over all turn instructions
                double dis = 0.00;
                double dis_finall = 0.00;
                for (Instruction instruction : il) {
                    dis = instruction.getDistance() + dis;
                }
                //System.out.println("ALL distance:" + dis);
                assert il.size() == 6;
                assert Helper.round(path.getDistance(), -2) == 900;


                if (rsp_withnottrunk.hasErrors())
                    throw new RuntimeException(rsp_withnottrunk.getErrors().toString());

                ResponsePath path_withnottrunk = rsp_withnottrunk.getBest();
                InstructionList il_withnottrunk = path_withnottrunk.getInstructions();
                // iterate over all turn instructions
                double dis_withnottrunk = 0.00;
                for (Instruction instruction_withnottrunk : il_withnottrunk) {
                    dis_withnottrunk = instruction_withnottrunk.getDistance() + dis_withnottrunk;
                }
                assert il_withnottrunk.size() == 6;
                assert Helper.round(path_withnottrunk.getDistance(), -2) == 900;

                if( dis_withnottrunk > 1.0 ){
                    if(dis_withnottrunk < dis){
                        dis_finall = dis_withnottrunk;}
                    else  dis_finall = dis;
                }


                File newcsv=new File("D://zhq/Available_routing_dis_20161101.csv");
                BufferedWriter bw=new BufferedWriter (new FileWriter(newcsv,true));

                String item_orderid = item[1];
                //将规划好的距离数据写入csv
                bw.write(item_orderid + "," + item1+ "," + item2+ "," + item3+ "," + item4+ "," + dis_finall);
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
