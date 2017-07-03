import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CountdownTimer extends BorderPane {

	private int millisec, sec, min, hour;
	private boolean timerIsRunning, timerIsPausedButNotReset;
	private Timeline timeline, blinkTimer;
	private Label timerLbl, millisecLbl;
	private Button playBtn, resetBtn, notesBtn;
	private int[] keyTypedOrder;
	private int keyTypedOrderInt;
	private MediaPlayer mediaPlayer;
	private Stage parentStage, textAreaStage;
	private String textAreaNote;

	/**
	 * 1-arg constructor.
	 * @param parentStage the stage that contains the countdown timer. This param is needed for setting the MODALITY of the textArea pop-up stage.
	 * See openTextArea() method for more details.
	 */
	public CountdownTimer(Stage parentStage) {
		this.parentStage = parentStage;		//save the parent stage to a var
		/* This array has 6 elements to keep track of each digit of the timer display 00:00:00, and how it changes dynamically when
		 * digit keys are typed on the keyboard. See setOnKeyTyped event handler for more details. */
		this.keyTypedOrder = new int[6];
		this.textAreaNote = "";		//This is the string that will show up on the textArea pop-up stage. See openTextArea() method for details.

		java.net.URL resource = getClass().getResource("Audio/coolNotes.mp3");
		Media media = new Media(resource.toString());

		/* Set up mediaplayer to control when the mp3 file is played */
		this.mediaPlayer = new MediaPlayer(media);

		/* Event handler that will control the timer display. Invoked by timeline.play() method and is updated every millisecond. */
		EventHandler<ActionEvent> eventHandler = e -> {
			/* Every time this event handler is invoked, we'll decrement millisec by one, and if necessary, decrement the other values as shown below */
			millisec--;
			if(millisec < 0) {
				millisec = 999;
				sec--;
				if(sec < 0) {
					sec = 59;
					min--;
					if(min < 0) {
						min = 59;
						hour--;
						if(hour < 0) {	//If we get this far, then it means that we have reached 00:00:00 and we need to stop the timer and play media file.
							millisec = 0; sec = 0; hour = 0; min = 0;
							resetTimer();	//resetting the timer via this custom method also stops any media files.
							playMedia();    //custom method to play audio file
							if(!textAreaNote.equals("")) openTextArea();	//If there is any custom note (set by end user), then auto-open the text area as a pop-up.
						}
					}
				}
			}
			updateTimer();	//Custom method to update this timer display every time the event handler is invoked no matter what.
		};
		//end eventhandler

		/* Instantiate a new timeline and prepare to invoke the above eventHandler every millisec.
		 * Don't actually invoke it yet. Invocation will occur once the end-user presses the "play" button. See timeline.play() method. */
		timeline = new Timeline(new KeyFrame(Duration.millis(1), eventHandler));
		timeline.setCycleCount(Timeline.INDEFINITE);

		/* A quick instruction label for the end user */
		Label instructionLbl = new Label("Use keyboard to set the desired countdown time.");
		instructionLbl.setStyle("-fx-text-fill: blue; -fx-font-size: 12");

		/* This label will contain the actual timer in the following format: 00:00:00. Notice the text has not been set yet.
		 * This will be done via the custom method resetTimer(), which will be invoked a bit later. */
		timerLbl = new Label();
		timerLbl.setStyle("-fx-font-size: 30; -fx-text-fill: darkgreen; -fx-font-weight: bold");

		/* This label contains the millisecs part of the timer, consisting of 3 digits. Again, the text has not been set yet
		 * and will be done via resetTimer() */
		millisecLbl = new Label();
		millisecLbl.setStyle("-fx-font-size: 15; -fx-text-fill: brown; -fx-font-weight: bold; -fx-underline: true");

		/* Make a playbutton */
		playBtn = new Button(">");
		playBtn.setPrefWidth(40);
		playBtn.setOnAction(new PlayHandler());	//event handler. See inner class PlayHandler

		/* Make a reset / set new time button */
		resetBtn = new Button("Reset / Set New Time");
		resetBtn.setOnAction(e -> {	//event handler. Using lambda this time
			timeline.stop();
			timerIsRunning = false;
			playBtn.setText(">");
			resetTimer();
		});

		/* A button to allow end-user to add notes. */
		notesBtn = new Button("Notes...");
		notesBtn.setOnAction(e -> {	//event handler.
			openTextArea();	//custom method to open new popup stage
		});

		/* HBox to contain both of the timer and millisec labels. */
		HBox hboxTimer = new HBox();
		hboxTimer.getChildren().addAll(timerLbl, millisecLbl);
		hboxTimer.setAlignment(Pos.CENTER);	//set alignment
		hboxTimer.setStyle("-fx-border-color: darkgreen");

		/* HBox to contain all the buttons */
		HBox hboxBtn = new HBox(5);
		hboxBtn.getChildren().addAll(playBtn, resetBtn, notesBtn);
		hboxBtn.setAlignment(Pos.CENTER);	//set alignment

		/* Add the nodes to this borderpane. */
		this.setBottom(hboxBtn);
		this.setCenter(hboxTimer);
		this.setTop(instructionLbl);
		this.setPadding(new Insets(10));	//set padding for borderpane. 10 px on every side

		BorderPane.setMargin(timerLbl, new Insets(10, 10, 10, 10));	//set margins for the timer label
		BorderPane.setMargin(hboxBtn, new Insets(10, 0, 0, 0));		//set margins for the hbox containing buttons. (10 px to the north only)
		BorderPane.setAlignment(instructionLbl, Pos.CENTER);		//set alignment for the instruction label to center
		//		BorderPane.setAlignment(hboxBtn, Pos.BASELINE_CENTER);	//this doesn't work. Instead, hboxBtn.setAlignment(Pos.CENTER) seems to work

		/* Event listener for whenever a key is typed while this border pane is in focus.
		 * Whenever the end user types a digit, and if the below conditions are met, then
		 * the timer label (that is, 00:00:00) will auto-update. Examples:
		 * On typing '0', the timer will still display 00:00:00.
		 * On typing '3', the timer will display 00:00:03.
		 * Then, on typing '0', the timer will display 00:00:30.
		 * Then, on typing '9', the timer will display 00:03:09.
		 * Then, on typing 'Z', the timer will be unchanged and show 00:03:09.
		 * And so on.... */
		this.setOnKeyTyped(e -> {
			/* NOTE: e.getCode() works for setOnKeyPressed event handler, but does NOT work for setOnKeyTyped. So instead,
			 * we'll use e.getCharacter(), which strangely returns a String. So we'll get the character by using the charAt(0)
			 * method. Then we'll:
			 * 1) check whether the character typed is between 0 and 9, inclusive. If it is, AND
			 * 2) if the timer is NOT currently running, AND
			 * 3) if it's NOT the case that the timer is currently paused,
			 * THEN and only THEN run the following code... */
			if(e.getCharacter().charAt(0) >= '0' && e.getCharacter().charAt(0) <= '9' && !timerIsRunning && !timerIsPausedButNotReset) {
//				System.out.println("DIGIT KEY TYPED");
				mediaPlayer.stop();		//If a sound file is currently playing, stop the sound.
				//If the timer is currently blinking or is invisible, set it to visible and stop blinking
				timerLbl.setVisible(true);
				blinkTimer.stop();
				int digit = Integer.parseInt(e.getCharacter());	//Parse the digit typed into an int.
				/* If the custom method keyTypedOrderIsEmpty() returns true and if the digit typed is something other than 0, then
				 * set the first element of the keyTypedOrder array to the digit, and increment the
				 * keyTypedOrderInt variable from -1 to 0. This keeps track of which element in the array should be updated. */
				if(keyTypedOrderIsEmpty() && digit != 0) {
					keyTypedOrder[0] = digit;
					keyTypedOrderInt = 0;
				}
				/* Else, if the keyTypedOrder array is NOT empty (has elements in it), then we'll increment
				 * the int var by 1, except if it's greater than 5 (indicating the 5th element of the array),
				 * then we'll have it stay at 5. */
				else if(!keyTypedOrderIsEmpty()) {
					keyTypedOrderInt = (keyTypedOrderInt + 1 > 5 ? 5 : keyTypedOrderInt + 1);	//conditional statement
					/* Now do a descending for loop from the last-updated index position down to 0, and basically
					 * scoot every typed digit thus far to the left. So, if the current display is 00:36:05, and
					 * if the user types '7', then the new display is: 03:60:57. */
					for (int i = keyTypedOrderInt; i > 0; i--) {
						keyTypedOrder[i] = keyTypedOrder[i-1];
					}
					keyTypedOrder[0] = digit;	//Add the most recently typed digit to the very 1st element of the array.
				}
				/* Hopefully it's clear from the above comments that the first element of the array corresponds to the
				 * RIGHT-MOST digit of the timer display, the 2nd element to the second-from-right digit, and so on...
				 * all the way up to the 5th and last element corresponding to the left-most digit of the display.
				 * Knowing this, let's update the current state of the timer.
				 * We calculate the values for sec, min, and hour, by doing a string concatenation of the
				 * keyTyped values from the array, then parsing the concatenated value into an Integer. */
				sec = Integer.parseInt(keyTypedOrder[1] + "" + keyTypedOrder[0]);
				min = Integer.parseInt(keyTypedOrder[3] + "" + keyTypedOrder[2]);
				hour = Integer.parseInt(keyTypedOrder[5] + "" + keyTypedOrder[4]);
				/* Now that we have the sec, min and hour values, we can update the timer display via the custom method */
				updateTimer();
			}
		});
		//end this.setOnKeyTyped

		//Finally, the last thing this constructor does is to ensure the timer is properly reset to zero, any sound files are
		//stopped, and so on.
		this.resetTimer();
	}
	//End public CountdownTimer

	/**
	 * Method: openTextArea
	 * Opens a pop-up window that allows user to type in any reminder note regarding the task to perform once the countdown timer reaches zero.
	 */
	public void openTextArea() {
		BorderPane bPane = new BorderPane();
		TextArea ta = new TextArea();
		ta.setText(textAreaNote);
		ta.setPrefSize(300, 75);

		HBox hb = new HBox(5);
		Button saveBtn = new Button("Save");
		saveBtn.setOnAction(e -> {		//event listener. Saves any text inside the text area before closing the window.
			textAreaNote = ta.getText();
			textAreaStage.close();
		});

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setOnAction(e -> {
			textAreaStage.close();
		});

		hb.getChildren().addAll(saveBtn, cancelBtn);
		hb.setAlignment(Pos.CENTER);

		bPane.setCenter(ta); bPane.setBottom(hb);

		Scene scn = new Scene(bPane);
		textAreaStage = new Stage();
		textAreaStage.setScene(scn);

		/* Set modality such that the parent stage (which is the countdown timer stage) is locked as long as this popup window is active. */
		textAreaStage.initModality(Modality.WINDOW_MODAL);
		textAreaStage.initOwner(parentStage);
		textAreaStage.show();
		textAreaStage.setY(parentStage.getY());	//Open the text area on the same x and y coordinates as the parent stage.
		textAreaStage.setX(parentStage.getX());
		textAreaStage.requestFocus();
	}

	/**
	 * Method: playMedia
	 * play the media file. Loop indefinitely until stopped.
	 */
	public void playMedia() {
		mediaPlayer.setCycleCount(Timeline.INDEFINITE);
		mediaPlayer.play();
	}

	/**
	 * Method: keyTypedOrderIsEmpty
	 * @return returns true if the array contains only 0 values, indicating a timer display of 00:00:00. Otherwise false
	 */
	public boolean keyTypedOrderIsEmpty() {
		for(int i : keyTypedOrder) {
			if(i != 0) return false;
		}
		return true;
	}

	/**
	 * Method: resetTimer
	 * reset timer to 00:00:00. Stop any event listener animation, stop any sound file playing, and make the timer display
	 * blink every half second. Also reset the key typed array to all 0's.
	 */
	public void resetTimer() {
		timeline.stop();
		timerIsRunning = false;			//reset set boolean variables
		timerIsPausedButNotReset = false;
		millisec = 0; sec = 0; min = 0; hour = 0;
		keyTypedOrderInt = -1;			//reset the value keeping track of which digit of the timer display has been updated by the end user
		playBtn.setText(">");			//The play button might be showing the pause symbol ('||'). So reset it to show '>'.
		mediaPlayer.stop();				//Sound file might be playing. So stop it.

		/* Stop any blinking of the timer and set it to visible. It is important to stop any previous blinking of the timer
		 * prior to running the BlinkTimer().handle event listener below, because otherwise, the previous blinking
		 * will overlap with the newly invoked blinking, making it blink twice as fast. */
		timerLbl.setVisible(true);
		if(blinkTimer != null) blinkTimer.stop();
		new BlinkTimer().handle(new ActionEvent());		//Run new event handler that makes the timer display blink.
		resetKeyTypedOrder();		//reset the key typed order array to all 0's
		updateTimer();				//Update the timer so it can show 00:00:00
	}

	/**
	 * Class: BlinkTimer
	 * Handles the timed blinking animation for the timer display every 0.5 secs. Invoked by the resetTimer() method.
	 * @author HAP
	 *
	 */
	class BlinkTimer implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			blinkTimer = new Timeline(new KeyFrame(Duration.millis(500), blinkHandler));
			blinkTimer.setCycleCount(Timeline.INDEFINITE);
			blinkTimer.play();
		}
		EventHandler<ActionEvent> blinkHandler = e -> {
			timerLbl.setVisible(!timerLbl.isVisible());	//toggle between visible and not visible every 0.5 secs.
		};

	}

	/**
	 * Method: resetKeyTypedOrder
	 * reset the array to all 0's. Invoked by resetTimer() method.
	 */
	public void resetKeyTypedOrder() {
		for(int i = 0; i < keyTypedOrder.length; i++) {
			keyTypedOrder[i] = 0;
		}
	}

	/**
	 * Method: updateTimer
	 * update the timer display to show current countdown time. Uses string format to preserve the 00:00:00 format for the timer
	 * as well as the 000 format for the millisec part of the timer.
	 */
	public void updateTimer() {
		timerLbl.setText(String.format("%02d:%02d:%02d ", hour, min, sec));
		millisecLbl.setText(String.format("%03d", millisec));
	}

	/**
	 * Class: PlayHandler
	 * Inner class that handles what happens once the end user presses the Play/Pause button.
	 */
	class PlayHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			if(timerIsRunning) {	//If timer is running at the time the button is clicked, then we must pause it.
				timerIsRunning = false;
				timerIsPausedButNotReset = true;
				timeline.stop();
				playBtn.setText(">");	//change the button display to play button.
			/* Else, if timer is NOT currently running, AND if the display does NOT show 00:00:00 000, then
			 * we can begin the countdown. */
			} else if(!timerIsRunning && !(millisec == 0 && sec == 0 && min == 0 && hour == 0)){
				timerIsRunning = true;
				timerIsPausedButNotReset = false;
				playBtn.setText("||");	//change the button display to a pause button.

				/* There may be instances where the user inputs something like 00:90:00, to indicate 90 mins countdown.
				 * If so, upon pressing play button, the timer will automatically update itself to 01:30:00, aka 1 hr and 30 mins. */
				if(sec > 59) {
					min++;
					sec -= 60;
				}
				if(min > 60) {
					hour++;
					min -= 60;
				}
				updateTimer();	//update timer to display the time set by the user
				timeline.play();	//and begin the countdown
			}
		}
	}
	//end class PlayHandler
}