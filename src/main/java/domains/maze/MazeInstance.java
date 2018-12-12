/*******************************************************************************
 * ConstrainedPlanningToolbox
 * Copyright (C) 2019 Algorithmics group, Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package domains.maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domains.maze.data.Direction;
import domains.maze.data.GridWorld;
import domains.maze.data.Location;
import domains.maze.data.MazeAction;
import domains.maze.data.Task;

public class MazeInstance {

	private final int agentID;

	private final GridWorld maze;
	private final int numDecisions;
	private final int numDomainResources;

	private Map<List<Direction>, int[]> noDoActionTranslation;
	private Map<List<Direction>, int[]> doActionTranslation;

	private int[][][] actions;
	private int[][][] Tstate;
	private double[][][] Tprob;
	
	private List<double[][]> costFunctions;

	public MazeInstance(int agentID, GridWorld maze, int numDecisions) {
		this.agentID = agentID;
		this.maze = maze;
		this.numDecisions = numDecisions;
		this.numDomainResources = maze.getNumResources();

		noDoActionTranslation = new HashMap<>();
		doActionTranslation = new HashMap<>();

		actions = new int[getNumStates()][getNumDecisions()][];
		Tstate = new int[getNumStates()][MazeAction.values().length][];
		Tprob = new double[getNumStates()][MazeAction.values().length][];

		initActions();
		initTransitions();
		initCostFunctions();
	}

	private void initActions() {
		for (int s = 0; s < getNumStates(); s++) {
			Location location = maze.getLocation(s);

			List<Direction> possibleDirections = findPossibleDirections(location);

			int[] noDoActions = getActions(possibleDirections, false);
			int[] doActions = getActions(possibleDirections, true);

			for (int t = 0; t < numDecisions; t++) {
				if (location.hasTask() && location.getTask().isAvailable(t)) {
					actions[s][t] = doActions;
				}
				else {
					actions[s][t] = noDoActions;
				}
			}
		}
	}

	private List<Direction> findPossibleDirections(Location location) {
		List<Direction> possible = new ArrayList<>();
		Direction[] allDirections = Direction.values();

		for (Direction direction : allDirections)
			if (maze.hasDirection(location, direction)) possible.add(direction);

		return possible;
	}

	private int[] getActions(List<Direction> possibleDirections, boolean doTask) {
		Map<List<Direction>, int[]> translation;

		if (doTask) translation = doActionTranslation;
		else        translation = noDoActionTranslation;

		if (!translation.containsKey(possibleDirections))
			translation.put(possibleDirections, createActionList(possibleDirections, doTask));

		return translation.get(possibleDirections);
	}

	private int[] createActionList(List<Direction> possibleDirections, boolean doTask) {
		int numActions = 1 + 2 * possibleDirections.size() + (doTask ? 1 : 0);

		int index = 0;
		int[] actions = new int[numActions];

		actions[index++] = MazeAction.WAIT.getID();

		for (Direction direction : possibleDirections) {
			actions[index++] = direction.getRegularAction().getID();
			actions[index++] = direction.getSafeAction().getID();
		}

		if (doTask)
			actions[index++] = MazeAction.DO_TASK.getID();

		return actions;
	}
	
	private void initCostFunctions() {
		costFunctions = new ArrayList<double[][]>();
		
		for(int k=0; k<numDomainResources; k++) {
			double[][] costFunction = new double[getNumStates()][MazeAction.values().length];
			
			for(int s=0; s<getNumStates(); s++) {
				for(int a=0; a<MazeAction.values().length; a++) {
					
					// check if there is a time step in which (s,a) is valid
					boolean actionValid = false;
					
					for(int t=0; t<numDecisions && !actionValid; t++) {
						for(int feasibleAction : getActions(t, s)) {
							if(a == feasibleAction) {
								actionValid = true;
							}
						}
					}
					
					if(actionValid && usesResource(a)) {
						int usedResource = getResourceType(s, a);
						
						if(k == usedResource) {
							costFunction[s][a] = 1.0;
						}
					}
				}
			}
			
			costFunctions.add(costFunction);
		}
	}
	
	public List<double[][]> getCostFunctions() {
		return costFunctions;
	}

	private void initTransitions() {
		Location outOfWorld = maze.getOutOfWorld();
		MazeAction[] actions = MazeAction.values();

		Tstate[outOfWorld.getID()][MazeAction.WAIT.getID()] = new int[] { outOfWorld.getID() };
		Tprob[outOfWorld.getID()][MazeAction.WAIT.getID()] = new double[] { 1D };

		int[] states;
		double[] probs;
		for (int s = 1; s < getNumStates(); s++) {
			Location location = maze.getLocation(s);

			List<Direction> possible = findPossibleDirections(location);

			for (int a = 0; a < actions.length; a++) {
				MazeAction action = actions[a];

				states = null;
				probs  = null;

				if (!action.isMoveAction()) {
					// Either WAIT, or DO_TASK. Both keep the robot active with p=0.95, break down with 0.05.
					states = new int[]    { location.getID(), outOfWorld.getID() };
					probs  = new double[] {             0.95,               0.05 };
				} else {
					Direction direction = action.getDirection();

					if (maze.hasDirection(location, direction)) {
						if (action.usesResource()) {
							// SAFE_MOVE. Move to the intended destination with p=0.95, break down with 0.05.
							states = new int[]    { maze.getDirection(location, direction).getID(), outOfWorld.getID() };
							probs  = new double[] {                                           0.95,               0.05 };
						} else {
							// UNSAFE MOVE. p=0.4 of reaching the destination, p=0.4 of going somewhere else, p=0.2 of breaking down.
							int index = 0;
							int numDestinations = 2 + possible.size();

							states = new int[numDestinations];
							probs = new double[numDestinations];

							// Probability 0.2 of breaking the robot for unsafe move.
							states[index] = outOfWorld.getID(); probs[index++] = 0.2;

							// Probability 0.1 plus however many walls surround us of staying put.
							states[index] = location.getID(); probs[index++] = 0.1 + (4-possible.size()) * 0.1;

							for (Direction possibleDirection : possible) {
								// Probability 0.4 of reaching the destination, probability 0.1 of another neighbor instead.
								states[index] = maze.getDirection(location, possibleDirection).getID();
								probs[index++] = possibleDirection.equals(direction) ? 0.4 : 0.1;
							}
						}
					}
				}

				// Round probability to two digits to avoid 0.30000000000000004.
				if (probs != null)
					for (int i = 0; i < probs.length; i++)
						probs[i] = Math.round(probs[i] * 100) / 100D;

				Tstate[s][a] = states;
				Tprob[s][a] = probs;
			}
		}
	}

	public int getAgentID() {
		return agentID;
	}

	public int getNumDecisions() {
		return numDecisions;
	}

	public int getNumDomainResources() {
		return numDomainResources;
	}

	public int getNumResourceTypes() {
		return maze.getNumResources();
	}

	public int getNumStates() {
		return maze.getNumLocations();
	}

	public int getNumActions() {
		// WAIT, MOVE(4), SAFE_MOVE(4), DO_TASK = 1 + 4 + 4 + 1 = 10.
		return 10;
	}

	public int getMaxNumStates() {
		return maze.getSize() * maze.getSize() + 1;
	}

	public int[] getActions(int t, int state) {
		return actions[state][t];
	}

	public int getInitialState() {
		return maze.getInitialLocation().getID();
	}

	public int[] getTransitionDestinations(int state, int action) {
//		if (Tstate[state][action] == null)
//			System.err.println(state + "," + MazeAction.values()[action] + " is null!");

		return Tstate[state][action];
	}

	public double[] getTransitionProbabilities(int state, int action) {
		return Tprob[state][action];
	}

	public double getReward(int time, int state, int action) {
		double reward = 0D;

		if (MazeAction.DO_TASK.equals(MazeAction.values()[action])) {
			Location location = maze.getLocation(state);

			if (location.hasTask()) {
				Task task = location.getTask();

				if (task.isAvailable(time)) {
					reward = task.getTaskReward();
				}
			}
		}

		return reward;
	}

	public boolean usesResource(int actionID) {
		return MazeAction.values()[actionID].usesResource();
	}

	public int getResourceType(int stateID, int actionID) {		
		MazeAction action = MazeAction.values()[actionID];
		Location state = maze.getLocation(stateID);

		assert (action.usesResource());

		int offset = 0 * maze.getNumResources(); // TODO simplify function (number 0 was time param in old version)
		if (action.isMoveAction()) {
			offset += state.getMoveResource();
		} else {
			offset += state.getTask().getTaskResource();
		}

		return offset;
	}

	public int getResourceConsumption(int actionID) {
		return (usesResource(actionID) ? 1 : 0);
	}

}
