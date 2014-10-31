package module3;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class View extends Application {
	
	private static String fileName;
	private double windowHeight;
	private double windowWidth;
	private double windowScalar = 0.9;
	private double rectangleScalar = 0.8;
	
	private Scene scene;
	private Stage stage;
	private GridPane grid;
	
	//Colors used for coloring squares in the board. Picked in ascending order
	public ArrayList<Color> availableColors = new ArrayList<Color>() {{
		add(Color.RED);
		add(Color.BLUE);
		add(Color.GREEN);
		add(Color.YELLOW);
		add(Color.PURPLE);
		add(Color.ORANGE);
		add(Color.PINK);
		add(Color.BROWN);
		add(Color.INDIGO);
		add(Color.GOLD);
		add(Color.CYAN);
		add(Color.GRAY);
		add(Color.MAGENTA);
		add(Color.NAVY);
	}};

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		//Initialize controller and let Controller take control
		new Controller(fileName, this);
	}
	
	/**
	 * Initialize grid with given domains, set the scene and show the stage in the window
	 * @param domains
	 */
	public void initializeStage(ArrayList<Domain> domains) {
		initializeWindowSize();
		initializeGrid(domains);
		scene = new Scene(grid, windowWidth, windowHeight);
		stage.setScene(scene);
		stage.setTitle("Flow");
		stage.show();
	}
	
	/**
	 * Initialize gridpane representing our game board. Filled with squares,
	 * each representing a variable. Endpoints have a circle on top of
	 * the square
	 * @param domains
	 */
	public void initializeGrid(ArrayList<Domain> domains) {
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		//Should be a square number of variables, so we'll try to cast it to int
		double rectangleSize = windowHeight/(int)Math.sqrt(domains.size())*rectangleScalar;
		for(Domain d:domains) {
			Variable variable = d.getVariable();
			StackPane stack = new StackPane();
			Rectangle rectangle = new Rectangle();
			rectangle.setHeight(rectangleSize);
			rectangle.setWidth(rectangleSize);
    		rectangle.setStroke(Color.BLACK);
    		if(variable.isEndpoint()) {
    			//If endpoint, set square color to endpoint color
    			rectangle.setFill(availableColors.get(d.getDomainValues().get(0)));
    			//Add circle to mark as endpoint
    			Circle circle = new Circle();
    			circle.setRadius(rectangleSize/3);
    			circle.setFill(Color.WHITE);
    			circle.setStroke(Color.BLACK);
    			//Add rectangle then circle to stack to display in correct order
    			stack.getChildren().addAll(rectangle, circle);
    		} else {
    			rectangle.setFill(Color.WHITE);
    			stack.getChildren().add(rectangle);
    		}
    		grid.add(stack, variable.getColumn(), variable.getRow());
		}
		//Rotate board to move index (0,0) to lower left corner
		grid.setRotate(-90);
	}
	
	/**
	 * Color rectangle at given index with given color
	 * @param row
	 * @param column
	 * @param color
	 */
	public void colorSquare(int row, int column, Color color) {
		Rectangle rectangle = findRectangle(row, column);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				rectangle.setFill(color);
			}
		});
	}
	
	/**
	 * Finds the rectangle at given row and column
	 * @param row
	 * @param column
	 * @return Rectangle at given index
	 */
	public Rectangle findRectangle(int row, int column) {
		//Get all stacks from grid
		ObservableList<Node> stacks = grid.getChildren();
		for(Node s: stacks) {
			//Find stack at given index
			if(GridPane.getColumnIndex(s)==column && GridPane.getRowIndex(s)==row) {
				//Cast to StackPane to access its getChildren method
				StackPane stack = (StackPane) s;
				//Shape at position 0 is always a rectangle
				return (Rectangle) stack.getChildren().get(0);
			}
		}
		//Should not get here
		return null;
	}
	
	/**
	 * Initializes window height and window width fields by measuring screen size
	 */
	public void initializeWindowSize() {
		Rectangle2D rect = Screen.getPrimary().getVisualBounds();
		windowHeight = rect.getHeight() * windowScalar;
		windowWidth = rect.getWidth() * windowScalar;
	}
	
	public static void main(String[] args) {
		fileName = args[0];
		launch(args);
	}
	

}
