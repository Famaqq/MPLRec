package com.graphhopper.example;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;
        import java.util.HashMap;
import java.util.List;

public class Offline {


    public static void main(String[] args)  throws Exception{
        Offline geohash = new Offline();
        String s = geohash.encode(30.684264,104.079711);
        System.out.println(s.substring(0,6));
        String search_name = s.substring(0,6);
//        double[] geo = geohash.decode(s);
//        System.out.println(geo[0]+" "+geo[1]);

        // 开始时间
        long stime = System.nanoTime();
        // 执行程序
        readFile(search_name);
        // 结束时间
        long etime = System.nanoTime();
//        System.out.printf("邻近八个区域的geohash："+'\n');
//        for(int k=0;k< getGeoHashBase32For9().size();k++) {
//            System.out.printf(getGeoHashBase32For9().get(k)+'\n');
//        }
        // 计算执行时间
        System.out.printf("执行时长：%f ms.", (etime - stime)/1000000.0);
    }

    private static int numbits = 6 * 5; //经纬度单独编码长度
    //32位编码对应字符
    final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    //定义编码映射关系
    final static HashMap<Character, Integer> lookup = new HashMap<Character, Integer>();
    //初始化编码映射内容
    static {
        int i = 0;
        for (char c : digits)
            lookup.put(c, i++);
    }

    //对编码后的字符串解码
    public double[] decode(String geohash) {
        StringBuilder buffer = new StringBuilder();
        for (char c : geohash.toCharArray()) {

            int i = lookup.get(c) + 32;
            buffer.append( Integer.toString(i, 2).substring(1) );
        }

        BitSet lonset = new BitSet();
        BitSet latset = new BitSet();

        //偶数位，经度
        int j =0;
        for (int i=0; i< numbits*2;i+=2) {
            boolean isSet = false;
            if ( i < buffer.length() )
                isSet = buffer.charAt(i) == '1';
            lonset.set(j++, isSet);
        }

        //奇数位，纬度
        j=0;
        for (int i=1; i< numbits*2;i+=2) {
            boolean isSet = false;
            if ( i < buffer.length() )
                isSet = buffer.charAt(i) == '1';
            latset.set(j++, isSet);
        }

        double lon = decode(lonset, -180, 180);
        double lat = decode(latset, -90, 90);

        return new double[] {lat, lon};
    }

    //根据二进制和范围解码
    private double decode(BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i=0; i<bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i))
                floor = mid;
            else
                ceiling = mid;
        }
        return mid;
    }

    //对经纬度进行编码
    public static String encode(double lat, double lon) {
        BitSet latbits = getBits(lat, -90, 90);
        BitSet lonbits = getBits(lon, -180, 180);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numbits; i++) {
            buffer.append( (lonbits.get(i))?'1':'0');
            buffer.append( (latbits.get(i))?'1':'0');
        }
        return base32(Long.parseLong(buffer.toString(), 2));
    }

    //根据经纬度和范围，获取对应二进制
    private static BitSet getBits(double lat, double floor, double ceiling) {
        BitSet buffer = new BitSet(numbits);
        for (int i = 0; i < numbits; i++) {
            double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    //将经纬度合并后的二进制进行指定的32位编码
    private static String base32(long i) {
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative)
            i = -i;
        while (i <= -32) {
            buf[charPos--] = digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = digits[(int) (-i)];

        if (negative)
            buf[--charPos] = '-';
        return new String(buf, charPos, (65 - charPos));
    }

    public static void readFile(String filename) throws FileNotFoundException {
        String line;
        String search_file = "F://实验数据记录/offline_point/"+filename+".txt";
        File testFile = new File(search_file);
        if(!testFile.exists()) {
            System.out.println(testFile.getName() + " isn't existed");
            throw new FileNotFoundException(testFile.getName() + " is not found");
        }
        // 使用try-resource方式
        try (FileReader fileReader = new FileReader(testFile); BufferedReader br = new BufferedReader(fileReader)) {
            line = br.readLine();
            while(line != null) {
                System.out.println(line);
                // Notice: the following statement is necessary.
                line = br.readLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static List<String> getGeoHashBase32For9() {
       double searchlng = 104.0944784;
       double searchlat =  30.6653857;
       double minLat = 180;
       double minLng = 360;
        for (int i = 0; i < 15; i++) {
            minLat /= 2.0;
        }

        for (int i = 0; i < 15; i++) {
            minLng /= 2.0;
        }
        double leftLat = searchlat - minLat;
        double rightLat = searchlat + minLat;
        double upLng = searchlng - minLng;
        double downLng = searchlng + minLng;
        List<String> base32For9 = new ArrayList<String>();
        //左侧从上到下 3个
        String leftUp = encode(leftLat, upLng).substring(0,6);
        if (!(leftUp == null || "".equals(leftUp))) {
            base32For9.add(leftUp);
        }
        String leftMid = encode(leftLat, searchlng).substring(0,6);
        if (!(leftMid == null || "".equals(leftMid))) {
            base32For9.add(leftMid);
        }
        String leftDown = encode(leftLat, downLng).substring(0,6);
        if (!(leftDown == null || "".equals(leftDown))) {
            base32For9.add(leftDown);
        }
        //中间从上到下 3个
        String midUp = encode(searchlat, upLng).substring(0,6);
        if (!(midUp == null || "".equals(midUp))) {
            base32For9.add(midUp);
        }
        String midMid = encode(searchlat, searchlng).substring(0,6);
        if (!(midMid == null || "".equals(midMid))) {
            base32For9.add(midMid);
        }
        String midDown = encode(searchlat, downLng).substring(0,6);
        if (!(midDown == null || "".equals(midDown))) {
            base32For9.add(midDown);
        }
        //右侧从上到下 3个
        String rightUp = encode(rightLat, upLng).substring(0,6);
        if (!(rightUp == null || "".equals(rightUp))) {
            base32For9.add(rightUp);
        }
        String rightMid = encode(rightLat, searchlng).substring(0,6);
        if (!(rightMid == null || "".equals(rightMid))) {
            base32For9.add(rightMid);
        }
        String rightDown = encode(rightLat, downLng).substring(0,6);
        if (!(rightDown == null || "".equals(rightDown))) {
            base32For9.add(rightDown);
        }
        return base32For9;
    }
}