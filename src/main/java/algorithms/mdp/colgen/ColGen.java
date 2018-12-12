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
package algorithms.mdp.colgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPException;
import lp.LPConstraintType;
import lp.LPModel;
import lp.LPSolver;
import lp.LPVariable;
import lp.LPVariableType;
import model.CMDP;
import instances.CMDPInstance;
import instances.ConstraintType;
import solutions.mdp.MDPAgentSolutionPolicyBased;
import solutions.mdp.MDPPolicy;
import solutions.mdp.MDPPolicySet;
import solutions.mdp.CMDPSolution;
import solutions.mdp.CMDPSolutionPolicyBased;
import util.ConfigFile;
import algorithms.UnsupportedInstanceException;
import algorithms.mdp.CMDPAlgorithm;
import algorithms.mdp.ReducedLimitAlgorithm;

public class ColGen implements CMDPAlgorithm, ReducedLimitAlgorithm {
	private LPSolver lpSolver;
	private ValueIterationFiniteHorizon vi;
	private Random rnd;
	
	private CMDP[] cmdps;
	private CMDPInstance instance;
	private int numDomainResources;
	private int numAgents;
	private int numDecisions;
	
	private boolean useBudgetConstraints = false;
	
	private double lambdaTolerance;
	
	private double timelimit;
	
	public ColGen(LPSolver lpSolver, Random rnd) {
		this.lpSolver = lpSolver;
		this.vi = new ValueIterationFiniteHorizon();
		this.rnd = rnd;
		this.lambdaTolerance = ConfigFile.getDoubleProperty("colgen_dual_convergence_tolerance");
		this.timelimit = ConfigFile.getDoubleProperty("colgen_time_limit");
	}

	@Override
	public void setInstance(CMDPInstance instance) throws UnsupportedInstanceException {		
		this.cmdps = instance.getCMDPs();
		this.numAgents = cmdps.length;
		this.instance = instance;
		this.numDomainResources = instance.getNumDomainResources();
		this.numDecisions = instance.getNumDecisions();
		
		if(instance.getConstraintType() == ConstraintType.BUDGET) {
			useBudgetConstraints = true;
		}
		
		initializeModel();
	}

	private LPModel model;
	private double[][] lambdaInstantaneous;
	private double[] lambdaBudget;
	private ArrayList<ArrayList<LPVariable>> vars;
	private ArrayList<ArrayList<MDPAgentSolutionPolicyBased>> mdpSolutions;
	private LPConstraint[][] costConstraints;
	private LPConstraint[] probabilityConstraints;
	
	private void initializeModel() {
		try {
			model = lpSolver.createModel();
			lambdaInstantaneous = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];
			lambdaBudget = new double[numDomainResources];
			
			vars = new ArrayList<ArrayList<LPVariable>>();
			mdpSolutions = new ArrayList<ArrayList<MDPAgentSolutionPolicyBased>>();
			
			costConstraints = useBudgetConstraints ? new LPConstraint[numDomainResources][1] : new LPConstraint[numDomainResources][numDecisions];
			for(int k=0; k<numDomainResources; k++) {
				if(useBudgetConstraints) {
					costConstraints[k][0] = model.addConstraint(model.createExpression(), LPConstraintType.LESS_EQUAL, instance.getCostLimit(k));
					lambdaInstantaneous[k][0] = Double.MAX_VALUE;
					lambdaBudget[k] = Double.MAX_VALUE;
				}
				else {
					for(int t=0; t<numDecisions; t++) {
						costConstraints[k][t] = model.addConstraint(model.createExpression(), LPConstraintType.LESS_EQUAL, instance.getCostLimit(k, t));
						lambdaInstantaneous[k][t] = Double.MAX_VALUE;
					}
				}
			}
			
			probabilityConstraints = new LPConstraint[numAgents];
			for(int i=0; i<numAgents; i++) {
				probabilityConstraints[i] = model.addConstraint(model.createExpression(), LPConstraintType.EQUAL, 1.0);
				vars.add(new ArrayList<LPVariable>());
				mdpSolutions.add(new ArrayList<MDPAgentSolutionPolicyBased>());
			}
		}
		catch(LPException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public CMDPSolution solve() {
		MDPAgentSolutionPolicyBased[] solution = null;
		
		long startTime = System.currentTimeMillis();
		
		try {			
			while(true) {
				// generate policies with lambda and generate the columns
				for(int i=0; i<numAgents; i++) {
					MDPAgentSolutionPolicyBased mdpSolution = null;
					
					if(this.useBudgetConstraints) {
						mdpSolution = vi.solve(cmdps[i], numDecisions, lambdaBudget);
					}
					else {
						mdpSolution = vi.solve(cmdps[i], numDecisions, lambdaInstantaneous);
					}
					
					
					LPColumn col = model.createColumn();
					for(int k=0; k<numDomainResources; k++) {
						if(useBudgetConstraints) {
							col.addTerm(mdpSolution.getExpectedTotalCost(k), costConstraints[k][0]);
						}
						else {
							for(int t=0; t<numDecisions; t++) {
								col.addTerm(mdpSolution.getExpectedInstantaneousCost(k, t), costConstraints[k][t]);
							}
						}
					}
					col.addTerm(1.0, probabilityConstraints[i]);
					LPVariable newVar = model.addColumn(0.0, 1.0, mdpSolution.getExpectedReward(), LPVariableType.CONTINUOUS, col);
					vars.get(i).add(newVar);
					mdpSolutions.get(i).add(mdpSolution);
				}
				
				// solve LP and check convergence
				boolean solved = model.solve();
				assert solved;
				
				double[][] newLambdaInstantaneous = useBudgetConstraints ? new double[numDomainResources][1] : new double[numDomainResources][numDecisions];
				double[] newLambdaBudget = new double[numDomainResources];
				double lambdaDiff = 0.0;
				
				for(int k=0; k<numDomainResources; k++) {
					if(useBudgetConstraints) {
						newLambdaBudget[k] = model.getDualPrice(costConstraints[k][0]);
						lambdaDiff += Math.abs(lambdaBudget[k]-newLambdaBudget[k]);
					}
					else {
						for(int t=0; t<numDecisions; t++) {
							newLambdaInstantaneous[k][t] = model.getDualPrice(costConstraints[k][t]);
							lambdaDiff += Math.abs(lambdaInstantaneous[k][t]-newLambdaInstantaneous[k][t]);
						}
					}
				}
				
				lambdaInstantaneous = newLambdaInstantaneous;
				lambdaBudget = newLambdaBudget;
				
				double elapsedTime = (System.currentTimeMillis() - startTime) * 0.001;
				
				if(lambdaDiff < lambdaTolerance || elapsedTime > timelimit) {
					break;
				}
			}
			
			// obtain solution
			solution = extractSolution();
		}
		catch(LPException e) {
			e.printStackTrace();
		}
		
		return new CMDPSolutionPolicyBased(solution);
	}
	
	private MDPAgentSolutionPolicyBased[] extractSolution() {
		MDPAgentSolutionPolicyBased[] solution = new MDPAgentSolutionPolicyBased[numAgents];
		
		for(int i=0; i<numAgents; i++) {
			List<MDPPolicy> sol = new ArrayList<MDPPolicy>();
			List<Double> probs = new ArrayList<Double>();
			double expectedReward = 0.0;
			double[] expectedTotalCost = new double[numDomainResources];
			double[][] expectedInstantaneousCost = new double[numDomainResources][numDecisions];
			
			for(int j=0; j<vars.get(i).size(); j++) {
				double prob = model.getVariableValue(vars.get(i).get(j));
				if(prob > 0.000001) {
					MDPAgentSolutionPolicyBased mdpSol = mdpSolutions.get(i).get(j);
					MDPPolicy policy = mdpSol.getPolicy();
					
					sol.add(policy);
					probs.add(prob);
					
					expectedReward += prob * mdpSol.getExpectedReward();
					
					for(int k=0; k<numDomainResources; k++) {
						expectedTotalCost[k] += prob * mdpSol.getExpectedTotalCost(k);
						for(int t=0; t<numDecisions; t++) {
							expectedInstantaneousCost[k][t] = prob * mdpSol.getExpectedInstantaneousCost(k, t);
						}
					}
				}
			}
			
			solution[i] = new MDPPolicySet(sol, probs, expectedReward, expectedInstantaneousCost, expectedTotalCost, rnd);
		}
		
		return solution;
	}

	@Override
	public String getName() {
		return "ColGen";
	}

	@Override
	public void modifyBudgetConstraints(double[] newLimits) {
		assert useBudgetConstraints;
		
		if(!useBudgetConstraints) {
			throw new RuntimeException("Can't change limits because instance does not have budget constraints");
		}
		
		for(int k=0; k<numDomainResources; k++) {
			try {
				model.changeConstraintRHS(costConstraints[k][0], newLimits[k]);
			} catch (LPException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void modifyInstantaneousConstraints(double[][] newLimits) {
		assert !useBudgetConstraints;
		
		if(useBudgetConstraints) {
			throw new RuntimeException("Can't change limits because instance does not have instantaneous constraints");
		}
		
		for(int k=0; k<numDomainResources; k++) {
			for(int t=0; t<numDecisions; t++) {
				try {
					model.changeConstraintRHS(costConstraints[k][t], newLimits[k][t]);
				} catch (LPException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
