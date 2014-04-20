package lib;

import lib.jog.graphics;
import lib.jog.input;

public class ButtonText {

	public interface Action {
		public void action();
	}

	private enum Alignment {LEFT, CENTRE, RIGHT};
	private int x, y, width, height, ox, oy, size;
	private String text;
	private org.newdawn.slick.Color colourDefault, colourHover, colourUnavailable;
	private Action action;
	private boolean available;
	private Alignment alignment;

	public ButtonText(String text, Action action, int x, int y, int w, int h, int ox, int oy) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = ox;
		this.oy = oy;
		this.size = 1;
		colourDefault = new org.newdawn.slick.Color(0, 128, 0);
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public ButtonText(String text, Action action, int x, int y, int w, int h) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		this.size = 1;
		colourDefault = new org.newdawn.slick.Color(0, 128, 0);
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}
	
	public ButtonText(String text, Action action, Alignment alignment, int x, int y, int w, int h) {
		this.text = text;
		this.action = action;
		this.alignment = alignment;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		this.size = 1;
		colourDefault = new org.newdawn.slick.Color(0, 128, 0);
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}
	
	public ButtonText(String text, Action action, int x, int y, int w, int h, int ox, int oy, int size) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = ox;
		this.oy = oy;
		this.size = size;
		colourDefault = new org.newdawn.slick.Color(0, 128, 0);
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}
	
	public ButtonText(String text, Action action, int x, int y, int w, int h, int size) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		this.size = size;
		colourDefault = new org.newdawn.slick.Color(0, 128, 0);
		colourHover = new org.newdawn.slick.Color(128, 128, 128);
		colourUnavailable = new org.newdawn.slick.Color(64, 64, 64);
		available = true;
	}

	public boolean isMouseOver(int mx, int my) {
		return (mx >= x && mx <= x + width && my >= y && my <= y + height);
	}

	public boolean isMouseOver() { 
		return isMouseOver(input.mouseX(), input.mouseY()); 
	}
	/**
	 * Sets the string of text used
	 * @param newText - The string to be used
	 */
	public void setText(String newText) {
		text = newText;
	}

	/**
	 * Sets the button text to available - Changing the color to the one specified in ButtonText()
	 * @param available - value of the availability, either True or False
	 */
	public void setAvailability(boolean available) {
		this.available = available;
	}

	public void act() {
		if (!available) return;
		action.action();
	}
	/**
	 * Draws the button text which reacts to mouse interactions 
	 */
	public void draw() {
		if (!available) {
			graphics.setColour(colourUnavailable);
		}
		else if (isMouseOver()) {
			graphics.setColour(colourHover);
		} else {
			graphics.setColour(colourDefault);
		}
		
		if (alignment == Alignment.RIGHT) {
			graphics.printRight(text, x + ox, y + oy, size, 0);
		} else {
			graphics.print(text, x + ox, y + oy, size);
		}
	}

}
