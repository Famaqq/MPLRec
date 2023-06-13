package com.graphhopper.example;

import org.apache.commons.collections.ListUtils;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * KMeans算法的实现
 * 包装成工具类，直接传参数调用即可使用
 */

public class KMeans {
    //聚类的个数
    int cluterNum;
    //数据集中的点
    List<Point> points = new ArrayList<>();
    //簇的中心点
    List<Point> centerPoints = new ArrayList<>();
    //聚类结果的集合簇，key为聚类中心点在centerPoints中的下标，value为该类簇下的数据点
    HashMap<Integer, List<Point>> clusters = new HashMap<>();
    //聚类次数
    int cluter_time_count = 0;
//    double lengthall = 0.0 ;
//    double lengthall_cu = 0.0 ;
//    double flength = 0.0 ;

    public KMeans(String path, int cluterNum){
        this.cluterNum = cluterNum;
        loadData(path);
    }

    //加载数据集
    public void loadData(String path) {
        File file = new File(path);
        int num = -1;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                num++;
                String[] strs = line.split(",");
                points.add(new Point(strs[0], strs[1]));
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化KMeans模型，这里随机选数据集cluterNum个点作为初始中心点
        for (int i = 0; i < cluterNum; i++) {
            int number = (int)(Math.random()*num)+1;
            centerPoints.add(points.get(number));
            clusters.put(i, new ArrayList<>());
        }
    }

    //KMeans聚类
    public void doKMeans(){
        double err = Integer.MAX_VALUE;
        /*当err < 0.01 * cluterNum时，认为当前聚类是合理的，如不满足，则重新聚类（即计算两个新、旧中心的距离，如果任意一个类中心移动的距离大于dis_diff则继续迭代）*/

        /*返回更新的中心点list*/
        List<Point> update_centerPoints = new ArrayList<>();

            //每次聚类前清空原聚类结果的list
            for (int key : clusters.keySet()){
                List<Point> list = clusters.get(key);
                list.clear();
                clusters.put(key, list);
            }
            //计算每个点所属类簇
            for (int i=0; i<points.size(); i++){
                dispatchPointToCluster(points.get(i), centerPoints);
            }
            System.out.println("初始化聚类！");
            update_centerPoints = getClusterCenterPoint(centerPoints, clusters);
//        System.out.println("对比："+!update_centerPoints.retainAll(centerPoints));

               while((update_centerPoints != null)){
                  // System.out.println("开始再一次聚类！");
                    //每次聚类前清空原聚类结果的list
                    for (int key : clusters.keySet()) {
                        List<Point> list = clusters.get(key);
                        list.clear();
                        clusters.put(key, list);
                    }
                    /*根据更新的中心点list，再一次进行聚类*/
                    for (int i = 0; i < points.size(); i++) {
                        dispatchPointToCluster(points.get(i), update_centerPoints);
                    }
                    /*传入再计算一次updat中心点与新聚类中的距离，并返回更新的中心点list*/
                    //update_centerPoints.clear();
                    update_centerPoints = getClusterCenterPoint(update_centerPoints, clusters);

                  // System.out.println("结束再一次聚类！");
                }

            //show(centerPoints, clusters);
            System.out.println("*************************");

        System.out.println("一共迭代聚类了："+cluter_time_count+"次！");
    }

    //计算点对应的中心点，并将该点划分到距离最近的中心点的簇中
    public void dispatchPointToCluster(Point point, List<Point> centerPoints){
        int index = 0;
        double tmpMinDistance = Double.MAX_VALUE;
        for (int i=0; i<centerPoints.size(); i++){
            double distance = calDistance(point, centerPoints.get(i));
            if (distance < tmpMinDistance){
                tmpMinDistance = distance;
                index = i;
            }
        }
        List<Point> list = clusters.get(index);
        list.add(point);
        clusters.put(index, list);
    }

    //计算每个类簇的最优中心点，并返回中心点偏移误差
    public List<Point>  getClusterCenterPoint(List<Point> centerPoints, HashMap<Integer, List<Point>> clusters){
        double error = 0;
        double f_centerX = 0.0;
        double f_centerY = 0.0;
        String[] str = new String[9];

        for (int i=0; i<centerPoints.size(); i++){
            /*初始化时候，随机设置的类中心*/
            Point tmpCenterPoint = centerPoints.get(i);

            System.out.println("tmpCenterPoint中心点，该点坐标为："+tmpCenterPoint.getX()+','+tmpCenterPoint.getY());

            double f_disall = 999999.0 ;

            List<Point> lists = clusters.get(i);
            System.out.println("*************************");
            System.out.println("*************************");
            System.out.println("现在计算第"+i+"个簇的距离最优中心点");

            /*计算出该聚簇中各个点与其他所有点的总和，若是有小于当前中心点的距离总和的，中心点去掉*/
            for (int j=0; j<lists.size(); j++){
                /*更新距离*/
                double disall = 0.0 ;
                /*簇中每个点遍历当一次中心点去计算距离*/
                Point tempc = lists.get(j);
                System.out.println("现在设簇内第"+j+"个点为中心点，该点坐标为："+tempc.getX()+','+tempc.getY());

                System.out.println("现在计算‘假设中心点’与簇内每个点的距离和");
                for (int k=0; k<lists.size(); k++){
                    disall += calDistance(lists.get(k),tempc);
                }
//                lengthall += disall;

                System.out.println("计算出来的距离和为："+disall);
                    /*筛选出最优中心点-Kmediod*/
                    if(disall < f_disall){
                        f_disall = disall;
                        f_centerX = tempc.getX();
                        f_centerY = tempc.getY();
                        System.out.println("该中心点与簇内其他点距离和优于目前距离和，更新中心点为："+f_centerX+','+f_centerY);
                    }
                    else{
                        System.out.println("该点计算出来的距离和不是目前最优，因此无需更新中心点");
                    }
                System.out.println("进入簇内下一轮更新");
                }
//            lengthall_cu += lengthall;
//            lengthall = 0.0;
            System.out.println("第"+i+"簇的距离最优中心点为："+f_centerX+','+f_centerY);
            System.out.println("*************************");
            System.out.println("*************************");

           /*计算两个新、旧类中心的距离，如果任意一个类中心移动的距离大于dis_diff则继续迭代*/
            error += Math.abs(f_centerX - tmpCenterPoint.getX());
            error += Math.abs(f_centerY - tmpCenterPoint.getY());

            centerPoints.set(i, new Point(f_centerX, f_centerY));

//            str[i] =  f_centerY+","+f_centerX;
//            System.out.println(str[i]);
        }

        for( int i = 0 ; i < centerPoints.size() ; i++) {//内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
            System.out.println("各簇的距离最优中心点为:"+centerPoints.get(i).getY()+','+centerPoints.get(i).getX());
        }
//        return error;
        //记住聚类迭代的次数
        cluter_time_count++;


//        if(cluter_time_count == 1){
//            flength = lengthall_cu;
//        }
//        if((lengthall_cu < flength)&&((cluter_time_count != 1))){
//            centerPoints = null;
//        }else{
//            lengthall_cu = 0.0;
//        }


        /*error满足条件，迭代停止*/
        if((error < 0.0001 * cluterNum)&&(cluter_time_count != 1)){
            centerPoints = null;
        }
        return centerPoints;
    }

    //计算点之间的距离，这里计算欧氏距离（不开方）
    public double calDistance(Point point1, Point point2){
        return Math.pow((point1.getX() - point2.getX()), 2) + Math.pow((point1.getY() - point2.getY()), 2);
    }


    //打印簇中心点坐标，及簇中其他点坐标
    public void show(List<Point> centerPoints, HashMap<Integer, List<Point>> clusters){
        for (int i=0; i<centerPoints.size(); i++){
            System.out.print(MessageFormat.format("类{0}的中心点: <{1}, {2}>",(i+1), centerPoints.get(i).getX(), centerPoints.get(i).getY()));
            List<Point> lists = clusters.get(i);
            System.out.print("\t类中成员点有：");
            for (int j=0; j<lists.size(); j++){
                System.out.print("<"+lists.get(j).getX()+ ","+ lists.get(j).getY()+">\t");
            }
            System.out.println();
        }
    }
}
