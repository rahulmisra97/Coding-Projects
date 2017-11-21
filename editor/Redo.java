package editor;

import javafx.scene.text.*;

/**
 * Created by akshatdas on 3/8/16.
 */
public class Redo extends LinkedList {
    Node position;
    String type;
    Text item;

    public Redo(Node position, Text item, String type) {
        this.position = position;
        this.type = type;
        this.item = item;
    }
}
