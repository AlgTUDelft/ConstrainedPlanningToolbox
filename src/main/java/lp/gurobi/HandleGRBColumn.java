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

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPException;

public class HandleGRBColumn implements LPColumn {

	private final Object theGRBColumn;

	private Method addTerm;

	private final int modelID;

	public HandleGRBColumn(Class<?> grbColumnClass,
						   Class<?> grbConstrClass,
						   int modelID) {

		// Store model ID.
		this.modelID = modelID;

		try {
			
			// Call the empty constructor.
			theGRBColumn = grbColumnClass.getConstructor().newInstance();
	
			// Extract the required method.
			addTerm = grbColumnClass.getMethod("addTerm", double.class, grbConstrClass);
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBColumn.", ex);
		}
	}

	@Override
	public int getModelID() {
		return modelID;
	}

	protected Object getColumn() {
		return theGRBColumn;
	}

	@Override
	public void addTerm(double coefficient, LPConstraint constraint) throws LPException {

		if (constraint instanceof HandleGRBConstr) {
			internalAddTerm(coefficient, (HandleGRBConstr) constraint);
		} else {
			throw new IllegalArgumentException("Unexpected LPConstraint belonging to a different model type " + constraint.getClass().getName());
		}
	}

	private void internalAddTerm(Double coeff, HandleGRBConstr constr) {

		try {
			addTerm.invoke(theGRBColumn, coeff, constr.getGRBConstr());
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBColumn.", ex);
		}		
	}
}
