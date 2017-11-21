import java.util.LinkedList;

/**
 * Created by rahulmisra on 4/17/16.
 */
public class GroupofNodes {
    LinkedList<Node> nodesontheway = new LinkedList();
    Long id;
    boolean TypeofWay;

    public GroupofNodes(Long id, LinkedList<Node> nodesInWay, boolean wayType) {
        this.id = id;
        this.nodesontheway = nodesInWay;
        this.TypeofWay = wayType;
    }

    public void connectnodesonway() {
        int sa = 0;
        while (sa < nodesontheway.size()) {
            if (sa != 0) {
                nodesontheway.get(sa).connections.add(nodesontheway.get(sa - 1));
                if (nodesontheway.size() > sa + 1) {
                    nodesontheway.get(sa).connections.add(nodesontheway.get(sa + 1));
                }
            } else {
                if (nodesontheway.size() > 1) {
                    nodesontheway.get(0).connections.add(nodesontheway.get(1));
                }
            }
            sa++;
        }
    }

    public LinkedList<Node> getNodesontheway() {
        return this.nodesontheway;
    }
}



