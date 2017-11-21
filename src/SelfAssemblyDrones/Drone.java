package SelfAssemblyDrones;

import java.util.*;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * 
 */

/**
 * Implementation of the self-assembly drone model
 * 
 * The drones look for a nearest promising location and update their
 * map through communicating with others in a local area.
 * 
 * @author Team WINTER SLAYERS
 */
public class Drone {

	private int 		edgeOfComm;	// Drone's edge of communication
	private int 		noise;		// Level of noise	
	private int 		sharingType;// Map sharing strategy
	/**
	 * Locations on map will be marked as follow:
	 * 0 - unknown cell
	 * 1 - visited empty cell
	 * 2 - empty flag
	 * 3 - occupied flag
	 */
	private int[][] 	ownMap;		// Drone's explored map
	private int[][][] 	othersMap;	// Map from others
	private int[][] 	map;		// Combination of two maps above
	private boolean 	stayed;		// Drone's moving or not
	private int 		gridSize;	// Size of the grid
	private GridPoint 	targetLoc;	// Drone's current target
	private List<Drone> neighbors;	// Drone's neighbors
	
	public Drone(int gridSize) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		//modify the input value so that it meets the program's constraint
		edgeOfComm = modifiedEdgeOfComm((Integer)p.getValue("edgeOfComm"));
		sharingType = modifiedSharingType((Integer)p.getValue("sharingType"));
		noise = modifiedNoise((Integer)p.getValue("noise"));
		this.gridSize = gridSize;
		ownMap = new int[gridSize][gridSize];
		othersMap = new int[gridSize][gridSize][4];
		map = new int[gridSize][gridSize];
		stayed = false;
		targetLoc = null;
		neighbors = new ArrayList<Drone>();
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// Gather information from the local area
		getNeighbors();
		// If there is at least one neighbor
		if (!neighbors.isEmpty()) {
			for(Drone neighbor : neighbors) {
				communicate(neighbor);
			}
		}
		// Update overall map
		updateMap();
		// If still looking for an empty flag
		if (!stayed) {
			// Get the most promising next move
			targetLoc = nextMove();
			// Move if there exists one
			if (targetLoc != null) move();
		} 
	}
	
	/**
	 * @return this drone stays or not
	 */
	public boolean isStayed() {
		return stayed;
	}

	/**
	 * @return the target location
	 */
	public GridPoint getTargetLoc() {
		return targetLoc;
	}
	
	/**
	 * @return the ownMap with noise
	 */
	public int[][] getOwnMap() {
		return applyNoise(ownMap);
	}

	/**
	 * @return the overall map with noise
	 */
	public int[][] getMap() {
		return applyNoise(map);
	}
	
	/**
	 * @return ownMap along with connected parts from 
	 * others with noise
	 */
	public int[][] getConnectedMap() {
		int[][] connectedMap = new int[gridSize][gridSize];
		Queue<GridPoint> queue = new LinkedList<GridPoint>();
		boolean[][] previous = new boolean[gridSize][gridSize];
		// x and y displacement for eight surrounding cells 
		int dx[] = {1, 0, -1, 0, -1, -1, 1, 1};
		int dy[] = {0, -1, 0, 1, 1, -1, 1, -1};

		// get ownMap
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				connectedMap[i][j] = ownMap[i][j];
				if (ownMap[i][j] > 1) {
					queue.add(new GridPoint(i, j));
					previous[i][j] = true;
				}
			}
		}		
		
		// Find connected portions
		while (!queue.isEmpty()) {
			GridPoint head = queue.poll();
			for(int i = 0; i < 8; i++) {
				int x = head.getX() + dx[i];
				int y = head.getY() + dy[i];
				if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) continue;
				GridPoint nxt = new GridPoint(x, y);
				if (previous[x][y]) continue;
				previous[x][y] = true;
				if (map[x][y] < 2) continue;
				connectedMap[x][y] = map[x][y];
				queue.add(nxt);
			}
		}
		
		return applyNoise(connectedMap);
	}
	
	/**
	 * Update overall map base on ownMap and othersMap
	 */
	private void updateMap() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				// If considering locations in the local area or
				// if that location is a fixed one
				if ((Math.abs(i - pt.getX()) <= (edgeOfComm / 2) 
						&& Math.abs(j - pt.getY()) <= (edgeOfComm / 2))
						|| ownMap[i][j] == 1 || ownMap[i][j] == 3) {
					map[i][j] = ownMap[i][j];
				}
				else if (ownMap[i][j] == 2) {
					map[i][j] = map[i][j] == 3 ? 3 : 2;
				}
			}
		}
	}
	
	/**
	 * Communicate with a neighboring drone
	 * @param neighbor
	 */
	private void communicate(Drone neighbor) {
		// Share nothing
		if (sharingType == 0) {
			return;
		}
		// Share only its own map
		int[][] nMap = null;
		if (sharingType == 1) {
			nMap = neighbor.getOwnMap();
		}
		// share its own map and connected portions of others map
		else if (sharingType == 2) {
			nMap = neighbor.getConnectedMap();
		}
		// share everything that it has
		else if (sharingType == 3) {
			nMap = neighbor.getMap();
		}
		
		// update map based on map from other, if there are conflicts
		// then pick the choice with the largest votes
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				++othersMap[i][j][nMap[i][j]];
				int iMax = 0;
				for(int k = 1; k < 4; k++) {
					if (othersMap[i][j][k] > othersMap[i][j][iMax]) {
						iMax = k;
					}
				}
				// if this information has the largest number of votes
				if (iMax == nMap[i][j]) {
					map[i][j] = nMap[i][j];
				}
			}
		}
	}
	
	/**
	 * Move to the target location
	 */
	private void move() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		/* check if there is any competitor who also wants to
		 * occupy the target location.
		 * The minimum-number direction will be allowed to move forward
		 */ 
		int dx[] = {1, 0, -1, 0};
		int dy[] = {0, -1, 0, 1};
		int minDir = 4;
		for(int i = 0; i < 4; i++) {
			int x = targetLoc.getX() + dx[i];
			int y = targetLoc.getY() + dy[i];
			if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) {
				continue;
			}
			if (x == pt.getX() && y == pt.getY()) continue;
			Object obj = grid.getObjectAt(x, y);
			if (obj != null && obj instanceof Drone) {
				GridPoint oTarget = ((Drone)obj).getTargetLoc();
				if (oTarget == null) continue;
				if (oTarget.getX() == targetLoc.getX() &&
					oTarget.getY() == targetLoc.getY()) {
					int dir = getDirection(targetLoc.getX(), targetLoc.getY(), x, y);
					minDir = Math.min(minDir, dir);
				}
			}
		}
		
		// Get the target object if exists
		Object obj = grid.getObjectAt(targetLoc.getX(), targetLoc.getY());
		int dir = getDirection(targetLoc.getX(), targetLoc.getY(), 
				pt.getX(), pt.getY());
		if (obj != null) {
			if (dir < minDir) {
				// occupy if it is a flag
				occupy((Flag)obj);
			}
		}
		else {
			if (dir < minDir) {
				// simply move to that location
				grid.moveTo(this, targetLoc.getX(), targetLoc.getY());
			}
		}
	}
	
	/**
	 * Gather information from the local area
	 */
	private void getNeighbors() {
		neighbors.clear();
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		int radius = edgeOfComm / 2;
		
		// Look at all location in the square of communication
		for(int dx = -radius; dx <= radius; dx++)
			for(int dy = -radius; dy <= radius; dy++) {
				int x = pt.getX() + dx;
				int y = pt.getY() + dy;
				if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) continue;
				ownMap[x][y] = 1;
				// If the considering location is its own location
				if (dx == 0 && dy == 0) {
					if (stayed) ownMap[x][y] = 3;
					continue;
				}
				// get the object at that location
				Object obj = grid.getObjectAt(x, y);
				if (obj == null) continue; // if no object is there
				// if there is a drone
				if (obj instanceof Drone) {
					Drone neighbor = (Drone)obj;
					neighbors.add(neighbor);
					if (neighbor.isStayed()) {
						ownMap[x][y] = 3;
					}
				}
				// if it is a flag
				else if (obj instanceof Flag) {
					ownMap[x][y] = 2;
				}
			}
	}
	
	/**
	 * Occupy a specific flag
	 * @param flag
	 */
	private void occupy(Flag flag) {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		// remove this flag
		context.remove(flag);
		// move this drone to a new place
		grid.moveTo(this, flag.getX(), flag.getY());
		this.stayed = true;
		
		// terminate if there is no flag on the map
		if (context.getObjects(Flag.class).size() == 0) {
			RunEnvironment.getInstance().endRun();
		}
	}
	
	/**
	 * Retrieve next move by breadth-first search
	 * @return one possible move or null
	 */
	private GridPoint nextMove() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		// Initialize for bfs
		Queue<GridPoint> queue = new LinkedList<GridPoint>();
		List<GridPoint> firstMoves = new ArrayList<GridPoint>();
		GridPoint[][] previous = new GridPoint[gridSize][gridSize];
		int dx[] = {1, 0, -1, 0};
		int dy[] = {0, -1, 0, 1};
		// if hasFlag then looking for flag, otherwise look for
		// a new cell
		int targetNum = hasFlag() ? 2 : 0;
		GridPoint target = null;
		List<Integer> index = new ArrayList<Integer>();
		index.add(0);
		index.add(1);
		index.add(2);
		index.add(3);
		
		// Add the starting location
		queue.add(pt);
		previous[pt.getX()][pt.getY()] = pt;
		while (!queue.isEmpty()) {
			GridPoint head = queue.poll();
			// If found a possible target
			if (map[head.getX()][head.getY()] == targetNum) {
				target = head;
				break;
			}
			// Look for 4 surrounding cells in random order
			Collections.shuffle(index);
			for(Integer i : index) {
				int x = head.getX() + dx[i];
				int y = head.getY() + dy[i];
				if (x < 0 || x >= gridSize || y < 0 || y >= gridSize) continue;
				GridPoint nxt = new GridPoint(x, y);
				if (previous[x][y] != null) continue;
				previous[x][y] = head;
				if (map[x][y] == 3) continue;
				if (head.getX() == pt.getX() && head.getY() == pt.getY()) {
					Object obj = grid.getObjectAt(x, y);
					if (obj != null && obj instanceof Drone) {
						continue;
					}
					else {
						firstMoves.add(nxt);
					}
				}
				queue.add(nxt);
			}
		}
		
		// If found a target then get its first move
		if (target != null) {
			GridPoint pre = previous[target.getX()][target.getY()];
			while (pre.getX() != pt.getX() || pre.getY() != pt.getY()) {
				target = pre;
				pre = previous[target.getX()][target.getY()];
			}
		}
		else {
			// otherwise choose a random first move if one is available
			if (!firstMoves.isEmpty()) {
				target = firstMoves.get(0);
			}
		}
		
		return target;
	}

	/**
	 * Check if there exists some flags on the map
	 * @return
	 */
	private boolean hasFlag() {
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				if (map[i][j] == 2) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Directions with respect to the target location will be
	 * numbered as follow:
	 * 0 -> Left
	 * 1 -> Bottom 
	 * 2 -> Right 
	 * 3 -> Top
	 * @param originX
	 * @param originY
	 * @param x
	 * @param y
	 * @return direction with respect to (originX, originY)
	 */
	private int getDirection(int originX, int originY, int x, int y) {
		if (originX == x) {
			if (originY < y) return 3;
			else return 1;
		}
		else {
			if (originX < x) return 0;
			else return 2;
		}
	}

	/**
	 * Apply noise in the communicated map
	 * @param map
	 * @return map with noise
	 */
	private int[][] applyNoise(int[][] map) {
		// Clone the current map
		int[][] result = new int[gridSize][gridSize];
		for(int i = 0; i < gridSize; i++) {
			for(int j = 0; j < gridSize; j++) {
				result[i][j] = map[i][j];
			}
		}
		// Find the number of noise cells
		int counter = (int)Math.floor(noise / 100.0 * (gridSize * gridSize));
		if (counter > 0) {
			// Get random locations on the whole map
			int[] ints = new Random().ints(1, gridSize * gridSize).distinct().limit(counter).toArray();
			for(int i = 0; i < counter; i++) {
				int x = ints[i] / gridSize;
				int y = ints[i] % gridSize;
				// Change its value to random value
				int rnd = new Random().nextInt(3);
				result[x][y] = rnd < result[x][y] ? rnd : rnd + 1;
			}
		}
		return result;
	}
	
	/**
	 * Modify the input edge of communication so that:
	 * 1. The minimum is 5
	 * 2. The maximum is 49
	 * 3. It has to be an odd integer
	 * @param value
	 * @return modified edgeOfComm
	 */
	private int modifiedEdgeOfComm(int value) {
		int result = Math.max(value, 5);
		result = Math.min(result, 49);
		if (result % 2 == 0) {
			result--;
		}
		return result;
	}
	
	/**
	 * Modify the sharing type so that it should be an 
	 * integer from 0 to 3.
	 * @param value
	 * @return modified sharingType
	 */
	private int modifiedSharingType(int value) {
		int result = Math.min(value, 3);
		result = Math.max(result, 0);
		return result;
	}
	
	/**
	 * Modify the noise level so that it does not exceed
	 * 100 and does not go below 0.
	 * @param value
	 * @return modified noise
	 */
	private int modifiedNoise(int value) {
		int result = Math.min(value, 100);
		result = Math.max(result, 0);
		return result;
	}
	
}
