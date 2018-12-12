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
package lp.gurobi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

public class HandleGRBModel implements LPModel {

	private static final double BIG_M = 100000000D;

	// Reflection fields.
	private final Object theGRBModel;

	private final Field maximize;
	private final Field lessEqual;
	private final Field equal;
	private final Field greaterEqual;
	private final Field continuous;
	private final Field integer;
	private final Field optimal;
	private final Field status;
	private final Field objVal;

	private final Method getObjective;
	private final Method setObjective;
	private final Method addVar;
	private final Method addColumn;
	private final Method addConstr;
	private final Method getIntAttr;
	private final Method getDoubleAttr;
	private final Method update;
	private final Method optimize;
	private final Method dispose;

	// Logic fields.
	private final int modelID;
	private final List<HandleGRBVar> variables;
	private final List<HandleGRBConstr> constraints;

	protected HandleGRBModel(Class<?> grbModelClass,
							 Class<?> grbEnvClass,
							 Class<?> grbExprClass,
							 Class<?> grbLinExprClass,
							 Class<?> grbColumnClass,
							 Class<?> grbClass,
							 Class<?> grbStatusClass,
							 Class<?> intAttrClass,
							 Class<?> doubleAttrClass,
							 HandleGRBEnv grbEnvHandle,
							 int modelID) {

		// Store model ID.
		this.modelID = modelID;

		// Create lists.
		this.variables = new ArrayList<>();
		this.constraints = new ArrayList<>();

		try {
			
			// Call the constructor with environment.
			theGRBModel = grbModelClass.getConstructor(grbEnvClass).newInstance(grbEnvHandle.getEnvironment());
	
			// Extract the required fields.
			maximize	 = grbClass.getField("MAXIMIZE");
			continuous	 = grbClass.getField("CONTINUOUS");
			integer		 = grbClass.getField("INTEGER");
			lessEqual	 = grbClass.getField("LESS_EQUAL");
			equal		 = grbClass.getField("EQUAL");
			greaterEqual = grbClass.getField("GREATER_EQUAL");
			optimal		 = grbStatusClass.getField("OPTIMAL");
			status		 = intAttrClass.getField("Status");
			objVal		 = doubleAttrClass.getField("ObjVal");

			// Extract the required methods.
			getObjective  = grbModelClass.getMethod("getObjective");
			setObjective  = grbModelClass.getMethod("setObjective", grbExprClass, int.class);
			addVar		  = grbModelClass.getMethod("addVar", double.class, double.class, double.class, char.class, String.class);
			addColumn	  = grbModelClass.getMethod("addVar", double.class, double.class, double.class, char.class, grbColumnClass, String.class);
			addConstr	  = grbModelClass.getMethod("addConstr", grbLinExprClass, char.class, double.class, String.class);
			getIntAttr	  = grbModelClass.getMethod("get", intAttrClass);
			getDoubleAttr = grbModelClass.getMethod("get", doubleAttrClass);
			update		  = grbModelClass.getMethod("update");
			optimize	  = grbModelClass.getMethod("optimize");
			dispose		  = grbModelClass.getMethod("dispose");
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}

		// Ensure the model is maximizing.
		setObjectiveMaximizing();
	}

	private void setObjectiveMaximizing() {

		try {

			setObjective.invoke(theGRBModel, 
								getObjective.invoke(theGRBModel),
								maximize.get(null));

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}
	}

	@Override
	public int getModelID() {
		return modelID;
	}

	@Override
	public LPVariable addVariable(double lowerbound, double upperbound, double obj, LPVariableType lpType) {

		HandleGRBVar grbVarHandle = null;

		// Map the type to GRB type.
		char grbType = convertVariableType(lpType);

		try {

			// Create the actual GRB variable.
			Object theGRBVar = addVar.invoke(theGRBModel, lowerbound, upperbound, obj, grbType, "");

			// Wrap the variable in a handle.
			grbVarHandle = HandleFactory.getFactory().wrapGRBVar(theGRBVar, modelID, variables.size());

			// Store the variable.
			variables.add(grbVarHandle);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}

		return grbVarHandle;
	}

	private char convertVariableType(LPVariableType lpType) {

		char grbType;

		try {
			if (lpType == LPVariableType.CONTINUOUS) {
				grbType = continuous.getChar(null);
			} else {
				grbType = integer.getChar(null);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}

		return grbType;
	}

	@Override
	public LPExpression createExpression() {
		return HandleFactory.getFactory().newGRBLinExpr(getModelID());
	}

	@Override
	public LPColumn createColumn() {
		return HandleFactory.getFactory().newGRBColumn(getModelID());
	}

	@Override
	public LPConstraint addConstraint(LPExpression expression, LPConstraintType type, double rhs) throws LPException {
		if(expression.getModelID() != this.getModelID()) {
			throw new LPException("Expression has not been created for this model");
		}

		HandleGRBConstr grbConstrHandle = null;

		// Determine constraint nature.
		char grbType = convertConstraintType(type);

		if(expression instanceof HandleGRBLinExpr) {

			// Unbox the expected GRBLinExpr.
			HandleGRBLinExpr gurobiExpression = (HandleGRBLinExpr) expression;
			Object theGRBLinExpr = gurobiExpression.getExpression();

			// Update model.
			update();

			try {
				
				// Create and add the actual constraint.
				Object theGRBConstr = addConstr.invoke(theGRBModel, theGRBLinExpr, grbType, rhs, "");

				// Wrap the constraint in a handle.
				grbConstrHandle = HandleFactory.getFactory().wrapGRBConstr(theGRBConstr, modelID, constraints.size());

				// Store the constraint.
				constraints.add(grbConstrHandle);

			} catch (Exception ex) {
				throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
			}
		}
		
		return grbConstrHandle;
	}
	
	private char convertConstraintType(LPConstraintType lpType) {
		char grbType;

		try {
			switch(lpType) {
				case LESS_EQUAL:
					grbType = lessEqual.getChar(null); break;
				case EQUAL:
					grbType = equal.getChar(null); break;
				case GREATER_EQUAL:
					grbType = greaterEqual.getChar(null); break;
				default:
					grbType = lessEqual.getChar(null); break;
			}
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}

		return grbType;
	}

	@Override
	public LPVariable addColumn(double lowerbound, double upperbound, double obj, LPVariableType lpType, LPColumn column)
			throws LPException {

		HandleGRBVar grbVarHandle = null;

		// Map the type to GRB type.
		char grbType = convertVariableType(lpType);

		if(column instanceof HandleGRBColumn) {
			Object theGRBColumn = ((HandleGRBColumn) column).getColumn();

			// Update the model.
			update();

			try {

				// Create the actual GRB variable.
				Object theGRBVar = addColumn.invoke(theGRBModel, lowerbound, upperbound, obj, grbType, theGRBColumn, "");
	
				// Wrap the variable in a handle.
				grbVarHandle = HandleFactory.getFactory().wrapGRBVar(theGRBVar, modelID, variables.size());
	
				// Store the variable.
				variables.add(grbVarHandle);
	
			} catch (Exception ex) {
				throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
			}
		}

		return grbVarHandle;
	}

	@Override
	public void changeConstraintRHS(LPConstraint constraint, double rhs) throws LPException {

		if(constraint.getModelID() != this.getModelID()) {
			throw new LPException("Expression has not been created for this model");
		}

		if (constraint instanceof HandleGRBConstr) {

			// Update model.
			update();

			// Update constraint right-hand-side.
			((HandleGRBConstr) constraint).setRHS(rhs);
		}
	}

	@Override
	public boolean solve() {

		boolean solved = false;
		
		try {

			// Call the optimize function.
			optimize.invoke(theGRBModel);

			// Determine the model status.
			int solverStatus = (int) getIntAttr.invoke(theGRBModel, status.get(null));

			// Test for success.
			solved = (solverStatus == optimal.getInt(null));

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}
		
		return solved;
	}

	@Override
	public double getObjectiveValue() {

		double objective = Double.NEGATIVE_INFINITY;
		
		try {

			// Determine solution objective.
			objective = (double) getDoubleAttr.invoke(theGRBModel, objVal.get(null));

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}
		
		return objective;
	}

	@Override
	public double getVariableValue(LPVariable var) {

		double decisionValue = 0.0;

		// If this variable is the correct type,
		if (var instanceof HandleGRBVar) {
			// Extract the value from the variable.
			decisionValue = ((HandleGRBVar) var).getX();
		} else {
			throw new RuntimeException("Variable not from Gurobi model, but " + var.getClass().getCanonicalName());
		}

		return decisionValue;
	}

	@Override
	public double getDualPrice(LPConstraint constr) {

		double dualLambda = 0.0;

		// If this constraint is the correct type,
		if (constr instanceof HandleGRBConstr) {
			// Extract the value from the constraint.
			dualLambda = ((HandleGRBConstr) constr).getPi();
		} else {
			throw new RuntimeException("Constraint not from Gurobi model, but " + constr.getClass().getCanonicalName());
		}

		return dualLambda;
	}

	private void update() {
		try {
			// Update model.
			update.invoke(theGRBModel);
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}
	}

	@Override
	public void dispose() {

		try {
			// Call dispose on the model.
			dispose.invoke(theGRBModel);
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBModel.", ex);
		}
	}

	@Override
	public double getInfinite() {
		return BIG_M;
	}
}
