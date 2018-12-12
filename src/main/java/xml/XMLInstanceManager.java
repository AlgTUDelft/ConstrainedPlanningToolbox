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

import instances.CMDPInstance;
import instances.CPOMDPInstance;
import instances.ConstraintType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


import model.BeliefPoint;
import model.CMDP;
import model.CPOMDP;

public class XMLInstanceManager {
	
	public static void writeCMDPInstance(CMDPInstance instance, String XML_FILE) {
		int numAgents = instance.getCMDPs().length;
		int numDecisions = instance.getNumDecisions();
		boolean useBudgetConstraints = (instance.getConstraintType()== ConstraintType.BUDGET);
		double[] costLimitsBudget = instance.getCostLimitsBudget();
		double[][] costLimitsInstantaneous = instance.getCostLimitsInstantaneous();
		
		CMDP[] cmdps = instance.getCMDPs();
		ArrayList<XMLCMDP> xmlCMDPs = new ArrayList<XMLCMDP>();
		for(int i=0; i<numAgents; i++) {
			xmlCMDPs.add(new XMLCMDP(cmdps[i]));
		}
		
		XMLCMDPInstance xmlInstance = useBudgetConstraints ? new XMLCMDPInstance(xmlCMDPs, costLimitsBudget, numDecisions) : new XMLCMDPInstance(xmlCMDPs, costLimitsInstantaneous, numDecisions);
		
		try {
			JAXBContext context = JAXBContext.newInstance(XMLCMDPInstance.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(xmlInstance, new File(XML_FILE));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeCPOMDPInstance(CPOMDPInstance instance, String XML_FILE) {
		int numAgents = instance.getCPOMDPs().length;
		int numDecisions = instance.getNumDecisions();
		double[] costLimits = instance.getCostLimits();
		
		CPOMDP[] cpomdps = instance.getCPOMDPs();
		ArrayList<XMLCPOMDP> xmlCPOMDPs = new ArrayList<XMLCPOMDP>();
		for(int i=0; i<numAgents; i++) {
			xmlCPOMDPs.add(new XMLCPOMDP(cpomdps[i]));
		}
		
		XMLCPOMDPInstance xmlInstance = new XMLCPOMDPInstance(xmlCPOMDPs, costLimits, numDecisions);
		
		try {
			JAXBContext context = JAXBContext.newInstance(XMLCPOMDPInstance.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(xmlInstance, new File(XML_FILE));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public static CMDPInstance readCMDPInstance(String XML_FILE) {
		XMLCMDPInstance instance = null;
		
		try {
			JAXBContext context = JAXBContext.newInstance(XMLCMDPInstance.class);
			Unmarshaller um = context.createUnmarshaller();
			instance = (XMLCMDPInstance) um.unmarshal(new FileReader(XML_FILE));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<XMLCMDP> xmlCMDPs = instance.getCMDPs();
		CMDP[] cmdps = new CMDP[xmlCMDPs.size()];
		
		for(int i=0; i<xmlCMDPs.size(); i++) {
			XMLCMDP xmlCMDP = xmlCMDPs.get(i);
			
			CMDP cmdp = new CMDP(xmlCMDP.getNumStates(), xmlCMDP.getNumActions(), xmlCMDP.getCostFunctions(), xmlCMDP.getInitialState(), xmlCMDP.getNumDecisions());
			
			if(xmlCMDP.isHasTimeDependentReward()) {
				cmdp.setRewardFunction(xmlCMDP.getTimeRewardFunction());
			}
			else {
				cmdp.setRewardFunction(xmlCMDP.getRewardFunction());
			}
			
			if(xmlCMDP.isHasTimeDependentTransitions()) {
				cmdp.setTransitionFunction(xmlCMDP.getTimeTransitionDestinations(), xmlCMDP.getTimeTransitionProbabilities());
			}
			else {
				cmdp.setTransitionFunction(xmlCMDP.getTransitionDestinations(), xmlCMDP.getTransitionProbabilities());
			}
			
			cmdps[i] = cmdp;
		}
		
		if(instance.getUseBudgetConstraints()) {
			double[] costLimits = instance.getCostLimitsBudget();
			int numDecisions = instance.getNumDecisions();
			return CMDPInstance.createBudgetInstance(cmdps, costLimits, numDecisions);
		}
		else {
			double[][] costLimitsInstantaneous = instance.getCostLimitsInstantaneous();
			int numDecisions = instance.getNumDecisions();			
			return CMDPInstance.createInstantaneousInstance(cmdps, costLimitsInstantaneous, numDecisions);
		}
	}
	
	public static CPOMDPInstance readCPOMDPInstance(String XML_FILE) {
		XMLCPOMDPInstance instance = null;
		
		try {
			JAXBContext context = JAXBContext.newInstance(XMLCPOMDPInstance.class);
			Unmarshaller um = context.createUnmarshaller();
			instance = (XMLCPOMDPInstance) um.unmarshal(new FileReader(XML_FILE));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<XMLCPOMDP> xmlCPOMDPs = instance.getCPOMDPs();
		CPOMDP[] cpomdps = new CPOMDP[xmlCPOMDPs.size()];
		
		for(int i=0; i<xmlCPOMDPs.size(); i++) {
			XMLCPOMDP xmlCPOMDP = xmlCPOMDPs.get(i);
			
			CPOMDP cmdp = new CPOMDP(xmlCPOMDP.getNumStates(), xmlCPOMDP.getNumActions(), xmlCPOMDP.getNumObservations(), xmlCPOMDP.getCostFunctions(), xmlCPOMDP.getObservationFunction(), new BeliefPoint(xmlCPOMDP.getInitialBelief()), xmlCPOMDP.getNumDecisions());
			
			if(xmlCPOMDP.isHasTimeDependentReward()) {
				cmdp.setRewardFunction(xmlCPOMDP.getTimeRewardFunction());
			}
			else {
				cmdp.setRewardFunction(xmlCPOMDP.getRewardFunction());
			}
			
			if(xmlCPOMDP.isHasTimeDependentTransitions()) {
				cmdp.setTransitionFunction(xmlCPOMDP.getTimeTransitionDestinations(), xmlCPOMDP.getTimeTransitionProbabilities());
			}
			else {
				cmdp.setTransitionFunction(xmlCPOMDP.getTransitionDestinations(), xmlCPOMDP.getTransitionProbabilities());
			}
			
			cpomdps[i] = cmdp;
		}
		
		return CPOMDPInstance.createBudgetInstance(cpomdps, instance.getCostLimits(), instance.getNumDecisions());
	}
}
