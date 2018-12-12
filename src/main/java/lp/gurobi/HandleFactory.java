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

import java.util.concurrent.atomic.AtomicInteger;

import lp.LPConstraintType;
import lp.LPException;
import lp.LPExpression;
import lp.LPVariable;
import lp.LPVariableType;
import util.DynamicLinker;

public class HandleFactory {

	// Unique ID of the model.
	private static final AtomicInteger modelCounter = new AtomicInteger(0);

	// Singleton of the factory.
	private static HandleFactory singletonFactory;

	// All the loaded functional classes.
	private final Class<?> grbEnvClass;
	private final Class<?> grbModelClass;
	private final Class<?> grbVarClass;
	private final Class<?> grbExprClass;
	private final Class<?> grbLinExprClass;
	private final Class<?> grbConstrClass;
	private final Class<?> grbColumnClass;

	// All the loaded constants classes.
	private final Class<?> grbDataClass;
	private final Class<?> grbStatusClass;
	private final Class<?> intAttrClass;
	private final Class<?> intParamClass;
	private final Class<?> doubleAttrClass;
	private final Class<?> doubleParamClass;

	private HandleFactory(ClassLoader loader) {

		try {

			// Load and store all the used (sub-)classes.
			grbEnvClass		= Class.forName("gurobi.GRBEnv", true, loader);
			grbModelClass	= Class.forName("gurobi.GRBModel", true, loader);
			grbVarClass		= Class.forName("gurobi.GRBVar", true, loader);
			grbExprClass	= Class.forName("gurobi.GRBExpr", true, loader);
			grbLinExprClass	= Class.forName("gurobi.GRBLinExpr", true, loader);
			grbConstrClass	= Class.forName("gurobi.GRBConstr", true, loader);
			grbColumnClass	= Class.forName("gurobi.GRBColumn", true, loader);

			grbDataClass	 = Class.forName("gurobi.GRB", true, loader);
			grbStatusClass	 = Class.forName("gurobi.GRB$Status", true, loader);
			intAttrClass	 = Class.forName("gurobi.GRB$IntAttr", true, loader);
			intParamClass	 = Class.forName("gurobi.GRB$IntParam", true, loader);
			doubleAttrClass	 = Class.forName("gurobi.GRB$DoubleAttr", true, loader);
			doubleParamClass = Class.forName("gurobi.GRB$DoubleParam", true, loader);

		} catch (Exception ex) {
			throw new RuntimeException("Reflection to Gurobi failed, JAR interface unsupported.", ex);
		}
	}

	public HandleGRBEnv newGRBEnvironment() {

		// Construct new environment handle.
		return new HandleGRBEnv(grbEnvClass, doubleParamClass, intParamClass);
	}

	public HandleGRBModel newGRBModel(HandleGRBEnv environment) {

		// Atomically get and increment.
		int modelID = modelCounter.getAndIncrement();

		// Construct new model handle.
		return new HandleGRBModel(grbModelClass,
								  grbEnvClass,
								  grbExprClass,
								  grbLinExprClass,
								  grbColumnClass,
								  grbDataClass,
								  grbStatusClass,
								  intAttrClass,
								  doubleAttrClass,
								  environment,
								  modelID);
	}

	public HandleGRBLinExpr newGRBLinExpr(int modelID) {

		// Construct new expression handle.
		return new HandleGRBLinExpr(grbLinExprClass,
									grbVarClass,
									modelID);
	}

	public HandleGRBColumn newGRBColumn(int modelID) {

		// Construct new column handle.
		return new HandleGRBColumn(grbColumnClass,
								   grbConstrClass,
								   modelID);
	}

	public HandleGRBVar wrapGRBVar(Object theGRBVar, int modelID, int varID) {

		// Construct new variable handle.
		return new HandleGRBVar(theGRBVar,
								grbVarClass,
								doubleAttrClass,
								modelID,
								varID);
	}

	public HandleGRBConstr wrapGRBConstr(Object theGRBConstr, int modelID, int constrID) {

		// Construct new constraint handle.
		return new HandleGRBConstr(theGRBConstr,
									grbConstrClass,
									doubleAttrClass,
									modelID,
									constrID);
	}

	public static HandleFactory getFactory() {

		if (singletonFactory == null) {

			// Obtain the Gurobi class loader.
			ClassLoader loader = DynamicLinker.getGurobiClassLoader();

			// Create class loader.
			singletonFactory = new HandleFactory(loader);
		}

		return singletonFactory;
	}

	public static void main(String[] args) throws LPException{

		HandleFactory factory = getFactory();

		HandleGRBEnv environment = factory.newGRBEnvironment();
		environment.setMIPgap(0.0001);
		environment.setOutputFlag(0);

		HandleGRBModel model = factory.newGRBModel(environment);

		LPVariable x1 = model.addVariable(0, 3, 3, LPVariableType.CONTINUOUS);
		LPVariable x2 = model.addVariable(0, 5, 1, LPVariableType.CONTINUOUS);

		LPExpression ex1 = model.createExpression();

		ex1.addTerm(5, x1);
		ex1.addTerm(0.1, x2);

		model.addConstraint(ex1, LPConstraintType.LESS_EQUAL, 2);

		model.solve();

		System.out.println("Objective value of solution is " + model.getObjectiveValue());

		model.dispose();
	}
}
