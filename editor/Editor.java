package editor;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.*;
import java.util.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;



public class Editor extends Application {

	private final Rectangle cursor = new Rectangle(0, 0);

	private static double WINDOW_WIDTH = 500;
    private static double WINDOW_HEIGHT = 500;
    private static final int L_MARGIN = 5;
    private static int R_MARGIN = 5;
    private static final int MARGIN = L_MARGIN + R_MARGIN;
    public Group root = new Group();
    public Group rootParent = new Group();
    // creates data structure to hold text
    LinkedList a = new LinkedList();
    // creates data structure to hold node pointers to newlines
    List<LinkedList.Node> arrayList = new ArrayList<>();
    ArrayList<Undo> undo = new ArrayList<Undo>();
    ArrayList<Redo> redo = new ArrayList<Redo>();
    int y_line_ctr = 0;
    int totalTextHeight = 0;
    double scrollBarWidth;
    boolean spacesInLine;
    private static final int STARTING_TEXT_POSITION_X = 5;
    private static final int STARTING_TEXT_POSITION_Y = 0;
    private static final int STARTING_FONT_SIZE = 12;
    private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
    private int fontSize = STARTING_FONT_SIZE;

    private String fontName = "Verdana";

    private int getDimensionInsideMargin(int outsideDimension) {
        return outsideDimension - 2 * MARGIN;
    }


    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        Text positionText;

        MouseClickEventHandler(Group root) {
            // For now, since there's no mouse position yet, just create an empty Text object.
            positionText = new Text("");
            // We want the text to show up immediately above the position, so set the origin to be
            // VPos.BOTTOM (so the x-position we assign will be the position of the bottom of the
            // text).
            positionText.setTextOrigin(VPos.BOTTOM);

            // Add the positionText to root, so that it will be displayed on the screen.
            root.getChildren().add(positionText);
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();

            // Display text right above the click.
            positionText.setText("(" + mousePressedX + ", " + mousePressedY + ")");
            cursor.setX(mousePressedX);
            cursor.setY(mousePressedY);
        }
    }

    private class KeyEventHandler implements EventHandler<KeyEvent> {
		int textCenterX;
		int textCenterY;

		public KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
			textCenterX = 5;
			textCenterY = 0;
            
			// Initialize some empty text and add it to root so that it will be displayed.
			displayText = new Text(textCenterX, textCenterY, "");
			// Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
			// that when the text is assigned a y-position, that position corresponds to the
			// highest position across all letters (for example, the top of a letter like "I", as
			// opposed to the top of a letter like "e"), which makes calculating positions much
			// simpler!
			displayText.setTextOrigin(VPos.TOP);
			displayText.setFont(Font.font (fontName, fontSize));

			// All new Nodes need to be added to the root in order to be displayed.
			root.getChildren().add(displayText);
		}

		@Override
		public void handle(KeyEvent keyEvent) {
			if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
				// Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
				// the KEY_TYPED event, javafx handles the "Shift" key and associated
				// capitalization.
				String characterTyped = keyEvent.getCharacter();
				Text textObject = new Text(keyEvent.getCharacter());
				textObject.setTextOrigin(VPos.TOP);
				textObject.setFont(Font.font (fontName, fontSize));
				if (! keyEvent.isShortcutDown()) {
					if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
					// Ignore control keys, which have non-zero length, as well as the backspace key, which is
					// represented as a character of value = 8 on Windows.
	                    // checks for an enter key being typed
                        if (characterTyped.equals("\r")) {
	                        Text newLine = new Text("\n");
	                        a.add(newLine);
	                        render();
	                        y_line_ctr += 1;
	                        arrayList.add(a.getCursor());
	                    }
                        if (characterTyped.equals(" ") && cursor.getX() + cursor.getLayoutBounds().getWidth() + textObject.getLayoutBounds().getWidth() >= WINDOW_WIDTH - (MARGIN + scrollBarWidth)) {
                            return;
                        }
                        // when a normal character is typed
                        else {
                            a.add(textObject);
                            undo.add(new Undo(a.getCursor(), textObject, "add"));
                            undoStackMaxSize();
                            redo.clear();
                            render();
                            root.getChildren().add(textObject);
                            keyEvent.consume();
                        }
					}
					updateCursor();
				}
				
			} else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
				// Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
				// events have a code that we can check (KEY_TYPED events don't have an associated
				// KeyCode).
				KeyCode code = keyEvent.getCode();
                if (keyEvent.isShortcutDown()) {
                    if (keyEvent.getCode() == KeyCode.P) {
                        System.out.println(Math.round((int)cursor.getX()) + Math.round((int)cursor.getLayoutBounds().getWidth()) + ", " + Math.round((int)cursor.getY()));
                    }
                    else if (keyEvent.getCode() == KeyCode.MINUS) {
                        if (fontSize - 4 <= 0) {
                            keyEvent.consume();
                        }
                        else {
                            fontSize -= 4;
                            render();
                            updateCursor();
                        }
                    }
                    else if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                        if (a.size() == 0) {
                            fontSize += 4;
                            updateCursor();
                        }
                        else {
                            fontSize += 4;
                            render();
                        }
                        updateCursor();
                    }
                    else if (keyEvent.getCode() == KeyCode.Z) {
                        if (undo.size() > 0) {
                            Undo undone = undo.remove(undo.size() - 1);
                            if (undone.type.equals("add")){
                                a.setCursor(undone.position);
                                Text removed = a.remove();
                                root.getChildren().remove(removed);
                                render();
                                updateCursor();
                                redo.add(new Redo(a.getCursor(), removed, "removed"));
                            } else if (undone.type.equals("removed")) {
                                a.setCursor(undone.position);
                                a.add(undone.item);
                                root.getChildren().add(undone.item);
                                render();
                                updateCursor();
                                redo.add(new Redo(a.getCursor(), undone.item, "add"));
                            }
                        }
                    }
                    else if (keyEvent.getCode() == KeyCode.Y){
                        if (redo.size() > 0){
                            Redo redone = redo.remove(redo.size() -1);
                            if (redone.type.equals("add")){
                                a.setCursor(redone.position);
                                Text removed = a.remove();
                                root.getChildren().remove(removed);
                                render();
                                updateCursor();
                                undo.add(new Undo(a.getCursor(), removed, "removed"));
                            } else {
                                a.setCursor(redone.position);
                                a.add(redone.item);
                                root.getChildren().add(redone.item);
                                render();
                                updateCursor();
                                undo.add(new Undo(a.getCursor(), redone.item, "added"));
                            }
                        }


                    }
                 }
                else if (code == KeyCode.UP) {
                    if (cursor.getY() == 0) {
                        keyEvent.consume();
                    }
                    else {
                        a.cursorUpKey();
                        render();
                        updateCursor();
                    }
				}
                else if (code == KeyCode.DOWN) {
                    Text checker = a.getPreviousText();
                    if (checker == null || a.size() == 0 || y_line_ctr == 0) { //|| arrayList.size() <= y_line_ctr) {
                        keyEvent.consume();
                        System.out.println(y_line_ctr);
                        System.out.println(arrayList.size());
                    }else {
                        a.cursorDownKey();
                        render();
                        updateCursor();
                        System.out.println(y_line_ctr);
                    }
				}
                else if (code == KeyCode.LEFT) {
                    if (cursor.getY() == 0 && cursor.getX() == 0) {
                        keyEvent.consume();
                    }
                    else {
                        a.cursorLeftKey();
                        updateCursor();
                    }
                }
                else if (code == KeyCode.RIGHT) {
                    a.cursorRightKey();
                    updateCursor();
                }
                else if (code == KeyCode.BACK_SPACE) {
                    if (a.getCursorText() == null || a.getPreviousText() == null && a.size() == 0) {
                        keyEvent.consume();
                    }
                    if (a.getCursorText() == null || a.getPreviousText() == null) {
                        keyEvent.consume();
                    }
                    else if (a.getCursorText().getText().equals("\n")) {
                        Text removed = a.remove();
                        root.getChildren().remove(removed);
                        render();
                        Text removed2 = a.remove();
                        root.getChildren().remove(removed2);
                        undoStackMaxSize();
                        redo.clear();
                        render();
                        undo.add(new Undo(a.getCursor(), removed2, "removed"));
                    }
                    else {
                        Text removed = a.remove();
                        root.getChildren().remove(removed);
                        render();
                        undo.add(new Undo(a.getCursor(), removed, "removed"));
                    }
                }
			}
		}
	}

	/** An EventHandler to handle changing the color of the rectangle. */
    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.WHITE};

        CursorBlinkEventHandler() {
            // Set the color to be the first color in the list.
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    public void render() {
        Text sampleChar = new Text();
        sampleChar.setFont(Font.font(fontName, fontSize));
        sampleChar.setY(displayText.getY());
        int SAMPLE_CHAR_HEIGHT = Math.round((int) sampleChar.getLayoutBounds().getHeight());
        a.movePtrRight();
        totalTextHeight = (y_line_ctr + 1) * SAMPLE_CHAR_HEIGHT;
        while (a.checkPtr()) {
            a.getPtrContent().setFont(Font.font(fontName, fontSize));
            if (a.getPtrContent().getText().equals(" ")) {
                a.setSpacePtrToPtr();
                spacesInLine = true;
            }
            if (a.getPtrContent().getText().equals(" ") && cursor.getX() + cursor.getLayoutBounds().getWidth() + a.getPtrContent().getLayoutBounds().getWidth() >= WINDOW_WIDTH - (MARGIN + scrollBarWidth)) {
                return;
            }
            // enter case
            if (a.getPrevPtrContent() == null && a.getPtrContent().getText().equals("\n")) {
                int CHAR_HEIGHT = Math.round((int) sampleChar.getLayoutBounds().getHeight());
                a.getPtrContent().setX(5);
                a.getPtrContent().setY(CHAR_HEIGHT);
                updateCursor();
                spacesInLine = false;
            }
            else if (a.getPrevPtrContent() == null) {
                a.getPtrContent().setX(5);
                a.getPtrContent().setY(0);
            }
            else if (a.getPtrContent().getText().equals("\n")) {
                int CHAR_HEIGHT = Math.round((int) sampleChar.getLayoutBounds().getHeight());
                a.getPtrContent().setX(5);
                a.getPtrContent().setY(a.getPrevPtrContent().getY() + CHAR_HEIGHT);
                updateCursor();
                spacesInLine = false;
            }
            else if (a.getPrevPtrContent() == null) {
                a.getPtrContent().setX(5);
                a.getPtrContent().setY(0);
            }
            else if (Math.round(a.getPrevPtrContent().getX()) + Math.round((int) a.getPrevPtrContent().getLayoutBounds().getWidth()) >= WINDOW_WIDTH - (MARGIN + scrollBarWidth)) {
                if (!spacesInLine) {
                    int PREV_CHAR_HEIGHT = Math.round((int) a.getPrevPtrContent().getLayoutBounds().getHeight());
                    a.getPtrContent().setX(5);
                    a.getPtrContent().setY(a.getPrevPtrContent().getY() + PREV_CHAR_HEIGHT);
                }
                else {
                    renderWrap();
                }
            }
            // normal case
            else {
                if (a.getPrevPtrContent() == null) {
                    a.getPtrContent().setX(5);
                    a.getPtrContent().setY(0);
                }
                else {
                    int PREV_CHAR_WIDTH = Math.round((int) a.getPrevPtrContent().getLayoutBounds().getWidth());
                    a.getPtrContent().setX(a.getPrevPtrContent().getX() + PREV_CHAR_WIDTH);
                    a.getPtrContent().setY(a.getPrevPtrContent().getY());
                }
            }
            a.movePtrRight();
        }
        a.resetPtr();
        updateCursor();
    }
    public void undoStackMaxSize() {
        if (undo.size() > 100) {
            undo.remove(0);
        }
    }
    public void renderWrap() {
            /* checkSpacePtr points to the most recent "space" that the user has entered
               this while loop keeps going as long as spacePtr isnt the pointer im using for my render loop.
               the reason im rendering using spacePtr is because i don't want to mess with my regular pointer.
            **/
        while (a.checkSpacePtr()) {
                /* this is the 1st time this function loops, since the spacePtr points at a loop
                   you don't want to change the X and Y of the space object, so you move one position over.
                 */
            if (a.getSpacePtr() != a.getSentinel() && a.getSpacePtrContent().getText().equals(" ")) {
                int PREV_CHAR_HEIGHT = Math.round((int) a.getSpacePtrContent().getLayoutBounds().getHeight());
                int PREV_CHAR_Y = Math.round((int) a.getSpacePtrContent().getY());
                a.moveSpacePtrRight();
                a.getSpacePtrContent().setX(5);
                a.getSpacePtrContent().setY(PREV_CHAR_Y + PREV_CHAR_HEIGHT);
                a.getSpacePtrContent().setFont(Font.font(fontName, fontSize));
                spacesInLine = false;
            }
            // after this function has looped once, wordwrap only goes into this case
            else {
                a.moveSpacePtrRight();
                a.getSpacePtrContent().setX(a.getPrevSpacePtrContent().getX() + a.getPrevSpacePtrContent().getLayoutBounds().getWidth());
                a.getSpacePtrContent().setY(a.getPrevSpacePtrContent().getY());
                a.getSpacePtrContent().setFont(Font.font(fontName, fontSize));
            }
        }
    }
    private void updateCursor() {
        Text checker = a.getCursorText();
        // Figure out the size of the current text.
        Text sampleChar = new Text();
        sampleChar.setFont(Font.font(fontName, fontSize));
        sampleChar.setY(displayText.getY());
        int SAMPLE_CHAR_HEIGHT = Math.round((int) sampleChar.getLayoutBounds().getHeight());
        if (checker == null) {
            cursor.setHeight(SAMPLE_CHAR_HEIGHT);
            cursor.setWidth(1);
            cursor.setX(5);
            cursor.setY(0);
        }
        // accounts for newline objects being 2x as tall, so cursor isnt screwed up on a new line
        if (cursor.getX() == 5 && cursor.getY() != 0 && a.getCursorText().getText().equals("\n")) {
            cursor.setX(a.getPrevCursorText().getX() + Math.round(a.getPrevCursorText().getLayoutBounds().getWidth()));
            cursor.setY(a.getPrevCursorText().getY());
            cursor.setHeight(SAMPLE_CHAR_HEIGHT);
        }
        else {
            int textHeight = (int)Math.round(a.getCursorText().getLayoutBounds().getHeight());
            int textWidth = (int)Math.round(a.getCursorText().getLayoutBounds().getWidth());
            cursor.setHeight(SAMPLE_CHAR_HEIGHT);
            cursor.setWidth(1);
            cursor.setX(a.getCursorText().getX() + textWidth);
            cursor.setY(a.getCursorText().getY());
        }
        // Make sure the text appears in front of the rectangle.
        displayText.toFront();
    }

    /** Makes the text bounding box change color periodically. */
    public void makeCursorColorChange() {
        // Create a Timeline that will call the "handle" function of RectangleBlinkEventHandler
        // every 1 second.
        final Timeline timeline = new Timeline();
        // The rectangle should continue blinking forever.
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

//    if file doesnt exist, then you create the file. Make a filewriter called writer with n input filename,
//    public void saveFile(List list1) {
//        if (list1.size() < 1) {
//            return;
//        }
//        String fileName = (String) list1.get(0);
//        try {
//            File  inputFile = new File(fileName);
//            if (!inputFile.exists()) {
//                inputFile.createNewFile();
//                System.out.println("Successfully created file " + fileName);
//            }
//
//        }
//    }

    public void readFile(List list1) {
        if (list1.size() < 1) {
            return;
        }
        // when open is called on a file that exists, it opens it weirdly, and messes up the other functions
        // otherwise it works fine.
        String fileName = (String) list1.get(0);
        try {
            File inputFile = new File(fileName);
            // Check to make sure that the input file exists!
            if (!inputFile.exists()) {
                inputFile.createNewFile();
                System.out.println("Successfully created file " + fileName);
//                saveFile();
            }
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            //FileWriter writer = new FileWriter(fileName);

            int intRead = -1;

            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                String alpha = Character.toString(charRead);
                Text beta = new Text(alpha);
                a.add(beta);
                root.getChildren().add(beta);
            }

            System.out.println("Successfully opened file " + fileName);

            // Close the reader
            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }


	@Override
	public void start(Stage primaryStage) {
        Parameters parameter = getParameters();
        List paramList = parameter.getRaw();
        // Create a Node that will be the parent of all things displayed on the screen.
        rootParent.getChildren().add(root);
		// The Scene represents the window: its height and width will be the height and width
		// of the window displayed.
		int windowWidth = 500;
		int windowHeight = 500;
		Scene scene = new Scene(rootParent, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        root.getChildren().add(cursor);
        makeCursorColorChange();
        updateCursor();

		// To get information about what keys the user is pressing, create an EventHandler.
		// EventHandler subclasses must override the "handle" function, which will be called
		// by javafx.
		EventHandler<KeyEvent> keyEventHandler =
				new KeyEventHandler(root, windowWidth, windowHeight);

        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
		readFile(paramList);
        scene.setOnKeyTyped(keyEventHandler);
		scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler(rootParent));
        // Scrollbar implementation
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth());
        scrollBar.setMin(0);
        scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth());
        scrollBarWidth = scrollBar.getLayoutBounds().getWidth();
        root.getChildren().add(scrollBar);

//         Resizing window
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                WINDOW_WIDTH = (Double) newScreenWidth;
                scrollBar.setLayoutX(WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth());
                if (!a.isEmpty()) {
                    render();
                }
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                WINDOW_HEIGHT = (Double) newScreenHeight;
                scrollBar.setPrefHeight(WINDOW_HEIGHT);
                if (!a.isEmpty()) {
                    render();
                }
            }
        });

		primaryStage.setTitle("Editor");

		// This is boilerplate, necessary to setup the window where things are displayed.
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}