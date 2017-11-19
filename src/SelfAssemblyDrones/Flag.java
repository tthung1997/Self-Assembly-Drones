/**
 * 
 */
package SelfAssemblyDrones;

/**
 * Implementation of the self-assembly drone model
 * 
 * A flag is a place-holder for a pixel in the drawing
 * 
 * @author Team WINTER SLAYERS
 * 
 */
public class Flag {
	
	private int x;
	private int y;
	
	public Flag(int x, int y) {
		this.x = x;
		this.y = y;
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
	
}
