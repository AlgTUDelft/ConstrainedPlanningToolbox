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

public class Blueprint
{
	private final int ID;

	private double resistance;
	private double capacitance;
	private double power;

	public Blueprint(int myID, double R, double C, double P)
	{
		ID = myID;

		setResistance(R);
		setCapacitance(C);
		setPower(P);
	}

	private void setResistance(double R)
	{
		resistance = R;
	}

	private void setCapacitance(double C)
	{
		capacitance = C;
	}

	private void setPower(double P)
	{
		power = P;
	}

	public int getID()
	{
		return ID;
	}

	public double getResistance()
	{
		return resistance;
	}

	public double getCapacitance()
	{
		return capacitance;
	}

	public double getPower()
	{
		return power;
	}

	@Override
	public String toString() {
		return String.format("%2d (R %7.3f, C %7.3f, P %7.3f)", getID(), getResistance(), getCapacitance(), getPower());
	}
}
