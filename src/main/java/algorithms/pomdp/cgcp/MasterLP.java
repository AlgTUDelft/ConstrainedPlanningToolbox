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
package algorithms.pomdp.cgcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.ConsoleOutput;

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPException;
import lp.LPExpression;
import lp.LPModel;
import lp.LPSolver;
import lp.LPVariable;
import lp.LPVariableType;
import lp.LPConstraintType;


public class MasterLP {
	private LPSolver lpSolver;
	
	private LPModel model;
	private LPConstraint[][] costConstraints;
	private LPConstraint[] weightConstraints;
	private double[][] initialCostLimits;
	private int nAgents;
	private int K;
	private int T;
	
	// properties of the first column
	double[] initialExpectedReward = null;
	List<double[][]> initialExpectedCost = null;
	
	// for each agent: list of vars corresponding to policies
	private List<ArrayList<LPVariable>> vars = new ArrayList<ArrayList<LPVariable>>();
	
	// for each agent: list of expected cost corresponding to policies
	private List<ArrayList<double[][]>> costCoefficients = new ArrayList<ArrayList<double[][]>>();
	
	// values set after solving
	private List<double[]> policyDistributions;
	private double[][] lambda;
	private double expectedReward;
	private double[][] expectedCost;
	
	public MasterLP(LPSolver lpSolver, int nAgents, double[][] costLimits) {
		this.initialCostLimits = costLimits;
		this.nAgents = nAgents;
		this.weightConstraints = new LPConstraint[nAgents];
		this.K = costLimits.length;
		this.T = costLimits[0].length;
		this.lpSolver = lpSolver;
		initModel();
	}
	
	public MasterLP(LPSolver lpSolver, int nAgents, double[][] costLimits, double[] initialExpectedReward, List<double[][]> initialExpectedCost) {
		this.initialCostLimits = costLimits;
		this.nAgents = nAgents;
		this.weightConstraints = new LPConstraint[nAgents];
		this.K = costLimits.length;
		this.T = costLimits[0].length;
		this.initialExpectedReward = initialExpectedReward;
		this.initialExpectedCost = initialExpectedCost;
		this.lpSolver = lpSolver;
		initModel();
	}
	
	private void initModel() {
		try {
			model = lpSolver.createModel();
			
			// create variable of the first policy for each agent
			for(int i=0; i<nAgents; i++) {
				ArrayList<LPVariable> agentVars = new ArrayList<LPVariable>();
				ArrayList<double[][]> agentCostCoefficients = new ArrayList<double[][]>();
				
				if(initialExpectedReward != null && initialExpectedCost != null) {
					LPVariable newVar = model.addVariable(0.0, 1.0, initialExpectedReward[i], LPVariableType.CONTINUOUS);
					agentVars.add(newVar);
					agentCostCoefficients.add(initialExpectedCost.get(i));
				}
				else {
					LPVariable newVar = model.addVariable(0.0, 1.0, -1.0 * model.getInfinite() + 1.0, LPVariableType.CONTINUOUS);
					agentVars.add(newVar);
					agentCostCoefficients.add(new double[K][T]);
				}
				
				vars.add(agentVars);
				costCoefficients.add(agentCostCoefficients);
			}
			
			
			// add expected cost constraints
			costConstraints = new LPConstraint[K][T];
			for(int k=0; k<K; k++) {
				for(int t=0; t<T; t++) {
					LPExpression expr = model.createExpression();
					for(int i=0; i<nAgents; i++) {
						double cost = (initialExpectedCost == null) ? 0.0 : initialExpectedCost.get(i)[k][t];
						expr.addTerm(cost, vars.get(i).get(0));
					}
					costConstraints[k][t] = model.addConstraint(expr, LPConstraintType.LESS_EQUAL, initialCostLimits[k][t]);
				}
			}
			
			// add constraint to ensure that sum of weights equals 1 for each agent
			for(int i=0; i<nAgents; i++) {
				LPExpression expr = model.createExpression();
				expr.addTerm(1.0, vars.get(i).get(0));
				weightConstraints[i] = model.addConstraint(expr, LPConstraintType.EQUAL, 1.0);
			}
			
		} catch (LPException e) {
			e.printStackTrace();
		}
	}
	
	public void solve() {
		boolean solved = model.solve();
		assert solved;
		
		// retrieve probability distribution over policies
		policyDistributions = new ArrayList<double[]>(nAgents);
		for(int i=0; i<nAgents; i++) {
			double[] agentDistribution = new double[vars.get(i).size()];
			for(int j=0; j<vars.get(i).size(); j++) {
				agentDistribution[j] = model.getVariableValue(vars.get(i).get(j));
			}
			ConsoleOutput.println("Probability distribution: "+Arrays.toString(agentDistribution));
			policyDistributions.add(agentDistribution);
		}
		
		// retrieve dual prices
		lambda = new double[K][T];
		for(int k=0; k<K; k++) {
			for(int t=0; t<T; t++) {
				lambda[k][t] = model.getDualPrice(costConstraints[k][t]);
			}
		}
		
		// retrieve expected reward and cost
		expectedReward = model.getObjectiveValue();
		expectedCost = new double[K][T];
		for(int k=0; k<K; k++) {
			for(int t=0; t<T; t++) {
				for(int i=0; i<nAgents; i++) {
					double[] agentDistribution = policyDistributions.get(i);
					
					for(int j=0; j<vars.get(i).size(); j++) {
						expectedCost[k][t] += agentDistribution[j] * costCoefficients.get(i).get(j)[k][t];
					}
				}
			}
		}
	}
	
	public double[] getPolicyDistribution(int agent) {
		assert agent >=0 && agent < nAgents;
		return policyDistributions.get(agent);
	}
	
	public double[][] getLambdas() {
		return lambda;
	}
	
	public double getLambda(int k, int t) {
		assert t >= 0 && t<T && k>=0 && k<K;
		return lambda[k][t];
	}
	
	public double getExpectedReward() {
		return expectedReward;
	}
	
	public double getExpectedCost(int k, int t) {
		assert t >= 0 && t<T && k>=0 && k<K;
		return expectedCost[k][t];
	}
	
	public void addColumns(double[] expectedReward, List<double[][]> expectedCost) {
		assert expectedReward.length == nAgents && expectedCost.size() == nAgents;
		
		try {
			for(int agent=0; agent<nAgents; agent++) {
				assert expectedCost.get(agent).length == costConstraints.length;
				
				// add the column
				LPColumn newCol = model.createColumn();
				
				newCol.addTerm(1.0, weightConstraints[agent]);
				for(int k=0; k<K; k++) {
					for(int t=0; t<T; t++) {
						newCol.addTerm(expectedCost.get(agent)[k][t], costConstraints[k][t]);
					}
				}
				
				LPVariable newVar = model.addColumn(0.0, 1.0, expectedReward[agent], LPVariableType.CONTINUOUS, newCol);
				vars.get(agent).add(newVar);
				costCoefficients.get(agent).add(expectedCost.get(agent));
			}
			
		} catch (LPException ex) {
			throw new RuntimeException("Adding column to model failed.", ex);
		}
	}
	
}
