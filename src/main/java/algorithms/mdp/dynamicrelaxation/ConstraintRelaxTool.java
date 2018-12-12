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

import instances.CMDPInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import solutions.mdp.CMDPSolution;
import util.ProbabilitySample;
import util.StatisticsCalculator;

import model.CMDP;


public class ConstraintRelaxTool {
	// simulation variables
	private Random rnd;
	private int nRuns;
	
	// instance variables
	private CMDP[] cmdps;
	private int numDecisions;
	private CMDPInstance instance;
	private int numDomainResources;
	
	private boolean useBudgetConstraints;
	
	protected ConstraintRelaxTool(CMDPInstance instance, Random rnd, int nRuns, boolean useBudgetConstraints) {
		this.cmdps = instance.getCMDPs();
		this.rnd = rnd;
		this.nRuns = nRuns;
		this.numDecisions = instance.getNumDecisions();
		this.useBudgetConstraints = useBudgetConstraints;
		this.numDomainResources = instance.getNumDomainResources();
		this.instance = instance;
	}

	protected double updateReduction(CMDPSolution solution, double[][] reduction, double maxViolations, double factor) {
		// evaluate the solution to obtain violation statistics.
		ConsumptionList[][] resourceConsumptions = evaluateSolution(solution);

		// determine how much of a resource may be used at most
		double[][] maxConsumption = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];
		if(useBudgetConstraints) {
			for(int k=0; k<numDomainResources; k++) {
				for (CMDP cmdp : cmdps) {
					maxConsumption[k][0] += cmdp.getMaxCost(k);
				}
			}
		}
		else {
			for(int k=0; k<numDomainResources; k++) {
				for(int t=0; t<numDecisions; t++) {
					for (CMDP cmdp : cmdps) {
						maxConsumption[k][t] += cmdp.getMaxCost(k);
					}
				}
			}
		}
		

		double maxDifference = 0;

		// determine if any resources are already overconsumed?
		boolean notOverconsumed = true;
		if(useBudgetConstraints) {
			for (int k = 0; k < numDomainResources && notOverconsumed; k++) {
				int numOver = 0;

				List<Double> consumptions = resourceConsumptions[k][0].getConsumptions();
				for (Double consumption : consumptions) {
					if (consumption > instance.getCostLimit(k)) {
						numOver++;
					}
				}

				notOverconsumed = notOverconsumed && (numOver < maxViolations*consumptions.size());
			}
		}
		else {
			for (int k = 0; k < numDomainResources && notOverconsumed; k++) {
				for(int t=0; t<numDecisions; t++) {
					int numOver = 0;

					List<Double> consumptions = resourceConsumptions[k][t].getConsumptions();
					for (Double consumption : consumptions) {
						if (consumption > instance.getCostLimit(k, t)) {
							numOver++;
						}
					}

					notOverconsumed = notOverconsumed && (numOver < maxViolations*consumptions.size());
				}
			}
		}

		if(useBudgetConstraints) {
			for(int k=0; k<numDomainResources && notOverconsumed; k++) {
				if (reduction[k][0] > 0) {
					// get the consumptions associated with this resource.
					List<Double> consumptions = resourceConsumptions[k][0].getConsumptions();

					double mean = StatisticsCalculator.getMean(consumptions);
					double std = StatisticsCalculator.getStd(consumptions);

					// determine the sigma that allows the desired tolerance.
					double meanLow = 0;
					double meanHigh = instance.getCostLimit(k);
					double meanMid = 0;
					while (Math.abs(meanHigh - meanLow) > 0.01) {
						meanMid = (meanHigh + meanLow) / 2;

						double pMid = StatisticsCalculator.getViolationProbability(meanMid, std, instance.getCostLimit(k));

						if (pMid > maxViolations) {
							meanHigh = meanMid;
						} else {
							meanLow = meanMid;
						}
					}

					// determine the reduction you would require to have the consumption at index be equal to the budget.
					double gaussSlack = meanMid - mean;

					double reductionSlack = gaussSlack;

					// if we need to slacken the constraint,
					if (reductionSlack > 0) {
						// move the current reduction to the midpoint of the current slack and the ideal slack.
						double slackening = Math.max(0, reduction[k][0] - reductionSlack / factor);

						// determine the maximum change in resource reduction.
						maxDifference = Math.max(maxDifference, (reduction[k][0] - slackening) / instance.getCostLimit(k));

						// set the level of reduction
						reduction[k][0] = slackening;
					}
				}
			}
		}
		else {
			for(int k=0; k<numDomainResources && notOverconsumed; k++) {
				for(int t=0; t<numDecisions; t++) {
					if (reduction[k][t] > 0) {
						// get the consumptions associated with this resource.
						List<Double> consumptions = resourceConsumptions[k][t].getConsumptions();

						double mean = StatisticsCalculator.getMean(consumptions);
						double std = StatisticsCalculator.getStd(consumptions);

						// determine the sigma that allows the desired tolerance.
						double meanLow = 0;
						double meanHigh = instance.getCostLimit(k, t);
						double meanMid = 0;
						while (Math.abs(meanHigh - meanLow) > 0.01) {
							meanMid = (meanHigh + meanLow) / 2;

							double pMid = StatisticsCalculator.getViolationProbability(meanMid, std, instance.getCostLimit(k, t));

							if (pMid > maxViolations) {
								meanHigh = meanMid;
							} else {
								meanLow = meanMid;
							}
						}

						// determine the reduction you would require to have the consumption at index be equal to the budget.
						double gaussSlack = meanMid - mean;

						double reductionSlack = gaussSlack;

						// if we need to slacken the constraint,
						if (reductionSlack > 0) {
							// move the current reduction to the midpoint of the current slack and the ideal slack.
							double slackening = Math.max(0, reduction[k][t] - reductionSlack / factor);

							// determine the maximum change in resource reduction.
							maxDifference = Math.max(maxDifference, (reduction[k][t] - slackening) / instance.getCostLimit(k, t));

							// set the level of reduction
							reduction[k][t] = slackening;
						}
					}
				}
			}
		}
		
		return maxDifference;
	}
	
	private class ConsumptionList {
		private List<Double> consumptions = new ArrayList<Double>();
		
		public List<Double> getConsumptions() {
			return consumptions;
		}
		
		public void addConsumption(double consumption) {
			consumptions.add(consumption);
		}
	}

	private ConsumptionList[][] evaluateSolution(CMDPSolution solution) {
		ConsumptionList[][] resourceConsumptions = useBudgetConstraints ? new ConsumptionList[numDomainResources][1] : new ConsumptionList[numDomainResources][numDecisions];
		
		if(useBudgetConstraints) {
			for(int k=0; k<numDomainResources; k++) {
				resourceConsumptions[k][0] = new ConsumptionList();
			}
		}
		else {
			for(int k=0; k<numDomainResources; k++) {
				for(int t=0; t<numDecisions; t++) {
					resourceConsumptions[k][t] = new ConsumptionList();
				}
			}
		}
		
		for(int run=0; run<nRuns; run++) {
			double[][] currentConsumption = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];

			// get initial states
			int[] state = new int[cmdps.length];
			for(int i=0; i<cmdps.length; i++) {
				state[i] = cmdps[i].getInitialState();
			}
			
			for(int t=0; t<numDecisions; t++) {
				int[] actions = solution.getActions(t, state);
				
				for(int i=0; i<cmdps.length; i++) {
					CMDP cmdp = cmdps[i];
					int a = actions[i];
					
					// update current consumption
					for(int k=0; k<numDomainResources; k++) {
						if(useBudgetConstraints) {
							currentConsumption[k][0] += cmdp.getCost(k, state[i], a);
						}
						else {
							currentConsumption[k][t] += cmdp.getCost(k, state[i], a);
						}
					}
					
					state[i] = sampleNextState(cmdp, state[i], a);
				}
			}
			
			if(useBudgetConstraints) {
				for(int k=0; k<numDomainResources; k++) {
					resourceConsumptions[k][0].addConsumption(currentConsumption[k][0]);
				}
			}
			else {
				for(int k=0; k<numDomainResources; k++) {
					for(int t=0; t<numDecisions; t++) {
						resourceConsumptions[k][t].addConsumption(currentConsumption[k][t]);
					}
				}
			}
			
		}
		
		return resourceConsumptions;
	}
	
	private int sampleNextState(CMDP cmdp, int s, int a) {
		assert s >= 0 && s < cmdp.getNumStates() : "Invalid state action pair";

		int[] destinations = cmdp.getTransitionDestinations(s, a);
		double[] probabilities = cmdp.getTransitionProbabilities(s, a);

		return ProbabilitySample.sampleItemInline(destinations, probabilities, rnd);
	}
}
