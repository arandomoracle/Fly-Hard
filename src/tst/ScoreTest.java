package tst;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scn.Game;
import scn.SinglePlayerGame;
import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Airport;
import cls.Player;
import cls.Waypoint;

public class ScoreTest {
	
	/** The test score */
	int testScore;
	
	/** The test aircraft */
	Aircraft testAircraft;
	
	Airport testAirport;
	
	Airport testAirport2;
	
	Airport airports[];
	
	Player testPlayer;
	
	Aircraft[] aircrafts;
	
	/**
	 * Sets up the tests.
	 */
	@Before
	public void setUp() {
		testScore = 0;
		
		if (Game.getInstance() != null) {
			Game.getInstance().close();
		}
		
		SinglePlayerGame.createSinglePlayerGame(DifficultySetting.EASY);
		
		Waypoint[] waypointList = new Waypoint[] {
				new Waypoint(0, 0, true, false),
				new Waypoint(100, 100, true, false),
				new Waypoint(25, 75, false, false),
				new Waypoint(75, 25, false, false),
				new Waypoint(50,50, false, false)
				};
		airports = new Airport[] {
				new Airport("Babbage International", (1d/7d), (1d/2d)),
				new Airport("Eboracum Airport", (6d/7d), (1d/2d))
		};
		
		testPlayer = new Player(0, airports, waypointList);
		
		
		testAirport = new Airport("Babbage International", (1d/7d), (1d/2d));
		testAirport2 = new Airport("Babbage International", (1d/7d), (1d/2d));
		
		testAircraft = new Aircraft("TSTAircraft", "TestAir", "Berlin", "Dublin",
				new Waypoint(100, 100, true, false), new Waypoint(0, 0, true, false),
				10.0, waypointList, DifficultySetting.MEDIUM, null, null);	
		aircrafts = new Aircraft[1];
		
		
		testPlayer.getAircraft().add(testAircraft);
	}
	/**
	 * Tests that score of a player starts at zero.
	 */
	@Test
	public void scoreStartsAtZero() {
		assertTrue("Score does not start at correct value", testPlayer.getScore() == 0);
	}
	
	/**
	 * Tests that score is decremented when an aircraft's flight path is altered.
	 */
	@Test
	public void testScoreDecrementAlterPath() {
		testAircraft.alterPath(1, new Waypoint(25, 75, false, false));
		assertTrue("Score not decremented successfully", testAircraft.getScore()==90);
	}

	/**
	 * Tests that increase score method works correctly
	 */
	@Test
	public void testAddScoreToPlayer() {
		testPlayer.increaseScore(testAircraft.getScore());
		assertTrue("Score not added to player successfully", testPlayer.getScore() == testAircraft.getScore());
	}
	
}
