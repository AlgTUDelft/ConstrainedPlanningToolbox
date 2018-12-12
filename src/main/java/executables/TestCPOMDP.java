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

import instances.CPOMDPInstance;

import java.util.Random;

import domains.CPOMDPInstanceGenerator;
import domains.cbm.CBMGenerator;
import domains.webad.WebAdGenerator;
import evaluation.CPOMDPSimulator;

import lp.LPSolver;
import lp.gurobi.LPSolverGurobi;
import lp.lpsolve.LPSolverLPSolve;
import lp.simplex.LPSolverSimplex;
import solutions.pomdp.POMDPAgentSolutionPolicyBased;
import solutions.pomdp.CPOMDPSolution;
import algorithms.UnsupportedInstanceException;
import algorithms.pomdp.CPOMDPAlgorithm;
import algorithms.pomdp.calp.FiniteCALP;
import algorithms.pomdp.cgcp.CGCP;
import algorithms.pomdp.cgcp.FiniteVI;

public class TestCPOMDP {
	public static void main(String[] args) {
		Random rnd = new Random(222);
		
		int nAgents = 1;
		int nDecisions = 10;
		
		CPOMDPInstanceGenerator generator = new CBMGenerator();
		CPOMDPInstance instance = generator.getInstance(nAgents, nDecisions);
		
//		LPSolver lpSolver = new LPSolverSimplex();
		LPSolver lpSolver = new LPSolverGurobi();
//		LPSolver lpSolver = new LPSolverLPSolve();
		
//		CPOMDPAlgorithm alg = new FiniteCALP(lpSolver, rnd);
		CPOMDPAlgorithm alg = new CGCP(lpSolver, rnd);
		
		try {
			alg.setInstance(instance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		CPOMDPSolution solution = alg.solve();
		
		// print expected reward
		double expectedReward = solution.getExpectedReward();
		System.out.println("Expected reward: "+expectedReward);
		
		for(int k=0; k<instance.getNumDomainResources(); k++) {
			System.out.println("Limit resource "+k+": "+instance.getCostLimit(k));
		}
		
		// run simulations to evaluate solution
		CPOMDPSimulator sim = new CPOMDPSimulator(instance, solution, rnd);
		sim.run(1000);
		System.out.println("Mean reward: "+sim.getMeanReward());
		System.out.println("Mean cost: "+sim.getMeanTotalCost(0));
		
	}
}
