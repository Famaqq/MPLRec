package com.graphhopper.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class postgresql_link_tran {
    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";

//    private static final String QUERY2 = " select ST_AsText(geom)from edges where gid= 3";
    //private static final String QUERY2 = "select * from osm_road where id= 0";



    private static final String QUERY2 = "select * from osm_road where id= ?";
    public String link_MULTILINESTRING ="";
    public String link_LINESTRING ="";
    public void getUserById(int id) {

        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY2);) {
            preparedStatement.setInt(1, id);
            //System.out.println(preparedStatement);
            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                link_LINESTRING = rs.getString("road");
                link_MULTILINESTRING = link_LINESTRING.replaceAll("LINESTRING\\(","MULTILINESTRING((");
                link_MULTILINESTRING = link_MULTILINESTRING.replaceAll("\\)","))");
                link_LINESTRING = "SRID=4326;"+link_LINESTRING;
            }

        } catch (SQLException e) {
            printSQLException(e);
        }
    }



//    String QUERY3 = "select *,ST_AsText(T.road_geom),ST_Length(T.road_geom,false)  from (SELECT id,road_geom,ST_DWithin(ST_GeographyFromText(?), road_geom,0.5)as a from osm_road) T where T.a = 'true'and abs(ST_Length(T.road_geom,false) - ST_Length(st_geometryfromtext(?,4326),false) ) < 2.0";
String QUERY3 = "select *,ST_AsText(T.geom),ST_Length(T.geom,false)  from (SELECT gid,geom,ST_DWithin(ST_GeographyFromText(?), geom,0.5)as a from edges) T where T.a = 'true' and abs(ST_Length(T.geom,false) - ST_Length(st_geometryfromtext(?,4326),false) ) < 5.0";

    public void getUserById3(int id) {
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY3);) {

            preparedStatement.setString(1, link_LINESTRING);
            preparedStatement.setString(2, link_MULTILINESTRING);
           // System.out.println(preparedStatement);
            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            int rowCount = 0;

            while(rs.next())

            {
                rowCount++;

            }
           // System.out.println("rowCount:"+rowCount);
//            if(rowCount != 0){
//                File newcsv=new File("D://zhq/fmm-edge_osm(2)gz_tran.csv");
//                BufferedWriter bw=new BufferedWriter (new FileWriter(newcsv,true));
//                //将规划好的距离数据写入csv
//                bw.write(id + "," + "999999"+ "," + "999999"+ "," + "999999");
//
//                bw.newLine();//换行
//                bw.close();
//            }
            if(rowCount > 1){
                System.out.println("多个返回值"+id);
            }
            if(rowCount == 1) {
                ResultSet rs2 = preparedStatement.executeQuery();
                while (rs2.next()) {
                    String line_id = rs2.getString("gid");
                    String line_st_astext = rs2.getString("st_astext");
                    String line_st_length = rs2.getString("st_length");

                    File newcsv = new File("D://zhq/fmm-edge_osm(2)gz_tran.csv");
                    BufferedWriter bw = new BufferedWriter(new FileWriter(newcsv, true));
                    //将规划好的距离数据写入csv
                    bw.write(id + "," + line_id + "," + line_st_astext + "," + line_st_length);

                    bw.newLine();//换行
                    bw.close();
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        postgresql_link_tran example = new postgresql_link_tran();
        int i = 0;
        for(i=0;i<6120;i++){
            example.getUserById(i);
            example.getUserById3(i);
            System.out.println(i);
        }


        //example.getAllUsers();
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
}