package com.graphhopper.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class postgresqlTest {
    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";

    public void getUserById() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY = "COPY (SELECT olat,olon,dlat,dlng,near_fid_o,near_fid,wtime,otime_slice,dtime_slice,dur_time,day,week,weekend,oddegrees FROM his_point where ogeohash ='"+geohash_arr[i]+
                    "' and oddegrees is not null"+" and wtime is not null"+") TO 'E:/PostgreSQL/"+geohash_arr[i]+".csv' (format csv, delimiter ',')";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }

    public void createtable() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY ="CREATE TABLE "+geohash_arr[i]+
                    " (\n" +
                    "    olat double precision,\n" +
                    "    olon double precision,\n" +
                    "    dlat double precision,\n" +
                    "    dlng double precision,\n" +
                    "    near_fid_o integer,\n" +
                    "    near_fid integer,\n" +
                    "    wtime double precision,\n" +
                    "    otime_slice integer,\n" +
                    "    dtime_slice integer,\n" +
                    "    dur_time integer,\n" +
                    "    day integer,\n" +
                    "    week integer,\n" +
                    "    weekend integer,\n" +
                    "    oddegrees double precision\n" +
                    ")";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
//                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }
    public void TRUNCATE() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY ="DROP TABLE "+ geohash_arr[i] ;
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }

    public void updatetable() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY ="copy "+ geohash_arr[i] + " from 'E:/PostgreSQL/"+geohash_arr[i]+".csv' delimiter ','";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }

    public void index() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY ="CREATE INDEX "+ geohash_arr[i] + "_t_index ON "+ geohash_arr[i] + " (otime_slice)";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        postgresqlTest example = new postgresqlTest();
 //      example.getUserById();
 //       example.createtable();
 //       example.TRUNCATE();
//       example.updatetable();
//        example.index();
//        example.split_time();
//        example.create_split_time();
 //       example.insert_split_time();
      //  example.add_index();
      //  example.add_ogeom_geohash();
        //example.ALTER_ogeom_geohash();
        example.create_ogeom_geohash_index();
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

    public void split_time() {

        /*按照时间片划分查询，同时筛选轨迹距离大于1000m的，保存至本地*/


        int[] splittime1 = {35,43,51,59,67,75,83};
        int[] splittime2 = {44,52,60,68,76,84,96};
        String[] day = {"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
        String QUERY = "";
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 7; i++) {
                String filename = day[j] + "_" + splittime1[i] + "_" + splittime2[i];
                QUERY = "COPY (select * from his_trj_monday where " +
                        " time_slot > " + splittime1[i] +
                        " and time_slot <= " + splittime2[i] +
                        " and (st_length(ogeom)*100000 )>1000) TO 'E:/PostgreSQL/his_trj_" +
                        filename + ".csv' (format csv, delimiter ',')";
                System.out.println(QUERY);
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(filename+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
            }
        }
    }

    public void create_split_time() {
        int[] splittime1 = {35,43,51,59,67,75,83};
        int[] splittime2 = {44,52,60,68,76,84,96};
        String[] day = {"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
        String QUERY = "";
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 7; i++) {
                String filename = day[j] + "_" + splittime1[i] + "_" + splittime2[i];
                QUERY = "CREATE TABLE " + filename + "( idx integer,id integer,cpath text,mgeom text,time_slot integer,day integer,week integer,weekend integer,ogeom geometry)" ;
                System.out.println(QUERY);
                // 开始时间
                long stime = System.currentTimeMillis();
                // Step 1: Establishing a Connection
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     // Step 2:Create a statement using connection object
                     PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                    // Step 3: Execute the query or update query
                    preparedStatement.executeQuery();
                } catch (SQLException e) {
                    printSQLException(e);
                }
                // 结束时间
                long etime = System.currentTimeMillis();
                // 计算执行时间
                System.out.printf(filename+"：查询执行时长：%d ms.", (etime - stime));
                System.out.println();
            }
        }
    }

    public void insert_split_time() {
        int[] splittime1 = {35,43,51,59,67,75,83};
        int[] splittime2 = {44,52,60,68,76,84,96};
        String[] day = {"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
        String QUERY = "";
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 7; i++) {
                String filename = day[j] + "_" + splittime1[i] + "_" + splittime2[i];
                /*导入数据*/

                QUERY = " copy "+ filename + " from 'E:/PostgreSQL/his_trj_" + filename + ".csv' delimiter ',' csv quote as '\"'  ";
                System.out.println(QUERY);
                // 开始时间
                long stime = System.currentTimeMillis();
                // Step 1: Establishing a Connection
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     // Step 2:Create a statement using connection object
                     PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                    // Step 3: Execute the query or update query
                    preparedStatement.executeQuery();
                } catch (SQLException e) {
                    printSQLException(e);
                }
                // 结束时间
                long etime = System.currentTimeMillis();
                // 计算执行时间
                System.out.printf(filename+"：查询执行时长：%d ms.", (etime - stime));
                System.out.println();
            }
        }
    }

    public void add_index() {
        int[] splittime1 = {35,43,51,59,67,75,83};
        int[] splittime2 = {44,52,60,68,76,84,96};
        String[] day = {"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};
        String QUERY = "";
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 7; i++) {
                String filename = day[j] + "_" + splittime1[i] + "_" + splittime2[i];
                /*导入数据*/
                QUERY = " CREATE INDEX his_trj_"+ filename + "_index ON "+ filename + " (time_slot)";
                System.out.println(QUERY);
                // 开始时间
                long stime = System.currentTimeMillis();
                // Step 1: Establishing a Connection
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     // Step 2:Create a statement using connection object
                     PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                    // Step 3: Execute the query or update query
                    preparedStatement.executeQuery();
                } catch (SQLException e) {
                    printSQLException(e);
                }
                // 结束时间
                long etime = System.currentTimeMillis();
                // 计算执行时间
                System.out.printf(filename+"：查询执行时长：%d ms.", (etime - stime));
                System.out.println();
            }
        }
    }

    public void add_ogeom_geohash() {
            String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                    "wm6n8h","wm6n2v","wm6n8f"};

            String QUERY = "";
            for (int i = 0; i < 130; i++) {
                QUERY ="ALTER TABLE "+ geohash_arr[i] + " ADD COLUMN ogeom geometry";
                // 开始时间
                long stime = System.currentTimeMillis();
                // Step 1: Establishing a Connection
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     // Step 2:Create a statement using connection object
                     PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                    // Step 3: Execute the query or update query
                    preparedStatement.executeQuery();
                } catch (SQLException e) {
                    printSQLException(e);
                }
                // 结束时间
                long etime = System.currentTimeMillis();
                // 计算执行时间
                System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
                System.out.println();
            }
        }

    public void ALTER_ogeom_geohash() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
            QUERY = "update "+ geohash_arr[i] + " SET ogeom= ST_GeomFromText (CONCAT( 'POINT(',olon, ' ', olat, ')' ),4326)";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }

    public void create_ogeom_geohash_index() {
        String[] geohash_arr = {"wm6n8z","wm6n86","wm3yxu","wm6n85","wm6n8t","wm6n83","wm6n9k",
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
                "wm6n8h","wm6n2v","wm6n8f"};

        String QUERY = "";
        for (int i = 0; i < 130; i++) {
           // QUERY = "create index "+ geohash_arr[i] + "_oidx on  "+ geohash_arr[i] + "  using GIST(ogeom)";
            QUERY ="CREATE INDEX "+ geohash_arr[i] + "_oddegrees_index ON "+ geohash_arr[i] + " (oddegrees)";
            // 开始时间
            long stime = System.currentTimeMillis();
            // Step 1: Establishing a Connection
            try (Connection connection = DriverManager.getConnection(url, user, password);
                 // Step 2:Create a statement using connection object
                 PreparedStatement preparedStatement = connection.prepareStatement(QUERY);) {
                // Step 3: Execute the query or update query
                preparedStatement.executeQuery();
            } catch (SQLException e) {
                printSQLException(e);
            }
            // 结束时间
            long etime = System.currentTimeMillis();
            // 计算执行时间
            System.out.printf(geohash_arr[i]+"：查询执行时长：%d ms.", (etime - stime));
            System.out.println();
        }
    }
}