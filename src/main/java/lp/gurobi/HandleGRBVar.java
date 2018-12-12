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

import lp.LPVariable;

public class HandleGRBVar extends LPVariable {

	private final Object theGRBVar;

	private Field fieldX;

	private Method getDoubleAttr;

	public HandleGRBVar(Object myGRBVar, 
						Class<?> grbVarClass,
						Class<?> doubleAttrClass,
						int modelID,
						int varID) {

		super(varID, modelID);

		try {
			
			// Store the object handle.
			theGRBVar = myGRBVar;
	
			// Extract the required fields.
			fieldX = doubleAttrClass.getField("X");
	
			// Extract the required methods.
			getDoubleAttr = grbVarClass.getMethod("get", doubleAttrClass);
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBVar.", ex);
		}
	}

	public Double getX() {

		try {

			Object doubleAttrX = fieldX.get(null);
	
			return getDoubleAttr(doubleAttrX);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBVar.", ex);
		}		
	}

	protected Object getGRBVar() {
		return theGRBVar;
	}

	private Double getDoubleAttr(Object attr) throws Exception {
		return (Double) getDoubleAttr.invoke(theGRBVar, attr);
	}
}
