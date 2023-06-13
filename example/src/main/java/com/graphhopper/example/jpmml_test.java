package com.graphhopper.example;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.support_vector_machine.VoteDistribution;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  分类模型测试
 */
 public class jpmml_test {

    /**
     *  加载模型
     */
    private Evaluator loadPmml() {
        PMML pmml = new PMML();
        InputStream inputStream = null;

        //注释这段是可以将pmml当成字符串传参,封装接口的时候就不用传pmml文件路径了
        //   try {
        //        inputStream = new ByteArrayInputStream(pmml.getBytes("utf-8"));
        //    } catch (
        //           IOException e) {
        //       e.printStackTrace();
        //   }
        try {
            inputStream = new FileInputStream("D:/JTest/recommend_osm_points/example/src/main/resources/rfc2.pmml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (inputStream == null) {
            return null;
        }
        InputStream is = inputStream;
        try {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (JAXBException e1) {
            e1.printStackTrace();
        } finally {
            //关闭输入流
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        return evaluator;
    }
    /**
     *  分类预测
     */
        private Double predict(Evaluator evaluator,Double a, Double b, Double c, Double d) {
            //输入特征赋值,iris数据类型是4维,数据维度顺序不能乱
            Map<String, Double> data = new HashMap<String, Double>();
            data.put("x1", a);
            data.put("x2", b);
            data.put("x3", c);
            data.put("x6", d);
            List<InputField> inputFields = evaluator.getInputFields();
            //构造模型输入
            Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
            for (InputField inputField : inputFields) {
                FieldName inputFieldName = inputField.getName();
                Object rawValue = data.get(inputFieldName.getValue());
                FieldValue inputFieldValue = inputField.prepare(rawValue);
                arguments.put(inputFieldName, inputFieldValue);
            }

            Map<FieldName, ?> results = evaluator.evaluate(arguments);
            List<TargetField> targetFields = evaluator.getTargetFields();

            TargetField targetField = targetFields.get(0);
            FieldName targetFieldName = targetField.getName();

            Object targetFieldValue = results.get(targetFieldName);
            System.out.println("target: " + targetFieldName.getValue() + " value: " + targetFieldValue);
            Double primitiveValue = -1.0;
            if (targetFieldValue instanceof Computable) {
                Computable computable = (Computable) targetFieldValue;
                primitiveValue = (Double)computable.getResult();
            }
//        System.out.println(a + " " + b + " " + c + " " + d + ":" + primitiveValue);
            return primitiveValue;
        }

//    public static void main(String args[]){
//        jpmml_test demo = new jpmml_test();
//        Evaluator model = demo.loadPmml();
//        Map<String, Double> data = new HashMap<String, Double>();
//        // 组合特征向量
//        data.put("x1", 2687.0);
//        data.put("x2", 4497.0);
//        data.put("x3", 48.0);
//        data.put("x6", 1122.038518);
//        demo.predict(model,data);
//    }
        public static void main(String args[]){
            jpmml_test demo = new jpmml_test();
            Evaluator model = demo.loadPmml();
//            System.out.println(demo.predict(model,2687.0,4497.0,48.0, 1122.038518));
            System.out.println(demo.predict(model,2908.0,218.0,60.0, 9191.114718));
            System.out.println(demo.predict(model,1944.0,263.0,51.0, 3490.915989));
        }
}
