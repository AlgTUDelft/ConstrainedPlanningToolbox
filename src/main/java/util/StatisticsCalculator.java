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
package util;

import java.util.List;
import org.apache.commons.math3.distribution.NormalDistribution;

public class StatisticsCalculator {

	public static double getMean(List<Double> list) {
		double mean = 0.0;
		double numElements = (double) list.size();
		
		for(int i=0; i<list.size(); i++) {
			mean += list.get(i) / numElements;
		}
		
		return mean;
	}

	public static double getStd(List<Double> list) {		
		double mean = getMean(list);
		double std = 0.0;
		double numElements = (double) list.size();
		
		for(int i=0; i<list.size(); i++) {
			std += Math.pow(list.get(i) - mean, 2) * (1.0 / numElements);
			
		}
		
		return Math.sqrt(std);
	}

	public static double getViolationProbability(double mu, double std, double x) {
		NormalDistribution nd = new NormalDistribution(mu, std+0.000000001);
		return 1.0 - nd.cumulativeProbability(x);
	}

}
