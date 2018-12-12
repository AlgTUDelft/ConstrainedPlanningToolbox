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

import instances.CMDPInstance;

import java.util.List;
import java.util.Random;

import model.CMDP;

import domains.CMDPInstanceGenerator;
import domains.maze.data.GridWorld;


public class MazeInstanceGenerator implements CMDPInstanceGenerator {
	private static final long   SEED = 3331L;
	private static final Random rnd = new Random(SEED);
	
	public MazeInstanceGenerator() {
		
	}
	
	public CMDPInstance getInstance(int numAgents, int numDecisions) {
		assert numAgents > 0 && numDecisions > 0;
		int mazeGridSize = 9;
		int numResources = 5;
		
		CMDP[] cmdpList = createMazeInstances(numAgents, mazeGridSize, numResources, numDecisions);
		double[] resourceLimit = createBudget(numAgents, numResources, numDecisions);
		
		CMDPInstance instance = CMDPInstance.createInstantaneousInstance(cmdpList, resourceLimit, numDecisions);
		return instance;
	}
	
	private CMDP[] createMazeInstances(int numAgents, int gridSize, int numResources, int numDecisions) {
		MazeGenerator gen = new MazeGenerator(SEED);

		CMDP[] instances = new CMDP[numAgents];

		for (int i = 0; i < numAgents; i++) {
			GridWorld maze = gen.generateGrid(gridSize, numResources, numDecisions);
			MazeInstance instance = new MazeInstance(i, maze, numDecisions);
			
			int nStates = instance.getNumStates();
			int nActions = instance.getNumActions();
			int nDecisions = instance.getNumDecisions();
			int initialState = instance.getInitialState();
			
			// get transition function
			int[][][] transitionDestinations = new int[nStates][nActions][];
			double[][][] transitionProbabilities = new double[nStates][nActions][];
			for(int s=0; s<nStates; s++) {
				for(int a=0; a<nActions; a++) {
					transitionDestinations[s][a] = instance.getTransitionDestinations(s, a);
					transitionProbabilities[s][a] = instance.getTransitionProbabilities(s, a);
					
					if(transitionDestinations[s][a] == null) {
						transitionDestinations[s][a] = new int[]{};
						transitionProbabilities[s][a] = new double[]{};
					}
				}
			}
			
			// get feasible actions
			int[][][] feasibleActions = new int[nDecisions][nStates][];
			for(int t=0; t<nDecisions; t++) {
				for(int s=0; s<nStates; s++) {
					feasibleActions[t][s] = instance.getActions(t, s);
					assert feasibleActions[t][s].length > 0;
					
					for(int a : feasibleActions[t][s]) {
						assert transitionDestinations[s][a].length > 0;
						assert transitionDestinations[s][a].length == transitionProbabilities[s][a].length;
						
						double sum = 0.0;
						for(int j=0; j<transitionDestinations[s][a].length; j++) {
							double prob = transitionProbabilities[s][a][j];
							sum += prob;
						}
						
						assert Math.abs(1.0-sum) < 0.0001;
					}
				}
			}
			
			// get reward function
			double[][][] rewardFunction = new double[nDecisions][nStates][nActions];
			for(int t=0; t<nDecisions; t++) {
				for(int s=0; s<nStates; s++) {
					for(int a=0; a<nActions; a++) {
						rewardFunction[t][s][a] = instance.getReward(t, s, a);
					}
				}
			}
			
			// get cost functions
			List<double[][]> costFunctions = instance.getCostFunctions();
			
			
			CMDP cmdp = new CMDP(nStates, nActions, costFunctions, initialState, nDecisions);
			cmdp.setRewardFunction(rewardFunction);
			cmdp.setTransitionFunction(transitionDestinations, transitionProbabilities);
			cmdp.setFeasibleActions(feasibleActions);
			
			instances[i] = cmdp;
		}

		return instances;
	}
	
	private double[] createBudget(int numAgents, int numResources, int numDecisions) {
		double[] perStep = new double[numResources];

		double expected = (double) numAgents / numResources / 2;
		double variance = expected / 2;
		for (int i = 0; i < perStep.length; i++) {
			perStep[i] = expected + 2 * (rnd.nextDouble()-0.6) * variance;
		}

		return perStep;
	}
}
