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
package xml;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "instance", name="probleminstance")
@XmlType(propOrder = {
		"numAgents",
		"numDecisions",
		"numDomainResources",
		"costLimits",
		"cpomdps"})

public class XMLCPOMDPInstance {
    @XmlElementWrapper(name = "agents")
    @XmlElement(name = "cpomdp")
    private ArrayList<XMLCPOMDP> cpomdps;
    
    private int numAgents;
    private int numDecisions;
    private int numDomainResources;
    private double[] costLimits;
    
    public XMLCPOMDPInstance() {
    	
    }
    
    public XMLCPOMDPInstance(ArrayList<XMLCPOMDP> cpomdps, double[] costLimits, int numDecisions) {
    	this.numAgents = cpomdps.size();
    	this.cpomdps = cpomdps;
    	this.numDecisions = numDecisions;
    	this.setCostLimits(costLimits);
    	this.setNumDomainResources(costLimits.length);
    }

    public ArrayList<XMLCPOMDP> getCPOMDPs() {
        return cpomdps;
    }

	public int getNumAgents() {
		return numAgents;
	}

	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}

	public int getNumDecisions() {
		return numDecisions;
	}

	public void setNumDecisions(int numDecisions) {
		this.numDecisions = numDecisions;
	}

	public int getNumDomainResources() {
		return numDomainResources;
	}

	public void setNumDomainResources(int numDomainResources) {
		this.numDomainResources = numDomainResources;
	}

	public double[] getCostLimits() {
		return costLimits;
	}

	public void setCostLimits(double[] costLimits) {
		this.costLimits = costLimits;
	}
	
}
