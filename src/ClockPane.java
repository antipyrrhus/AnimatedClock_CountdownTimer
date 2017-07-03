import java.util.Calendar;
import java.util.GregorianCalendar;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class ClockPane extends Pane {
	private int hour;
	private int minute;
	private int second;
	private boolean isPM;
	private String date;

	// Clock pane's width and height
	private double w = 300, h = 300;

	/** Construct a default clock with the current time*/
	public ClockPane() {
		setCurrentTime();
	}

	/** Construct a clock with specified hour, minute, and second */
	public ClockPane(int hour, int minute, int second) {
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		paintClock();
	}

	/** Return hour */
	public int getHour() {
		return hour;
	}

	/** Set a new hour */
	public void setHour(int hour) {
		this.hour = hour;
		paintClock();
	}

	/** Return minute */
	public int getMinute() {
		return minute;
	}

	/** Set a new minute */
	public void setMinute(int minute) {
		this.minute = minute;
		paintClock();
	}

	/** Return second */
	public int getSecond() {
		return second;
	}

	/** Set a new second */
	public void setSecond(int second) {
		this.second = second;
		paintClock();
	}

	/**
	 * @return the isPM
	 */
	public boolean isPM() {
		return isPM;
	}

	/**
	 * @param isPM the isPM to set
	 */
	public void setPM(boolean isPM) {
		this.isPM = isPM;
	}

	/** Return clock pane's width */
	public double getW() {
		return w;
	}

	/** Set clock pane's width */
	public void setW(double w) {
		this.w = w;
		paintClock();
	}

	/** Return clock pane's height */
	public double getH() {
		return h;
	}

	/** Set clock pane's height */
	public void setH(double h) {
		this.h = h;
		paintClock();
	}

	/* Set the current time for the clock */
	public void setCurrentTime() {
		// Construct a calendar for the current date and time
		Calendar calendar = new GregorianCalendar();

		this.date = String.format("%02d/%02d/%s", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE), calendar.get(Calendar.YEAR));

		// Set current hour, minute and second
		int tempHour = calendar.get(Calendar.HOUR_OF_DAY);
		this.hour = (tempHour > 12 ? tempHour - 12 : tempHour);
		this.minute = calendar.get(Calendar.MINUTE);
		this.second = calendar.get(Calendar.SECOND);
		if(tempHour >= 12) this.isPM = true; else this.isPM = false;

		paintClock(); // Repaint the clock
	}

	private void setTextStyle(Text t, String fontWeight, int fontSize) {
		t.setStyle(String.format("-fx-font-weight: %s; -fx-font-size: %s", fontWeight, fontSize));
	}

	/** Paint the clock */
	private void paintClock() {
		// Initialize clock parameters
		double clockRadius = Math.min(w, h) * 0.8 * 0.5;
		double centerX = w / 2;
		double centerY = h / 2 + 20;

		// Draw circle
		Circle circle = new Circle(centerX, centerY, clockRadius);
		circle.setStyle("-fx-fill: white; -fx-stroke: saddlebrown; -fx-stroke-width: 3");
		Text t1 = new Text(centerX - 9, centerY - clockRadius + 18, "12");
		Text t2 = new Text(centerX - clockRadius + 5, centerY + 5, "9");
		Text t3 = new Text(centerX + clockRadius - 14, centerY + 3, "3");
		Text t4 = new Text(centerX - 6, centerY + clockRadius - 5, "6");
		setTextStyle(t1, "bold", 18); setTextStyle(t2, "bold", 18); setTextStyle(t3, "bold", 18); setTextStyle(t4, "bold", 18);

		// Draw second hand
		double sLength = clockRadius * 0.8;
		double secondX = centerX + sLength *
				Math.sin(second * (2 * Math.PI / 60));
		double secondY = centerY - sLength *
				Math.cos(second * (2 * Math.PI / 60));
		Line sLine = new Line(centerX, centerY, secondX, secondY);
		sLine.setStroke(Color.RED);

		// Draw minute hand
		double mLength = clockRadius * 0.8;
		double xMinute = centerX + mLength *
				Math.sin(minute * (2 * Math.PI / 60));
		double minuteY = centerY - mLength *
				Math.cos(minute * (2 * Math.PI / 60));
		Line mLine = new Line(centerX, centerY, xMinute, minuteY);
		mLine.setStroke(Color.BLUE);
		mLine.setStrokeWidth(5);

		// Draw hour hand
		double hLength = clockRadius * 0.6;
		double hourX = centerX + hLength *
				Math.sin((hour % 12 + minute / 60.0) * (2 * Math.PI / 12));
		double hourY = centerY - hLength *
				Math.cos((hour % 12 + minute / 60.0) * (2 * Math.PI / 12));
		Line hLine = new Line(centerX, centerY, hourX, hourY);
		hLine.setStroke(Color.TEAL);
		hLine.setStrokeWidth(5);

		//Set digital time
		String timeString = String.format("%02d:%02d:%02d %s",
				this.getHour(), this.getMinute(), this.getSecond(), (this.isPM() ? "PM" : "AM"));
		Label lblTime = new Label(timeString);
		lblTime.setStyle("-fx-font-size: 19; -fx-text-fill: blue; -fx-font-weight: bold");
		lblTime.relocate(centerX - 52, centerY + this.h / 2 - 27);

		//Set today's date
		Label lblDate = new Label("Today: " + this.date);
		lblDate.setPadding(new Insets(1, 7, 1, 7));
//		lblDate.setTextFill(Paint.valueOf("darkgray"));
		lblDate.setStyle("-fx-font-size: 14; -fx-text-fill: lightyellow; -fx-background-color: tomato; -fx-background-radius: 20px, 20px, 2px, 1px;");
		lblDate.relocate(centerX - 65, centerY - this.h / 2 - 5);
//		lblDate.relocate(63, 12);


		//Loading an image this way loads it from the root directory.
//		ImageView iview = new ImageView(new Image("file:Poohcrew.jpg"));

		//Loading an image this way loads it from the specified directory.
//		ImageView iview = new ImageView(new Image("file:src/pooh_clock/Image/Poohcrew.jpg"));

		//Use URL stream to load image
		ImageView iview = new ImageView(new Image(getClass().getResourceAsStream("Image/Poohcrew.jpg")));

		iview.setFitHeight(100); iview.setFitWidth(150);
		iview.setX(centerX - iview.getFitWidth() / 2); iview.setY(centerY - iview.getFitHeight() / 2);
		iview.setOpacity(0.8);

		getChildren().clear();
		getChildren().addAll(circle, iview,  t1, t2, t3, t4, mLine, hLine, sLine, lblTime, lblDate);
	}
}
