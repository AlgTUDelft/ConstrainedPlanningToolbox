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
package domains.tcl.simpletcl;

import domains.tcl.Agent;
import domains.tcl.primitives.TCL;



public class TCLAgent extends Agent
{
	private static final long serialVersionUID = -8406810689593572416L;

	private final TCL fTCL;

	private final double fR;
	private final double fC;
	private final double fP;

	public TCLAgent(TCL pTCL)
	{
		super(pTCL.getID());

		this.fTCL = pTCL;
		this.fR   = pTCL.getResistanceR();
		this.fC   = pTCL.getCapacitanceC();
		this.fP   = pTCL.getHeatingP();
	}

	public TCL getTCL()
	{
		return this.fTCL;
	}

	public double getInsulationR()
	{
		return this.fR;
	}

	public double getInsulationC()
	{
		return this.fC;
	}

	public double getHeatingP()
	{
		return this.fP;
	}

	@Override
	public int hashCode()
	{
		return fTCL.getID();
	}

	@Override
	public boolean equals(Object that)
	{
		boolean equal = false;

		if (that != null && that instanceof TCLAgent)
		{
			equal = this.equals((TCLAgent) that);
		}

		return equal;
	}

	private boolean equals(TCLAgent that)
	{
		return (this.getTCL().equals(that.getTCL()));
	}
}
