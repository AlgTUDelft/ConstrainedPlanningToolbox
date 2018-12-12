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
package domains.tcl;

import instances.CMDPInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.CMDP;

import domains.CMDPInstanceGenerator;
import domains.tcl.multitcl.MultiLevelTCLDomain;
import domains.tcl.primitives.NeighborhoodFactory;
import domains.tcl.primitives.generator.FactoredInstanceGenerator;
import domains.tcl.primitives.generator.InstanceGenerator;
import domains.tcl.primitives.generator.agent.AnalyticAgentGen;
import domains.tcl.primitives.generator.limit.MultiActionLimitGen;


public class TCLInstanceGeneratorMultiLevel implements CMDPInstanceGenerator {
	private final int STEPSIZE = 60;
	private final long SEED = 3331L;
	
	public TCLInstanceGeneratorMultiLevel() {
		
	}
	
	public CMDPInstance getInstance(int nAgents, int nDecisions) {
		assert nAgents > 0 && nDecisions > 0;
		int nActions = 3;
		
		MultiLevelTCLDomain mdp = createMultiLevelInstance(nAgents, 80, nActions, nDecisions);
		
		CMDP[] cmdps = convertAgentProblems(mdp);
		
		double[][] resourceLimit = new double[1][mdp.getHorizon()];

		for (int t = 0; t < mdp.getHorizon(); t++) {
			resourceLimit[0][t] = mdp.getResourceLimit(t);
		}
		
		CMDPInstance instance = CMDPInstance.createInstantaneousInstance(cmdps, resourceLimit, nDecisions);
		return instance;
	}
	
	private MultiLevelTCLDomain createMultiLevelInstance(int numAgents, int numStates, int numActions, int numDecisions) {
		InstanceGenerator generator = new FactoredInstanceGenerator(new AnalyticAgentGen(20), new MultiActionLimitGen(numActions));
		generator.setSeed(SEED);
		NeighborhoodFactory factory = generator.generateExtendableFactory(numAgents, numDecisions);
		MultiLevelTCLDomain domain = new MultiLevelTCLDomain(factory, 0, 40, numStates, STEPSIZE, numDecisions*STEPSIZE, numActions);

		return domain;
	}
	
	private CMDP[] convertAgentProblems(MultiLevelTCLDomain mdp) {
		CMDP[] agentProblems = new CMDP[mdp.getNumAgents()];

		int agentID = 0;
		for (Agent agent : mdp.getAgents()) {
			int nStates = mdp.getNumStates();
			int nActions = mdp.getNumActions();
			int initialState = mdp.findTemperatureState(20).getID();
			int numDecisions = mdp.getHorizon();
			
			double[][] costFunction = new double[nStates][nActions];
			double[][] rewardFunction = new double[nStates][nActions];
			int[][][] transitionDestinations = new int[nStates][nActions][];
			double[][][] transitionProbabilities = new double[nStates][nActions][];
			
			for (int s = 0; s < mdp.getNumStates(); s++) {
				State state = mdp.getState(s);

				for (Action action : mdp.getActions()) {
					Map<? extends State, Double> transition = mdp.getTransitionFunction(agent, state, action);
					int[] dest = new int[transition.size()];
					double[] prob = new double[transition.size()];
					int index = 0;

					for (State nextState : transition.keySet()) {
						dest[index] = nextState.getID();
						prob[index] = transition.get(nextState);
						index++;
					}

					transitionDestinations[state.getID()][action.getID()] = dest;
					transitionProbabilities[state.getID()][action.getID()] = prob;
					rewardFunction[state.getID()][action.getID()] = mdp.getRewardFunction(agent, state, action);
				}

				// generate action costs.
				for(Action action : mdp.getActions()) {
					costFunction[s][action.getID()] = mdp.getResourceConsumption(action);
				}
			}
			
			List<double[][]> costFunctions = new ArrayList<double[][]>();
			costFunctions.add(costFunction);
			
			CMDP cmdp = new CMDP(nStates, nActions, costFunctions, initialState, numDecisions);
			cmdp.setRewardFunction(rewardFunction);
			cmdp.setTransitionFunction(transitionDestinations, transitionProbabilities);
			
			agentProblems[agentID] = cmdp;
			agentID++;
		}

		return agentProblems;
	}
	
}
