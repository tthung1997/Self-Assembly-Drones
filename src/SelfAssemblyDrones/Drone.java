package SelfAssemblyDrones;

import java.util.*;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * 
 */

/**
 * @author TRIEU HUNG
 *
 */
public class Drone {

	private int edgeOfComm;
	private int noise;
	private int sharingType;
	private int[][] ownMap;
	private int[][][] othersMap;
	private int[][] map;
	private boolean stayed;
	private int gridWidth;
	private int gridHeight;
	private GridPoint targetLoc;
	private List<Drone> neighbors;
	
	public Drone(int gridWidth, int gridHeight) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		edgeOfComm = (Integer)p.getValue("edgeOfComm");
		sharingType = (Integer)p.getValue("sharingType");
		noise = (Integer)p.getValue("noise");
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		ownMap = new int[gridWidth][gridHeight];
		othersMap = new int[gridWidth][gridHeight][5];
		map = new int[gridWidth][gridHeight];
		stayed = false;
		targetLoc = null;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		getNeighbors();
		for(Drone neighbor : neighbors) {
			communicate(neighbor);
		}
		updateMap();
		if (!stayed) {
			targetLoc = nextMove();
			if (targetLoc != null) move();
		}
	}
	
	public void updateMap() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		for(int i = 0; i < gridWidth; i++) {
			for(int j = 0; j < gridHeight; j++) {
				if (Math.abs(i - pt.getX()) > 2 || Math.abs(j - pt.getY()) > 2
						|| ownMap[i][j] == 1 || ownMap[i][j] == 3) {
					map[i][j] = ownMap[i][j];
				}
				else if (ownMap[i][j] == 2 && othersMap[i][j][4] == 3) {
					map[i][j] = 3;
				}
				else {
					map[i][j] = othersMap[i][j][4];
				}
			}
		}
	}
	
	public void communicate(Drone neighbor) {
		if (sharingType == 0) {
			return;
		}
		int[][] nMap = null;
		if (sharingType == 1) {
			nMap = neighbor.getOwnMap();
		}
		else if (sharingType == 2) {
			nMap = neighbor.getMap();
		}
		for(int i = 0; i < gridWidth; i++) {
			for(int j = 0; j < gridHeight; j++) {
				++othersMap[i][j][nMap[i][j]];
				if (othersMap[i][j][nMap[i][j]] > othersMap[i][j][othersMap[i][j][4]]) {
					othersMap[i][j][4] = nMap[i][j];
				}
			}
		}
	}
	
	public void move() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		int dx[] = {1, 0, -1, 0};
		int dy[] = {0, -1, 0, 1};
		int minDir = 4;
		for(int i = 0; i < 4; i++) {
			int x = targetLoc.getX() + dx[i];
			int y = targetLoc.getY() + dy[i];
			if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
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
		
		Object obj = grid.getObjectAt(targetLoc.getX(), targetLoc.getY());
		int dir = getDirection(targetLoc.getX(), targetLoc.getY(), 
				pt.getX(), pt.getY());
		if (obj != null) {
			if (dir < minDir) {
				occupy((Flag)obj);
			}
		}
		else {
			if (dir < minDir) {
				grid.moveTo(this, targetLoc.getX(), targetLoc.getY());
			}
		}
	}
	
	public void getNeighbors() {
		neighbors = new ArrayList<Drone>();
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		int radius = edgeOfComm / 2;
		
		for(int dx = -radius; dx <= radius; dx++)
			for(int dy = -radius; dy <= radius; dy++) {
				int x = pt.getX() + dx;
				int y = pt.getY() + dy;
				if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) continue;
				ownMap[x][y] = 1;
				if (dx == 0 && dy == 0) continue;
				
				Object obj = grid.getObjectAt(x, y);
				if (obj instanceof Drone) {
					Drone neighbor = (Drone)obj;
					neighbors.add(neighbor);
					if (neighbor.isStayed()) {
						ownMap[x][y] = 3;
					}
				}
				else if (obj instanceof Flag) {
					ownMap[x][y] = 2;
				}
			}
	}
	
	public void occupy(Flag flag) {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		
		context.remove(flag);
		grid.moveTo(this, flag.getX(), flag.getY());
		this.stayed = true;
		
		if (context.getObjects(Flag.class).size() == 0) {
			RunEnvironment.getInstance().endRun();
		}
	}
	
	public GridPoint nextMove() {
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		Queue<GridPoint> queue = new LinkedList<GridPoint>();
		List<GridPoint> firstMoves = new ArrayList<GridPoint>();
		GridPoint[][] previous = new GridPoint[gridWidth][gridHeight];
		int dx[] = {1, 0, -1, 0};
		int dy[] = {0, -1, 0, 1};
		boolean[][] mark = new boolean[gridWidth][gridHeight];
		int targetNum = hasFlag() ? 2 : 0;
		GridPoint target = null;
		List<Integer> index = new ArrayList<Integer>();
		index.add(0);
		index.add(1);
		index.add(2);
		index.add(3);
		
		queue.add(pt);
		mark[pt.getX()][pt.getY()] = true;
		while (!queue.isEmpty()) {
			GridPoint head = queue.poll();
			if (map[head.getX()][head.getY()] == targetNum) {
				target = head;
				break;
			}
			Collections.shuffle(index);
			for(Integer i : index) {
				int x = head.getX() + dx[i];
				int y = head.getY() + dy[i];
				if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) continue;
				GridPoint nxt = new GridPoint(x, y);
				if (mark[x][y]) continue;
				mark[x][y] = true;
				if (map[x][y] == 3) continue;
				if (head.getX() == pt.getX() && head.getY() == pt.getY()) {
					Object obj = grid.getObjectAt(x, y);
					if (obj != null && obj instanceof Drone) {
						continue;
					}
					else {
						firstMoves.add(new GridPoint(x, y));
					}
				}
				queue.add(nxt);
				previous[x][y] = head;
			}
		}
		
		if (target != null) {
			GridPoint pre = previous[target.getX()][target.getY()];
			while (pre.getX() != pt.getX() || pre.getY() != pt.getY()) {
				target = pre;
				pre = previous[target.getX()][target.getY()];
			}
		}
		else {
			if (!firstMoves.isEmpty()) {
				target = firstMoves.get(0);
			}
		}
		
		return target;
	}

	public boolean hasFlag() {
		for(int i = 0; i < gridWidth; i++) {
			for(int j = 0; j < gridHeight; j++) {
				if (map[i][j] == 2) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return the stay
	 */
	public boolean isStayed() {
		return stayed;
	}

	/**
	 * @return the targetLoc
	 */
	public GridPoint getTargetLoc() {
		return targetLoc;
	}
	
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

	private int[][] applyNoise(int[][] map) {
		int[][] result = new int[gridWidth][gridHeight];
		for(int i = 0; i < gridWidth; i++) {
			for(int j = 0; j < gridHeight; j++) {
				result[i][j] = map[i][j];
			}
		}
		int counter = (int)Math.floor(noise / 100.0 * (gridWidth * gridHeight));
		int[] ints = new Random().ints(1, gridWidth * gridHeight).distinct().limit(counter).toArray();
		for(int i = 0; i < counter; i++) {
			int x = ints[i] / gridWidth;
			int y = ints[i] % gridWidth;
			int rnd = new Random().nextInt(3);
			result[x][y] = rnd < result[x][y] ? rnd : rnd + 1;
		}
		return result;
	}
	
	/**
	 * @return the ownMap
	 */
	public int[][] getOwnMap() {
		return applyNoise(ownMap);
	}

	/**
	 * @return the map
	 */
	public int[][] getMap() {
		return applyNoise(map);
	}
	
}
