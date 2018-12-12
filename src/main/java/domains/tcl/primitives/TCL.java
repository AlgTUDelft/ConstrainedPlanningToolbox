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

public class TCL
{
	// Construction parameters.
	private final Blueprint design;
	private final double fSetPoint;

	// Time-based parameters.
	private int fStepSize;
	private double fOutTemp;

	// Cached parameters.
	private double fLeakageA;
	private double fExternalOff;
	private double fExternalOn;

	public TCL(int ID, double R, double C, double P, double pSetPoint)
	{
		this(new Blueprint(ID, R, C, P), pSetPoint);
	}

	public TCL(Blueprint design, double setpoint)
	{
		this.design    = design;
		this.fSetPoint = setpoint;

		this.fStepSize = 1;
		this.fOutTemp  = 0;

		this.recomputeCached();
	}

	public int getID()
	{
		return design.getID();
	}

	public Blueprint getBlueprint()
	{
		return design;
	}

	public double getResistanceR()
	{
		return design.getResistance();
	}

	public double getCapacitanceC()
	{
		return design.getCapacitance();
	}

	public double getHeatingP()
	{
		return design.getPower();
	}

	public double getLeakageA()
	{
		return this.fLeakageA;
	}

	public double computeLeakageA(int pTimestep)
	{
		return Math.exp((- pTimestep / 3600.0) / (this.getCapacitanceC() * this.getResistanceR()));
	}

	public double getSetPoint()
	{
		return this.fSetPoint;
	}

	public void setStepSize(int pStepSize)
	{
		this.fStepSize = pStepSize;

		this.recomputeCached();
	}

	public void setOutsideTemperature(double pOutTemp)
	{
		this.fOutTemp = pOutTemp;

		this.recomputeCached();
	}

	private void recomputeCached()
	{
		this.fLeakageA	  = this.computeLeakageA(this.fStepSize);
		this.fExternalOff = (1-this.fLeakageA) * (this.fOutTemp);
		this.fExternalOn  = (1-this.fLeakageA) * (this.fOutTemp + (this.getResistanceR() * this.getHeatingP()));
	}

	public double computePenalty(double lTemperature)
	{
		double lError = Math.max(0, Math.abs(lTemperature - this.fSetPoint) - 0.5);

		return lError * lError;
	}

	public double computeNextTemperature(double pInTemperature, boolean pOn)
	{
		double lBase = this.fLeakageA * pInTemperature;

		if (pOn)
		{
			return lBase + this.fExternalOn;
		}
		else
		{
			return lBase + this.fExternalOff;
		}
	}

	@Override
	public int hashCode()
	{
		return design.getID();
	}

	@Override
	public boolean equals(Object pOther)
	{
		if (pOther instanceof TCL)
		{
			TCL lThat = (TCL) pOther;

			if (this.getID() == lThat.getID())
			{
				return true;
			}
		}

		return false;
	}
}
