
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.List;

import static spark.Spark.*;

/* Maven is used to pull in these dependencies. */

/**
 * This MapServer class is the entry point for running the JavaSpark web server for the BearMaps
 * application project, receiving API calls, handling the API call processing, and generating
 * requested images and routes.
 * @author Alan Yao
 */
public class MapServer {
    /**
     * The root upper left/lower right longitudes and latitudes represent the bounding box of
     * the root tile, as the images in the img/ folder are scraped.
     * Longitude == x-axis; latitude == y-axis.
     */
    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    /** Each tile is 256x256 pixels. */
    public static final int TILE_SIZE = 256;
    /** HTTP failed response. */
    private static final int HALT_RESPONSE = 403;
    /** Route stroke information: typically roads are not more than 5px wide. */
    public static final float ROUTE_STROKE_WIDTH_PX = 5.0f;
    /** Route stroke information: Cyan with half transparency. */
    public static final Color ROUTE_STROKE_COLOR = new Color(108, 181, 230, 200);
    /** The tile images are in the IMG_ROOT folder. */
    private static final String IMG_ROOT = "img/";

    public static double querypixelsize;
    public static double screenwidth;
    public static double  upperLongitude ;
    public static double lowerLongitude;
    public static double upperLatitude;
    public static double lowerLatitude;
    public static double height;
    public static double depth = 0;
    public static double width = 0;
    private static Node start = null;
    private static Node end = null;
    private static LinkedList<Long> pathway = new LinkedList<>();
    private static int widthofimage;
    private static int  heightofimage;


    /**
     * The OSM XML file path. Downloaded from <a href="http://download.bbbike.org/osm/">here</a>
     * using custom region selection.
     **/
    private static final String OSM_DB_PATH = "berkeley.osm";
    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside getMapRaster(). <br>
     * ullat -> upper left corner latitude,<br> ullon -> upper left corner longitude, <br>
     * lrlat -> lower right corner latitude,<br> lrlon -> lower right corner longitude <br>
     * w -> user viewport window width in pixels,<br> h -> user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
        "lrlon", "w", "h"};
    /**
     * Each route request to the server will have the following parameters
     * as keys in the params map.<br>
     * start_lat -> start point latitude,<br> start_lon -> start point longitude,<br>
     * end_lat -> end point latitude, <br>end_lon -> end point longitude.
     **/
    private static final String[] REQUIRED_ROUTE_REQUEST_PARAMS = {"start_lat", "start_lon",
        "end_lat", "end_lon"};
    /* Define any static variables here. Do not define any instance variables of MapServer. */
    private static GraphDB g;

    /**
     * Place any initialization statements that will be run before the server main loop here.
     * Do not place it in the main function. Do not place initialization code anywhere else.
     * This is for testing purposes, and you may fail tests otherwise.
     **/
    public static void initialize() {
        g = new GraphDB(OSM_DB_PATH);
    }

    public static void main(String[] args) {
        initialize();
        staticFileLocation("/page");
        /* Allow for all origin requests (since this is not an authenticated server, we do not
         * care about CSRF).  */
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });

        /* Define the raster endpoint for HTTP GET requests. I use anonymous functions to define
         * the request handlers. */
        get("/raster", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_RASTER_REQUEST_PARAMS);
            /* The png image is written to the ByteArrayOutputStream */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            /* getMapRaster() does almost all the work for this API call */
            Map<String, Object> rasteredImgParams = getMapRaster(params, os);
            /* On an image query success, add the image data to the response */
            if (rasteredImgParams.containsKey("query_success")
                    && (Boolean) rasteredImgParams.get("query_success")) {
                String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
                rasteredImgParams.put("b64_encoded_image_data", encodedImage);
            }
            /* Encode response to Json */
            Gson gson = new Gson();
            return gson.toJson(rasteredImgParams);
        });

        /* Define the routing endpoint for HTTP GET requests. */
        get("/route", (req, res) -> {
            HashMap<String, Double> params =
                    getRequestParams(req, REQUIRED_ROUTE_REQUEST_PARAMS);
            LinkedList<Long> route = findAndSetRoute(params);
            return !route.isEmpty();
        });

        /* Define the API endpoint for clearing the current route. */
        get("/clear_route", (req, res) -> {
            clearRoute();
            return true;
        });

        /* Define the API endpoint for search */
        get("/search", (req, res) -> {
            Set<String> reqParams = req.queryParams();
            String term = req.queryParams("term");
            Gson gson = new Gson();
            /* Search for actual location data. */
            if (reqParams.contains("full")) {
                List<Map<String, Object>> data = getLocations(term);
                return gson.toJson(data);
            } else {
                /* Search for prefix matching strings. */
                List<String> matches = getLocationsByPrefix(term);
                return gson.toJson(matches);
            }
        });

        /* Define map application redirect */
        get("/", (request, response) -> {
            response.redirect("/map.html", 301);
            return true;
        });
    }

    /**
     * Validate & return a parameter map of the required request parameters.
     * Requires that all input parameters are doubles.
     * @param req HTTP Request
     * @param requiredParams TestParams to validate
     * @return A populated map of input parameter to it's numerical value.
     */
    private static HashMap<String, Double> getRequestParams(
            spark.Request req, String[] requiredParams) {
        Set<String> reqParams = req.queryParams();
        HashMap<String, Double> params = new HashMap<>();
        for (String param : requiredParams) {
            if (!reqParams.contains(param)) {
                halt(HALT_RESPONSE, "Request failed - parameters missing.");
            } else {
                try {
                    params.put(param, Double.parseDouble(req.queryParams(param)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    halt(HALT_RESPONSE, "Incorrect parameters - provide numbers.");
                }
            }
        }
        return params;
    }


    /**
     * Handles raster API calls, queries for tiles and rasters the full image. <br>
     * <p>
     *     The rastered photo must have the following properties:
     *     <ul>
     *         <li>Has dimensions of at least w by h, where w and h are the user viewport width
     *         and height.</li>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *         <li>If a current route exists, lines of width ROUTE_STROKE_WIDTH_PX and of color
     *         ROUTE_STROKE_COLOR are drawn between all nodes on the route in the rastered photo.
     *         </li>
     *     </ul>
     *     Additional image about the raster is returned and is to be included in the Json response.
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query bounding box and
     *               the user viewport width and height.
     * @param os     An OutputStream that the resulting png image should be written to.
     * @return A map of parameters for the Json response as specified:
     * "raster_ul_lon" -> Double, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Double, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Double, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Double, the bounding lower right latitude of the rastered image <br>
     * "raster_width"  -> Double, the width of the rastered image <br>
     * "raster_height" -> Double, the height of the rastered image <br>
     * "depth"         -> Double, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image string. <br>
     * "query_success" -> Boolean, whether an image was successfully rastered. <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public static Map<String, Object> getMapRaster(Map<String, Double> params, OutputStream os) {
        HashMap<String, Object> rasteredImageParams = new HashMap<>();
        Quadtree result = new Quadtree();
        upperLongitude = params.get("ullon");
        lowerLongitude = params.get("lrlon");
        upperLatitude = params.get("ullat");
        lowerLatitude = params.get("lrlat");
        screenwidth = params.get("w");
        height = params.get("h");
        querypixelsize = (lowerLongitude - upperLongitude) / screenwidth;
        ArrayList<Quadtree.QuadtreeNode> arrayofimages = result.getrightnodes(result.root);
        BufferedImage image = new BufferedImage((int) result.width1, (int) result.height1, BufferedImage.TYPE_INT_RGB);
        Graphics2D imagedraws = (Graphics2D) image.getGraphics();

        try {
            int x = 0;
            int y = 0;
            for (Quadtree.QuadtreeNode a : arrayofimages) {
                BufferedImage bi = ImageIO.read(new File(IMG_ROOT + a.filename));
                imagedraws.drawImage(bi, x, y, null);
                x += 256;
                if (x >= image.getWidth()) {
                    x = 0;
                    y += 256;
                }
            }
            widthofimage = image.getWidth();
            heightofimage = y;
    //        ImageIO.write(image, "png", os);
            rasteredImageParams.put("raster_ul_lon", result.upperlon);
            rasteredImageParams.put("raster_ul_lat", result.upperlat);
            rasteredImageParams.put("raster_lr_lon", result.lowerlon);
            rasteredImageParams.put("raster_lr_lat", result.lowerlat);
            rasteredImageParams.put("raster_width", (int) result.width1);
            rasteredImageParams.put("raster_height", (int) result.height1);
            rasteredImageParams.put("depth", (int) result.depth1);
            rasteredImageParams.put("query_success", true);
        } catch (IOException ioexception) {
            System.out.println("Could not reach image");
        }
        if (!pathway.isEmpty()) {
            imagedraws.setStroke(new BasicStroke(MapServer.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            imagedraws.setColor(ROUTE_STROKE_COLOR);
        }

        Quadtree.QuadtreeNode firstrasteredimage = arrayofimages.get(0);
        Quadtree.QuadtreeNode lastrasteredimage = arrayofimages.get(arrayofimages.size() - 1);
        double firstlatitude = 1;
        double firstLon = 1;
        double depthper1 = (lastrasteredimage.lrlon - firstrasteredimage.ullon) / widthofimage;
        double depthper2 = (firstrasteredimage.ullat - lastrasteredimage.lrlat) / heightofimage;
        int i = 0;
        while(i < pathway.size()){
            Long idens = pathway.get(i);
            Node current = g.maphandler.nodesList.get(idens);
            if (firstlatitude != 1 && firstLon != 1) {
                int xcordinate1 = (int) ((firstLon - firstrasteredimage.ullon) / depthper1);
                int xcordinate2 = (int) ((current.lon - firstrasteredimage.ullon) / depthper1);
                int ycordinate2 = (int) ((firstrasteredimage.ullat - current.lat) / depthper2);
                int ycordinate1 = (int) ((firstrasteredimage.ullat - firstlatitude) / depthper2);

                imagedraws.drawLine(xcordinate1, ycordinate1, xcordinate2, ycordinate2);
            }
            firstLon = current.lon;
            firstlatitude = current.lat;
            i++;
        }
        try {
            ImageIO.write(image, "png", os);
        } catch (IOException ioexception) {
            System.out.println("sah dude");
        }
        return rasteredImageParams;
    }




    /**
     * Searches for the shortest route satisfying the input request parameters, sets it to be the
     * current route, and returns a <code>LinkedList</code> of the route's node ids for testing
     * purposes. <br>
     * The route should start from the closest node to the start point and end at the closest node
     * to the endpoint. Distance is defined as the euclidean between two points (lon1, lat1) and
     * (lon2, lat2).
     * @param params from the API call described in REQUIRED_ROUTE_REQUEST_PARAMS
     * @return A LinkedList of node ids from the start of the route to the end.
     */
    public static LinkedList<Long> findAndSetRoute(Map<String, Double> params) {
        HashMap<Long, Node> nodeList = g.maphandler.nodesList;
        PriorityQueue<SearchNode> prioritiesinlife = new PriorityQueue<>();
        HashMap<Long, SearchNode> whathaveivisited = new HashMap<>();
        double startingLongitude = params.get("start_lon");
        double startingLatitude = params.get("start_lat");
        double endingLongitude = params.get("end_lon");
        double endLatitude = params.get("end_lat");
        Node starter = null;
        Node ender = null;

        for (Map.Entry<Long, Node> entry : nodeList.entrySet()) {
            Node node = entry.getValue();
            if (starter!= null &&calcDist(node, startingLongitude, startingLatitude) < calcDist(starter, startingLongitude, startingLatitude)){
                starter = node;
            }
            else if(starter == null){
                starter = node;
            }
            if (ender != null && calcDist(node, endingLongitude, endLatitude) < calcDist(ender, endingLongitude, endLatitude)) {
                ender = node;
            } else if (ender == null) {
                ender = node;
            }

        }
        return findingrightroute(prioritiesinlife, whathaveivisited, starter, ender);
    }

    public static LinkedList<Long> findingrightroute(PriorityQueue<SearchNode> pqq, HashMap<Long, SearchNode> hm, Node Start, Node Ends){
        SearchNode start = new SearchNode(Start, Ends, null);
        pqq.add(start);
        while (pqq.peek().currentNode.givemeID() != Ends.givemeID()) {
            SearchNode temp = pqq.remove();
            for(int x = 0; x < temp.currentNode.connections.size(); x++){
                Node asah = temp.currentNode.connections.get(x);
                SearchNode newNode = new SearchNode(asah, Ends, temp);
                Long iden = asah.givemeID();
                if(hm.containsKey(iden) && newNode.priority < hm.get(iden).priority){
                    hm.put(iden, newNode);
                    pqq.add(newNode);

                }
                else if(!hm.containsKey(iden)){
                    hm.put(iden, newNode);
                    pqq.add(newNode);
                }
            }
        }
        SearchNode Showmethemoney = pqq.peek();
        LinkedList<Long> solutionroute = new LinkedList<>();
        while (Showmethemoney != null) {
            solutionroute.addFirst(Showmethemoney.currentNode.givemeID());
            Showmethemoney = Showmethemoney.previousNode;
        }
        pathway = solutionroute;
        return solutionroute;
    }


    public static Node getStartNode(){
        return start;
    }
    public static Node getEndNode(){
        return end;
    }


    private static double calcDist(Node node, double x2, double y2) {
        double x1 = node.lon;
        double y1 = node.lat;
        double returnvale = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return returnvale;
    }


    /**
     * Clear the current found route, if it exists.
     */
    public static void clearRoute() {
        LinkedList<Long> hey = new LinkedList<>();
        pathway = hey;
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public static List<String> getLocationsByPrefix(String prefix) {
        return new LinkedList<>();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public static List<Map<String, Object>> getLocations(String locationName) {
        return new LinkedList<>();
    }
}
