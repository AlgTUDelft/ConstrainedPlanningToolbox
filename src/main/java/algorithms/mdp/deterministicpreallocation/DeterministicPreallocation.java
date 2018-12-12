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
package algorithms.mdp.deterministicpreallocation;

import instances.CMDPInstance;
import instances.ConstraintType;

import java.util.Random;

import algorithms.UnsupportedInstanceException;
import algorithms.mdp.CMDPAlgorithm;

import solutions.mdp.MDPAgentSolutionPolicyBased;
import solutions.mdp.MDPPolicyStochastic;
import solutions.mdp.CMDPSolution;
import solutions.mdp.CMDPSolutionPolicyBased;

import lp.LPConstraintType;
import lp.LPException;
import lp.LPExpression;
import lp.LPModel;
import lp.LPSolver;
import lp.LPVariable;
import lp.LPVariableType;
import model.CMDP;

public class DeterministicPreallocation implements CMDPAlgorithm {
	private LPSolver lpSolver;
	private Random rnd;
	
	private CMDP[] cmdps;
	private CMDPInstance instance;
	private int numDomainResources;
	private int numAgents;
	private int numDecisions;

	private boolean useBudgetConstraints = true;
	
	public DeterministicPreallocation(LPSolver lpSolver, Random rnd) throws Exception {
		this.lpSolver = lpSolver;
		this.rnd = rnd;
		
		if(!lpSolver.supportsMILP()) {
			System.out.println("Deterministic preallocation requires LP solver with MILP support");
			throw new Exception("Deterministic preallocation requires LP solver with MILP support");
		}
	}
	
	public void setInstance(CMDPInstance instance) throws UnsupportedInstanceException {
		this.cmdps = instance.getCMDPs();
		this.numAgents = cmdps.length;
		this.instance = instance;
		this.numDomainResources = instance.getNumDomainResources();
		this.numDecisions = instance.getNumDecisions();
		
		this.useBudgetConstraints = (instance.getConstraintType() == ConstraintType.BUDGET);
		
		// check if all costs are non-negative
		for(int i=0; i<numAgents; i++) {
			CMDP cmdp = cmdps[i];
			for(int k=0; k<numDomainResources; k++) {
				if(cmdp.getMinCost(k) < 0.0) {
					throw new UnsupportedInstanceException();
				}
			}
		}
	}
	
	public CMDPSolution solve() {
		MDPAgentSolutionPolicyBased[] retSolution = null;
		
		try {
			LPModel model = lpSolver.createModel();
			
			// create variables
			LPVariable[][][][] xVar = new LPVariable[numAgents][][][];
			LPVariable[][][][] xBarVar = new LPVariable[numAgents][][][];
			for(int i=0; i<numAgents; i++) {
				CMDP cmdp = cmdps[i];
				xVar[i] = new LPVariable[numDecisions][cmdp.getNumStates()][cmdp.getNumActions()];
				xBarVar[i] = new LPVariable[numDecisions][cmdp.getNumStates()][cmdp.getNumActions()];
				
				for(int t=0; t<numDecisions; t++) {
					for(int s=0; s<cmdp.getNumStates(); s++) {
						for(int a : cmdp.getFeasibleActions(t, s)) {
							xVar[i][t][s][a] = model.addVariable(0.0, 1.0, cmdp.getReward(t, s, a), LPVariableType.CONTINUOUS);
							xBarVar[i][t][s][a] = model.addVariable(0.0, 1.0, cmdp.getReward(t, s, a), LPVariableType.INTEGER);
						}
					}
				}
			}
			
			// add constraints with binary vars
			for(int i=0; i<numAgents; i++) {
				CMDP cmdp = cmdps[i];
				
				for(int t=0; t<numDecisions; t++) {
					for(int s=0; s<cmdp.getNumStates(); s++) {
						for(int a : cmdp.getFeasibleActions(t, s)) {
							LPExpression expr = model.createExpression();
							expr.addTerm(1.0, xVar[i][t][s][a]);
							expr.addTerm(-1.0, xBarVar[i][t][s][a]);
							model.addConstraint(expr, LPConstraintType.LESS_EQUAL, 0.0);
						}
					}
				}
			}
			
			// create flow conservation constraints
			for(int i=0; i<numAgents; i++) {
				CMDP cmdp = cmdps[i];
				
				for(int t=0; t<numDecisions-1; t++) {
					LPExpression[] expr = new LPExpression[cmdp.getNumStates()];
					for(int sPrime=0; sPrime<cmdp.getNumStates(); sPrime++) {
						expr[sPrime] = model.createExpression();
					}
					
					// LHS
					for(int sPrime=0; sPrime<cmdp.getNumStates(); sPrime++) {
						for(int aPrime : cmdp.getFeasibleActions(t+1, sPrime)) {
							expr[sPrime].addTerm(1.0, xVar[i][t+1][sPrime][aPrime]);
						}
					}
					
					// RHS
					for(int s=0; s<cmdp.getNumStates(); s++) {
						for(int a : cmdp.getFeasibleActions(t, s)) {
							int[] transitionDestinations = cmdp.getTransitionDestinations(t, s, a);
							double[] transitionProbabilities = cmdp.getTransitionProbabilities(t, s, a);
							
							for(int j=0; j<transitionDestinations.length; j++) {
								int sPrime = transitionDestinations[j];
								double prob = transitionProbabilities[j];
								expr[sPrime].addTerm(-1.0 * prob, xVar[i][t][s][a]);
							}
						}
					}
					
					for(int sPrime=0; sPrime<cmdp.getNumStates(); sPrime++) {
						model.addConstraint(expr[sPrime], LPConstraintType.EQUAL, 0.0);
					}
				}
			}
			
			// create initial state constraints
			for(int i=0; i<numAgents; i++) {
				CMDP cmdp = cmdps[i];
				int initialState = cmdp.getInitialState();
				
				for(int s=0; s<cmdp.getNumStates(); s++) {
					LPExpression expr = model.createExpression();
					
					for(int a : cmdp.getFeasibleActions(0, s)) {
						expr.addTerm(1.0, xVar[i][0][s][a]);
					}
					
					model.addConstraint(expr, LPConstraintType.EQUAL, (s==initialState) ? 1.0 : 0.0);
				}
			}
			
			// CMDP constraints
			if(useBudgetConstraints) {
				for(int k=0; k<numDomainResources; k++) {
					LPExpression expr = model.createExpression();
					
					for(int i=0; i<numAgents; i++) {
						CMDP cmdp = cmdps[i];
						for(int t=0; t<numDecisions; t++) {
							for(int s=0; s<cmdp.getNumStates(); s++) {
								for(int a : cmdp.getFeasibleActions(t, s)) {
									expr.addTerm(cmdp.getCost(k, s, a), xBarVar[i][t][s][a]);
								}
							}
						}
					}
					
					model.addConstraint(expr, LPConstraintType.LESS_EQUAL, instance.getCostLimit(k));
				}
			}
			else {
				for(int k=0; k<numDomainResources; k++) {
					for(int t=0; t<numDecisions; t++) {
						LPExpression expr = model.createExpression();
						
						for(int i=0; i<numAgents; i++) {
							CMDP cmdp = cmdps[i];
							for(int s=0; s<cmdp.getNumStates(); s++) {
								for(int a : cmdp.getFeasibleActions(t, s)) {
									expr.addTerm(cmdp.getCost(k, s, a), xBarVar[i][t][s][a]);
								}
							}
						}
						
						model.addConstraint(expr, LPConstraintType.LESS_EQUAL, instance.getCostLimit(k, t));
					}
				}
			}
			
			// solve the model
			boolean status = model.solve();
			assert status : status+"";
			
			// return solution
			retSolution = new MDPAgentSolutionPolicyBased[numAgents];
			for(int i=0; i<numAgents; i++) {
				CMDP cmdp = cmdps[i];
				
				double[][][] x = new double[numDecisions][cmdp.getNumStates()][cmdp.getNumActions()];
				double expectedReward = 0.0;
				for(int t=0; t<numDecisions; t++) {
					for(int s=0; s<cmdp.getNumStates(); s++) {
						for(int a : cmdp.getFeasibleActions(t, s)) {
							x[t][s][a] = model.getVariableValue(xVar[i][t][s][a]);
							assert x[t][s][a] >= -0.0001 && x[t][s][a] <= 1.0001 : x[t][s][a]+"";
							expectedReward += x[t][s][a] * cmdp.getReward(t, s, a);
						}
					}
				}
				
				MDPPolicyStochastic policy = new MDPPolicyStochastic(x, expectedReward, rnd);
				retSolution[i] = policy;
			}
			
			model.dispose();
		} catch (LPException e) {
			e.printStackTrace();
		}
		
		return new CMDPSolutionPolicyBased(retSolution);
	}
	
	public String getName() {
		return "DeterministicPreallocation";
	}
	
}
