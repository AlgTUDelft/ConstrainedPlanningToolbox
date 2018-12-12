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

import java.lang.reflect.Method;

import lp.LPException;
import lp.LPExpression;
import lp.LPVariable;

public class HandleGRBLinExpr implements LPExpression {

	private final Object theGRBLinExpr;

	private Method addTerm;

	private final int modelID;

	public HandleGRBLinExpr(Class<?> grbLinExprClass,
							Class<?> grbVarClass,
							int modelID) {

		// Store model ID.
		this.modelID = modelID;

		try {
			
			// Call the empty constructor.
			theGRBLinExpr = grbLinExprClass.getConstructor().newInstance();
	
			// Extract the required method.
			addTerm = grbLinExprClass.getMethod("addTerm", double.class, grbVarClass);
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBLinExpr.", ex);
		}
	}

	@Override
	public int getModelID() {

		return modelID;
	}

	public Object getExpression() {
		return theGRBLinExpr;
	}

	@Override
	public void addTerm(double coefficient, LPVariable var) throws LPException {

		if (var instanceof HandleGRBVar) {
			internalAddTerm(coefficient, (HandleGRBVar) var);
		} else {
			throw new IllegalArgumentException("Unexpected LPVariable belonging to a different model type " + var.getClass().getName());
		}
	}

	private void internalAddTerm(double coefficient, HandleGRBVar var) {
		try {
			addTerm.invoke(theGRBLinExpr, coefficient, var.getGRBVar());
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBLinExpr.", ex);
		}		
	}
}
