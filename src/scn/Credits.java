package scn;

import java.io.File;

import btc.Main;

import lib.jog.audio;
import lib.jog.audio.Music;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;

public class Credits extends Scene {
	
	/**
	 * default speed to scroll the credits
	 */
	private final static int SCROLL_SPEED = 64;
	
	private float speed;
	/**
	 * The position to print the credits text at. Initially offscreen
	 */
	private double scrollPosition;
	/**
	 * Music to play during the credits
	 */
	private Music music;

	/**
	 * Constructor
	 * @param main The main containing the scene
	 */
	public Credits() {
		super();
	}
	
	/**
	 * Initiate music, and the credits text to be offscreen
	 */
	@Override
	public void start() {
		speed = 1f;
		scrollPosition = -window.height();
		music = audio.newMusic("sfx" + File.separator + "piano.ogg");
		music.play();
	}

	@Override
	/**
	 * Update the credits's scroll position
	 * Speed up the credits movement if keys are pressed
	 */
	public void update(double time_difference) {
		boolean hurried = input.isKeyDown(input.KEY_SPACE) || input.isMouseDown(input.MOUSE_LEFT);
		speed = hurried ? 4f : 1f;
		scrollPosition += SCROLL_SPEED * time_difference * speed;
		if (scrollPosition > 1500) scrollPosition = -window.height();
	}

	@Override
	/**
	 * Print the credits based on the current scroll position
	 */
	public void draw() {
		graphics.setFont(Main.flightstripFontSuper);
		int gap = 64;
		int currentHeight = 0;
		graphics.setColour(graphics.safetyOrange);
		graphics.push();
		graphics.translate(0, scrollPosition);
		currentHeight += gap;
		graphics.printCentred("Fly Hard", 0, currentHeight, 3, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Created by", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team FLR:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Josh Adams", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Gareth Handley", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Sanjit Samaddar", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Alex Stewart", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Huw Taylor", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Stephen Webb)", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("Improved by", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team MQV:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Adam Al-jidy", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jakub Brezonak", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jack Chapman", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Liam Mullane", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Matt Munro", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Liam Wellacott", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("Finalised by", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team GOA:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Richard Aickin", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jaron Ali", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Emily Hall", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Sam Hopkins", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jon Howell", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Richard Kirby", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("Special Mentions", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Mark Woosey", 0, currentHeight, 2, window.width());

		currentHeight += gap * 2;

		graphics.printCentred("Music", 0, currentHeight, 2, window.width());
		graphics.printCentred("_____", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("____", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Retro 90's arcade machine", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Beep SFX", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap / 2;
		graphics.printCentred("www.freestockmusic.com", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Partners in Rhyme", 2*window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("FreeSound", window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap * 2;
		
		graphics.printCentred("External Libraries", 0, currentHeight, 2, window.width());
		graphics.printCentred("__________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("LWJGL", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Slick2D", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("JOG", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap * 2;
		
		graphics.printCentred("Thank you for playing!", 0, currentHeight, 2, window.width());
		graphics.pop();
	}

	/**
	 * Input handlers
	 */
	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	@Override
	public void keyPressed(int key) {}

	@Override
	/**
	 * Exit to the title screen if escape is pressed
	 */
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			Main.closeScene();
		}
	}
	
	@Override
	public void close() {
		music.stop();
	}

	@Override
	public void playSound(Sound sound) {		
	}

}
