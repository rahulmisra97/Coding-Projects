
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by rahulmisra on 10/04/16.
 */
public class Quadtree {

    public class QuadtreeNode {
        QuadtreeNode child1;
        QuadtreeNode child2;
        QuadtreeNode child3;
        QuadtreeNode child4;
        double ullat;
        double ullon;
        double lrlat;
        double lrlon;
        String filename;
        double depth;


        QuadtreeNode(double ulla, double ullo, double lrla, double lrlo, String file, int depthvalue) {
            ullat = ulla;
            ullon = ullo;
            lrlat = lrla;
            lrlon = lrlo;
            filename = file;
            depth = depthvalue;

            if (depth < 7) {
                this.child1 = new QuadtreeNode(ullat, ullon, (ullat + lrlat) / 2, (lrlon + ullon) / 2, child1String(filename), depthvalue + 1);
                this.child2 = new QuadtreeNode(ullat, (ullon + lrlon) / 2, (ullat + lrlat) / 2, lrlon, child2String(filename), depthvalue + 1);
                this.child3 = new QuadtreeNode((ullat + lrlat) / 2, ullon, lrla, (lrlon + ullon) / 2, child3String(filename), depthvalue + 1);
                this.child4 = new QuadtreeNode((ullat + lrlat) / 2, (lrlon + ullon) / 2, lrlat, lrlon, child4String(filename), depthvalue + 1);

            }

        }

        public double nodepixelsize() {
            return ((this.lrlon - this.ullon) / MapServer.TILE_SIZE);
        }
    }

    public QuadtreeNode root;
    public ArrayList<QuadtreeNode> tilestoadd = new ArrayList<>();
    public static double upperlat;
    public static double upperlon;
    public static double lowerlat;
    public static double lowerlon;
    public static double height1;
    public static double depth1;
    public static double width1;
    public static double lrlatoffirst;
    public static double lrlonoffirst;


    public Quadtree() {
        root = new QuadtreeNode(MapServer.ROOT_ULLAT, MapServer.ROOT_ULLON, MapServer.ROOT_LRLAT, MapServer.ROOT_LRLON, "root.png", 0);

    }


    public String child1String(String parent) {
        if (parent.equals("root.png")) {
            return "1.png";
        } else {
            int index = parent.lastIndexOf(".");
            String res = parent.substring(0, index);
            String res2 = res.concat("1.png");
            return res2;
        }
    }

    public String child2String(String parent) {
        if (parent.equals("root.png")) {
            return "2.png";
        } else {
            int index = parent.lastIndexOf(".");
            String res = parent.substring(0, index);
            String res2 = res.concat("2.png");
            return res2;
        }
    }

    public String child3String(String parent) {
        if (parent.equals("root.png")) {
            return "3.png";
        } else {
            int index = parent.lastIndexOf(".");
            String res = parent.substring(0, index);
            String res3 = res.concat("3.png");
            return res3;
        }
    }


    public String child4String(String parent) {
        if (parent.equals("root.png")) {
            return "4.png";
        } else {
            int index = parent.lastIndexOf(".");
            String res = parent.substring(0, index);
            String res4 = res.concat("4.png");
            return res4;
        }
    }

    public ArrayList<QuadtreeNode> getrightnodes(QuadtreeNode a) {
        if (!depthfinder(a)) {
            if (intersectsTile(a.child1)) {
                getrightnodes(a.child1);
            }
            if (intersectsTile(a.child2)) {
                getrightnodes(a.child2);
            }
            if (intersectsTile(a.child3)) {
                getrightnodes(a.child3);
            }
            if (intersectsTile(a.child4)) {
                getrightnodes(a.child4);
            }
        } else {
            if (intersectsTile(a)) {
                tilestoadd.add(a);
                //System.out.println(a.filename);
            }
        }
        Collections.sort(tilestoadd, new TileComparator());
        upperlat = tilestoadd.get(0).ullat;
        upperlon = tilestoadd.get(0).ullon;
        lowerlat = tilestoadd.get(tilestoadd.size() - 1).lrlat;
        lowerlon = tilestoadd.get(tilestoadd.size() - 1).lrlon;
        depth1 = tilestoadd.get(0).depth;
        lrlatoffirst = tilestoadd.get(0).lrlat;
        lrlonoffirst = tilestoadd.get(0).lrlon;
        height1 = Math.round(((upperlat - lowerlat) / (upperlat - lrlatoffirst)) * 256);
        width1 = Math.round(((lowerlon - upperlon) / (lrlonoffirst - upperlon)) * 256);
       // System.out.println(tilestoadd.size());
        return tilestoadd;
    }

    private class TileComparator implements Comparator<QuadtreeNode> {

        @Override
        public int compare(QuadtreeNode o1, QuadtreeNode o2) {
            int a = 0;
            if (o1.ullat > o2.ullat) {
                a = -1;
            } else if (o1.ullat == o2.ullat) {
                if (o1.ullon < o2.ullon) {
                    a = -1;
                } else if (o1.ullon == o2.ullon) {
                    a = 0;
                } else if (o1.ullon > o2.ullon) {
                    a = 1;
                }
            } else  {
                a = 1;
            }
        return a;
        }

    }






    public boolean depthfinder(QuadtreeNode a){
        double pixelvalue = MapServer.querypixelsize;
        QuadtreeNode nodepointer = this.root;
        while (nodepointer.depth < 7 && nodepointer.nodepixelsize() > pixelvalue){
            nodepointer = nodepointer.child1;
        }
        // find all nodes of the same depth
         double rightdepth = nodepointer.depth;
         if(a.depth == rightdepth){
             return true;
         }
        else {
             return false;
         }
    }

    public boolean intersectsTile(QuadtreeNode a) {

        // If one rectangle is on left side of other
        if (a.ullon > MapServer.lowerLongitude || MapServer.upperLongitude > a.lrlon)
            return false;

        // If one rectangle is above other
        if (a.ullat < MapServer.lowerLatitude || MapServer.upperLatitude < a.lrlat)
            return false;

        return true;
    }
}




