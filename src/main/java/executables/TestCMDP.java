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

import instances.CMDPInstance;

import java.util.Random;

import lp.LPSolver;
import lp.gurobi.LPSolverGurobi;
import lp.simplex.LPSolverSimplex;
import solutions.mdp.MDPAgentSolutionPolicyBased;
import solutions.mdp.CMDPSolution;
import util.ConsoleOutput;
import util.SolutionManager;
import algorithms.UnsupportedInstanceException;
import algorithms.mdp.CMDPAlgorithm;
import algorithms.mdp.colgen.ColGen;
import algorithms.mdp.colgen.ValueIterationFiniteHorizon;
import algorithms.mdp.constrainedmdp.ConstrainedMDP;
import algorithms.mdp.deterministicpreallocation.DeterministicPreallocation;
import algorithms.mdp.dynamicrelaxation.DynamicRelaxation;
import domains.CMDPInstanceGenerator;
import domains.advertising.AdvertisingInstanceGenerator;
import domains.maze.MazeInstanceGenerator;
import domains.tcl.TCLInstanceGeneratorFixedLimit;
import domains.tcl.TCLInstanceGeneratorMultiLevel;
import evaluation.CMDPSimulator;
import instances.ConstraintType;

public class TestCMDP {
	public static void main(String[] args) {
		// LPSolver lpSolver = new LPSolverSimplex();
		 LPSolver lpSolver = new LPSolverGurobi();
		// LPSolver lpSolver = new LPSolverGurobi();
		// LPSolver lpSolver = new LPSolverLPSolve();
		
		Random rnd = new Random(222);
		
		ConsoleOutput.enableOutput();
		
		// initialize the instance generator
		CMDPInstanceGenerator generator = new AdvertisingInstanceGenerator();
		// CMDPInstanceGenerator generator = new TCLInstanceGeneratorFixedLimit();
		// CMDPInstanceGenerator generator = new TCLInstanceGeneratorMultiLevel();
		// CMDPInstanceGenerator generator = new MazeInstanceGenerator();
		
		// create the instance
		int nAgents = 20;
		int nDecisions = 10;
		CMDPInstance instance = generator.getInstance(nAgents, nDecisions);
		
		// initialize algorithm and solve
		CMDPAlgorithm alg = new ColGen(lpSolver, rnd);
		// CMDPAlgorithm alg = new ConstrainedMDP(lpSolver, rnd);
		// CMDPAlgorithm alg = new DPFiniteHorizon(lpSolver, rnd);
		// CMDPAlgorithm alg = new DynamicRelaxation(new ConstrainedMDP(lpSolver, rnd), 0.05, 2, rnd);
		// CMDPAlgorithm alg = new DynamicRelaxation(new ColGen(lpSolver, new ValueIterationFiniteHorizon(), rnd), 0.05, 2, rnd);
		
		try {
			alg.setInstance(instance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		CMDPSolution solution = alg.solve();
		
		// print expected reward
		double expectedReward = solution.getExpectedReward();
		System.out.println("Expected reward: "+expectedReward);
		
		// run simulations to evaluate solution
		CMDPSimulator sim = new CMDPSimulator(instance, solution, rnd);
		sim.run(200000);
		System.out.println("Mean reward: "+sim.getMeanReward());
		
		// print cost limits, mean in simulation, violation probability estimates
		if(instance.getConstraintType() == ConstraintType.BUDGET) {
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				System.out.println("Limit resource "+k+": "+instance.getCostLimit(k));
			}
			
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				System.out.println("Mean total cost resource "+k+": "+sim.getMeanTotalCost(k));
			}
			
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				System.out.println("Violation probability estimate resource "+k+": "+sim.getViolationProbabilityEstimateTotal(k));
			}
		}
		else {
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				for(int t=0; t<nDecisions; t++) {
					System.out.println("Limit resource "+k+" at time "+t+": "+instance.getCostLimit(k,t));
				}
			}
			
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				for(int t=0; t<nDecisions; t++) {
					System.out.println("Mean cost resource "+k+" at time "+t+": "+sim.getMeanInstantaneousCost(k, t));
				}
			}
			
			for(int k=0; k<instance.getNumDomainResources(); k++) {
				for(int t=0; t<nDecisions; t++) {
					System.out.println("Violation probability estimate resource "+k+" at time "+t+": "+sim.getViolationProbabilityEstimateInstantaneous(k, t));
				}
			}
		}
	}
}
