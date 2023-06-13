package com.graphhopper.example;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.OSMSurfaceParser;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.reader.*;
import java.util.Arrays;
import java.util.Locale;

import static com.graphhopper.json.Statement.If;
import static com.graphhopper.json.Statement.Op.LIMIT;
import static com.graphhopper.json.Statement.Op.MULTIPLY;

public class RoutingExample {
    public static void main(String[] args) {
        String relDir = args.length == 1 ? args[0] : "";
        GraphHopper hopper = createGraphHopperInstance(relDir + "core/files/output.osm.pbf");

        System.out.println(hopper.getLocationIndex().findClosest(30.7145002151176 ,104.101136944116, EdgeFilter.ALL_EDGES));
        System.out.println(hopper.getOSMFile());
        GraphHopperStorage graph = hopper.getGraphHopperStorage();
        AllEdgesIterator iter = graph.getAllEdges();
        for(int i = 0;i<6723;i++) {
            iter.next();
            System.out.println(iter.getEdge()+':'+iter.getName()+':'+iter.fetchWayGeometry(FetchMode.ALL));
        }
//        GraphHopperStorage graph = hopper.getGraphHopperStorage();
//        //返回图的node数目：graph.getNodes()
//        // 返回图edge数目：graph.getEdges()
//        System.out.println(graph.getNodes());
//        System.out.println(graph.getEdges());
//        AllEdgesIterator iter = graph.getAllEdges();
//        iter.next();
//System.out.println(iter.getName());
//        System.out.println(iter.getEdge());
//       // System.out.println(graph.getEdgeIteratorState().);
//        System.out.println(iter.toString());
//        assertEquals(1, iter.getAdjNode());
//        assertEquals(51.21, graph.getNodeAccess().getLat(0), 1.e-3);
//        assertEquals(9.41, graph.getNodeAccess().getLon(0), 1.e-3);
//        assertEquals(51.22, graph.getNodeAccess().getLat(1), 1.e-3);
//        assertEquals(9.42, graph.getNodeAccess().getLon(1), 1.e-3);
//        assertEquals(DistanceCalcEarth.DIST_EARTH.calcDistance(iter.fetchWayGeometry(FetchMode.ALL)), iter.getDistance(), 1.e-3);
//        assertEquals(1312.1, iter.getDistance(), 1.e-1);
//        assertEquals(1312.1, DistanceCalcEarth.DIST_EARTH.calcDistance(iter.fetchWayGeometry(FetchMode.ALL)), 1.e-1);
//        assertFalse(iter.next());


//        GraphHopperStorage graph = hopper.getGraphHopperStorage();
       //routing(hopper);
        //speedModeVersusFlexibleMode(hopper);
        //headingAndAlternativeRoute(hopper);

        //customizableRouting(relDir + "core/files/chengdu.osm.pbf");


        // release resources to properly shutdown or start a new instance
        hopper.close();
    }


    private LongIntMap osmNodeIdToInternalNodeMap;
    protected LongIntMap getNodeMap() {
        return osmNodeIdToInternalNodeMap;
    }

    static GraphHopper createGraphHopperInstance(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        // specify where to store graphhopper files
        hopper.setGraphHopperLocation("target/routing-graph-cache7");

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("shortest").setTurnCosts(false));

        // this enables speed mode for the profile we called car
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));

        // now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
        hopper.importOrLoad();
        return hopper;
    }

    public static void routing(GraphHopper hopper) {
        // simple configuration of the request object
        GHRequest req = new GHRequest(30.68601735,104.0859389

                ,30.66182851,104.0732662).
                // note that we have to specify which profile we are using even when there is only one like here
                        setProfile("car").
                // define the language for the turn instructions
                        setLocale(Locale.SIMPLIFIED_CHINESE);
        GHResponse rsp = hopper.route(req);

        //System.out.println("rsp:" + rsp);

        // handle errors
        if (rsp.hasErrors())
            throw new RuntimeException(rsp.getErrors().toString());

        // use the best path, see the GHResponse class for more possibilities.
        ResponsePath path = rsp.getBest();
        //System.out.println("path:" + path);
        // points, distance in meters and time in millis of the full path
        PointList pointList = path.getPoints();
        double distance = path.getDistance();
        long timeInMs = path.getTime();

        double fullWeight = path.getRouteWeight();
        System.out.println("pointList:" + pointList);
        System.out.println("distance:" + distance);
        System.out.println("timeInMs:" + timeInMs);
        System.out.println("fullWeight:" + fullWeight);


        Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
        InstructionList il = path.getInstructions();
        // iterate over all turn instructions
        double dis = 0.00;
        for (Instruction instruction : il) {
            System.out.println("distance " + instruction.getDistance() + " for instruction: " + instruction.getTurnDescription(tr));
            dis = instruction.getDistance() + dis;
        }
        System.out.println("ALL distance " + dis);
        assert il.size() == 6;
        assert Helper.round(path.getDistance(), -2) == 900;
    }

    public static void speedModeVersusFlexibleMode(GraphHopper hopper) {
        GHRequest req = new GHRequest(42.508552, 1.532936, 42.507508, 1.528773).
                setProfile("car").setAlgorithm(Parameters.Algorithms.ASTAR_BI).putHint(Parameters.CH.DISABLE, true);
        GHResponse res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());
        assert Helper.round(res.getBest().getDistance(), -2) == 900;
    }

    public static void headingAndAlternativeRoute(GraphHopper hopper) {
        // define a heading (direction) at start and destination
        GHRequest req = new GHRequest().setProfile("car").
                addPoint(new GHPoint(42.508774, 1.535414)).addPoint(new GHPoint(42.506595, 1.528795)).
                setHeadings(Arrays.asList(180d, 90d)).
                // use flexible mode (i.e. disable contraction hierarchies) to make heading and pass_through working
                        putHint(Parameters.CH.DISABLE, true);
        // if you have via points you can avoid U-turns there with
        // req.getHints().putObject(Parameters.Routing.PASS_THROUGH, true);
        GHResponse res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());
        assert res.getAll().size() == 1;
        assert Helper.round(res.getBest().getDistance(), -2) == 800;

        // calculate potential alternative routes to the current one (supported with and without CH)
        req = new GHRequest().setProfile("car").
                addPoint(new GHPoint(30.666714,104.059959)).addPoint(new GHPoint(30.655596,104.050121)).
                setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
        req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 3);
        res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());
        assert res.getAll().size() == 2;
        assert Helper.round(res.getBest().getDistance(), -2) == 1600;
    }

    public static void customizableRouting(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/routing-custom-graph-cache");
        hopper.setProfiles(new CustomProfile("car_custom").setCustomModel(new CustomModel()).setVehicle("car"));

        // The hybrid mode uses the "landmark algorithm" and is up to 15x faster than the flexible mode (Dijkstra).
        // Still it is slower than the speed mode ("contraction hierarchies algorithm") ...
        hopper.getLMPreparationHandler().setLMProfiles(new LMProfile("car_custom"));
        hopper.importOrLoad();

        // ... but for the hybrid mode we can customize the route calculation even at request time:
        // 1. a request with default preferences
        GHRequest req = new GHRequest().setProfile("car_custom").
                addPoint(new GHPoint(30.667438,104.059904)).addPoint(new GHPoint(30.668721,104.055333)).addPoint(new GHPoint(30.669062,104.049486));

        GHResponse res = hopper.route(req);
        System.out.println("客制化路线:" + res);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());

        ResponsePath path = res.getBest();
        System.out.println("path:" + path);
        // points, distance in meters and time in millis of the full path
        PointList pointList = path.getPoints();
        double distance = path.getDistance();
        System.out.println("distance:" + distance);

        /*assert Math.round(res.getBest().getTime() / 1000d) == 96;

        // 2. now avoid primary roads and reduce maximum speed, see docs/core/custom-models.md for an in-depth explanation
        // and also the blog posts https://www.graphhopper.com/?s=customizable+routing
        CustomModel model = new CustomModel();
        model.addToPriority(If("road_class == PRIMARY", MULTIPLY, 0.5));

        // unconditional limit to 100km/h
        model.addToPriority(If("true", LIMIT, 100));

        req.setCustomModel(model);
        res = hopper.route(req);
        System.out.println("客制化路线二:" + res);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());

        assert Math.round(res.getBest().getTime() / 1000d) == 165;*/


    }



}
