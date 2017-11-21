
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by rahul misra on 4/16/16.
 */

public class Node{
    Long id;
    LinkedList<Node> connections = new LinkedList<>();
    double lon;
    double lat;
    String Nodename;
    double priority = 0;
    double distancetostart;
    double distancetoend;


    public Node(double lon, double lat, Long id, LinkedList<Node> connections) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.connections = connections;

    }

    public void setName(String dawg) {
        this.Nodename = dawg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (id != node.id) return false;
        if (Double.compare(node.lon, lon) != 0) return false;
        if (Double.compare(node.lat, lat) != 0) return false;
        if (Nodename != null ? !Nodename.equals(node.Nodename) : node.Nodename != null) return false;
        return connections != null ? connections.equals(node.connections) : node.connections == null;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lon, lat);
    }

    public LinkedList<Node> getConnections() {
        return this.connections;
    }

    public Long givemeID(){
        return this.id;
    }


    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", lon=" + lon +
                ", lat=" + lat +
                ", name='" + Nodename + '\'' +
                ", connections=" + connections +
                '}';
    }
}
