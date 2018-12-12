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
package lp;


public interface LPModel {
	public int getModelID();
	
	public LPExpression createExpression();
	public LPColumn createColumn();
	
	public LPVariable addVariable(double lowerbound, double upperbound, double obj, LPVariableType type) throws LPException;
	public LPConstraint addConstraint(LPExpression expression, LPConstraintType type, double rhs) throws LPException;
	public LPVariable addColumn(double lowerbound, double upperbound, double obj, LPVariableType type, LPColumn column) throws LPException;
	public void changeConstraintRHS(LPConstraint constraint, double rhs) throws LPException;
	
	public boolean solve();
	
	public double getObjectiveValue();
	public double getVariableValue(LPVariable var);
	public double getDualPrice(LPConstraint constr);
	
	public double getInfinite();
	
	public void dispose();
}
