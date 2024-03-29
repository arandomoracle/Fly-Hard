package cls;

import java.io.Serializable;

import org.newdawn.slick.Color;

import lib.jog.graphics;

public class Waypoint implements Serializable {
	
	/** Serialisation ID */
	private static final long serialVersionUID = 1413476488063120300L;

	/** Leniency to allow mouse input to be accepted in a small area around the waypoint  */
	public static final int MOUSE_LENIENCY = 32;
	
	/** The radius of the waypoint image */
	public static final int WAYPOINT_ICON_RADIUS = 8;
	
	/** The vector position of the waypoint */
	protected Vector waypointLocation;
	
	/** The name to associate with this waypoint */
	protected String name;
	
	/** Marks whether the waypoint is an entry point, exit point or airport */
	protected boolean entryOrExit;
	
	/** The powerup of this waypoint */
	protected Powerup powerup = null;
	
	
	/**
	 * Constructor for waypoints.
	 * @param x - the x coordinate of the waypoint
	 * @param y - the y coordinate of the waypoint
	 * @param entryOrExit - whether the waypoint is a point where planes may
	 * 						enter and leave the airspace
	 * @param name - the waypoint's name
	 * @param relative - <code>true</code> if the co-ordinates are relative
	 * 						to the screen
	 */
	public Waypoint(double x, double y, boolean entryOrExit,
			String name, boolean relative) {
		if (relative) {
			this.waypointLocation = new Vector(x, y, 0, true);
		} else {
			this.waypointLocation = new Vector(x, y, 0);
		}
		
		this.entryOrExit = entryOrExit;
		this.name = name;
	}
	
	/**
	 * Constructor for waypoints.
	 * @param x - the x coordinate of the waypoint
	 * @param y - the y coordinate of the waypoint
	 * @param entryOrExit - whether the waypoint is a point where planes may
	 * 						enter and leave the airspace
	 * @param relative - <code>true</code> if the co-ordinates are relative
	 * 						to the screen
	 */
	public Waypoint(double x, double y, boolean entryOrExit, boolean relative) {
		if (relative) {
			this.waypointLocation = new Vector(x, y, 0, true);
		} else {
			this.waypointLocation = new Vector(x, y, 0);
		}
		
		this.entryOrExit = entryOrExit;
		this.name = "";
	}
	
	/**
	 * Constructor for waypoints.
	 * @param waypoint - the waypoint to copy
	 */
	protected Waypoint(Waypoint waypoint) {
		waypointLocation = (waypoint.waypointLocation != null)
				? waypoint.waypointLocation.clone() : null;
		name = waypoint.name;
		entryOrExit = waypoint.entryOrExit;
		powerup = (waypoint.powerup != null)
				? waypoint.powerup.clone() : null;
	}
	
	
	/**
	 * Gets the waypoint's vector location.
	 * @return the waypoint's location
	 */
	public Vector getLocation() {
		return waypointLocation ;
	}
	
	/**
	 * Gets whether the waypoint is an entry point, exit point or airport.
	 * @return <code>true</code> if the waypoint is an entry point, exit point
	 * 			or airport, otherwise <code>false</code>
	 */
	public boolean isEntryOrExit() {
		return this.entryOrExit;
	}
	
	/**
	 * Gets the name associated with this waypoint.
	 * <p>
	 * This will typically be the null string ("") unless the waypoint
	 * is an entry point, exit point or airport.
	 * </p>
	 * @return the waypoint's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Draws the waypoint.
	 */
	public void draw() {
		draw(waypointLocation.getX(),
				waypointLocation.getY(),
				graphics.red_transp);
	}
	
	/**
	 * Draws the waypoint.
	 * <p>
	 * Allows a colour to be specified.
	 * </p>
	 */
	public void draw(Color colour) {
		draw(waypointLocation.getX(),
				waypointLocation.getY(),
				colour);
	}
	
	/**
	 * Draws the waypoint.
	 * @param x - the x location to draw the waypoint at
	 * @param y - the y location to draw the waypoint at
	 */
	public void draw(double x, double y, Color colour) {
		if (this.isEntryOrExit()) {
			graphics.setColour(graphics.safetyOrange);
		} else if (powerup != null) {
			graphics.setColour(graphics.blue_transp);
		} else {
			graphics.setColour(colour);
		}
		
		graphics.circle(false, x-WAYPOINT_ICON_RADIUS/2 + 2,
				y-WAYPOINT_ICON_RADIUS/2 + 2, WAYPOINT_ICON_RADIUS);
		graphics.circle(true, x-WAYPOINT_ICON_RADIUS/2 + 2,
				y-WAYPOINT_ICON_RADIUS/2 + 2, WAYPOINT_ICON_RADIUS - 2);
	}
	
	/**
	 * Checks if the mouse is over the waypoint, within MOUSE_LENIENCY
	 * @param x - the mouse's x location
	 * @param y - the mouse's y location
	 * @return whether the mouse is over the waypoint
	 */
	public boolean isMouseOver(int x, int y) {
		double dx = waypointLocation.getX() - x;
		double dy = waypointLocation.getY() - y;
		return dx*dx + dy*dy < MOUSE_LENIENCY*MOUSE_LENIENCY;
	}
	
	/**
	 * Gets the cost of travelling between this waypoint and another.
	 * <p>
	 * Used for path finding.
	 * </p>
	 * @param fromPoint - the point to consider cost from
	 * @return the distance(cost) between the two waypoints
	 */
	public double getCost(Waypoint fromPoint) {
		return waypointLocation .sub(fromPoint.getLocation()).magnitude();
	}
	
	/**
	 * Gets the cost between two waypoints.
	 * @param source - the source waypoint
	 * @param target - the target waypoint
	 * @return the cost of travelling between the source and the target
	 */
	public static double getCostBetween(Waypoint source, Waypoint target) {
		return target.getCost(source);
	}

	/**
	 * Gets the powerup attached to the waypoint.
	 * @return the powerup attached to the waypoint
	 */
	public Powerup getPowerup() {
		return powerup;
	}
	
	/**
	 * Sets the powerup attached to the waypoint. If powerup is null, allow it to be set. If specified powerup is null, clear powerup
	 * @param powerup -  the powerup to attach to the waypoint
	 */
	public void setPowerup(Powerup powerup) {
		if (powerup == null){
			this.powerup = null;
		} else if (this.powerup == null) {
			this.powerup = powerup;
		}
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((waypointLocation == null) ? 0 : waypointLocation.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Waypoint other = (Waypoint) obj;
		if (waypointLocation == null) {
			if (other.waypointLocation != null)
				return false;
		} else if (!waypointLocation.equals(other.waypointLocation))
			return false;
		return true;
	}
	
	
	/**
	 * Clones the waypoint.
	 */
	public Waypoint clone() {
		return new Waypoint(this);
	}
	
}
