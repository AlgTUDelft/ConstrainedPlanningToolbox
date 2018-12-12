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

import lp.LPException;
import lp.LPExpression;
import lp.LPVariable;

public class LPExpressionLPSolve implements LPExpression {

	private LPModelLPSolve model;
	private double[] expr;
	
	private final double coefficientTolerance = 0.000000001;
	
	@SuppressWarnings("unused")
	private LPExpressionLPSolve() {
		
	}
	
	public LPExpressionLPSolve(LPModelLPSolve model) {
		this.model = model;
		this.expr = new double[model.getNumVars()+1]; // lpsolve counts from 1 so first entry remains empty
	}
	
	@Override
	public void addTerm(double coefficient, LPVariable var) throws LPException {
		if(var.getModelID() != model.getModelID()) {
			throw new LPException("Variable does not belong to this model");
		}
		
		if(Math.abs(coefficient) > coefficientTolerance) {
			int id = var.getID();
			expr[id] = coefficient;
		}
	}

	@Override
	public int getModelID() {
		return model.getModelID();
	}
	
	protected double[] getExpression() {
		return expr;
	}
}
