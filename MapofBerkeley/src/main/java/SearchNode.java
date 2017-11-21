/**
 * Created by rahulmsira on 4/17/16.
 */
public class SearchNode implements Comparable<SearchNode>{
    Node currentNode;
    Node LastNode;
    SearchNode previousNode;
    double priority;
    double logic;
    double stepDistance;

    public SearchNode(Node currentNode, Node endNode, SearchNode prevNode) {
        this.previousNode = prevNode;
        this.currentNode = currentNode;
        this.LastNode = endNode;
        logic = calDist(currentNode, endNode);
        if (prevNode != null){
            stepDistance = calculatethestepdistance(currentNode, prevNode);
        }
        else if (prevNode == null) {
            stepDistance = 0;
        }
        this.priority = 400000000 * (logic + stepDistance);
//
    }
    public double calDist(Node node1, Node node2) {
        double xco1 = node1.lon;
        double yco1 = node1.lat;
        double xco2 = node2.lon;
        double yco2 = node2.lat;
        double returnvalue = Math.sqrt(Math.pow(xco2 - xco1, 2) + Math.pow(yco2 - yco1, 2));
        return returnvalue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchNode that = (SearchNode) o;

        return currentNode.equals(that.currentNode);

    }

    public double calculatethestepdistance(Node curNode, SearchNode prevNode) {
        Node past = prevNode.currentNode;
        Node prevpast = null;
        double prevDistance = prevNode.stepDistance;
        double EuclidianDistance = calDist(curNode, past);
        return prevDistance + EuclidianDistance;
    }

    @Override
    public int hashCode() {
        return currentNode.hashCode();
    }

    public double getPriority() {
        return this.priority;
    }
    public double changethePriority(SearchNode pichalaNode, SearchNode abhiNode) {
        double sahPriority;
        double finalPriority;
        finalPriority = calculatethestepdistance(abhiNode.currentNode, pichalaNode) + pichalaNode.getPriority();
        sahPriority = finalPriority - (pichalaNode.logic - abhiNode.logic);
        if(sahPriority > this.priority){
            this.priority = this.priority;
        }
        else if (sahPriority < this.priority) {
            this.priority = sahPriority;
        }
        else{
            this.priority = this.priority;
        }
        return sahPriority;
    }

    public int compareTo(SearchNode o) {
        return Double.compare(priority, o.priority);
    }
}
