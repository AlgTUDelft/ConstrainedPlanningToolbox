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
package domains.cbm;

import java.util.ArrayList;
import java.util.List;

import model.CPOMDP;
import model.POMDP;
import model.ParsePOMDP;
import instances.CPOMDPInstance;
import domains.CPOMDPInstanceGenerator;

public class CBMGenerator implements CPOMDPInstanceGenerator {
	private CPOMDP getCBMAgent(int T) {
		POMDP pomdp = ParsePOMDP.readPOMDP("domains/bridge-repair-modified.POMDP", T);
		int nStates = pomdp.getNumStates();
		int nActions = pomdp.getNumActions();
		int nObservations = pomdp.getNumObservations();
		
		double[][] rewardFunction = new double[nStates][nActions];
		double[][] costFunction = new double[nStates][nActions];
		double maxReward = pomdp.getMaxReward();
		for(int s=0; s<nStates; s++) {
			for(int a=0; a<nActions; a++) {
				rewardFunction[s][a] = (-1.0 * pomdp.getReward(s, a) + maxReward + 1.0) * 0.1;
				
				if(a >= 3 && a <= 5) {
					costFunction[s][a] = 10.0;
				}
				else if(a >= 6 && a <= 8) {
					costFunction[s][a] = 20.0;
				}
				else if(a >= 9 && a <= 11) {
					costFunction[s][a] = 30.0;
				}
			}
		}

		List<double[][]> costFunctions = new ArrayList<double[][]>();
		costFunctions.add(costFunction);
		
		CPOMDP cpomdp = new CPOMDP(nStates, nActions, nObservations, costFunctions, pomdp.getObservationFunction(), pomdp.getInitialBelief(), T);
		cpomdp.setRewardFunction(pomdp.getRewardFunction());
		cpomdp.setTransitionFunction(pomdp.getTransitionDestinations(), pomdp.getTransitionProbabilities());
		
		return cpomdp;
	}
	
	private CPOMDP[] getCBMInstance(int nAgents, int T) {
		CPOMDP[] cpomdps = new CPOMDP[nAgents];
		
		CPOMDP cpomdp = getCBMAgent(T);
		
		for(int i=0; i<nAgents; i++) {
			cpomdps[i] = cpomdp;
		}
		
		return cpomdps;
	}

	@Override
	public CPOMDPInstance getInstance(int nAgents, int nDecisions) {
		CPOMDP[] cpomdps = getCBMInstance(nAgents, nDecisions);
		double L = 100.0 * ((double) nAgents);
		CPOMDPInstance instance = CPOMDPInstance.createBudgetInstance(cpomdps, new double[]{L}, nDecisions);
		return instance;
	}
}
