package com.graphhopper.example;
import java.io.File;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

public class weka_test {

    public static void main(String[] args) throws Exception {

        Classifier m_classifier = new RandomForest();
        File inputFile = new File("C:/Users/fama/Desktop/fama/ml2.arff");//训练语料文件
        ArffLoader atf = new ArffLoader();
        atf.setFile(inputFile);
        Instances instancesTrain = atf.getDataSet(); // 读入训练文件
        instancesTrain.setClassIndex(3);
        m_classifier.buildClassifier(instancesTrain); //训练
        System.out.println(m_classifier);

        // 保存模型
        SerializationHelper.write("LibSVM.model", m_classifier);//参数一为模型保存文件，classifier4为要保存的模型



        // 获取上面保存的模型
        Classifier classifier8 = (Classifier) weka.core.SerializationHelper.read("LibSVM.model");
        double right2 = 0.0f;

        System.out.println(right2);
    }
}
