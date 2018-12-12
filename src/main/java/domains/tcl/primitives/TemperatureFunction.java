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
package domains.tcl.primitives;

public class TemperatureFunction
{
	public static double nextTemperature(double tIn, double tOut, int delta, Blueprint design, ControlAction action)
	{
		return TemperatureFunction.nextTemperature(tIn, tOut, delta,
												   design.getResistance(),
												   design.getCapacitance(),
												   design.getPower(),
												   action.getPowerFraction());
	}

	public static double nextTemperature(double tIn, double tOut, int delta, double R, double C, double P, double power)
	{
		double alpha = Math.exp((- delta / 3600.0) / (C * R));

		double tNext =	   alpha  *  tIn  +
						(1-alpha) * (tOut + power * R * P);

		return tNext;
	}
}
