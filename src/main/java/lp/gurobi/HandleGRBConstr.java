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

import lp.LPConstraint;

public class HandleGRBConstr extends LPConstraint {

	private final Object theGRBConstr;

	private Field fieldRHS;
	private Field fieldPi;

	private Method setDoubleAttr;
	private Method getDoubleAttr;

	public HandleGRBConstr(Object myGRBConstr,
						   Class<?> grbConstrClass,
						   Class<?> doubleAttrClass,
						   int modelID,
						   int constrID) {

		super(constrID, modelID);

		try {
			
			// Store the object handle.
			theGRBConstr = myGRBConstr;
	
			// Extract the required fields.
			fieldRHS = doubleAttrClass.getField("RHS");
			fieldPi = doubleAttrClass.getField("Pi");
	
			// Extract the required methods.
			setDoubleAttr = grbConstrClass.getMethod("set", doubleAttrClass, double.class);
			getDoubleAttr = grbConstrClass.getMethod("get", doubleAttrClass);
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBConstr.", ex);
		}
	}

	public void setRHS(double newRHS) {

		try {

			Object doubleAttrRHS = fieldRHS.get(null);
	
			setDoubleAttr(doubleAttrRHS, newRHS);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBConstr.", ex);
		}		
	}

	public Double getPi() {

		try {

			Object doubleAttrPi = fieldPi.get(null);
	
			return getDoubleAttr(doubleAttrPi);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBConstr.", ex);
		}
	}

	protected Object getGRBConstr() {
		return theGRBConstr;
	}

	private void setDoubleAttr(Object attr, Double newValue) throws Exception {
		setDoubleAttr.invoke(theGRBConstr, attr, newValue);
	}

	private Double getDoubleAttr(Object attr) throws Exception {
		return (Double) getDoubleAttr.invoke(theGRBConstr, attr);
	}
}
