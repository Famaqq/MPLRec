//package com.graphhopper.example;
//
//import smile.classification.SVM;
//        import smile.math.kernel.GaussianKernel;
//
//        import java.io.BufferedReader;
//        import java.io.File;
//        import java.io.FileReader;
//        import java.util.ArrayList;
//        import java.util.Arrays;
//        import java.util.List;
//
///**
// * Created by zhanghuayan on 2017/1/16.
// */
//public class m2c_test {
//
//    public static void main(String[] args) throws Exception {
//
//        List<List<Double>> datas = new ArrayList<List<Double>>();
//        List<Double> data = new ArrayList<Double>();
//        List<Integer> labels = new ArrayList<Integer>();
//
//        String line;
//        List<String> lines;
//        File file = new File("C://Users/fama/Desktop/fama/iris.txt");
//        BufferedReader reader = new BufferedReader(new FileReader(file));
//        while ((line = reader.readLine()) != null) {
//            lines = Arrays.asList(line.trim().split(","));
//            for (int i = 0; i < lines.size() - 1; i++) {
//                data.add(Double.parseDouble(lines.get(i)));
//            }
//            labels.add(Integer.parseInt(lines.get(lines.size() - 1)));
//
//            datas.add(data);
//            data = new ArrayList<Double>();
//
//        }
//
//        //转换label
//        int[] label = new int[labels.size()];
//        for (int i = 0; i < label.length; i++) {
//            label[i] = labels.get(i);
//        }
//
//        //转换属性
//        int rows = datas.size();
//        int cols = datas.get(0).size();
//        double[][] srcData = new double[rows][cols];
//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
//                srcData[i][j] = datas.get(i).get(j);
//            }
//        }
//
//        SVM<double[]> svm = new SVM<double[]>(new GaussianKernel(1.0), 1.0, 3, SVM.Multiclass.ONE_VS_ALL);
//        svm.learn(srcData, label);
//        svm.finish();
//
//        double right = 0;
//        for (int i = 0; i < srcData.length; i++) {
//            int tag = svm.predict(srcData[i]);
//            if (tag == label[i]) {
//                right += 1;
//            }
//        }
//        right = right / srcData.length;
//
//        System.out.println("Accrurate: " + right * 100 + "%");
//
//    }
//}