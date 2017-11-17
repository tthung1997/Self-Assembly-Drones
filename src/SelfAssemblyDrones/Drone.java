package SelfAssemblyDrones;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * @author TRIEU HUNG
 *
 */
public class Drone {

	private int edgeOfComm;
	private int[][] map;
	
	public Drone() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		edgeOfComm = (Integer)p.getValue("edgeOfComm");
		map = new int[100][100];
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		List<GridPoint> flags = findPixels();
		List<GridPoint> newPixels = findPixels();
		List<Drone> neighbors = getNeighbors();
		
		for(Drone neighbor : neighbors) {
			communicate(neighbor);
		}
		
//		try(FileWriter fw = new FileWriter("temp.txt", true);
//			    BufferedWriter bw = new BufferedWriter(fw);
//			    PrintWriter out = new PrintWriter(bw))
//			{
//			    out.println(sum);
//			} catch (IOException e) {
//			    //exception handling left as an exercise for the reader
//			}
	}
	
	public void communicate(Drone neighbor) {
		
	}
	
	public List<Drone> getNeighbors() {
		List<Drone> neighbors = new ArrayList<Drone>();
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		int radius = edgeOfComm / 2;
		
		for(int dx = -radius; dx <= radius; dx++)
			for(int dy = -radius; dy <= radius; dy++) {
				if (dx == 0 && dy == 0) continue;
				int x = pt.getX() + dx;
				int y = pt.getY() + dy;
				if (x < 0 || x >= 100) continue;
				if (y < 0 || y >= 100) continue;
				Drone drone = (Drone)grid.getObjectAt(x, y);
				if (drone != null) neighbors.add(drone);
			}
		
		return neighbors;
	}
	
	public List<GridPoint> findPixels() {
		List<GridPoint> pixels = new ArrayList<GridPoint>();
		Context context = ContextUtils.getContext(this);
		Grid grid = (Grid)context.getProjection("Grid");
		GridPoint pt = grid.getLocation(this);
		
		Collections.shuffle(pixels);
		
		return pixels;
	}
	
}
