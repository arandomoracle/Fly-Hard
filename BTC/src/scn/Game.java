package scn;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import lib.ButtonText;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Music;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;
import cls.Aircraft;
import cls.Airport;
import cls.FlightStrip;
import cls.OrdersBox;
import cls.Player;
import cls.Player.TurningState;
import cls.Waypoint;
import btc.Main;

public abstract class Game extends Scene {
	
	/** The distance between the left edge of the screen and the map area */
	public final static int X_OFFSET = 196;

	/** The distance between the top edge of the screen and the map area */
	public final static int Y_OFFSET = 48;

	private final int FLIGHTSTRIP_X = 16;

	private final int FLIGHTSTRIP_Y = window.height()/3;
	
	private final int FLIGHTSTRIP_W = 120;
	
	private final int FLIGHTSTRIP_H = 60;
	
	/** The image to use for aircraft */
	public static Image aircraftImage;

	/** The image to use for airports */
	public static Image airportImage;

	/** The unique instance of this class */
	protected static Game instance = null;
	
	/** The time since the scene began */
	protected static double timeElapsed;
	
	/** The music to play during the game scene */
	protected static Music music;

	/** The background to draw in the airspace */
	protected static Image background;

	/** The airports in the airspace */
	protected static Airport[] airports;

	/** The set of waypoints in the airspace which are entry/exit points */
	protected static Waypoint[] locationWaypoints;

	/** The waypoints through which aircraft must travel to reach their destination */
	protected static Waypoint[] airspaceWaypoints;

	/** Is the game paused */
	protected static boolean paused;
	
	/** The the game about to end */
	protected static boolean ending;
	
	/** The manual control buttons */
	protected static ButtonText manualControlButton;

	// PLEASE DO NOT REMOVE - this is very useful for debugging
	public static OrdersBox out;
	
	/** Difficulty settings: easy, medium and hard */
	public enum DifficultySetting {EASY, MEDIUM, HARD}
	
	/** The current player */
	protected Player player;

	// Testing FlightStrip output
	private static FlightStrip flightStrip;
	
	/** The current difficulty setting */
	protected DifficultySetting difficulty;


	// Constructors ---------------------------------------------------------------------

	/**
	 * Constructor for Game.
	 * @param difficulty - the difficulty the scene is to be initialised with
	 */
	public Game(DifficultySetting difficulty) {
		super();

		this.difficulty = difficulty;

		// Define airports
		airports = new Airport[] {
				new Airport("Mosgrizzly Airport", (1d/7d), (1d/2d)),
				new Airport("Mosbear Airport", (6d/7d), (1d/2d))
		};

		// Define entry and exit points
		locationWaypoints = new Waypoint[] {
				new Waypoint(8, 8,
						true, "North West Top Leftonia", false),
				new Waypoint(8, window.height() - (2 * Y_OFFSET) - 4,
						true, "100 Acre Woods", false),
				new Waypoint(window.width() - (2 * X_OFFSET) - 4, 8,
						true, "City of Rightson", false),
				new Waypoint(window.width() - (2 * X_OFFSET) - 4,
						window.height() - (2 * Y_OFFSET) - 4,
						true, "South Sea", false), airports[0], airports[1]
		};

		// Define other waypoints
		airspaceWaypoints = new Waypoint[] {
				new Waypoint(0.10, 0.18, false, true),
				new Waypoint(0.16, 0.66, false, true),
				new Waypoint(0.20, 0.42, false, true),
				new Waypoint(0.23, 0.90, false, true),
				new Waypoint(0.25, 0.30, false, true),
				new Waypoint(0.27, 0.70, false, true),
				new Waypoint(0.39, 0.21, false, true), //middle waypoints
				new Waypoint(0.39, 0.69, false, true), //middle waypoints
				new Waypoint(0.55, 0.10, false, true),
				new Waypoint(0.63, 0.78, false, true),
				new Waypoint(0.67, 0.20, false, true),
				new Waypoint(0.70, 0.50, false, true),
				new Waypoint(0.85, 0.80, false, true),
				new Waypoint(0.78, 0.78, false, true),
				new Waypoint(0.81, 0.16, false, true),
				new Waypoint(0.82, 0.42, false, true)
		};
	}


	// Implemented methods --------------------------------------------------------------

	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty.
	 */
	@Override
	public void start() {
		// Set up variables
		out = new OrdersBox(window.width() - X_OFFSET + 20,
				Y_OFFSET, X_OFFSET - 40, window.height() - (2 * Y_OFFSET), 30);
		paused = false;

		if (!Main.testing) {
			// Load in graphics
			background = graphics.newImage("gfx" + File.separator + "background_base.png");

			aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
			airportImage = graphics.newImage("gfx" + File.separator + "Airport.png");

			// Load in music
			music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");

			// Start the music
			//music.play(); TODO <- add this back in for release
		}

	if (!Main.testing) {
		flightStrip = new FlightStrip(FLIGHTSTRIP_X, FLIGHTSTRIP_Y, FLIGHTSTRIP_W, FLIGHTSTRIP_H);
	}

		// Reset game attributes
		timeElapsed = 0;
	}

	/**
	 * Update all objects within the scene, e.g. aircraft.
	 * <p>
	 * Also runs collision detection and generates a new flight if flight
	 * generation interval has been exceeded.
	 * </p>
	 */
	@Override
	public void update(double timeDifference) {
		if (paused) return;
		
		if (ending) Main.closeScene();

		// Update the time the game has run for
		timeElapsed += timeDifference;

		// Check if any aircraft in the airspace have collided
		checkCollisions(timeDifference);

		// Update the player
		updatePlayer(timeDifference, player);

		if (player.getSelectedAircraft() != null) {
			if (player.getSelectedAircraft().isManuallyControlled()) {
				// Handle directional control for a manually
				// controlled aircraft
				if (input.keyPressed(new int[] {input.KEY_LEFT, input.KEY_A})) {
					// Turn left when 'Left' or 'A' key is pressed
					player.setTurningState(TurningState.TURNING_LEFT);
				} else if (input.keyPressed(new int[] {input.KEY_RIGHT,
						input.KEY_D})) {
					// Turn right when 'Right' or 'D' key is pressed
					player.setTurningState(TurningState.TURNING_RIGHT);
				} else {
					// Clear the turning state
					player.setTurningState(TurningState.NOT_TURNING);
				}
			} else if (input.keyPressed(new int[] {input.KEY_LEFT, input.KEY_A,
					input.KEY_RIGHT, input.KEY_D})) {
				// If any of the directional keys is pressed, set
				// selected aircraft to manual control
				toggleManualControl(player);
			}

			// Handle altitude controls
			if (input.keyPressed(new int[] {input.KEY_S, input.KEY_DOWN})
					&& (player.getSelectedAircraft()
							.getPosition().getZ() > 28000)) {
				player.getSelectedAircraft()
				.setAltitudeState(Aircraft.ALTITUDE_FALL);
			} else if (input.keyPressed(new int[] {input.KEY_W, input.KEY_UP})
					&& (player.getSelectedAircraft()
							.getPosition().getZ() < 30000)) {
				player.getSelectedAircraft()
				.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
			}
		}

		// Update debug box
		// PLEASE DO NOT REMOVE - this is very useful for debugging
		out.update(timeDifference);
	}
	
	/**
	 * Updates a player's attributes.
	 * <p>
	 * This updates aircraft, airports etc.
	 * </p>
	 * @param timeDifference - the time since the last update
	 * @param player - the player to update
	 */
	protected void updatePlayer(double timeDifference, Player player) {
		// Update aircraft
		for (Aircraft aircraft : player.getAircraft()) {
			aircraft.update(timeDifference);
		}

		// Deselect and remove any aircraft which have completed their routes
		for (int i = player.getAircraft().size() - 1; i >= 0; i--) {
			if (player.getAircraft().get(i).isFinished()) {
				if (player.getAircraft().get(i).equals(player
						.getSelectedAircraft())) {
					deselectAircraft(player);
				}

				player.getScore().addScore(player.getAircraft().get(i));
				player.getAircraft().remove(i);
			}
		}

		// Update the airports
		for (Airport airport : player.getAirports()) {
			airport.update(player.getAircraft());
		}

		// Deselect any aircraft which are outside the airspace
		// This ensures that players can't keep controlling aircraft
		// after they've left the airspace
		for (Aircraft airc : player.getAircraft()) {
			if (!(airc.isAtDestination())) {
				if (airc.isOutOfAirspaceBounds()) {
					deselectAircraft(airc, player);
				}
			}
		}
		
		// Handle turning
		if (player.getSelectedAircraft() != null
				&& player.getSelectedAircraft().isManuallyControlled()) {
			if (player.isTurningLeft()) {
				player.getSelectedAircraft().turnLeft(timeDifference);
			} else if (player.isTurningRight()) {
				player.getSelectedAircraft().turnRight(timeDifference);
			}
		}

		// Update the counter used to determine when another flight should
		// enter the airspace
		// If the counter has reached 0, then spawn a new aircraft
		player.setFlightGenerationTimeElapsed(player
				.getFlightGenerationTimeElapsed() + timeDifference);
		
		if (player.getFlightGenerationTimeElapsed()
				>= getFlightGenerationInterval(player)) {
			player.setFlightGenerationTimeElapsed(
					player.getFlightGenerationTimeElapsed()
					- getFlightGenerationInterval(player));

			if (player.getAircraft().size() < player.getMaxAircraft()) {
				generateFlight(player);
			}
		}

		// If there are no aircraft in the airspace, spawn a new aircraft
		//if (player.getAircraft().size() == 0) generateFlight(player);
	}

	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints.
	 */
	@Override
	public void draw() {
		// Draw the rectangle surrounding the map area
		graphics.setColour(graphics.green);
		graphics.rectangle(false, X_OFFSET, Y_OFFSET, window.width() - (2 * X_OFFSET),
				window.height() - (2 * Y_OFFSET));

		// Set the viewport - this is the boundary used when drawing objects
		graphics.setViewport(X_OFFSET, Y_OFFSET, window.width() - (2 * X_OFFSET),
				window.height() - (2 * Y_OFFSET));

		// Draw the map background
		graphics.setColour(255, 255, 255, 48);
		graphics.drawScaled(background, 0, 0,
				Math.max(Main.getXScale(), Main.getYScale()));

		// Draw individual map features
		drawMapFeatures();
		
		// Reset the viewport - these statistics can appear outside the game
		// area
		graphics.setViewport();
		drawAdditional(getAllAircraft().size());

		// Temp flightstrip draw call.
		flightStrip.draw();

		// Draw debug box
		// PLEASE DO NOT REMOVE - this is very useful for debugging
		out.draw();
	}

	/**
	 * Draws map features.
	 */
	protected void drawMapFeatures() {
		drawAirports(player);
		drawWaypoints(player);
		drawAircraft(player);
		drawSelectedAircraft();
		drawManualControlButton(player);
	}

	/**
	 * Draws aircraft.
	 * <p>
	 * Calls the aircraft.draw() method for each aircraft.
	 * </p>
	 * <p>
	 * Also draws flight paths, and the manual control compass.
	 * </p>
	 */
	protected void drawAircraft(Player player) {
		graphics.setColour(255, 255, 255);

		// Draw all aircraft, and show their routes if the mouse is hovering
		// above them
		for (Aircraft aircraft : player.getAircraft()) {
			aircraft.draw(player.getAircraftColour(), player.getControlAltitude());

			//draw the score of each aircraft
			aircraft.drawScore();
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
	}
	
	/**
	 * Draws additional features around the selected aircraft.
	 */
	protected void drawSelectedAircraft() {
		if (player.getSelectedAircraft() != null) {
			// If the selected aircraft is under manual control,
			// draw a directional compass around it
			if (player.getSelectedAircraft().isManuallyControlled()) {
				player.getSelectedAircraft().drawCompass();
			}

			if (player.getSelectedAircraft() != null) {
				// If the selected aircraft's flight path is being manipulated,
				// draw the manipulated path
				if (player.getSelectedWaypoint() != null
						&& !player.getSelectedAircraft().isManuallyControlled()) {
					player.getSelectedAircraft().drawModifiedPath(
							player.getSelectedPathpoint(),
							input.mouseX() - X_OFFSET,
							input.mouseY() - Y_OFFSET);
				}

				// Draw the selected aircraft's flight path
				player.getSelectedAircraft().drawFlightPath(true);
				graphics.setColour(graphics.green);
			}
		}
	}

	/**
	 * Draws waypoints.
	 * <p>
	 * Calls the waypoint.draw() method for each waypoint, excluding airport
	 * waypoints.
	 * </p>
	 * <p>
	 * Also prints the names of the entry/exit points.
	 * </p>
	 */
	protected void drawWaypoints(Player player) {
		// Draw all waypoints, except airport waypoints
		for (Waypoint waypoint : player.getWaypoints()) {
			if (!(waypoint instanceof Airport)) {
				waypoint.draw();
			}
		}

		// Draw entry/exit points
		graphics.setColour(graphics.green);

		graphics.print(locationWaypoints[0].getName(),
				locationWaypoints[0].getLocation().getX() + 9,
				locationWaypoints[0].getLocation().getY() - 6);
		graphics.print(locationWaypoints[1].getName(),
				locationWaypoints[1].getLocation().getX() + 9,
				locationWaypoints[1].getLocation().getY() - 6);
		graphics.print(locationWaypoints[2].getName(),
				locationWaypoints[2].getLocation().getX() - 141,
				locationWaypoints[2].getLocation().getY() - 6);
		graphics.print(locationWaypoints[3].getName(),
				locationWaypoints[3].getLocation().getX() - 91,
				locationWaypoints[3].getLocation().getY() - 6);
	}

	/**
	 * Draws airports.
	 * <p>
	 * Calls the airport.draw() method for each airport.
	 * </p>
	 * <p>
	 * Also prints the names of the airports.
	 * </p>
	 */
	protected void drawAirports(Player player) {
		// Draw the airports
		for (Airport airport : player.getAirports()) {
			graphics.setColour(255, 255, 255, 64);
			airport.draw();
		}

		// Draw the airport names
		graphics.setColour(graphics.green);

		graphics.print(locationWaypoints[4].getName(),
				locationWaypoints[4].getLocation().getX() - 20,
				locationWaypoints[4].getLocation().getY() + 25);
		graphics.print(locationWaypoints[5].getName(),
				locationWaypoints[5].getLocation().getX() - 20,
				locationWaypoints[5].getLocation().getY() + 25);
	}

	/**
	 * Draws the manual control button.
	 */
	protected void drawManualControlButton(Player player) {
		if (player.getSelectedAircraft() != null) {
			graphics.setColour(graphics.green);
			// Display the manual control button
			graphics.setColour(graphics.black);
			graphics.rectangle(true, (window.width() - 128 - (2 * X_OFFSET)) / 2,
					32, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false, (window.width() - 128 - (2 * X_OFFSET)) / 2,
					32, 128, 32);
			manualControlButton.draw();
		}
	}

	/**
	 * Draws a readout of the time the game has been played for, and number of planes
	 * in the sky.
	 */
	protected void drawAdditional(int aircraftCount) {
		graphics.setColour(graphics.green);

		// Get the time the game has been played for
		int hours = (int)(timeElapsed / (60 * 60));
		int minutes = (int)(timeElapsed / 60);
		minutes %= 60;
		double seconds = timeElapsed % 60;

		// Display this in the form 'hh:mm:ss'
		DecimalFormat df = new DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes)
				+ df.format(seconds);

		// Print this to the screen
		graphics.print(timePlayed, window.width() - X_OFFSET
				- (timePlayed.length() * 8 + 32), 32);

		// Print the highlighted altitude to the screen TODO <- check with Mark
		//graphics.print(String.valueOf("Highlighted altitude: " + Integer
		//		.toString(highlightedAltitude)) , 32 + xOffset, 15);

		// Print the number of aircraft in the airspace to the screen
		graphics.print(String.valueOf(aircraftCount)
				+ " aircraft in the airspace.", 32 + X_OFFSET, 32);
	}

	/**
	 * Plays the music attached to the game.
	 */
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}


	// Event handling -------------------------------------------------------------------

	/**
	 * Handles mouse click events.
	 * @param key - the button which was pressed
	 * @param x - the x position of the click event
	 * @param y - the y position of the click event
	 */
	@Override
	public void mousePressed(int key, int x, int y) {
		if (paused) return;

		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y, player)) {
				// If an aircraft has been clicked, select it
				Aircraft clickedAircraft = findClickedAircraft(x, y, player);
				deselectAircraft(player);
				player.setSelectedAircraft(clickedAircraft);
				flightStrip.show(clickedAircraft);
			} else if (waypointInFlightplanClicked(x, y,
					player.getSelectedAircraft(), player)
					&& !player.getSelectedAircraft().isManuallyControlled()) {
				// If a waypoint in the currently selected aircraft's flight
				// plan has been clicked, save this waypoint to the
				// clicked waypoint attribute
				player.setSelectedWaypoint(findClickedWaypoint(x, y, player));
				if (player.getSelectedWaypoint() != null) {
					if (!player.getSelectedWaypoint().isEntryOrExit()) {
						player.setWaypointClicked(true); // Flag to mouseReleased
						player.setSelectedPathpoint(player.getSelectedAircraft()
								.getFlightPlan()
								.indexOfWaypoint(player.getSelectedWaypoint()));
					} else {
						// If the clicked waypoint is an entry/exit point, discard it
						// as we don't want the user to be able to move these points
						player.setSelectedWaypoint(null);
					}
				}

			}

			for (Airport airport : player.getAirports()) {
				if (player.getSelectedAircraft() != null
						&& airport.isArrivalsClicked(x, y)) {
					if ((player.getSelectedAircraft().isWaitingToLand)
							&& (player.getSelectedAircraft()
									.currentTarget.equals(airport.getLocation()))) {
						// If arrivals is clicked, and the selected aircraft
						// is waiting to land at that airport, cause the aircraft
						// to land
						airport.mousePressed(key, x, y);
						player.getSelectedAircraft().land();
						deselectAircraft(player);
					}
				} else if (airport.isDeparturesClicked(x, y)) {
					if (airport.aircraftHangar.size() > 0) {
						// If departures is clicked, and there is a flight waiting
						// to take off, let it take off
						airport.mousePressed(key, x, y);
						airport.signalTakeOff();
					}
				}
			}
		} else if (key == input.MOUSE_RIGHT) {
			if (aircraftClicked(x, y, player)) {
				deselectAircraft(player);
				player.setSelectedAircraft(findClickedAircraft(x, y, player));
			}

			if (player.getSelectedAircraft() != null) {
				if (compassClicked(x, y, player.getSelectedAircraft())) {
					player.setCompassClicked(true); // Flag to mouseReleased
					if (!player.getSelectedAircraft().isManuallyControlled()) {
						toggleManualControl(player);
					}
				} else {
					if (player.getSelectedAircraft().isManuallyControlled()) {
						toggleManualControl(player);
					} else {
						deselectAircraft(player);
					}
				}
			}
		}
	}

	/**
	 * Handles mouse release events.
	 * @param key - the button which was pressed
	 * @param x - the x position of the release event
	 * @param y - the y position of the release event
	 */
	@Override
	public void mouseReleased(int key, int x, int y) {
		if (paused) return;

		for (Airport airport : player.getAirports()) {
			airport.mouseReleased(key, x, y);
		}

		if (key == input.MOUSE_LEFT) {
			if (manualOverridePressed(x, y, player)) {
				manualControlButton.act();
			} else if (player.isWaypointClicked() && player.getSelectedAircraft() != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y, player);
				if (newWaypoint != null) {
					player.getSelectedAircraft().alterPath(player.getSelectedPathpoint(),
							newWaypoint);
				}

				player.setSelectedPathpoint(-1);
			}
			// Fine to set to null now as will have been dealt with
			player.setSelectedWaypoint(null);
		} else if (key == input.MOUSE_RIGHT) {
			if (player.isCompassClicked() && player.getSelectedAircraft() != null) {
				double dx = (input.mouseX() - X_OFFSET)
						- player.getSelectedAircraft().getPosition().getX()
						- 8;
				double dy = (input.mouseY() - Y_OFFSET)
						- player.getSelectedAircraft().getPosition().getY()
						- 8;
				double newBearing = Math.atan2(dy, dx);
				player.getSelectedAircraft().setBearing(newBearing);
			}
		} else if (key == input.MOUSE_WHEEL_UP) {
			player.setControlAltitude(30000);
		} else if (key == input.MOUSE_WHEEL_DOWN){
			player.setControlAltitude(28000);
		}
	}

	/**
	 * Handles key press events.
	 * @param key - the key which was pressed
	 */
	@Override
	public void keyPressed(int key) {
		if (paused) return;
	}

	/**
	 * Handles key release events.
	 * @param key - the key which was pressed
	 */
	@Override
	public void keyReleased(int key) {
		// Ensure p and escape still work when paused
		if (key == input.KEY_P) {
			paused = !paused;
		} else if (key == input.KEY_ESCAPE) {
			paused = false;
		}

		if (paused) return;

		switch (key) {
		case input.KEY_SPACE :
			toggleManualControl(player);
			break;
		case input.KEY_LCRTL :
			generateFlight(player);
			break;
		case input.KEY_ESCAPE :
			player.getAircraft().clear();
			for (Airport airport : player.getAirports()) airport.clear();
			Main.closeScene();
			break;
		case input.KEY_F5 :
			Aircraft a1 = createAircraft(player);
			Aircraft a2 = createAircraft(player);
			gameOver(a1, a2);
			break;
		}
	}


	// Game ending ----------------------------------------------------------------------

	/**
	 * Check if any aircraft in the airspace have collided.
	 * @param timeDifference - the time since the last collision check
	 */
	protected void checkCollisions(double timeDifference) {
		for (Aircraft plane : getAllAircraft()) {
			int collisionState = plane.updateCollisions(timeDifference,
					getAllAircraft());
			if (collisionState >= 0) {
				gameOver(plane, getAllAircraft().get(collisionState));
				return;
			}
		}
	}

	/**
	 * Handle a game over caused by two planes colliding.
	 * Create a GameOver scene and make it the current scene.
	 * @param plane1 - the first plane involved in the collision
	 * @param plane2 - the second plane in the collision
	 */
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		player.getAircraft().clear();

		for (Airport airport : player.getAirports()) {
			airport.clear();
		}

		// TODO <- add back in for release
		//playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));

		Main.closeScene();
		Main.setScene(new GameOver(plane1, plane2, player.getScore().getScore())); //TODO <- pass score
	}

	/**
	 * Cleanly exit by stopping the scene's music.
	 */
	@Override
	public void close() {
		music.stop();
		instance = null;
	}


	// Helper methods -------------------------------------------------------------------

	/**
	 * The interval in seconds to generate flights after.
	 * @param player - the player to get the flight generation time for
	 */
	protected int getFlightGenerationInterval(Player player) {
		switch (difficulty) {
		case MEDIUM:
			// Planes move 2x faster on medium so this makes them spawn
			// 2 times as often to keep the ratio
			return (30 / (player.getMaxAircraft() * 2));
		case HARD:
			// Planes move 3x faster on hard so this makes them spawn
			// 3 times as often to keep the ratio
			return (30 / (player.getMaxAircraft() * 3) );
		default:
			return (30 / player.getMaxAircraft());
		}
	}

	/**
	 * Creates a new aircraft object and introduces it to the airspace.
	 * @param player - generates a new aircraft for the specified player
	 */
	protected void generateFlight(Player player) {
		Aircraft aircraft = createAircraft(player);

		if (aircraft != null && player != null) {
			// If the aircraft starts at an airport, add it to that airport
			for (Airport airport : player.getAirports()) {
				if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
					airport.addToHangar(aircraft);
					return;
				}
			}

			// Otherwise, add the aircraft to the airspace
			player.getAircraft().add(aircraft);
		}
	}

	/**
	 * Handle aircraft creation.
	 * @return the created aircraft object
	 */
	protected Aircraft createAircraft(Player player) {
		String destinationName;
		String originName = "";
		Waypoint originPoint = null;
		Waypoint destinationPoint;
		Airport originAirport = null;
		Airport destinationAirport = null;

		// Get a list of this player's location waypoints
		Waypoint[] playersLocationWaypoints = getLocationWaypoints(player);

		// Get a list of location waypoints where a crash would not be immediate
		ArrayList<Waypoint> availableOrigins = getAvailableEntryPoints(player);

		if (availableOrigins.isEmpty()) {
			int randomAirport = Main.getRandom()
					.nextInt((player.getAirports().length - 1) + 1);

			if (player.getAirports()[randomAirport].aircraftHangar.size()
					== player.getAirports()[randomAirport].getHangarSize()) {
				return null;
			} else {
				originAirport = player.getAirports()[randomAirport];
				originPoint = player.getAirports()[randomAirport]
						.getDeparturesCentre();
				originName = player.getAirports()[randomAirport].name;
			}
		} else {
			originPoint = availableOrigins.get(
					Main.getRandom().nextInt((availableOrigins.size() - 1) + 1));

			// If random point is an airport, use its departures location
			if (originPoint instanceof Airport) {
				originAirport = ((Airport) originPoint);
				originName = originPoint.name;
				originPoint = ((Airport) originPoint).getDeparturesCentre();
			} else {
				for (int i = 0; i < playersLocationWaypoints.length; i++) {
					if (playersLocationWaypoints[i].equals(originPoint)) {
						originName = playersLocationWaypoints[i].getName();
						break;
					}
				}
			}
		}

		// Generate a destination
		// Keep trying until the random destination is not equal to the chosen origin
		// Also, if origin is an airport, prevent destination from being an airport
		int destination = 0;

		do {
			destination = Main.getRandom()
					.nextInt((playersLocationWaypoints.length - 1) + 1);
			destinationName = playersLocationWaypoints[destination].getName();
			destinationPoint = playersLocationWaypoints[destination];
		} while (destinationName.equals(originName) ||
				((getAirportFromName(originName) != null)
						&& (getAirportFromName(destinationName) != null)));

		// If destination is an airport, flag it
		if (destinationPoint instanceof Airport) {
			destinationAirport = (Airport) destinationPoint;
		}

		String carrier = "";
		String carrierTag = "";

		// Assign a random airline to the flight and generate tag for flightName.
		switch(Main.getRandom().nextInt(8)) {		//Generates random number between 0-5
			case 0:
				carrier = "Doge Air";
				carrierTag = "DG";
				break;
			case 1:
				carrier = "Britaniair";
				carrierTag = "BA";
				break;
			case 2:
				carrier = "KDT";
				carrierTag = "KT";
				break;
			case 3:
				carrier = "Canadair";
				carrierTag = "CA";
				break;
			case 4:
				carrier = "Wandairline";
				carrierTag = "WZ";
				break;
			case 5:
				carrier = "Wow Such Air";
				carrierTag = "WW";
				break;
			case 6:
				carrier = "Planet Express";
				carrierTag = "PX";
				break;
			case 7:
				carrier = "Aerobonia";
				carrierTag = "AR";
				break;
			default:
				Exception e = new Exception("Invalid carrier: " + carrier
					+ ".");
					e.printStackTrace();
					break;
		}

		// Generate a unique, random flight name, using carrierTag as prefix
		String name = "";
		boolean nameTaken = true;

		while (nameTaken) {
			name = carrierTag + String.format("%03d", (int)(1 + Main.getRandom().nextInt(999)));

			// Check the generated name against every other flight name
			boolean foundName = false;
			for (Aircraft a : getAllAircraft()) {
				if (a.getName().equals(name)) {
					foundName = true;
					break;
				}
			}
			
			if (!foundName) {
				nameTaken = false;
			}
		}

		// Generate a random speed, centred around 37
		int speed = 32 + (int)(Main.getRandom().nextInt(10));

		return new Aircraft(name, carrier, destinationName, originName,
				destinationPoint, originPoint, speed,
				player.getWaypoints(), difficulty, originAirport,
				destinationAirport);
	}

	/**
	 * Causes deselection of the selected aircraft.
	 * @param player
	 * 			the player to reset the selected plane attribute for
	 */
	public void deselectAircraft(Player player) {
		deselectAircraft(player.getSelectedAircraft(), player);
	}

	/**
	 * Causes deselection of the specified aircraft.
	 * @param aircraft
	 * 			the aircraft to deselect
	 * @param player
	 * 			the player to reset the selected plane attribute for
	 */
	protected void deselectAircraft(Aircraft aircraft, Player player) {
		if (aircraft != null && aircraft.isManuallyControlled()) {
			aircraft.toggleManualControl();
			manualControlButton.setText(" Take Control");
		}

		if (aircraft != null && aircraft.equals(player.getSelectedAircraft())) {
			player.setSelectedAircraft(null);
		}

		player.setSelectedWaypoint(null); 
		player.setSelectedPathpoint(-1);
	}

	/**
	 * Causes a player's selected aircraft to call methods to toggle manual control.
	 */
	protected void toggleManualControl(Player player) {
		Aircraft selectedAircraft = player.getSelectedAircraft();

		if (selectedAircraft == null) return;

		selectedAircraft.toggleManualControl();
		manualControlButton.setText(
				(selectedAircraft.isManuallyControlled() ?
						"Remove" : " Take") + " Control");
	}

	/**
	 * Returns an array of location waypoints for the specified player.
	 * @param player - the player whose entry points should be checked
	 * @return a list of available entry points
	 */
	public Waypoint[] getLocationWaypoints(Player player) {
		ArrayList<Waypoint> locationWaypoints = new ArrayList<Waypoint>();

		// Only check location waypoints which are under the players' control
		Waypoint[] playersLocationWaypoints = player.getWaypoints();

		for (Waypoint entryPoint : playersLocationWaypoints) {
			if (entryPoint.isEntryOrExit()) {
				locationWaypoints.add(entryPoint);
			}
		}

		return locationWaypoints.toArray(
				new Waypoint[locationWaypoints.size()]);
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane.
	 * <p>
	 * Specifically, returns points where no plane is currently going to exit the
	 * airspace there, also it is not too close to any plane.
	 * </p>
	 * @param player - the player whose entry points should be checked
	 * @return a list of available entry points
	 */
	public ArrayList<Waypoint> getAvailableEntryPoints(Player player) {
		ArrayList<Waypoint> availableEntryPoints = new ArrayList<Waypoint>();

		// Only check location waypoints which are under the players' control
		Waypoint[] playersLocationWaypoints = getLocationWaypoints(player);

		for (Waypoint entryPoint : playersLocationWaypoints) {
			boolean isAvailable = true;
			// Prevents spawning a plane at a waypoint if:
			//   - any plane is currently going towards it
			//   - or any plane is less than 250 from it
			
			for (Aircraft aircraft : getAllAircraft()) {
				// Check if any plane is currently going towards the
				// exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close
				// from the chosen originPoint
				if (aircraft.currentTarget.equals(entryPoint.getLocation())
						|| aircraft.isCloseToEntry(entryPoint.getLocation())) {
					isAvailable = false;
				}
			}

			if (isAvailable) {
				availableEntryPoints.add(entryPoint);
			}
		}
		
		return availableEntryPoints;
	}

	/**
	 * Returns whether a given name is an airport or not.
	 * @param name
	 * 			the name to test
	 * @return <code>true</code> if the name matches an airport name,
	 * 			otherwise <code>false</code>
	 */
	public Airport getAirportFromName(String name) {
		for (Airport airport : player.getAirports()) {
			// If a match is found, return true
			if (airport.name.equals(name)) return airport;
		}

		// Otherwise
		return null;
	}


	// Click event helpers --------------------------------------------------------------

	/**
	 * Gets whether the manual control compass has been clicked or not.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param aircraft
	 * 			the aircraft whose compass region should be checked
	 * @return <code>true</code> if the compass has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected boolean compassClicked(int x, int y, Aircraft aircraft) {
		if (aircraft != null) {
			double dx = aircraft.getPosition().getX() - x + X_OFFSET;
			double dy = aircraft.getPosition().getY() - y + Y_OFFSET;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		return false;
	}

	/**
	 * Gets whether an aircraft has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose aircraft should be checked
	 * @return <code>true</code> if an aircraft has been clicked,
	 * 			otherwise <code>false</code>
	 */
	protected boolean aircraftClicked(int x, int y, Player player) {
		return (findClickedAircraft(x, y, player) != null);
	}

	/**
	 * Gets the aircraft which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose aircraft should be checked
	 * @return if an aircraft was clicked, returns the corresponding aircraft object,
	 * 			otherwise returns null
	 */
	protected Aircraft findClickedAircraft(int x, int y, Player player) {
		for (Aircraft a : player.getAircraft()) {
			if (a.isMouseOver(x - X_OFFSET, y - Y_OFFSET)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Gets whether a waypoint in an aircraft's flight plan has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose waypoints should be searched
	 * @return <code>true</code> if a waypoint in an aircraft's flight plan
	 * 			has been clicked, otherwise <code>false</code>
	 */
	protected boolean waypointInFlightplanClicked(int x, int y,
			Aircraft aircraft, Player player) {
		Waypoint clickedWaypoint = findClickedWaypoint(x, y, player);
		return (clickedWaypoint != null) && (aircraft != null)
				&& (aircraft.getFlightPlan().indexOfWaypoint(clickedWaypoint) > -1);
	}

	/**
	 * Gets the waypoint which has been clicked.
	 * @param x
	 * 			the x position of the mouse
	 * @param y
	 * 			the y position of the mouse
	 * @param player
	 * 			the player whose waypoints should be searched
	 * @return if a waypoint was clicked, returns the corresponding waypoint object,
	 * 			otherwise returns null
	 */
	protected Waypoint findClickedWaypoint(int x, int y, Player player) {
		for (Waypoint w : player.getWaypoints()) {
			if (w.isMouseOver(x - X_OFFSET, y - Y_OFFSET)) {
				return w;
			}
		}
		return null;
	}

	/**
	 * Gets whether the manual control button has been clicked.
	 * @param x
	 * 			the mouse's x position
	 * @param y
	 * 			the mouse's y position
	 * @param player
	 * 			the player to check this for
	 * @return <code>true</code> if the manual control button has been
	 * 			pressed, otherwise <code>false</code>
	 */
	protected boolean manualOverridePressed(int x, int y, Player player) {
		return manualControlButton.isMouseOver(x - X_OFFSET, y - Y_OFFSET);
	}


	// Accessors ------------------------------------------------------------------------

	/**
	 * Gets the current instance of the game.
	 * @return the current game
	 */
	public static Game getInstance() {
		return instance;
	}

	/**
	 * Gets the current player.
	 * @return the current player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets a list of all aircraft in the airspace.
	 * @return a list of all the aircraft in the airspace
	 */
	public ArrayList<Aircraft> getAllAircraft() {
		return player.getAircraft();
	}

	/**
	 * Gets a player from an aircraft.
	 * @param aircraft - the aircraft to get the controlling player of
	 * @return the player controlling the specified aircraft
	 */
	public Player getPlayerFromAircraft(Aircraft aircraft) {
		for (Aircraft a : player.getAircraft()) {
			if (a.equals(aircraft)) {
				return player;
			}
		}
		
		return null;
	}

	/**
	 * Gets a player from an airport.
	 * @param airport - the airport to get the controlling player of
	 * @return the player controlling the specified airport
	 */
	public Player getPlayerFromAirport(Airport airport) {
		for (int i = 0; i < player.getAirports().length; i++) {
			if (player.getAirports()[i].equals(airport)) {
				return player;
			}
		}

		return null;
	}

	/**
	 * Gets a list of all airports in the airspace.
	 * @return a list of all the airports in the airspace
	 */
	public Airport[] getAllAirports() {
		return player.getAirports();
	}
	
	/**
	 * Gets an aircraft from its name.
	 * @param name - the aircraft's name
	 * @return the aircraft with the specified name
	 */
	public Aircraft getAircraftFromName(String name) {
		for (Aircraft a : getAllAircraft()) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
	
		return null;
	}

	/**
	 * Gets how long the game has been played for.
	 * @return the length of time the game has been running for
	 */
	public double getTime() {
		return timeElapsed;
	}
	

	// Mutators -------------------------------------------------------------------------

	/**
	 * Sets the current player.
	 * @param player - the player to set as the current player
	 */
	public void setCurrentPlayer(Player player) {
		this.player = player;
	}
	
	public void setEnding(boolean end) {
		ending = end;
	}


	// Deprecated -----------------------------------------------------------------------

	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class).
	 * Its purpose is to initialise the list of aircraft.
	 */
	@Deprecated
	public abstract void initializeAircraftArray();

}
