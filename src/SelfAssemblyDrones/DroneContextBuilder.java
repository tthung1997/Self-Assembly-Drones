package SelfAssemblyDrones;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.StrictBorders;

/**
 * Implementation of the self-assembly drone model
 * 
 * @author Team WINTER SLAYERS
 *
 */
public class DroneContextBuilder implements ContextBuilder {

	@Override
	public Context build(Context context) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		int gridSize = (Integer)p.getValue("gridSize");
		int shapeType = (Integer)p.getValue("shapeType");
		
		GridFactoryFinder.createGridFactory(null).createGrid("Grid",
				context, GridBuilderParameters.singleOccupancy2D(new RandomGridAdder(),
						new StrictBorders(), gridSize, gridSize));
		
		// Get shape
		boolean[][] shape = null;
		try {
			shape = getShape(shapeType, gridSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Add flags
		Grid grid = (Grid)context.getProjection("Grid");
		int initialDrones = 0;
		for(int x = 0; x < gridSize; x++) {
			for(int y = 0; y < gridSize; y++) {
				if (shape[x][y]) {
					Flag flag = new Flag(x, y);
					context.add(flag);
					grid.moveTo(flag, x, y);
					
					initialDrones++;
				}
			}
		}
		// Add drones
		for(int i = 0; i < initialDrones; i++){
			context.add(new Drone(gridSize));
		}
		
		return context;
	}
	
	/**
	 * Retrieve the appropriate shape for the environment
	 * 
	 * @param type
	 * @param gridSize
	 * @return
	 * @throws FileNotFoundException
	 */
	public boolean[][] getShape(int type, int gridSize) throws FileNotFoundException {
		boolean[][] shape = new boolean[gridSize][gridSize];
		int xL, xR, yL, yH;
		Scanner scnr;
		switch (type) {
			case 0: //line
				for(int x = gridSize / 4; x < (gridSize * 3 / 4); x++) {
					shape[x][gridSize / 2] = true;
				}
				break;
			case 1: //square
				for(int x = gridSize / 4; x < (gridSize * 3 / 4); x++) {
					shape[x][gridSize * 3 / 4] = shape[x][gridSize / 4] = true;
				}
				for(int y = gridSize / 4 + 1; y < gridSize * 3 / 4; y++) {
					shape[gridSize / 4 - 1][y] = shape[gridSize * 3 / 4][y] = true;
				}
				break;
			case 2: //circle
				int radius = gridSize / 4;
				GridPoint center = new GridPoint(gridSize / 2, gridSize / 2);
				for(int x = center.getX() - radius / 2; x <= center.getX() + radius / 2; x++) {
					shape[x][center.getY() + radius] = shape[x][center.getY() - radius] = true;
					shape[center.getX() - radius][x] = shape[center.getX() + radius][x] = true;
				}
				yL = center.getY() - radius + 1;
				yH = center.getY() + radius - 1;
				for(int x = center.getX() - radius / 2 - 1; x > center.getX() - radius; x--) {
					shape[x][yL] = shape[x][yH] = true;
					yL++;
					yH--;
				}
				yL = center.getY() - radius + 1;
				yH = center.getY() + radius - 1;
				for(int x = center.getX() + radius / 2 + 1; x < center.getX() + radius; x++) {
					shape[x][yL] = shape[x][yH] = true;
					yL++;
					yH--;
				}
				break;
			case 3: //triangle
				xL = xR = gridSize / 2;
				for(int y = gridSize * 3 / 4; y >= gridSize * 3 / 8; y--) {
					shape[xL][y] = shape[xR][y] = true;
					xL--;
					xR++;
				}
				for(int x = xL + 2; x < xR - 1; x++) {
					shape[x][gridSize * 3 / 8 - 1] = true;
				}
				break;
			case 4: //adidas
				scnr = new Scanner(new File("data/figure4.txt"));
				for(int x = 49; x >= 0; x--) {
					for(int y = 0; y < 50; y++) {
						if (scnr.nextInt() == 1) {
							shape[y][x] = true;
						}
					}
				}
				scnr.close();
				break;
			case 5: //star
				scnr = new Scanner(new File("data/figure5.txt"));
				for(int x = 49; x >= 0; x--) {
					for(int y = 0; y < 50; y++) {
						if (scnr.nextInt() == 1) {
							shape[y][x] = true;
						}
					}
				}
				scnr.close();
				break;
		}
		return shape;
	}
	
}
