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

import lp.LPModel;
import lp.LPSolver;

public class LPSolverGurobi implements LPSolver {

	private static final double MIP_GAP = 00001;
	private static final int OUTPUT_FLAG = 0;

	private static HandleGRBEnv environment;

	public LPSolverGurobi() {

		if (environment == null) {
			environment = HandleFactory.getFactory().newGRBEnvironment();
			environment.setMIPgap(MIP_GAP);
			environment.setOutputFlag(OUTPUT_FLAG);
		}
	}

	@Override
	public LPModel createModel() {
		return HandleFactory.getFactory().newGRBModel(environment);
	}

	@Override
	public boolean supportsMILP() {
		return true;
	}
}
