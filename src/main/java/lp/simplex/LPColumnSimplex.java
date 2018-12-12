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

import lp.LPColumn;
import lp.LPConstraint;
import lp.LPException;

public class LPColumnSimplex implements LPColumn {
	private LPModelSimplex model;
	private double[] col;
	
	@SuppressWarnings("unused")
	private LPColumnSimplex() {
		
	}
	
	public LPColumnSimplex(LPModelSimplex model) {
		this.model = model;
		this.col = new double[model.getNumConstraints()];
	}
	
	@Override
	public void addTerm(double coefficient, LPConstraint constraint) throws LPException {
		if(constraint.getModelID() != model.getModelID()) {
			throw new LPException("Constraint does not belong to this model");
		}
		
		col[constraint.getID()] = coefficient;
	}

	@Override
	public int getModelID() {
		return model.getModelID();
	}

	protected double[] getCol() {
		return col;
	}
}
