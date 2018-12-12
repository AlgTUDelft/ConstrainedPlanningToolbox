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
package algorithms.mdp.dynamicrelaxation;

import java.util.Random;

import model.CMDP;
import instances.CMDPInstance;
import instances.ConstraintType;
import solutions.mdp.CMDPSolution;
import util.ConfigFile;
import util.ConsoleOutput;
import algorithms.UnsupportedInstanceException;
import algorithms.mdp.CMDPAlgorithm;
import algorithms.mdp.ReducedLimitAlgorithm;

public class DynamicRelaxation implements CMDPAlgorithm {
	
	private double convergenceTolerance;
	
	private ReducedLimitAlgorithm alg;
	private double tolerance;
	private double beta;
	
	private Random rnd;
	
	private CMDP[] cmdps;
	private CMDPInstance instance;
	private int numDomainResources;
	private int numAgents;
	private int numDecisions;
	
	private boolean useBudgetConstraints = false;
	
	private double timeLimit;
	
	public DynamicRelaxation(ReducedLimitAlgorithm alg, double alpha, double beta, Random rnd) {
		this.alg = alg;
		this.tolerance = alpha;
		this.beta = beta;
		this.rnd = rnd;
		this.convergenceTolerance = ConfigFile.getDoubleProperty("dynamicrelaxation_convergence_tolerance");
		this.timeLimit = ConfigFile.getDoubleProperty("dynamicrelaxation_time_limit");
	}
	
	@Override
	public void setInstance(CMDPInstance instance) throws UnsupportedInstanceException {		
		this.cmdps = instance.getCMDPs();
		this.numAgents = cmdps.length;
		this.instance = instance;
		this.numDomainResources = instance.getNumDomainResources();
		this.numDecisions = instance.getNumDecisions();
		
		// check if all costs are non-negative
		for(int i=0; i<numAgents; i++) {
			CMDP cmdp = cmdps[i];
			for(int k=0; k<numDomainResources; k++) {
				if(cmdp.getMinCost(k) < 0.0) {
					throw new UnsupportedInstanceException();
				}
			}
		}
		
		alg.setInstance(instance);
		
		useBudgetConstraints = (instance.getConstraintType() == ConstraintType.BUDGET);
	}

	@Override
	public CMDPSolution solve() {
		long startTime = System.currentTimeMillis();
		
		ConstraintRelaxTool crt = new ConstraintRelaxTool(instance, rnd, 100000, useBudgetConstraints);
		
		// obtain initial limits using Hoeffding bound
		double[][] reductions = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];
		
		for(int k=0; k<numDomainResources; k++) {
			if(useBudgetConstraints) {
				double maxConsumption = 0.0;
				
				for(int i=0; i<numAgents; i++) {
					maxConsumption += Math.pow(cmdps[i].getMaxCost(k) * ((double) numDecisions), 2.0);
				}
				
				reductions[k][0] = Math.sqrt((Math.log(tolerance) * maxConsumption) / -2.0);
				
				if(instance.getCostLimit(k) - reductions[k][0] < 0.0) {
					throw new RuntimeException("Cannot compute initial reduced limits, tolerance too low?");
				}
			}
			else {
				double maxConsumption = 0.0;
				
				for(int i=0; i<numAgents; i++) {
					maxConsumption += Math.pow(cmdps[i].getMaxCost(k), 2.0);
				}
				
				for(int t=0; t<numDecisions; t++) {
					reductions[k][t] = Math.sqrt((Math.log(tolerance) * maxConsumption) / -2.0);
					
					if(instance.getCostLimit(k, t) - reductions[k][t] < 0.0) {
						throw new RuntimeException("Cannot compute initial reduced limits, tolerance too low?");
					}
				}
			}
		}
		
		CMDPSolution solution = null;
		
		while(true) {
			// compute reduced limits
			double[][] reducedLimits = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];
			double[] reducedBudgets = new double[numDomainResources];
			for(int k=0; k<numDomainResources; k++) {
				if(useBudgetConstraints) {
					reducedLimits[k][0] = instance.getCostLimit(k) - reductions[k][0];
					reducedBudgets[k] = reducedLimits[k][0];
				}
				else {
					for(int t=0; t<numDecisions; t++) {
						reducedLimits[k][t] = instance.getCostLimit(k,t) - reductions[k][t];
					}
				}
			}
			
			// set reduced limits
			if(useBudgetConstraints) {
				alg.modifyBudgetConstraints(reducedBudgets);
			}
			else {
				alg.modifyInstantaneousConstraints(reducedLimits);
			}
			
			// solve instance with reduced limits
			solution = alg.solve();
			
			// obtain new reductions
			double reduction = crt.updateReduction(solution, reductions, tolerance, beta);
			
			ConsoleOutput.println("Max resource limit change: "+reduction);
			
			// check if we can stop
			double elapsedTime = (System.currentTimeMillis()-startTime) * 0.001;
			if(reduction < convergenceTolerance || elapsedTime > timeLimit) {
				break;
			}
			
		}
		
		return solution;
	}

	@Override
	public String getName() {
		return "DynamicRelaxation";
	}
}
