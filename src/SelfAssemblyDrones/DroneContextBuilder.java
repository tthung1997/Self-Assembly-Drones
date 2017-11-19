package SelfAssemblyDrones;

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
 * 
 */

/**
 * @author TRIEU HUNG
 *
 */
public class DroneContextBuilder implements ContextBuilder {

	@Override
	public Context build(Context context) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		int gridWidth = (Integer)p.getValue("gridWidth");
		int gridHeight = gridWidth;
		int initialDrones = (Integer)p.getValue("initialDrones");
		
		GridFactoryFinder.createGridFactory(null).createGrid("Grid",
				context, GridBuilderParameters.singleOccupancy2D(new RandomGridAdder(),
						new StrictBorders(), gridWidth, gridHeight));
		
		for(int y = 0; y < initialDrones; y++) {
//			Flag flag = new Flag(0, y);
//			context.add(flag);
//			Grid grid = (Grid)context.getProjection("Grid");
//			grid.moveTo(flag, 0, y);
			Flag flag = new Flag();
			context.add(flag);
			Grid grid = (Grid)context.getProjection("Grid");
			GridPoint pt = grid.getLocation(flag);
			flag.setX(pt.getX());
			flag.setY(pt.getY());
		}
		
		for(int i = 0; i < initialDrones; i++){
			context.add(new Drone(gridWidth, gridHeight));
		}
		
		return context;
	}
	
}
