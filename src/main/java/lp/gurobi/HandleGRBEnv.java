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

public class HandleGRBEnv {

	private final Object theGRBEnv;

	private Field mipGap;
	private Field outputFlag;

	private Method setDoubleParam;
	private Method setIntParam;

	public HandleGRBEnv(Class<?> grbEnvClass,
						Class<?> doubleParamClass,
						Class<?> intParamClass) {

		try {
			
			// Call the empty constructor.
			theGRBEnv = grbEnvClass.getConstructor().newInstance();
	
			// Extract the required fields.
			mipGap = doubleParamClass.getField("MIPGap");
			outputFlag = intParamClass.getField("OutputFlag");

			// Extract the required methods.
			setDoubleParam = grbEnvClass.getMethod("set", doubleParamClass, double.class);
			setIntParam = grbEnvClass.getMethod("set", intParamClass, int.class);
			
		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBEnv.", ex);
		}
	}

	protected Object getEnvironment() {
		return theGRBEnv;
	}

	public void setMIPgap(double newGap) {

		try {

			Object doubleParamMIPgap = mipGap.get(null);
	
			setDoubleParam(doubleParamMIPgap, newGap);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBEnv.", ex);
		}
	}

	public void setOutputFlag(int newFlag) {

		try {

			Object intParamOutputFlag = outputFlag.get(null);
	
			setIntParam(intParamOutputFlag, newFlag);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection failed, interface error in GRBEnv.", ex);
		}		
	}

	private void setDoubleParam(Object param, Double newValue) throws Exception {
		setDoubleParam.invoke(theGRBEnv, param, newValue);
	}

	private void setIntParam(Object param, Integer newValue) throws Exception {
		setIntParam.invoke(theGRBEnv, param, newValue);
	}
}
