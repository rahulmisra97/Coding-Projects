import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Wraps the parsing functionality of the MapDBHandler as an example.
 * You may choose to add to the functionality of this class if you wish.
 * @author Alan Yao
 */
public class GraphDB {
    /**
     * Example constructor shows how to create and start an XML parser.
     * @param db_path Path to the XML file to be parsed.
     */
    public MapDBHandler maphandler;

    public GraphDB(String db_path) {
        try {
            File inputFile = new File(db_path);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            maphandler = new MapDBHandler(this);
            saxParser.parse(inputFile, maphandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        System.out.println(maphandler.nodesList.size());
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {

        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        HashMap<Long, Node> ways = this.maphandler.nodesList;
        HashMap<Long, Node> waysreplica = new HashMap<>();
        Set<Long> Ids = ways.keySet();
        for(Long i: Ids){
            if(ways.get(i).connections.size() == 0){
                waysreplica.put(i, ways.get(i));
            }
        }
        Set<Long> Identitys = waysreplica.keySet();
        for(Long i: Identitys){
            ways.remove(i);
        }
    }

    public HashMap<Long, Node> getcleaneditems (){
        return this.maphandler.nodesList;
    }
}
