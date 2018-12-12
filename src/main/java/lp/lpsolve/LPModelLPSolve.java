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
package lp.lpsolve;

import java.util.ArrayList;
import java.util.List;

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPConstraintType;
import lp.LPException;
import lp.LPExpression;
import lp.LPModel;
import lp.LPVariable;
import lp.LPVariableType;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class LPModelLPSolve implements LPModel {
	private static int numModels = 0;
	private int modelID = -1;
	
	private LpSolve model;
	
	private List<LPVariable> lpVariables = new ArrayList<LPVariable>();
	private List<LPConstraint> lpConstraints = new ArrayList<LPConstraint>();

	public static LPModelLPSolve createModel() {
		LPModelLPSolve model = new LPModelLPSolve(numModels);
		numModels++;
		return model;
	}
	
	private LPModelLPSolve(int modelID) {
		System.loadLibrary("lpsolve55");
		
		try {
			model = LpSolve.makeLp(0, 0);
			model.setVerbose(0);
			model.setMaxim();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		this.modelID = modelID;
	}
	
	@Override
	public int getModelID() {
		return modelID;
	}

	@Override
	public LPExpression createExpression() {
		return new LPExpressionLPSolve(this);
	}

	@Override
	public LPColumn createColumn() {
		return new LPColumnLPSolve(this);
	}

	@Override
	public LPVariable addVariable(double lowerbound, double upperbound, double obj, LPVariableType type) {
		LPVariable lpVar = null;
		
		try {
			int id = lpVariables.size()+1;
			model.addColumn(new double[lpConstraints.size()+1]);
			lpVar = new LPVariable(id, modelID);
			lpVariables.add(lpVar);
			model.setLowbo(id, lowerbound);
			model.setUpbo(id, upperbound);
			model.setObj(id, obj);
			boolean intVar = (type == LPVariableType.INTEGER);
			model.setInt(id, intVar);
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		return lpVar;
	}

	private int getConstraintType(LPConstraintType type) {
		switch(type) {
			case LESS_EQUAL:
				return LpSolve.LE;
			case EQUAL:
				return LpSolve.EQ;
			case GREATER_EQUAL:
				return LpSolve.GE;
			default:
				return LpSolve.LE;
		}
	}
	
	@Override
	public LPConstraint addConstraint(LPExpression expression, LPConstraintType type, double rhs) throws LPException {
		if(expression.getModelID() != modelID) {
			throw new LPException("Expression has not been created for this model");
		}
		
		LPConstraint lpConstr = null;
		
		if(expression instanceof LPExpressionLPSolve) {
			LPExpressionLPSolve lpSolveExpression = (LPExpressionLPSolve) expression;
			double[] expr = lpSolveExpression.getExpression();
			
			if(expr.length != lpVariables.size()+1) {
				throw new LPException("Length of the expression does not match number of variables");
			}
			
			try {
				model.addConstraint(expr, getConstraintType(type), rhs);
				int id = lpConstraints.size();
				lpConstr = new LPConstraint(id, modelID);
				lpConstraints.add(lpConstr);
			} catch (LpSolveException e) {
				e.printStackTrace();
			}
		}
		
		return lpConstr;
	}

	@Override
	public LPVariable addColumn(double lowerbound, double upperbound, double obj, LPVariableType type, LPColumn column) throws LPException {
		LPVariable lpVar = null;
		
		if(column instanceof LPColumnLPSolve) {
			try {
				LPColumnLPSolve col = (LPColumnLPSolve) column;
				
				int id = lpVariables.size()+1;
				model.addColumn(col.getCol());
				lpVar = new LPVariable(id, modelID);
				lpVariables.add(lpVar);
				model.setLowbo(id, lowerbound);
				model.setUpbo(id, upperbound);
				model.setObj(id, obj);
				boolean intVar = (type == LPVariableType.INTEGER);
				model.setInt(id, intVar);
			} catch (LpSolveException e) {
				e.printStackTrace();
			}
		}
		
		return lpVar;
	}
	
	@Override
	public void changeConstraintRHS(LPConstraint constraint, double rhs) throws LPException {
		int constraintID = constraint.getID();
		try {
			model.setRh(constraintID+1, rhs);  // +1 because constraint indices in LPsolve start at 1
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean solve() {
		int result = -1;
		
		try {			
			result = model.solve();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		return (result == 0);
	}

	@Override
	public double getObjectiveValue() {
		double objVal = 0.0;
		
		try {
			objVal = model.getObjective();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		return objVal;
	}

	@Override
	public double getVariableValue(LPVariable var) {
		double val = 0.0;
		
		try {
			// in the array first entry corresponds to first variable, so we subtract 1
			val = model.getPtrVariables()[var.getID()-1];
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		return val;
	}

	@Override
	public double getDualPrice(LPConstraint constr) {
		double val = 0.0;
		
		try {
			double[] dual = model.getPtrDualSolution();
			// our constraint IDs start at 0, and duals start from second entry, so we add 1
			val = dual[constr.getID()+1];
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
		
		return val;
	}

	@Override
	public void dispose() {
		model.deleteLp();
	}
	
	public int getNumVars() {
		return lpVariables.size();
	}
	
	public int getNumConstraints() {
		return lpConstraints.size();
	}

	@Override
	public double getInfinite() {
		return model.getInfinite();
	}
}
