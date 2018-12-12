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
package executables;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import evaluation.CMDPSimulator;
import evaluation.CPOMDPSimulator;

import solutions.mdp.CMDPSolution;
import solutions.pomdp.CPOMDPSolution;

import algorithms.UnsupportedInstanceException;
import algorithms.mdp.constrainedmdp.ConstrainedMDP;
import algorithms.pomdp.CPOMDPAlgorithm;
import algorithms.pomdp.cgcp.CGCP;

import instances.CMDPInstance;
import instances.CPOMDPInstance;
import lp.LPSolver;
import lp.gurobi.LPSolverGurobi;
import model.BeliefPoint;
import model.CMDP;
import model.CPOMDP;

public class ToyExamples {
	public static void main(String[] args) {
		
		LPSolver lpSolver = new LPSolverGurobi();
		Random rnd = new Random(222);
		
		int nStates = 2;
		int nActions = 2;
		int nDecisions = 10;
		int initialState = 0;
				
		
		double[][] rewardFunction = new double[nStates][nActions];
		rewardFunction[1][1] = 10.0;
		
		int[][][] transitionDestinations = new int[nStates][nActions][];
		double[][][] transitionProbabilities = new double[nStates][nActions][];
		
		transitionDestinations[0][0] = new int[]{0};
		transitionProbabilities[0][0] = new double[]{1.0};
		
		transitionDestinations[0][1] = new int[]{0, 1};
		transitionProbabilities[0][1] = new double[]{0.1, 0.9};
		
		transitionDestinations[1][0] = new int[]{0};
		transitionProbabilities[1][0] = new double[]{1.0};
		
		transitionDestinations[1][1] = new int[]{1};
		transitionProbabilities[1][1] = new double[]{1.0};
		
		double[][] costFunction = new double[nStates][nActions];
		costFunction[1][1] = 2.0;
		List<double[][]> costFunctions = new ArrayList<double[][]>();
		costFunctions.add(costFunction);
		
		CMDP cmdp = new CMDP(nStates, nActions, costFunctions, initialState, nDecisions);
		cmdp.setRewardFunction(rewardFunction);
		cmdp.setTransitionFunction(transitionDestinations, transitionProbabilities);
		
		CMDPInstance cmdpInstance = CMDPInstance.createBudgetInstance(new CMDP[]{cmdp}, new double[]{0.5}, nDecisions);
		
		
		ConstrainedMDP mdpAlgorithm = new ConstrainedMDP(lpSolver, rnd);
		try {
			mdpAlgorithm.setInstance(cmdpInstance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
		}
		
		CMDPSolution mdpSolution = mdpAlgorithm.solve();
		
		
		CMDPSimulator mdpSim = new CMDPSimulator(cmdpInstance, mdpSolution, rnd);
		mdpSim.run(2000000);
		System.out.println("Mean reward: "+mdpSim.getMeanReward());
		System.out.println("Mean cost: "+mdpSim.getMeanTotalCost(0));
		
		
		
		
		int nObservations = 2;
		double[][][] observationFunction = new double[nActions][nStates][nObservations];
		
		for(int a=0; a<nActions; a++) {
			for(int sNext=0; sNext<nStates; sNext++) {
				int o = sNext;
				observationFunction[a][sNext][o] = 1.0;
			}
		}
		
		BeliefPoint b0 = new BeliefPoint(new double[]{1.0, 0.0});
		
		CPOMDP cpomdp = new CPOMDP(nStates, nActions, nObservations, costFunctions, observationFunction, b0, nDecisions);
		cpomdp.setRewardFunction(rewardFunction);
		cpomdp.setTransitionFunction(transitionDestinations, transitionProbabilities);
		
		CPOMDPInstance cpomdpInstance = CPOMDPInstance.createBudgetInstance(new CPOMDP[]{cpomdp}, new double[]{0.5}, nDecisions);
		
		CPOMDPAlgorithm pomdpAlgorithm = new CGCP(lpSolver, rnd);
		try {
			pomdpAlgorithm.setInstance(cpomdpInstance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
			System.exit(0);
		}	
		CPOMDPSolution pomdpSolution = pomdpAlgorithm.solve();
		
		CPOMDPSimulator pomdpSim = new CPOMDPSimulator(cpomdpInstance, pomdpSolution, rnd);
		pomdpSim.run(2000000);
		System.out.println("Mean reward: "+pomdpSim.getMeanReward());
		System.out.println("Mean cost: "+pomdpSim.getMeanTotalCost(0));
	}
}
