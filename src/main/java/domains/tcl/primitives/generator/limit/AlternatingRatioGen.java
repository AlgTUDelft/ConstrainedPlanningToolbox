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
package domains.tcl.primitives.generator.limit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class AlternatingRatioGen implements LimitGenerator {

	private Random randGen;

	@Override
	public void setRandomGen(Random randGen) {
		this.randGen = randGen;
	}

	@Override
	public List<Integer> generateLimits(int numAgents, int horizon) {
		int max = getMaximumConsumption();

		final double lowMin   = 0.1;
		final double lowMean  = 0.25;
		final double lowMax   = 0.45;

		final double highMin  = 0.55;
		final double highMean = 0.75;
		final double highMax  = 0.9;

		final double variance = 0.125;

		List<Integer> limits = new ArrayList<Integer>();

		int time = 0;
		while (time < horizon) {

			int step =        8 + randGen.nextInt(3) * 2;
			int drop = step / 2 + randGen.nextInt(5) - 3;

			double low  = lowMean  + randGen.nextGaussian() * variance;
			double high = highMean + randGen.nextGaussian() * variance;

			low  = Math.max(lowMin, low);
			low  = Math.min(lowMax, low);
			high = Math.max(highMin, high);
			high = Math.min(highMax, high);

			for (int t = 0; (t < drop) && (time < horizon); t++)
			{
				limits.add((int) Math.round(high * max));
				time++;
			}

			for (int t = drop; (t < step) && (time < horizon); t++)
			{
				limits.add((int) Math.round(low * max));
				time++;
			}
		}

		return limits;
	}

	public abstract int getMaximumConsumption();
}
