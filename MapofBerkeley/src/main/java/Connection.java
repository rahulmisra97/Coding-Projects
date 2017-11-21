import java.util.Objects;

/**
 * Created by rahulmisra on 17/04/16.
 */
public class Connection {
    private Node n1, n2;

    public Connection(Node n1, Node n2){
        this.n1 = n1;
        this.n2 = n2;
    }

    @Override
    public boolean equals (Object o){
        if(this == o){
            return true;
        }
        if(o == null|| getClass() != o.getClass()){
            return false;
        }
        Connection that = (Connection) o;
        return Objects.equals(n1, that.n1) && Objects.equals(n2, that.n2);

    }

    @Override
    public int hashCode(){
        return Objects.hash(n1, n2);
    }

    @Override
    public String toString(){
        return "Connection{" + "n1=" + n1 + ",n2=" + n2 + '}';
    }


}
