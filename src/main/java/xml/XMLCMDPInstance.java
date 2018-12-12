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
		"useBudgetConstraints",
		"costLimitsBudget",
		"costLimitsInstantaneous",
		"cmdps"})

public class XMLCMDPInstance {
    @XmlElementWrapper(name = "agents")
    @XmlElement(name = "cmdp")
    private ArrayList<XMLCMDP> cmdps;
    
    private int numAgents;
    private int numDecisions;
    private int numDomainResources;
    private boolean useBudgetConstraints;
    
    private double[] costLimitsBudget;
    private double[][] costLimitsInstantaneous;
    
    public XMLCMDPInstance() {
    	
    }
    
    public XMLCMDPInstance(ArrayList<XMLCMDP> cmdps, double[] costLimitsBudget, int numDecisions) {
    	this.numAgents = cmdps.size();
    	this.cmdps = cmdps;
    	this.numDecisions = numDecisions;
    	this.setNumDomainResources(costLimitsBudget.length);
    	this.costLimitsBudget = costLimitsBudget;
    	this.useBudgetConstraints = true;
    }
    
    public XMLCMDPInstance(ArrayList<XMLCMDP> cmdps, double[][] costLimitsInstantaneous, int numDecisions) {
    	this.numAgents = cmdps.size();
    	this.cmdps = cmdps;
    	this.numDecisions = numDecisions;
    	this.setNumDomainResources(costLimitsInstantaneous.length);
    	this.costLimitsInstantaneous = costLimitsInstantaneous;
    	this.useBudgetConstraints = false;
    }

    public ArrayList<XMLCMDP> getCMDPs() {
        return cmdps;
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

	public boolean getUseBudgetConstraints() {
		return useBudgetConstraints;
	}

	public void setUseBudgetConstraints(boolean useBudgetConstraints) {
		this.useBudgetConstraints = useBudgetConstraints;
	}

	public double[] getCostLimitsBudget() {
		return costLimitsBudget;
	}

	public void setCostLimitsBudget(double[] costLimits) {
		this.costLimitsBudget = costLimits;
	}

	public double[][] getCostLimitsInstantaneous() {
		return costLimitsInstantaneous;
	}

	public void setCostLimitsInstantaneous(double[][] costLimitsInstantaneous) {
		this.costLimitsInstantaneous = costLimitsInstantaneous;
	}

	public int getNumDomainResources() {
		return numDomainResources;
	}

	public void setNumDomainResources(int numDomainResources) {
		this.numDomainResources = numDomainResources;
	}
	
}
