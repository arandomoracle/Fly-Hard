package cls;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import scn.Game;
import scn.Game.DifficultySetting;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.input;
import lib.jog.window;

/**
 * <h1>Aircraft</h1>
 * <p>
 * Represents an aircraft. Calculates velocity, route-following, etc.
 * </p>
 */
public class Aircraft implements Serializable {

	// TODO last updated: 2014.03.12 23:50
	private static final long serialVersionUID = -6259795098509299784L;

	// Static ints for use where altitude state is to be changed
	public final static int ALTITUDE_CLIMB = 1;
	public final static int ALTITUDE_FALL = -1;
	public final static int ALTITUDE_LEVEL = 0;

	/** The size of the aircraft in pixels */
	private final static int RADIUS = 16;

	/**
	 * How far away (in pixels) the mouse can be from the plane but still select
	 * it
	 */
	private final static int MOUSE_LENIENCY = 32;

	/** The size of the compass circle */
	public final static int COMPASS_RADIUS = 64;

	/** The sound to play when the separation distance is violated */
	private final static Sound WARNING_SOUND = audio.newSoundEffect("sfx"
			+ File.separator + "beep.ogg");

	/** The minimum distance planes should keep apart */
	private static int minimumSeparation;

	/** The image to use to represent the aircraft */
	private Image image;

	/** How much the plane can turn per second - in radians */
	private double turnSpeed;

	/**
	 * The unique name of the aircraft. Format is Flight followed by a random
	 * number between 100 and 900.
	 */
	private String flightName;

	private Vector position;

	/** The aircraft's current velocity */
	private Vector velocity;

	/** The aircraft's score */
	private int score;

	/** Whether the aircraft is currently under manual control */
	private boolean isManuallyControlled = false;

	/**
	 * Whether the aircraft has reached its destination. Note that if the
	 * destination is airport, a land command must then be given before the
	 * flight can terminate.
	 */
	private boolean hasFinished = false;

	/** Whether the aircraft is currently at an airport and waiting to land */
	public boolean isWaitingToLand;

	/** The speed at which the aircraft will ascend/descend */
	private int verticalVelocity;

	/** The plan the aircraft will follow to reach its destination */
	private FlightPlan flightPlan;

	/** Whether the aircraft is currently landing */
	private boolean isLanding = false;

	/** The point the aircraft is currently heading towards */
	public Vector currentTarget;

	/** The bearing specified whilst the aircraft is under manual control */
	private double manualBearingTarget = Double.NaN;

	/** The stage of its flight path the aircraft is at */
	private int currentRouteStage = 0;

	/** Avalue representing whether the plane is climbing or falling */
	private int altitudeState;

	/** Whether the collision warning sound is currently playing */
	private boolean collisionWarningSoundFlag = false;

	/** A list of the aircraft violation this aircraft's separation distance */
	private ArrayList<Aircraft> planesTooNear = new ArrayList<Aircraft>();

	/**
	 * Constructor for an aircraft.
	 * 
	 * @param name
	 *            the name of the flight
	 * @param nameOrigin
	 *            the name of the location from which the plane hails
	 * @param nameDestination
	 *            the name of the location to which the plane is going
	 * @param originPoint
	 *            the point to initialise the plane
	 * @param destinationPoint
	 *            the end point of the plane's route
	 * @param image
	 *            the image to represent the plane
	 * @param speed
	 *            the speed the plane will travel at
	 * @param sceneWaypoints
	 *            the waypoints on the map
	 * @param difficulty
	 *            the difficulty the game is set to
	 */
	public Aircraft(String name, String nameDestination, String nameOrigin,
			Waypoint destinationPoint, Waypoint originPoint, Image image,
			double speed, Waypoint[] sceneWaypoints,
			DifficultySetting difficulty, Airport airport) {
		this.flightName = name;
		this.flightPlan = new FlightPlan(sceneWaypoints, nameOrigin,
				nameDestination, originPoint, destinationPoint, airport);
		this.image = image;
		this.position = originPoint.getLocation();
		this.isWaitingToLand = (airport != null);
		this.score = 100;

		// Set aircraft's altitude to a random height
		int altitudeOffset = ((new Random()).nextInt(2)) == 0 ? 28000 : 30000;
		this.position = position.add(new Vector(0, 0, altitudeOffset));

		// Calculate initial velocity (direction)
		this.currentTarget = flightPlan.getRoute()[0].getLocation();
		double x = currentTarget.getX() - position.getX();
		double y = currentTarget.getY() - position.getY();
		this.velocity = new Vector(x, y, 0).normalise().scaleBy(speed);

		// Set the aircraft's difficulty settings
		// e.g. the minimum separation distance, turning speed, velocity
		setDifficultySettings(difficulty);
	}

	/**
	 * Adjust the aircraft's attributes according to the difficulty of the
	 * parent scene.
	 * <p>
	 * 0 has the easiest attributes (slower aircraft, more forgiving separation
	 * rules)
	 * </p>
	 * <p>
	 * 2 has the hardest attributes (faster aircraft, least forgiving separation
	 * rules)
	 * </p>
	 */
	private void setDifficultySettings(DifficultySetting difficulty) {
		switch (difficulty) {
		case EASY:
			minimumSeparation = 64;
			turnSpeed = Math.PI / 4;
			verticalVelocity = 500;
			break;

		case MEDIUM:
			minimumSeparation = 96;
			velocity = velocity.scaleBy(2);
			turnSpeed = Math.PI / 3;
			verticalVelocity = 300;
			break;

		case HARD:
			minimumSeparation = 128;
			velocity = velocity.scaleBy(3);
			// At high velocities, the aircraft is allowed to turn faster - this
			// helps keep the aircraft on track.
			turnSpeed = Math.PI / 2;
			verticalVelocity = 200;
			break;

		default:
			Exception e = new Exception("Invalid Difficulty: " + difficulty
					+ ".");
			e.printStackTrace();
		}
	}

	/**
	 * Updates the plane's position and bearing, the stage of its route, and
	 * whether it has finished its flight.
	 * 
	 * @param time_difference
	 */
	public void update(double time_difference) {
		if (hasFinished)
			return;

		// Update altitude
		if (isLanding) {
			if (position.getZ() > 100) {
				// Decrease altitude rapidly (2501/second),
				// ~11 seconds to fully descend
				position.setZ(position.getZ() - 2501 * time_difference);
			} else { // Gone too low, land it now TODO (check this)
				if (flightPlan.getAirport() != null) {
					flightPlan.getAirport().isActive = false;
					hasFinished = true;
				}
			}
		} else {
			switch (altitudeState) {
			case -1:
				fall();
				break;
			case 0:
				break;
			case 1:
				climb();
				break;
			}
		}

		// Update position
		Vector dv = velocity.scaleBy(time_difference);
		position = position.add(dv);

		// Update target
		if (currentTarget.equals(flightPlan.getDestination())
				&& isAtDestination()) { // At finishing point
			if (!isWaitingToLand) { // Ready to land
				hasFinished = true;
				if (flightPlan.getAirport() != null) { // Landed at airport
					flightPlan.getAirport().isActive = false;
				}
			}
		} else if (isAt(currentTarget)) {
			currentRouteStage++;
			// Next target is the destination if you're at the end of the plan,
			// otherwise it's the next waypoint
			currentTarget = (currentRouteStage >= flightPlan.getRoute().length) ? flightPlan
					.getDestination()
					: flightPlan.getRoute()[currentRouteStage].getLocation();
		}

		// Update bearing
		if (Math.abs(angleToTarget() - getBearing()) > 0.01) {
			turnTowardsTarget(time_difference);
		}
	}

	/**
	 * Calculates the angle from the plane's position, to its current target.
	 * 
	 * @return the angle in radians to the plane's current target.
	 */
	private double angleToTarget() {
		if (isManuallyControlled) {
			return (manualBearingTarget == Double.NaN) ? getBearing()
					: manualBearingTarget;
		} else {
			return Math.atan2(currentTarget.getY() - position.getY(),
					currentTarget.getX() - position.getX());
		}
	}

	public boolean isOutOfAirspaceBounds() {
		double x = position.getX();
		double y = position.getY();
		return ((x < (RADIUS / 2))
				|| (x > window.width() - (RADIUS / 2) - (2 * Game.getXOffset()))
				|| (y < (RADIUS / 2)) || (y > window.height() + (RADIUS / 2)
				- (2 * Game.getYOffset())));
	}

	public boolean isAt(Vector point) {
		double dy = point.getY() - position.getY();
		double dx = point.getX() - position.getX();
		return dy * dy + dx * dx < 6;
	}

	/**
	 * Edits the plane's path by changing the waypoint it will go to at a
	 * certain stage in its route.
	 * 
	 * @param routeStage
	 *            the stage at which the new waypoint will replace the old
	 * @param newWaypoint
	 *            the new waypoint to travel to
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		if ((!newWaypoint.isEntryOrExit()) && (routeStage > -1)) {
			flightPlan.alterPath(routeStage, newWaypoint);
			if (!isManuallyControlled)
				resetBearing();
			if (routeStage == currentRouteStage) {
				currentTarget = newWaypoint.getLocation();
				// turnTowardsTarget(0);
			}
		}
	}

	public boolean isMouseOver(int mx, int my) {
		double dx = position.getX() - mx;
		double dy = position.getY() - my;
		return dx * dx + dy * dy < MOUSE_LENIENCY * MOUSE_LENIENCY;
	}

	/**
	 * Calls {@link isMouseOver()} using {@link input.mouseX()} and {@link
	 * input.mouseY()} as the arguments.
	 * 
	 * @return <code>true</code> if the mouse is close enough to this plane,
	 *         otherwise <code>false</code>
	 */
	public boolean isMouseOver() {
		return isMouseOver(input.mouseX() - Game.getXOffset(), input.mouseY()
				- Game.getYOffset());
	}

	public boolean isAtDestination() {
		if (flightPlan.getAirport() != null) { // At airport
			return flightPlan.getAirport().isWithinArrivals(position, false); // Within
																				// Arrivals
																				// rectangle
		} else {
			return isAt(flightPlan.getDestination()); // Very close to
														// destination
		}
	}

	public void turnLeft(double time_difference) {
		turnBy(time_difference * -turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	public void turnRight(double time_difference) {
		turnBy(time_difference * turnSpeed);
		manualBearingTarget = Double.NaN;
	}

	/**
	 * Turns the plane by a certain angle (in radians). Positive angles turn the
	 * plane clockwise.
	 * 
	 * @param angle
	 *            the angle by which to turn
	 */
	private void turnBy(double angle) {
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double x = velocity.getX();
		double y = velocity.getY();

		velocity = new Vector((x * cosA) - (y * sinA), (y * cosA) + (x * sinA),
				velocity.getZ());
	}

	private void turnTowardsTarget(double time_difference) {
		// Get difference in angle
		double angleDifference = (angleToTarget() % (2 * Math.PI))
				- (getBearing() % (2 * Math.PI));
		boolean crossesPositiveNegativeDivide = angleDifference < -Math.PI * 7 / 8;

		// Correct difference
		angleDifference += Math.PI;
		angleDifference %= (2 * Math.PI);
		angleDifference -= Math.PI;

		// Get which way to turn.
		int angleDirection = (int) (angleDifference /= Math
				.abs(angleDifference));
		if (crossesPositiveNegativeDivide)
			angleDirection *= -1;

		double angleMagnitude = Math.min(
				Math.abs((time_difference * turnSpeed)),
				Math.abs(angleDifference));

		// Scale if the angle is greater than 90 degrees
		// This allows aircraft to break out of loops around waypoints
		if (Math.abs(angleToTarget()) >= (Math.PI / 2))
			angleMagnitude *= 1.75;
		turnBy(angleMagnitude * angleDirection);
	}

	public void draw(int highlightedAltitude) {
		draw(highlightedAltitude, null);
	}

	/**
	 * Draws the plane and any warning circles if necessary.
	 * 
	 * @param the
	 *            altitude to highlight aircraft at
	 */
	public void draw(int highlightedAltitude, Vector offset) {
		double alpha;
		if (position.getZ() >= 28000 && position.getZ() <= 29000) { // 28000-29000
			alpha = highlightedAltitude == 28000 ? 255 : 128; // 255 if
																// highlighted,
																// else 128
		} else if (position.getZ() <= 30000 && position.getZ() >= 29000) { // 29000-30000
			alpha = highlightedAltitude == 30000 ? 255 : 128; // 255 if
																// highlighted,
																// else 128
		} else { // If it's not 28000-30000, then it's currently landing
			alpha = 128;
		}

		// Draw planes with a lower altitude smaller
		double scale = 2 * (position.getZ() / 30000);

		// Draw plane image
		graphics.setColour(128, 128, 128, alpha);

		if (offset != null) {
			graphics.draw(image, scale, position.getX() - (image.width() / 2)
					+ offset.getX(), position.getY() - (image.height() / 2)
					+ offset.getY(), getBearing(), (RADIUS / 2), (RADIUS / 2));
		} else {
			graphics.draw(image, scale, position.getX() - (image.width() / 2),
					position.getY() - (image.height() / 2), getBearing(),
					(RADIUS / 2), (RADIUS / 2));
		}

		// Draw altitude label
		graphics.setColour(128, 128, 128, alpha / 2.5);

		if (offset != null) {
			graphics.print(String.format("%.0f", position.getZ()) + "+",
					position.getX() + (RADIUS / 2) + offset.getX(),
					position.getY() - (RADIUS / 2) + offset.getY());
		} else {
			graphics.print(String.format("%.0f", position.getZ()) + "+",
					position.getX() + (RADIUS / 2), position.getY()
							- (RADIUS / 2));
		}

		drawWarningCircles(offset);
	}

	/**
	 * Draws the compass around this plane - used for manual control
	 */
	public void drawCompass() {
		graphics.setColour(graphics.green);

		// Centre positions of aircraft
		Double xpos = position.getX() - image.width() / 2;
		Double ypos = position.getY() - image.height() / 2;

		// Draw the compass circle
		graphics.circle(false, xpos, ypos, COMPASS_RADIUS, 30);

		// Draw the angle labels (0, 60 .. 300)
		for (int i = 0; i < 360; i += 60) {
			double r = Math.toRadians(i - 90);
			double x = xpos + (1.1 * COMPASS_RADIUS * Math.cos(r));
			double y = ypos - 2 + (1.1 * COMPASS_RADIUS * Math.sin(r));
			if (i > 170)
				x -= 24;
			if (i == 180)
				x += 12;
			graphics.print(String.valueOf(i), x, y);
		}

		// Draw the line to the mouse pointer
		double x, y;
		if (isManuallyControlled && input.isMouseDown(input.MOUSE_RIGHT)) {
			graphics.setColour(graphics.green_transp);
			double r = Math.atan2(input.mouseY() - position.getY(),
					input.mouseX() - position.getX());
			x = xpos + (COMPASS_RADIUS * Math.cos(r));
			y = ypos + (COMPASS_RADIUS * Math.sin(r));
			// Draw several lines to make the line thicker
			graphics.line(xpos, ypos, x, y);
			graphics.line(xpos - 1, ypos, x, y);
			graphics.line(xpos, ypos - 1, x, y);
			graphics.line(xpos + 1, ypos, x, y);
			graphics.line(xpos + 1, ypos + 1, x, y);
			graphics.setColour(0, 128, 0, 16);
		}

		// Draw current bearing line
		x = xpos + (COMPASS_RADIUS * Math.cos(getBearing()));
		y = ypos + (COMPASS_RADIUS * Math.sin(getBearing()));

		// Draw several lines to make it thicker
		graphics.line(xpos, ypos, x, y);
		graphics.line(xpos - 1, ypos, x, y);
		graphics.line(xpos, ypos - 1, x, y);
		graphics.line(xpos + 1, ypos, x, y);
		graphics.line(xpos + 1, ypos + 1, x, y);
	}

	/**
	 * Draws warning circles around this plane and any others that are too near.
	 */
	private void drawWarningCircles(Vector offset) {
		for (Aircraft plane : planesTooNear) {
			Vector midPoint = position.add(plane.position).scaleBy(0.5);
			double radius = position.sub(midPoint).magnitude() * 2;
			graphics.setColour(graphics.red);

			if (offset != null) {
				graphics.circle(false, midPoint.getX() + offset.getX(),
						midPoint.getY() + offset.getY(), radius);
			} else {
				graphics.circle(false, midPoint.getX(), midPoint.getY(), radius);
			}
		}
	}

	/**
	 * Draws lines starting from the plane, along its flight path to its
	 * destination.
	 */
	public void drawFlightPath(boolean isSelected) {
		if (isSelected) {
			graphics.setColour(0, 128, 128);
		} else {
			graphics.setColour(0, 128, 128, 128);
		}

		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();

		if (currentTarget != destination) {
			// Draw line from plane to next waypoint
			graphics.line(position.getX() - image.width() / 2, position.getY()
					- image.height() / 2, route[currentRouteStage]
					.getLocation().getX(), route[currentRouteStage]
					.getLocation().getY());
		} else {
			// Draw line from plane to destination
			graphics.line(position.getX() - image.width() / 2, position.getY()
					- image.height() / 2, destination.getX(),
					destination.getY());
		}

		for (int i = currentRouteStage; i < route.length - 1; i++) { // Draw
																		// lines
																		// between
																		// successive
																		// waypoints
			graphics.line(route[i].getLocation().getX(), route[i].getLocation()
					.getY(), route[i + 1].getLocation().getX(), route[i + 1]
					.getLocation().getY());
		}
	}

	/**
	 * Visually represents the waypoint being moved.
	 * 
	 * @param modified
	 *            the index of the waypoint being modified
	 * @param mouseX
	 *            the current x position of the mouse
	 * @param mouseY
	 *            the current y position of the mouse
	 */
	public void drawModifiedPath(int modified, double mouseX, double mouseY) {
		graphics.setColour(0, 128, 128, 128);
		Waypoint[] route = flightPlan.getRoute();
		Vector destination = flightPlan.getDestination();

		if (currentRouteStage > modified - 1) {
			graphics.line(getPosition().getX(), getPosition().getY(), mouseX,
					mouseY);
		} else {
			graphics.line(route[modified - 1].getLocation().getX(),
					route[modified - 1].getLocation().getY(), mouseX, mouseY);
		}

		if (currentTarget == destination) {
			graphics.line(mouseX, mouseY, destination.getX(),
					destination.getY());
		} else {
			int index = modified + 1;

			if (index == route.length) { // Modifying final waypoint in route
				// Line drawn to final waypoint
				graphics.line(mouseX, mouseY, destination.getX(),
						destination.getY());
			} else {
				graphics.line(mouseX, mouseY,
						route[index].getLocation().getX(), route[index]
								.getLocation().getY());
			}
		}
	}

	/**
	 * Updates the number of planes that are violating the separation rule. Also
	 * checks for crashes.
	 * 
	 * @param time_difference
	 *            the time elapsed since the last frame.
	 * @param aircraftList
	 *            all aircraft in the airspace
	 * @param global
	 *            score object used to decrement score if separation is breached
	 * @return index of plane breaching separation distance with this plane, or
	 *         -1 if no planes are in violation.
	 */
	public int updateCollisions(double time_difference,
			ArrayList<Aircraft> aircraftList) {
		planesTooNear.clear();
		for (int i = 0; i < aircraftList.size(); i++) {
			Aircraft plane = aircraftList.get(i);
			if (plane != this && isWithin(plane, RADIUS)) { // Planes crash
				hasFinished = true;
				return i;
			} else if (plane != this && isWithin(plane, minimumSeparation)) { // Breaching
																				// separation
																				// distance
				planesTooNear.add(plane);
				if (!collisionWarningSoundFlag) {
					collisionWarningSoundFlag = true;
					WARNING_SOUND.play();
				}
			}
		}
		if (planesTooNear.isEmpty()) {
			collisionWarningSoundFlag = false;
		}
		return -1;
	}

	/**
	 * Checks whether an aircraft is within a certain distance from this one.
	 * 
	 * @param aircraft
	 *            the aircraft to check.
	 * @param distance
	 *            the distance within which to care about.
	 * @return true, if the aircraft is within the distance. False, otherwise.
	 */
	private boolean isWithin(Aircraft aircraft, int distance) {
		double dx = aircraft.getPosition().getX() - position.getX();
		double dy = aircraft.getPosition().getY() - position.getY();
		double dz = aircraft.getPosition().getZ() - position.getZ();
		return dx * dx + dy * dy + dz * dz < distance * distance;
	}

	public void toggleManualControl() {
		if (isLanding) { // Can't manually control while landing
			isManuallyControlled = false;
		} else {
			isManuallyControlled = !isManuallyControlled;
			if (isManuallyControlled) {
				setBearing(getBearing());
			} else {
				resetBearing();
			}
		}
	}

	private void resetBearing() {
		if (currentRouteStage < flightPlan.getRoute().length
				& flightPlan.getRoute()[currentRouteStage] != null) {
			currentTarget = flightPlan.getRoute()[currentRouteStage]
					.getLocation();
		}
		turnTowardsTarget(0);
	}

	private void climb() {
		if (position.getZ() < 30000 && altitudeState == ALTITUDE_CLIMB)
			setAltitude(verticalVelocity);
		if (position.getZ() >= 30000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 30000);
		}
	}

	private void fall() {
		if (position.getZ() > 28000 && altitudeState == ALTITUDE_FALL)
			setAltitude(-verticalVelocity);
		if (position.getZ() <= 28000) {
			setAltitude(0);
			altitudeState = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 28000);
		}
	}

	public void land() {
		isWaitingToLand = false;
		isLanding = true;
		isManuallyControlled = false;
		if (flightPlan.getAirport() != null) {
			flightPlan.getAirport().isActive = true;
		}
	}

	public void takeOff() {
		if (flightPlan.getAirport() != null) {
			flightPlan.getAirport().isActive = true;
		}

		Game.addAircraftWaitingToTakeOff(this);
	}

	/**
	 * Checks if an aircraft is close to an its parameter (entry point).
	 * 
	 * @param position
	 *            of a waypoint
	 * @return True it if it close
	 */
	public boolean isCloseToEntry(Vector position) {
		double x = this.getPosition().getX() - position.getX();
		double y = this.getPosition().getY() - position.getY();
		return x * x + y * y <= 300 * 300;
	}

	/**
	 * Gets the aircraft's position.
	 * 
	 * @return the aircraft's position
	 */
	public Vector getPosition() {
		return position;
	}

	/**
	 * Gets the aircraft's name.
	 * 
	 * @return the aircraft's name
	 */
	public String getName() {
		return flightName;
	}

	/**
	 * Gets whether or not the aircraft has completed its route.
	 * 
	 * @return <code>true</code> if the aircraft has finished, otherwise
	 *         <code>false</code>
	 */
	public boolean isFinished() {
		return hasFinished;
	}

	/**
	 * Gets whether or not the aircraft is under manual control.
	 * 
	 * @return <code>true</code> if the aircraft is under manual control,
	 *         otherwise <code>false</code>
	 */
	public boolean isManuallyControlled() {
		return isManuallyControlled;
	}

	/**
	 * Gets the aircraft's altitude state.
	 * 
	 * @return the aircraft's altitude state
	 */
	public int getAltitudeState() {
		return altitudeState;
	}

	/**
	 * Gets the aircraft's bearing.
	 * 
	 * @return the aircraft's bearing
	 */
	public double getBearing() {
		return Math.atan2(velocity.getY(), velocity.getX());
	}

	/**
	 * Gets the aircraft's speed.
	 * 
	 * @return the aircraft's speed
	 */
	public double getSpeed() {
		return velocity.magnitude();
	}

	/**
	 * Gets the aircraft's flight plan.
	 * 
	 * @return the aircraft's flight plan
	 */
	public FlightPlan getFlightPlan() {
		return flightPlan;
	}

	/**
	 * Sets the manual bearing the aircraft is following.
	 * <p>
	 * NOTE: the aircraft will only follow this heading if it is under manual
	 * control.
	 * </p>
	 * 
	 * @param newHeading
	 *            the new bearing to follow
	 */
	public void setBearing(double newHeading) {
		this.manualBearingTarget = newHeading;
	}

	/**
	 * Sets the aircraft's altitude.
	 * 
	 * @param height
	 *            the altitude to move the aircraft to
	 */
	private void setAltitude(int height) {
		this.velocity.setZ(height);
	}

	/**
	 * Sets the aircraft's altitude state to climbing, falling or level.
	 * 
	 * @param state
	 *            the new altitude state: 0 = level, 1 = climbing and -1 =
	 *            falling
	 */
	public void setAltitudeState(int state) {
		this.altitudeState = state;
	}

}