/**
 * 
 */
package SelfAssemblyDrones;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * @author TRIEU HUNG
 *
 */
public class Flag {
	
	private int x;
	private int y;
	
	public Flag(int x, int y) {
		this.x = y;
		this.y = y;
	}

	public Flag() {
		
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	
}
