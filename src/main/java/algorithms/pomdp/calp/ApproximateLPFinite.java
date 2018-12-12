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
package algorithms.pomdp.calp;

import java.util.ArrayList;
import java.util.List;

import util.ConsoleOutput;

import lp.LPConstraint;
import lp.LPException;
import lp.LPExpression;
import lp.LPModel;
import lp.LPSolver;
import lp.LPVariable;
import lp.LPConstraintType;
import lp.LPVariableType;
import model.BeliefPoint;
import model.CPOMDP;

public class ApproximateLPFinite {
	// LP environment
	private LPSolver lpSolver;
	private final double objectiveCoefficientTolerance = 0.00001; // if coefficient is below threshold, it will be discarded
	
	// CPOMDP model solved in this object
	private CPOMDP[] cpomdps;
	private int T;
	private double costLimit;
	
	private LPModel cmdpModel;
	private LPConstraint cmdpCostConstraint;
	
	private final double binarySearchTolerance = 0.001;
	private final double policyEvaluationTolerance = 0.00001;
	
	// variables below are set during solving
	private double approximateExpectedReward;
	private double approximateExpectedCost;
	private double exactExpectedReward;
	private double exactExpectedCost;
	private double[][][] policy;
	private boolean[][] policyActionOptions;
	private double[][][][] weights;
	
	private List<BeliefPoint> lastB;
	private double[][] apxRewardModel;
	private double[][] apxCostModel;
	private LPVariable[][][] cmdpXVar;
	
	public ApproximateLPFinite(CPOMDP[] cpomdps, int T, double costLimit, LPSolver lpSolver) {
		assert cpomdps != null && lpSolver != null;
		this.lpSolver = lpSolver;
		this.cpomdps = cpomdps;
		this.T = T;
		this.costLimit = costLimit;
	}
	
	private double getCoefficient(double c) {
		return (Math.abs(c) < objectiveCoefficientTolerance) ? 0.0 : c;
	}
	
	private double[] getWeights(List<BeliefPoint> B, BeliefPoint b) {
		int nBeliefs = B.size();
		int nStates = b.getBelief().length;
		
		double[] weights = new double[nBeliefs];
		
		try {
			LPModel model = lpSolver.createModel();
			
			// create variables
			LPVariable[] wVar = new LPVariable[nBeliefs];
			for(int i=0; i<nBeliefs; i++) {
				double norm = getCoefficient(getEuclideanNorm(b, B, i));
				double obj = Math.abs(norm) > objectiveCoefficientTolerance ? -1.0*norm : 0.0; // we minimize
				wVar[i] = model.addVariable(0.0, 1.0, obj, LPVariableType.CONTINUOUS);
			}
			
			// add constraint for each state variable
			for(int s=0; s<nStates; s++) {
				LPExpression expr = model.createExpression();
				
				for(int i=0; i<nBeliefs; i++) {
					double coefficient = getCoefficient(B.get(i).getBelief(s));
					expr.addTerm(coefficient, wVar[i]);
				}
				
				model.addConstraint(expr, LPConstraintType.EQUAL, b.getBelief(s));
			}
			
			// add constraint to ensure that sum of weights equals 1
			LPExpression expr = model.createExpression();
			for(int i=0; i<nBeliefs; i++) {
				expr.addTerm(1.0, wVar[i]);
			}
			model.addConstraint(expr, LPConstraintType.EQUAL, 1.0);
			
			// solve the model
			boolean status = model.solve();
			assert status;
			
			// retrieve weights
			for(int i=0; i<nBeliefs; i++) {
				weights[i] = model.getVariableValue(wVar[i]);
			}
			
			model.dispose();
		} catch (LPException e) {
			e.printStackTrace();
		}
		
		return weights;
	}
	
	private double getEuclideanNorm(BeliefPoint b, List<BeliefPoint> B, int i) {
		BeliefPoint bi = B.get(i);
		assert b.getBelief().length == bi.getBelief().length;
		int nEntries = b.getBelief().length;
		
		double norm = 0.0;
		for(int s=0; s<nEntries; s++) {
			norm += Math.pow(b.getBelief(s) - bi.getBelief(s), 2.0);
		}
		
		return Math.sqrt(norm);
	}
	
	public void solve(List<BeliefPoint> B) {
		assert B.size() > 0;
		CPOMDP cpomdp = cpomdps[0];
		this.lastB = B;
		
		// compute weights (Algorithm 1, lines 3-5)
		int nBeliefs = B.size();
		int nStates = cpomdp.getNumStates();
		int nActions = cpomdp.getNumActions();
		int nObservations = cpomdp.getNumObservations();
		double[][][][] w = new double[nBeliefs][nBeliefs][nActions][nObservations]; // w[bPrime][b][a][o] = w(b', b^{a,o})
		
		for(int bIndex=0; bIndex<B.size(); bIndex++) {
			BeliefPoint b = B.get(bIndex);
			cpomdp.prepareBelief(b);
			
			for(int a=0; a<nActions; a++) {
				for(int o=0; o<nObservations; o++) {					
					if(b.getActionObservationProbability(a, o) > policyEvaluationTolerance) {
						BeliefPoint bao = cpomdp.updateBelief(b, a, o);
						double [] weights = getWeights(B, bao);
						assert weights.length == B.size();
						
						double sum = 0.0;
						for(int bPrimeIndex=0; bPrimeIndex<B.size(); bPrimeIndex++) {
							w[bPrimeIndex][bIndex][a][o] = weights[bPrimeIndex];
							sum += w[bPrimeIndex][bIndex][a][o];
						}
						
						assert Math.abs(sum-1.0) < 0.01 : "Weights do not sum to 1: "+sum;
					}
					else {
						/*
						 * We define a uniform distribution here, but in the final computation
						 * these weights never occur, since o cannot be observed after executing
						 * action a in belief b. In the computation of the transition model,
						 * it will by multiplied by zero anyway.
						 */
						for(int bPrimeIndex=0; bPrimeIndex<B.size(); bPrimeIndex++) {
							w[bPrimeIndex][bIndex][a][o] = 1.0 / ((double) B.size());
						}
					}
				}
			}
		}
		weights = w;
		
		// compute approximate transition model (Algorithm 1, line 6)
		double[][][] newTransitionModel = new double[nBeliefs][nActions][nBeliefs];
		
		for(int bIndex=0; bIndex<B.size(); bIndex++) {
			BeliefPoint b = B.get(bIndex);
			
			for(int a=0; a<nActions; a++) {
				double probSum = 0.0;
				
				for(int bPrimeIndex=0; bPrimeIndex<B.size(); bPrimeIndex++) {
					double prob = 0.0;
					
					for(int o=0; o<nObservations; o++) {
						prob += b.getActionObservationProbability(a, o) * w[bPrimeIndex][bIndex][a][o];
					}
					
					newTransitionModel[bIndex][a][bPrimeIndex] = prob;
					probSum += prob;
				}
				
				assert Math.abs(1.0-probSum) < 0.01 : "New reward model seems to be wrong, probability sum "+probSum;
			}
		}
		
		// compute approximate reward and cost model (Equations 6 and 7)		
		apxRewardModel = new double[nBeliefs][nActions];
		apxCostModel = new double[nBeliefs][nActions];
		for(int bIndex=0; bIndex<B.size(); bIndex++) {
			for(int a=0; a<nActions; a++) {
				for(int s=0; s<nStates; s++) {
					apxRewardModel[bIndex][a] += B.get(bIndex).getBelief(s) * cpomdp.getReward(s, a);
					apxCostModel[bIndex][a] += B.get(bIndex).getBelief(s) * cpomdp.getCost(0, s, a);
				}
			}
		}
		
		// solve new model using CMDP linear program
		double[][][] x = solveCMDP(newTransitionModel, apxRewardModel, apxCostModel);
		
		// compute V_R^*(b_0) using Equation 10
		double[][] beliefFlow = new double[T+1][B.size()];
		
		approximateExpectedReward = 0.0;
		approximateExpectedCost = 0.0;
		for(int t=0; t<T; t++) {
			for(int bIndex=0; bIndex<B.size(); bIndex++) {
				beliefFlow[t][bIndex] = 0.0;
				
				for(int a=0; a<cpomdp.getNumActions(); a++) {
					approximateExpectedReward += x[t][bIndex][a] * apxRewardModel[bIndex][a];
					approximateExpectedCost += x[t][bIndex][a] * apxCostModel[bIndex][a];
					beliefFlow[t][bIndex] += x[t][bIndex][a];
				}
			}
		}
		
		// compute policy using Equation 9
		policy = new double[T+1][B.size()][nActions];
		policyActionOptions = new boolean[B.size()][nActions];
		for(int t=0; t<=T; t++) {
			for(int bIndex=0; bIndex<B.size(); bIndex++) {
				for(int a=0; a<nActions; a++) {
					if(beliefFlow[t][bIndex] > 0.001) {
						policy[t][bIndex][a] = x[t][bIndex][a] / beliefFlow[t][bIndex];
						assert x[t][bIndex][a] <= beliefFlow[t][bIndex]+0.001 : bIndex+" "+a+" "+x[t][bIndex][a]+" "+beliefFlow[t][bIndex];
						assert policy[t][bIndex][a] >= -0.01 && policy[t][bIndex][a] <= 1.01 : "prob: "+policy[t][bIndex][a]+" ("+x[t][bIndex][a]+" / "+beliefFlow[t][bIndex]+")";
					
						if(policy[t][bIndex][a] > policyEvaluationTolerance) {
							policyActionOptions[bIndex][a] = true;
						}
					}
					else {
						// apparently the belief corresponding to bIndex is never reached, define uniform
						policy[t][bIndex][a] = 1.0 / ((double) nActions);
						assert policy[t][bIndex][a] >= -0.01 && policy[t][bIndex][a] <= 1.01 : "prob: "+policy[t][bIndex][a];
					}
				}
			}
		}
		
		// compute exact expected reward and cost using Equation 14, 15 and 16
		evaluateExact(nBeliefs, weights, policy);
		
		// we are done
	}
	
	private void evaluateExact(int numNodes, double[][][][] weights, double[][][] policy) {
		CPOMDP cpomdp = cpomdps[0];
		
		int t = T;
		
		double[][] Vreward = new double[numNodes][cpomdp.getNumStates()];
		double[][] Vcost = new double[numNodes][cpomdp.getNumStates()];
		
		while(t > 0) {
			t--;
			
			double[][] VrewardNext = new double[numNodes][cpomdp.getNumStates()];
			double[][] VcostNext = new double[numNodes][cpomdp.getNumStates()];
			
			for(int q=0; q<numNodes; q++) {
				for(int s=0; s<cpomdp.getNumStates(); s++){
					// expected immediate cost
					for(int a=0; a<cpomdp.getNumActions(); a++) {
						double prob = policy[t][q][a];
						VrewardNext[q][s] += prob * cpomdp.getReward(s, a);
						VcostNext[q][s] += prob * cpomdp.getCost(0, s, a);
					}
					
					// expected future cost
					for(int a=0; a<cpomdp.getNumActions(); a++) {
						if(policy[t][q][a] > policyEvaluationTolerance) {
							for(int o=0; o<cpomdp.getNumObservations(); o++) {
								for(int qNext=0; qNext<numNodes; qNext++) {
									double prob = policy[t][q][a] * weights[qNext][q][a][o];
									
									if(prob > policyEvaluationTolerance) {
										int[] transitionDestinations = cpomdp.getTransitionDestinations(t, s, a);
										double[] transitionProbabilities = cpomdp.getTransitionProbabilities(t, s, a);
										
										for(int j=0; j<transitionDestinations.length; j++) {
											int sNext = transitionDestinations[j];
											double transitionProb = transitionProbabilities[j];
											
											VrewardNext[q][s] += prob * transitionProb * cpomdp.getObservationProbability(a, sNext, o) * Vreward[qNext][sNext];
											VcostNext[q][s] += prob * transitionProb * cpomdp.getObservationProbability(a, sNext, o) * Vcost[qNext][sNext];											
										}
									}
								}
							}
						}
					}
				}
			}
			
			Vreward = VrewardNext;
			Vcost = VcostNext;
		}
		
		double expectedReward = 0.0;
		double expectedCost = 0.0;
		for(int s=0; s<cpomdp.getNumStates(); s++) {
			expectedReward += Vreward[0][s] * cpomdp.getInitialBelief().getBelief(s);
			expectedCost += Vcost[0][s] * cpomdp.getInitialBelief().getBelief(s);
		}
		
		exactExpectedReward = expectedReward;
		exactExpectedCost = expectedCost;
	}
	
	private double[][][] solveCMDP(double[][][] transitionModel, double[][] rewardModel, double[][] costModel) {
		int nBeliefs = transitionModel.length;
		int nActions = transitionModel[0].length;
		
		double[][][] retX = null;
		
		try {
			cmdpModel = lpSolver.createModel();
			
			// create variables
			cmdpXVar = new LPVariable[T+1][nBeliefs][nActions];
			for(int t=0; t<=T; t++) {
				for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
					for(int a=0; a<nActions; a++) {
						double coefficient = getCoefficient(rewardModel[bIndex][a]);
						double obj = Math.abs(coefficient) > objectiveCoefficientTolerance ? coefficient : 0.0;
						cmdpXVar[t][bIndex][a] = cmdpModel.addVariable(0.0, Double.POSITIVE_INFINITY, obj, LPVariableType.CONTINUOUS);
					}
				}
			}
			
			// add flow constraints
			for(int bPrimeIndex=0; bPrimeIndex<nBeliefs; bPrimeIndex++) {
				for(int t=0; t<T; t++) {
					LPExpression expr = cmdpModel.createExpression();
					
					for(int aPrime=0; aPrime<nActions; aPrime++) {
						expr.addTerm(1.0, cmdpXVar[t+1][bPrimeIndex][aPrime]);
					}
					
					for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
						for(int a=0; a<nActions; a++) {
							expr.addTerm(-1.0 * transitionModel[bIndex][a][bPrimeIndex], cmdpXVar[t][bIndex][a]);
						}
					}
					
					cmdpModel.addConstraint(expr, LPConstraintType.EQUAL, 0.0);
				}
			}
			
			// add initial flow constraint
			for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
				LPExpression expr = cmdpModel.createExpression();
				
				for(int a=0; a<nActions; a++) {
					expr.addTerm(1.0, cmdpXVar[0][bIndex][a]);
				}
				
				cmdpModel.addConstraint(expr, LPConstraintType.EQUAL, (bIndex==0) ? 1.0 : 0.0);
			}
			
			// add cost constraint
			LPExpression expr = cmdpModel.createExpression();
			for(int t=0; t<T; t++) {
				for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
					for(int a=0; a<nActions; a++) {
						expr.addTerm(costModel[bIndex][a], cmdpXVar[t][bIndex][a]);
					}
				}
			}
			cmdpCostConstraint = cmdpModel.addConstraint(expr, LPConstraintType.LESS_EQUAL, costLimit);
			
			// solve the model
			boolean solved = cmdpModel.solve();
			assert solved;
			
			// get flows
			double[][][] x = new double[T+1][nBeliefs][nActions];
			for(int t=0; t<=T; t++) {
				for(int bIndex=0; bIndex<nBeliefs; bIndex++) {
					for(int a=0; a<nActions; a++) {
						x[t][bIndex][a] = cmdpModel.getVariableValue(cmdpXVar[t][bIndex][a]);
					}
				}
			}
			retX = x;
			
		} catch (LPException e) {
			e.printStackTrace();
		}
		
		assert retX != null;
		return retX;
	}
	
	public void runBinarySearch() {
		double minLimit = 0.0;
		double maxLimit = costLimit;
		
		int nActions = cpomdps[0].getNumActions();
		int numNodes = lastB.size();
		
		double[][][] x;
		
		try {			
			while(true) {
				double currentLimit = (maxLimit + minLimit) / 2.0;
				cmdpModel.changeConstraintRHS(cmdpCostConstraint, currentLimit);
				boolean solved = cmdpModel.solve();
				
				ConsoleOutput.println(minLimit+" "+maxLimit+", current: "+currentLimit);
				
				if(solved) {
					// get x
					x = new double[T+1][numNodes][nActions];
					for(int t=0; t<=T; t++) {
						for(int bIndex=0; bIndex<numNodes; bIndex++) {
							for(int a=0; a<nActions; a++) {
								x[t][bIndex][a] = cmdpModel.getVariableValue(cmdpXVar[t][bIndex][a]);
							}
						}
					}
					
					// derive current policy from the model
					double[][] beliefFlow = new double[T+1][lastB.size()];
					approximateExpectedReward = 0.0;
					approximateExpectedCost = 0.0;
					for(int t=0; t<=T; t++) {
						for(int bIndex=0; bIndex<numNodes; bIndex++) {
							beliefFlow[t][bIndex] = 0.0;
							
							for(int a=0; a<nActions; a++) {
								approximateExpectedReward += x[t][bIndex][a] * apxRewardModel[bIndex][a];
								approximateExpectedCost += x[t][bIndex][a] * apxCostModel[bIndex][a];
								beliefFlow[t][bIndex] += x[t][bIndex][a];
							}
						}
					}
					
					// compute policy using Equation 9
					policy = new double[T+1][numNodes][nActions];
					policyActionOptions = new boolean[numNodes][nActions];
					for(int t=0; t<=T; t++) {
						for(int bIndex=0; bIndex<numNodes; bIndex++) {
							for(int a=0; a<nActions; a++) {
								if(beliefFlow[t][bIndex] > 0.001) { // FIXME we encounter tiny numbers here, which causes problems
									policy[t][bIndex][a] = x[t][bIndex][a] / beliefFlow[t][bIndex];
									//assert x[t][bIndex][a] <= beliefFlow[t][bIndex] : bIndex+" "+a+" "+x[t][bIndex][a]+" "+beliefFlow[t][bIndex];
									assert policy[t][bIndex][a] >= -0.01 && policy[t][bIndex][a] <= 1.01 : "prob: "+policy[t][bIndex][a];
								
									if(policy[t][bIndex][a] > policyEvaluationTolerance) {
										policyActionOptions[bIndex][a] = true;
									}
								}
								else {
									// apparently the belief corresponding to bIndex is never reached, define uniform
									policy[t][bIndex][a] = 1.0 / ((double) nActions);
									assert policy[t][bIndex][a] >= -0.01 && policy[t][bIndex][a] <= 1.01 : "prob: "+policy[t][bIndex][a];
								}
							}
						}
					}
					
					// perform exact policy evaluation
					evaluateExact(numNodes, weights, policy);
					
					// update binary search bound
					ConsoleOutput.println("Exact expected cost: "+exactExpectedCost);
					
					if(exactExpectedCost > costLimit) {
						maxLimit = currentLimit;
					}
					else {
						minLimit = currentLimit;
					}
					
					// check if we can stop
					if(Math.abs(exactExpectedCost - costLimit) < binarySearchTolerance) {
						break;
					}
				}
				else {
					// constraint too tight, search in upper half
					minLimit = currentLimit;
				}
				
				ConsoleOutput.println();
			}
			
			// compute approximate reward and cost
			approximateExpectedReward = 0.0;
			approximateExpectedCost = 0.0;
			for(int t=0; t<T; t++) {
				for(int bIndex=0; bIndex<numNodes; bIndex++) {
					for(int a=0; a<cpomdps[0].getNumActions(); a++) {
						approximateExpectedReward += x[t][bIndex][a] * apxRewardModel[bIndex][a];
						approximateExpectedCost += x[t][bIndex][a] * apxCostModel[bIndex][a];
					}
				}
			}
			
			// done!
		} catch (LPException e) {
			e.printStackTrace();
		}
	}
	
	public double getDistance(List<BeliefPoint> existingBeliefs, List<BeliefPoint> newBeliefs, int candidateID) {
		// find the belief point for which we want to compute the distance
		BeliefPoint b = newBeliefs.get(candidateID);
		
		// merge the belief sets, except the belief point corresponding to the candidate ID
		List<BeliefPoint> B = new ArrayList<BeliefPoint>();
		B.addAll(existingBeliefs);
		
		for(int i=0; i<newBeliefs.size(); i++) {
			if(i != candidateID) {
				B.add(newBeliefs.get(i));
			}
		}
		
		int nBeliefs = B.size();
		int nStates = b.getBelief().length;
		
		double distance = 0.0;
		
		try {
			LPModel model = lpSolver.createModel();
			
			// create variables
			LPVariable[] wVar = new LPVariable[nBeliefs];
			for(int i=0; i<nBeliefs; i++) {
				double norm = getCoefficient(getEuclideanNorm(b, B, i));
				double obj = Math.abs(norm) > objectiveCoefficientTolerance ? -1.0*norm : 0.0; // minimize
				wVar[i] = model.addVariable(0.0, 1.0, obj, LPVariableType.CONTINUOUS);
			}
			
			// add constraint for each state variable
			for(int s=0; s<nStates; s++) {
				LPExpression expr = model.createExpression();
				
				for(int i=0; i<nBeliefs; i++) {
					double coefficient = getCoefficient(B.get(i).getBelief(s));
					expr.addTerm(coefficient, wVar[i]);
				}
				
				model.addConstraint(expr, LPConstraintType.EQUAL, b.getBelief(s));
			}
			
			// add constraint to ensure that sum of weights equals 1
			LPExpression expr = model.createExpression();
			for(int i=0; i<nBeliefs; i++) {
				expr.addTerm(1.0, wVar[i]);
			}
			model.addConstraint(expr, LPConstraintType.EQUAL, 1.0);
			
			// solve the model
			boolean solved = model.solve();
			
			if(solved) {
				distance = model.getObjectiveValue() * -1.0;
			}
			else {
				// LP could not be solved, return 0 such that belief point is discarded
				ConsoleOutput.println("Belief point distance could not be computed!");
				distance = 0.0;
			}
			
			model.dispose();
		} catch (LPException e) {
			e.printStackTrace();
		}
		
		return distance;
	}
	
	public double getApproximateExpectedReward() {
		return approximateExpectedReward;
	}
	
	public double getApproximateExpectedCost() {
		return approximateExpectedCost;
	}
	
	public double getExactExpectedReward() {
		return exactExpectedReward;
	}
	
	public double getExactExpectedCost() {
		return exactExpectedCost;
	}
	
	public double[][][] getPolicy() {
		return policy;
	}
	
	public boolean isActionPolicyFeasible(int bIndex, int a) {
		assert bIndex>=0 && bIndex<policyActionOptions.length;
		return policyActionOptions[bIndex][a];
	}
	
	public double[][][][] getWeights() {
		return weights;
	}
	
	public void dispose() {
		cmdpModel.dispose();
	}
	
}
