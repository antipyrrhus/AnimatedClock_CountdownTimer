import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;

/** Class: ClockAnimation.java
 *  @author Yury Park
 *  @version 1.0 <p>
 *
 *  This Class - The animating clock. Wall clock with options.
 */
public class ClockAnimation extends Application {

	private Stage primaryStage;
	private final double STAGE_WIDTH = 305, STAGE_HEIGHT = 400;		//default stage size

	private MenuBar menuBar;		//menu bar
	private final Menu MENU01 = new Menu("File");	//File menu

	private final String SAV_FILE = "ClockAnimation.sav";	//save file w/options
	private BorderPane bp;
	private Timeline animation;
	private CheckBox onTopchkbox;
	private boolean isAlwaysOnTop;
	private boolean countdownStageIsVisible;


	/**
	 * Method: start
	 * @param primaryStage
	 */
	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;		//Primary stage.
		this.countdownStageIsVisible = false;	//This is a separate stage for countdown timer. Initially will be invisible.

		//Will primary stage always be on top? Set boolean value to false before loading any saved options from save file
		isAlwaysOnTop = false;

		/* Try to open save file and read settings from it if the save file exists. If not just move on */
		try {
			Scanner fileSc = new Scanner(new File(SAV_FILE));	//try to open save file
			int i = 0;
			/* Read each line from save file and set options accordingly */
			while(fileSc.hasNext()) {
				i++;
				if(i == 1) this.isAlwaysOnTop = (fileSc.nextLine().startsWith("t") ? true: false);
			}
			fileSc.close();
			System.out.println("Loaded from Save File:\nalways on top? " + this.isAlwaysOnTop);	//testing
		}
		catch(FileNotFoundException fnf) {
			System.out.printf("File %s not found! Moving on...\n", SAV_FILE);
		}

		ClockPane clock = new ClockPane(); // Create a clock from custom class

		// Create a handler for animation
		/*EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				clock.setCurrentTime();
			}
		};*/

		//ALT: Shorter way to do the above
		EventHandler<ActionEvent> eventHandler = e -> {
			clock.setCurrentTime(); // Set clock to the current time every time this handler is invoked
		};

		/* Create an animation for a running clock. This calls the above event handler every second. */
		this.animation = new Timeline(new KeyFrame(Duration.millis(1000), eventHandler));
		animation.setCycleCount(Timeline.INDEFINITE);	//This animation goes on forever.
		animation.play(); // Start animation

		this.bp = new BorderPane();
		bp.setCenter(clock);

		/* Create a menubar with items. */
		this.menuBar = new MenuBar();
		menuBar.getMenus().addAll(MENU01);	//add File menu to the menubar
		menuBar.setStyle("-fx-background-color: lightgray");	//set style
		bp.setTop(menuBar);	//add menubar to the top of the borderpane
		menuBar.setVisible(false);	//initially set menubar to invisible. This doesn't affect the placement
									//of the other nodes.
//		MENU01.setVisible(this.fileMenuVisible);	//Setting menu itself to invisible (as opposed to menubar)
													//causes the other nodes to scoot up or down as a result.


		/* Create items to be included in the File menu. This includes:
		 * Countdown timer
		 * Always On Top (with a checkbox)
		 * Exit */
		MenuItem countdownItem = new MenuItem("Countdown Timer (Alt+C)");
		onTopchkbox = new CheckBox();
		/* This checkbox will be initially auto-selected or not depending on the save file setting */
		onTopchkbox.setSelected(this.isAlwaysOnTop);
		MenuItem alwaysOnTopItem = new MenuItem("Always On Top (Alt+T) ", onTopchkbox);
		MenuItem exitItem = new MenuItem("Exit (Alt+F4)");

		/* Add these items to the File menu */
		MENU01.getItems().addAll(countdownItem, alwaysOnTopItem, exitItem);

		/* Add event listener for choosing countdown from file menu */
		countdownItem.setOnAction(new Countdown());		//invoke inner class to handle this one

		/* Add event listener for choosing always On Top from file menu. */
		alwaysOnTopItem.setOnAction(new AlwaysOnTopEvent());

		/* Add event listener for choosing exit from file menu. */
		exitItem.setOnAction(e -> primaryStage.close());

		/* Add event listener upon exit (stage close). */
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			/**
			 * Method: handle
			 * Upon primary stage closing, try to save settings to a text file.
			 */
			@Override
			public void handle(WindowEvent we) {
				try {
					PrintWriter pw = new PrintWriter(SAV_FILE);
					pw.println(isAlwaysOnTop + "=isAlwaysOnTop");
					pw.close();
				}
				catch(FileNotFoundException fnf) {
					System.out.printf("File %s not found while trying to save settings.", SAV_FILE);
				}
			}
		});

		/* Create scene, add borderpane to it, add to stage and show */
		Scene scene = new Scene(bp);
		primaryStage.setTitle("Pooh Crew Wall Clock v1.0"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.setWidth(STAGE_WIDTH); primaryStage.setHeight(STAGE_HEIGHT);
		primaryStage.setResizable(false);	//Make it so that end-user cannot resize window
		primaryStage.setAlwaysOnTop(this.isAlwaysOnTop);	//initialize as either true or false
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("Image/pooh.png")));	//Set icon for the stage using URL.
		primaryStage.show(); // Display the stage

		/* Event listener to detect mouse movement and to show the file menu bar when
		 * the mouse gets close. */
		scene.setOnMouseMoved(e -> {
			if(e.getY() < 35) {
				menuBar.setVisible(true);
			}
			else {
				menuBar.setVisible(false);
				//If the mouse clicks on "File" thereby showing the file items, but then the mouse
				//moves away, then the file menubar will disappear but the file items will still be shown.
				//This looks rather ugly and awkward. So the following code will ensure that
				//the file items will be hidden as well.
				MENU01.hide();
			}
		});

		/* Key press listener */
		scene.setOnKeyPressed(e -> {
			/* ALT+C brings up countdown timer */
			if(e.getCode() == KeyCode.C && e.isAltDown()) {
				new Countdown().handle(new ActionEvent());
//				System.out.println("Alt-C pressed");
			}
			/* ALT+T toggles "always on top" */
			else if(e.getCode() == KeyCode.T && e.isAltDown()) {
				new AlwaysOnTopEvent().handle(new ActionEvent());
//				System.out.println("Alt-T pressed");
			}
			/* ALT+F shows the File menu items ONLY IF the menubar is already visible */
			else if(e.getCode() == KeyCode.F && e.isAltDown() && menuBar.isVisible())
				MENU01.show();
		});
	}
	//End start

	/**
	 * Class that implements event handler. Invoked by choosing "Countdown Timer" from
	 * file menu in the GUI.
	 */
	class Countdown implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			/* If there is already a countdown window, then do nothing. Otherwise, do the following... */
			if(!countdownStageIsVisible) {
				countdownStageIsVisible = true;
				Stage cdownStage = new Stage();			//Create new stage to hold the countdown timer.
				CountdownTimer cdownTimer = new CountdownTimer(cdownStage);		//Instantiate custom class that extends a borderpane.
				Scene cdownScene = new Scene(cdownTimer);	//add to scene
				cdownStage.setScene(cdownScene);			//add scene to stage
				cdownStage.setTitle("Countdown Timer");		//set title
				cdownStage.show();
				cdownStage.setY(primaryStage.getY() + primaryStage.getHeight());	//set the x and y location of this stage right below primary stage
				cdownStage.setX(primaryStage.getX());
				cdownStage.requestFocus();
				cdownStage.setOnCloseRequest(f -> {		//event handler for when the countdown stage is closed
					countdownStageIsVisible = false;
					cdownTimer.resetTimer();			//Reset the timer when the stage closes (as in, don't keep the timer running upon close)
				});
			}
		}
	}

	/**
	 * Inner class implementing event handler.
	 * Invoked when Always On Top option is toggled.
	 */
	class AlwaysOnTopEvent implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			isAlwaysOnTop = !isAlwaysOnTop;
			onTopchkbox.setSelected(isAlwaysOnTop);
			primaryStage.setAlwaysOnTop(isAlwaysOnTop);
		}
	}

	/**
	 * Method: main
	 * Launch the GUI stage.
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}
}