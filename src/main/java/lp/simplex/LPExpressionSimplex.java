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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import lp.LPException;
import lp.LPExpression;
import lp.LPVariable;

public class LPExpressionSimplex implements LPExpression {

	private LPModelSimplex model;
	private RealVector expr;
	
	private final double coefficientTolerance = 0.000000001;
	
	@SuppressWarnings("unused")
	private LPExpressionSimplex() {
		
	}
	
	public LPExpressionSimplex(LPModelSimplex model) {
		this.model = model;
		this.expr = new ArrayRealVector();
		
		for(int i=0; i<model.getNumVars(); i++) {
			this.expr = this.expr.append(0.0);
		}
	}
	
	@Override
	public void addTerm(double coefficient, LPVariable var) throws LPException {
		if(var.getModelID() != model.getModelID()) {
			throw new LPException("Variable does not belong to this model");
		}
		
		if(Math.abs(coefficient) > coefficientTolerance) {
			int id = var.getID();
			expr.setEntry(id, coefficient);
		}
	}

	@Override
	public int getModelID() {
		return model.getModelID();
	}
	
	protected RealVector getExpression() {
		return expr;
	}
}
