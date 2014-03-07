package scn;

import java.io.File;

import cls.Aircraft;
import cls.Vector;
import lib.SpriteAnimation;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.window;
import btc.Main;

public class GameOver extends Scene {
	
	/** Text box to write the details of the game failure */
	private lib.TextBox textBox;
	
	// Used to position the explosion, and provide graphical feedback of how and where the player failed
	/** The first plane involved in the collision */
	private Aircraft aircraft1;
	
	/** The second plane involved in the collision */
	private Aircraft aircraft2;
	
	/** A vector storing the point which distances should be measured relative to */
	private Vector origin;
	
	/** A random number of deaths caused by the crash */
	private int deaths;
	
	/** The score the player achieved */
	private int score;
	
	/** The position of the crash - the vector midpoint of the positions of the two crashed planes */
	private Vector crash;
	
	/** A sprite animation to handle the frame by frame drawing of the explosion */
	private SpriteAnimation explosionAnim;
	
	/** The explosion image to use for the animation */
	private Image explosion;
	
	/** The value corresponding to the key which has most recently been pressed */
	private int keyPressed;
	
	/** Timer to allow for explosion and plane to be shown for a period, followed by the text box */
	private double timer;
	
	/**
	 * Constructor for the Game Over scene.
	 * @param main main containing the scene
	 * @param plane1 the first plane involved in the crash
	 * @param plane2 the second plane involved in the crash
	 * @param score the score achieved during gameplay
	 */
	public GameOver(Main main, Aircraft plane1, Aircraft plane2, int score) {
		super(main);
		
		// The number of frams in each dimension of the animation image
		int framesAcross = 8;
		int framesDown = 4;
		
		aircraft1 = plane1;
		aircraft2 = plane2;
		origin = new Vector(Game.xOffset, Game.yOffset, 0);
		
		crash = plane1.getPosition().add(new Vector((plane1.getPosition().getX() - plane2.getPosition().getX()) / 2,
				(plane1.getPosition().getY() - plane2.getPosition().getY()) / 2, 0)).add(origin);
		
		this.score = score;
		
		// Load explosion animation image
		explosion = graphics.newImage("gfx" + File.separator + "explosionFrames.png");
		
		Vector midPoint = aircraft1.getPosition().add(aircraft2.getPosition())
				.scaleBy(0.5).add(origin);
		Vector explosionPos = midPoint.sub(new Vector(explosion.width()/(framesAcross*2),
				explosion.height()/(framesDown*2), 0));
		
		explosionAnim = new SpriteAnimation(explosion,
				(int)explosionPos.getX(), (int)explosionPos.getY(),
				6, 16, framesAcross, framesDown, false);
	}
	
	/**
	 * Initialises the random number of deaths, timer, and text box with strings
	 * to be written about the game failure.
	 */
	@Override
	public void start() {
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		deaths = (int)(Math.random() * 500) + 300;
		timer = 0;
		textBox = new lib.TextBox(window.width() / 10, 186,
				window.width() - ((window.width() / 10) * 2), window.height() - 96, 32);
		
		textBox.addText(String.valueOf(deaths) + " people died in the crash.");
		textBox.delay(0.4);
		textBox.addText("British Bearways is facing heavy legal pressure from the family and loved-ones of the dead and an investigation is underway.");
		textBox.newline();
		textBox.delay(0.8);
		textBox.addText("The inquiry into your incompetence will lead to humanity discovering your true nature.");
		textBox.newline();
		textBox.delay(0.8);
		textBox.addText("The guilt for the death you have caused and your failure to pass as a human will gnaw at you and you will revert to drinking in an attempt to cope.");
		textBox.newline();
		textBox.newline();
		textBox.delay(0.8);
		textBox.addText("With no income, there will be no way your family can survive the fast-approaching winter months.");
		textBox.newline();
		textBox.newline();
		textBox.delay(0.8);
		textBox.addText("Game Over.");
	}

	/**
	 * If it runs before the explosion has finished, update the explosion
	 * otherwise, update text box instead.
	 */
	@Override
	public void update(double timeDifference) {
		if (explosionAnim.hasFinished()){
			timer += timeDifference;
			textBox.update(timeDifference);
		} else {
			explosionAnim.update(timeDifference);
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	/**
	 * Tracks if any keys are pressed when the game over screen begins.
	 * Prevents the scene instantly ending due to a key press from previous scene.
	 */
	@Override
	public void keyPressed(int key) {
		keyPressed = key;
	}

	/**
	 * Ends the scene if any key is released.
	 */
	@Override
	public void keyReleased(int key) {
		if (key == keyPressed) {
			main.closeScene();
			main.closeScene();
		}
	}

	@Override
	/**
	 * Draws game over
	 * If explosion has finished, draw the textbox; otherwise, draw the planes and explosion.
	 */
	public void draw() {
		graphics.setColour(graphics.green);
		graphics.printCentred(aircraft1.getName() + " crashed into " + aircraft2.getName()
				+ ".", 0, 32, 2, window.width());
		graphics.printCentred("Total score: " + String.valueOf(score), 0, 64, 4, window.width());
		if (explosionAnim.hasFinished()) {
			textBox.draw();
		} else {
			aircraft1.draw((int) aircraft1.getPosition().getZ(), origin);
			aircraft2.draw((int) aircraft1.getPosition().getZ(), origin);
			
			double radius = 20; // Radius of explosion
			graphics.setColour(graphics.red);
			graphics.circle(false, crash.getX() - 5, crash.getY() - 5, radius);
			
			explosionAnim.draw();
		}
		
		int opacity = (int)(255 * Math.sin(timer));
		graphics.setColour(0, 128, 0, opacity);
		graphics.printCentred("Press any key to continue", 0, window.height() - 256, 1, window.width());
	}

	@Override
	public void close() {}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}