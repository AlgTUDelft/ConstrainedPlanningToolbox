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
package model;

import java.util.HashMap;

import libpomdp.common.Pomdp;
import libpomdp.parser.FileParser;

public class ParsePOMDP {
	/**
	 * Parse a .POMDP file and create POMDP object
	 * @param filePath full path to the .POMDP file
	 * @return a POMDP object
	 */
	public static POMDP readPOMDP(String filePath, int T) {
		System.out.println();
		System.out.println("=== READ POMDP FILE ===");
		System.out.println("File: "+filePath);
		
		Pomdp pomdp = FileParser.loadPomdp(filePath, 0);
		int nStates = pomdp.nrStates();
		int nActions = pomdp.nrActions();
		int nObservations = pomdp.nrObservations();
		
		double[][] rewardFunction = new double[nStates][nActions];
		int[][][] transitionDestinations = new int[nStates][nActions][];
		double[][][] transitionProbabilities = new double[nStates][nActions][];
		double[][][] observationFunction = new double[nActions][nStates][nObservations];
		
		HashMap<Integer,String> actionLabels = new HashMap<Integer,String>();
		
		for(int s=0; s<nStates; s++) {
			for(int a=0; a<nActions; a++) {
				int numDest = 0;
				
				for(int sNext=0; sNext<nStates; sNext++) {
					if(pomdp.getTransitionTable(a).get(s, sNext) > 0.0) {
						numDest++;
					}
				}
				
				transitionDestinations[s][a] = new int[numDest];
				transitionProbabilities[s][a] = new double[numDest];
				
				int destID = 0;				
				for(int sNext=0; sNext<nStates; sNext++) {
					if(pomdp.getTransitionTable(a).get(s, sNext) > 0.0) {
						transitionDestinations[s][a][destID] = sNext;
						transitionProbabilities[s][a][destID] = pomdp.getTransitionTable(a).get(s, sNext);
						destID++;
					}
				}
				
			}
		}
		
		for(int a=0; a<nActions; a++) {
			for(int sNext=0; sNext<nStates; sNext++) {
				for(int o=0; o<nObservations; o++) {
					observationFunction[a][sNext][o] = pomdp.getObservationTable(a).get(sNext, o);
				}
			}
		}
		
		for(int s=0; s<nStates; s++) {
			for(int a=0; a<nActions; a++) {
				rewardFunction[s][a] = pomdp.getRewardTable(a).get(s);
			}
		}
		
		for(int a=0; a<nActions; a++) {
			try {
				actionLabels.put(a, pomdp.getActionString(a));
			}
			catch (NullPointerException e) {
				actionLabels.put(a, a+"");
			}
		}
		
		double[] beliefEntries = pomdp.getInitialBeliefState().getPoint().getArray();
		BeliefPoint b0 = new BeliefPoint(beliefEntries);
		
		POMDP newPOMDP = new POMDP(nStates, nActions, nObservations, observationFunction, b0, T);
		newPOMDP.setRewardFunction(rewardFunction);
		newPOMDP.setTransitionFunction(transitionDestinations, transitionProbabilities);
		
		return newPOMDP;
	}
}
