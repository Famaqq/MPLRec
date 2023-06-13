package com.graphhopper.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class update_search {
    private final String url = "jdbc:postgresql://localhost:5432/cdosm";
    private final String user = "postgres";
    private final String password = "880123";

//    private static final String QUERY2 = " With CTE AS (SELECT *,TRUNC(ST_Distance(ST_GeomFromText(?,4326), geom  )*100000)::integer as distance FROM allwaynodes ORDER BY geom <-> ST_GeomFromText('POINT(104.039919 30.660976)', 4326) LIMIT 500\n" +
//            ")SELECT * FROM CTE where distance < 500";
    private static final String SELECT_ALL_QUERY = "with st as (SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2m union \n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2t union \n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2v union \n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2q union \n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2w union\n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2y union\n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2r union\n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2x union\n" +
        "\t   SELECT oddegrees,otime_slice,near_fid_o,weekend FROM wm6n2z \n" +
        "\t  )\n" +
        ",s as\n" +
        "(\n" +
        "SELECT oddegrees,otime_slice,near_fid_o,weekend FROM st where near_fid_o = any (\n" +
        "\tarray(\n" +
        "\t\tSELECT gid FROM link_half_point WHERE ST_Intersects(link_half_point.point_geom,\n" +
        "\t\tST_Buffer(ST_GeomFromText('POINT(104.08149 30.66693)',4326), 0.004)) group by gid\n" +
        "\t)\n" +
        "\t)\n" +
        "),\n" +
        "ss as(select oddegrees,near_fid_o from s where weekend = 0 and otime_slice BETWEEN 59 and 61 ),\n" +
        "sss as(select near_fid_o from ss where (ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT(104.08149 30.66693)',4326), ST_GeomFromText('POINT(104.1007867 30.6832882)',4326))) - ss.oddegrees) < 30)  or\n" +
        "   ABS(degrees(ST_Azimuth(ST_GeomFromText('POINT(104.08149 30.66693)',4326), ST_GeomFromText('POINT(104.1007867 30.6832882)',4326))) - ss.oddegrees) > 330 )\n" +
        "select near_fid_o,count(near_fid_o) from sss group by near_fid_o";

    public void getUserById() {
        // Step 1: Establishing a Connection
        try (Connection connection = DriverManager.getConnection(url, user, password);
             // Step 2:Create a statement using connection object
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_QUERY);) {
            // String point_input = "POINT(104.039919 30.660976)";
            String point_input = "POINT("+"104.039919"+" "+"30.660976"+")";
//            preparedStatement.setString(1, point_input);
//            System.out.println(preparedStatement);
            // Step 3: Execute the query or update query
            ResultSet rs = preparedStatement.executeQuery();
            int i = 0;
            List nodes_pointList = new ArrayList();
            // Step 4: Process the ResultSet object.
            while (rs.next()) {
                String near_fid_o = rs.getString("near_fid_o");
                String count = rs.getString("count");
                System.out.println(near_fid_o+','+count);
//                i++;
//                nodes_pointList.add(near_fid_o+','+count);
            }
//            System.out.println("nodes_pointList_num:"+i);
//            for (int k = 0; k < nodes_pointList.size(); k++) {
//                System.out.println(nodes_pointList.get(k));
//            }
        } catch (SQLException e) {
            printSQLException(e);
        }
    }


    public static void main(String[] args) {

        // 执行程序
        update_search example = new update_search();
        // 开始时间
        long stime = System.currentTimeMillis();
        example.getUserById();
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d ms.", (etime - stime));
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
