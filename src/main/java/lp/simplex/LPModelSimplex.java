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
package lp.simplex;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPConstraintType;
import lp.LPException;
import lp.LPExpression;
import lp.LPModel;
import lp.LPVariable;
import lp.LPVariableType;

public class LPModelSimplex implements LPModel {
	private static int numModels = 0;
	private int modelID = -1;
	
	private int numVariables = 0;
	private int numConstraints = 0;
	
	// variable data
	private ArrayList<Double> varLowerBounds = new ArrayList<Double>();
	private ArrayList<Double> varUpperBounds = new ArrayList<Double>();
	private RealVector varObjectiveCoefficients = new ArrayRealVector();
	
	// constraint data
	private ArrayList<RealVector> constraintLHS = new ArrayList<RealVector>();
	private ArrayList<Relationship> constraintType = new ArrayList<Relationship>();
	private ArrayList<Double> constraintRHS = new ArrayList<Double>();
	
	// last solution found
	private PointValuePair solution;
	private double[] inequalityDuals;
	
	public static LPModelSimplex createModel() {
		LPModelSimplex model = new LPModelSimplex(numModels);
		numModels++;
		return model;
	}
	
	private LPModelSimplex(int modelID) {
		this.modelID = modelID;
		this.numVariables = 0;
		this.numConstraints = 0;
	}
	
	@Override
	public int getModelID() {
		return modelID;
	}

	@Override
	public LPExpression createExpression() {
		return new LPExpressionSimplex(this);
	}

	@Override
	public LPColumn createColumn() {
		return new LPColumnSimplex(this);
	}
	
	private Relationship getConstraintType(LPConstraintType type) {
		switch(type) {
			case LESS_EQUAL:
				return Relationship.LEQ;
			case EQUAL:
				return Relationship.EQ;
			case GREATER_EQUAL:
				return Relationship.GEQ;
			default:
				return Relationship.LEQ;
		}
	}

	@Override
	public LPVariable addVariable(double lowerbound, double upperbound, double obj, LPVariableType type) throws LPException {
		if(type == LPVariableType.INTEGER) {
			throw new LPException("Integer variables not supported by built-in LP solver");
		}
		
		LPVariable lpVar = new LPVariable(numVariables, modelID);
		
		varLowerBounds.add(lowerbound);
		varUpperBounds.add(upperbound);
		varObjectiveCoefficients = varObjectiveCoefficients.append(obj);
		numVariables++;
		
		return lpVar;
	}

	@Override
	public LPConstraint addConstraint(LPExpression expression, LPConstraintType type, double rhs) throws LPException {		
		LPConstraint lpConstr = null;
		
		if(expression instanceof LPExpressionSimplex) {
			LPExpressionSimplex simplexExpression = (LPExpressionSimplex) expression;
			RealVector expr = simplexExpression.getExpression();
			
			if(expr.getDimension() != numVariables) {
				throw new LPException("Length of the expression does not match number of variables");
			}
			
			int id = constraintLHS.size();
			constraintLHS.add(expr);
			constraintType.add(getConstraintType(type));
			constraintRHS.add(rhs);
			lpConstr = new LPConstraint(id, modelID);
			numConstraints++;
		}
		
		return lpConstr;
	}

	@Override
	public LPVariable addColumn(double lowerbound, double upperbound, double obj, LPVariableType type, LPColumn column) throws LPException {
		if(type == LPVariableType.INTEGER) {
			throw new LPException("Integer variables not supported by built-in LP solver");
		}
		
		LPVariable lpVar = null;
		
		if(column instanceof LPColumnSimplex) {
			LPColumnSimplex col = (LPColumnSimplex) column;
			double[] colEntries = col.getCol();
			assert colEntries.length == numConstraints;
			
			for(int i=0; i<colEntries.length; i++) {
				RealVector oldCoefficients = constraintLHS.get(i);
				RealVector newCoefficients = oldCoefficients.append(colEntries[i]);
				constraintLHS.set(i, newCoefficients);
			}
			
			lpVar = addVariable(lowerbound, upperbound, obj, type);
		}
		
		return lpVar;
	}

	@Override
	public void changeConstraintRHS(LPConstraint constraint, double rhs) throws LPException {
		if(constraint.getModelID() != modelID) {
			throw new LPException("Expression has not been created for this model");
		}
		
		constraintRHS.set(constraint.getID(), rhs);
	}

	@Override
	public boolean solve() {
		// construct objective function
		LinearObjectiveFunction f = new LinearObjectiveFunction(varObjectiveCoefficients, 0);
		
		// construct constraint collection
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		for(int i=0; i<numConstraints; i++) {			
			LinearConstraint constr = new LinearConstraint(constraints.size(), constraintLHS.get(i), constraintType.get(i), constraintRHS.get(i));
			constraints.add(constr);
		}
		
		// add additional constraints for variable range
		for(int i=0; i<numVariables; i++) {
			double lower = varLowerBounds.get(i);
			double upper = varUpperBounds.get(i);
			
			RealVector expr = new ArrayRealVector();
			for(int j=0; j<numVariables; j++) {
				expr = expr.append(0.0);
			}
			expr.setEntry(i, 1.0);
			
			LinearConstraint constr = new LinearConstraint(constraints.size(), expr, Relationship.LEQ, upper);
			constraints.add(constr);
			
			constr = new LinearConstraint(constraints.size(), expr, Relationship.GEQ, lower);
			constraints.add(constr);
		}
		
		// solve the problem using simplex
		SimplexSolver solver = new SimplexSolver();
		LinearConstraintSet lcs = new LinearConstraintSet(constraints);
		solution = solver.optimize(f, lcs, GoalType.MAXIMIZE, new NonNegativeConstraint(false));
		inequalityDuals = solver.getInequalityConstraintDuals();
		
		return (solution != null);
	}

	@Override
	public double getObjectiveValue() {
		return solution.getValue();
	}

	@Override
	public double getVariableValue(LPVariable var) {
		int varID = var.getID();
		return solution.getPoint()[varID];
	}

	@Override
	public double getDualPrice(LPConstraint constr) {
		int constraintID = constr.getID();
		
		if(constraintType.get(constraintID) != Relationship.EQ) {
			int inequalityIndex = 0;
			
			for(int i=0; i<constraintID; i++) {
				if(constraintType.get(i) != Relationship.EQ) {
					inequalityIndex++;
				}
			}
			
			return inequalityDuals[inequalityIndex];
		}
		else {
			// we cannot derive dual price from the slack in this case, so we return the invalid dual price 0
			return 0.0;
		}
	}

	@Override
	public double getInfinite() {
		return 100000000.0;
	}

	@Override
	public void dispose() {
		varLowerBounds = null;
		varUpperBounds = null;
		varObjectiveCoefficients = null;
		constraintLHS = null;
		constraintType = null;
		constraintRHS = null;
		solution = null;
		inequalityDuals = null;
	}

	public int getNumVars() {
		return numVariables;
	}
	
	public int getNumConstraints() {
		return numConstraints;
	}
}
