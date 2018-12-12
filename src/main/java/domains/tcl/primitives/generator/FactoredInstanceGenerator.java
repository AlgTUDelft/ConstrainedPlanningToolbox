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
package domains.tcl.primitives.generator;

import java.util.List;
import java.util.Random;


import domains.tcl.primitives.Blueprint;
import domains.tcl.primitives.NeighborhoodFactory;
import domains.tcl.primitives.generator.agent.AgentGenerator;
import domains.tcl.primitives.generator.limit.LimitGenerator;

public class FactoredInstanceGenerator implements InstanceGenerator
{
	private Random randGen;
	private Random aRndGen;
	private Random lRndGen;

	private AgentGenerator agentGen;

	private LimitGenerator limitGen;

	public FactoredInstanceGenerator(AgentGenerator aGen, LimitGenerator lGen)
	{
		randGen = new Random();
		aRndGen = new Random();
		lRndGen = new Random();

		agentGen = aGen;
		limitGen = lGen;

		agentGen.setRandomGen(aRndGen);
		limitGen.setRandomGen(lRndGen);
	}

	public void setSeed(long seed)
	{
		randGen.setSeed(seed);
		aRndGen.setSeed(randGen.nextLong());
		lRndGen.setSeed(randGen.nextLong());
	}

	public NeighborhoodFactory generateExtendableFactory(int numAgents, int horizon)
	{
		// The neighborhood.
		NeighborhoodFactory factory = new NeighborhoodFactory();

		// Environment variables.
		factory.setOutsideTemperature(0);
		factory.setSetpoint(20);

		generateAndAddAgents(factory, numAgents);
		generateAndAddLimit(factory, numAgents, horizon);

		return factory;
	}

	protected void generateAndAddAgents(NeighborhoodFactory factory, int numAgents)
	{
		List<Blueprint> blueprints = agentGen.generateAgents(numAgents);

		for (int i = 0; i < numAgents; i++)
		{
			Blueprint blueprint = blueprints.get(i);

			double R = blueprint.getResistance();
			double C = blueprint.getCapacitance();
			double P = blueprint.getPower();

			factory.addTCL(R, C, P);
		}
	}

	protected void generateAndAddLimit(NeighborhoodFactory factory, int numAgents, int horizon)
	{
		List<Integer> limits = limitGen.generateLimits(numAgents, horizon);

		for (int time = 0; time < horizon; time++)
		{
			int limit = limits.get(time);

			factory.addLimit(time*60, limit);
		}
	}

	public String getName()
	{
		return (agentGen.getName() + "-" + limitGen.getName());
	}

}
