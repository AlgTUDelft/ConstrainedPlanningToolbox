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
package executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import algorithms.UnsupportedInstanceException;
import algorithms.mdp.CMDPAlgorithm;
import algorithms.mdp.colgen.ColGen;
import algorithms.mdp.colgen.ValueIterationFiniteHorizon;
import algorithms.mdp.constrainedmdp.ConstrainedMDP;
import algorithms.mdp.deterministicpreallocation.DeterministicPreallocation;
import algorithms.mdp.dynamicrelaxation.DynamicRelaxation;
import algorithms.pomdp.CPOMDPAlgorithm;
import algorithms.pomdp.calp.FiniteCALP;
import algorithms.pomdp.cgcp.CGCP;
import domains.advertising.AdvertisingInstanceGenerator;
import domains.cbm.CBMGenerator;
import domains.maze.MazeInstanceGenerator;
import domains.tcl.TCLInstanceGeneratorFixedLimit;
import domains.tcl.TCLInstanceGeneratorMultiLevel;
import domains.webad.WebAdGenerator;
import instances.CMDPInstance;
import instances.CPOMDPInstance;
import lp.LPSolver;
import lp.gurobi.LPSolverGurobi;
import lp.lpsolve.LPSolverLPSolve;
import lp.simplex.LPSolverSimplex;
import model.BeliefPoint;
import model.CPOMDP;
import solutions.mdp.CMDPSolution;
import solutions.pomdp.CPOMDPSolution;
import util.ConsoleOutput;
import util.SolutionManager;
import xml.XMLInstanceManager;

public class Server {
	private Random rnd = new Random(222);
	private LPSolver lpSolver;
	private int port;
	private String clientFolder;
	
	private CMDPInstance cmdpInstance = null;
	private CMDPSolution cmdpSolution = null;
	private CPOMDPInstance cpomdpInstance = null;
	private CPOMDPSolution cpomdpSolution = null;
	
	public Server(LPSolver lpSolver, int port, String clientFolder) {
		this.port = port;
		this.lpSolver = lpSolver;
		this.clientFolder = clientFolder;
	}
	
	public void run() {
		try {
			// start the server
			ServerSocket server = new ServerSocket(port);
			boolean serverRunning = true;
			
			while(serverRunning) {
				// block until a client connects to the server
				System.out.println("waiting for client to connect");
				Socket pythonClient = server.accept();
				System.out.println("client connected");
				
				// connected, so we initialize input and output
				BufferedReader in = new BufferedReader(new InputStreamReader(pythonClient.getInputStream()));
				PrintWriter out = new PrintWriter(pythonClient.getOutputStream(), true);
				
				// main processing loop
				while(true) {				
					String msg = in.readLine();
					
					if(msg == null || msg.equals("disconnect")) {
						System.out.println("disconnect request received");
						out.println();
						pythonClient.close();
						break;
					}
					else if(msg.equals("shutdown")) {
						System.out.println("shutdown request received");
						out.println();
						pythonClient.close();
						serverRunning = false;
						break;
					}
					else {
						processRequest(msg, out);
					}
				}
			}
			
			System.out.println("server shutting down");
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processRequest(String msg, PrintWriter out) {
		String response = "";
		
		if(msg.startsWith("dumpDefaultDomain")) {
			// load cmdp or cpomdp domain and dump xml
			System.out.println(" dump default domain");
			String[] command = msg.split("_");
			String domainName = command[1];
			int numAgents = Integer.parseInt(command[2]);
			int numDecisions = Integer.parseInt(command[3]);
			dumpDefaultDomain(domainName, numAgents, numDecisions);
		}
		else if(msg.startsWith("solveDefaultDomain")) {
			// solve built-in domain and send expected reward back
			System.out.println(" solveDefaultDomain");
			String[] command = msg.split("_");
			String domainName = command[1];
			int numAgents = Integer.parseInt(command[2]);
			int numDecisions = Integer.parseInt(command[3]);
			String algName = command[4];
			
			try {
				double expectedReward = solveProblem(domainName, numAgents, numDecisions, algName);
				response = expectedReward+"";
			} catch (Exception e) {
				response = "EXCEPTION";
				e.printStackTrace();
			}
		}
		else if(msg.startsWith("solveXMLDomainMDP")) {
			// solve domain in xml file and send expected reward back
			System.out.println(" solveXMLDomainMDP");
			String[] command = msg.split("_");
			String algName = command[1];
			
			try {
				double expectedReward = solveXMLMDP(algName);
				response = expectedReward+"";
			} catch (Exception e) {
				response = "EXCEPTION";
				e.printStackTrace();
			}
		}
		else if(msg.startsWith("solveXMLDomainPOMDP")) {
			// solve domain in xml file and send expected reward back
			System.out.println(" solveXMLDomainPOMDP");
			String[] command = msg.split("_");
			String algName = command[1];
			
			try {
				double expectedReward = solveXMLPOMDP(algName);
				response = expectedReward+"";
			} catch (Exception e) {
				response = "EXCEPTION";
				e.printStackTrace();
			}
		}
		else if(msg.startsWith("writeCMDPSolution")) {
			// write existing solution to file
			System.out.println(" writeCMDPSolution");
			String[] command = msg.split("_");
			String filename = command[1];
			
			if(cmdpSolution != null) {
				SolutionManager.writeCMDPSolution(cmdpSolution, filename);
			}
			
			response = "";
		}
		else if(msg.startsWith("writeCPOMDPSolution")) {
			// write existing solution to file
			System.out.println(" writeCPOMDPSolution");
			String[] command = msg.split("_");
			String filename = command[1];
			
			if(cpomdpSolution != null) {
				SolutionManager.writeCPOMDPSolution(cpomdpSolution, filename);
			}
			
			response = "";
		}
		else if(msg.startsWith("readCMDPSolution")) {
			// read solution from file
			System.out.println(" readCMDPSolution");
			String[] command = msg.split("_");
			String filename = command[1];
			cmdpSolution = SolutionManager.readCMDPSolution(filename);
			response = "";
		}
		else if(msg.startsWith("readCPOMDPSolution")) {
			// read solution from file
			System.out.println(" readCPOMDPSolution");
			String[] command = msg.split("_");
			String filename = command[1];
			cpomdpSolution = SolutionManager.readCPOMDPSolution(filename);
			response = "";
		}
		else if(msg.startsWith("getActionsCMDP")) {			
			// get actions based on joint state
			String[] command = msg.split("_");
			int t = Integer.parseInt(command[1]);
			String states = command[2];
			states = states.substring(1, states.length()-1);
			states = states.replaceAll(" ","");
			
			String[] stateSplit = states.split(",");
			int[] jointState = new int[stateSplit.length];
			for(int s=0; s<stateSplit.length; s++) {
				jointState[s] = Integer.parseInt(stateSplit[s]);
			}
			int[] actions = cmdpSolution.getActions(t, jointState);
			response = Arrays.toString(actions).replaceAll(" ", "");
			response = response.replaceAll(",", " ");
			response = response.substring(1, response.length()-1);
		}
		else if(msg.startsWith("getActionsCPOMDP")) {
			// get actions based on joint belief (maintained by server)
			String[] command = msg.split("_");
			int t = Integer.parseInt(command[1]);
			
			int[] actions = getPOMDPActions(t);
			response = Arrays.toString(actions).replaceAll(" ", "");
			response = response.replaceAll(",", " ");
			response = response.substring(1, response.length()-1);
		}
		else if(msg.startsWith("actionObservations")) {
			// use observations to update beliefs and policy representations
			String[] command = msg.split("_");
			
			String actions = command[1];
			actions = actions.substring(1, actions.length()-1);
			actions = actions.replaceAll(" ","");
			
			String observations = command[2];
			observations = observations.substring(1, observations.length()-1);
			observations = observations.replaceAll(" ","");
			
			String[] actionSplit = observations.split(",");
			int[] ac = new int[actionSplit.length];
			for(int i=0; i<actionSplit.length; i++) {
				ac[i] = Integer.parseInt(actionSplit[i]);
			}
			
			String[] obsSplit = observations.split(",");
			int[] obs = new int[obsSplit.length];
			for(int i=0; i<obsSplit.length; i++) {
				obs[i] = Integer.parseInt(obsSplit[i]);
			}
			
			cpomdpSolution.update(ac, obs);
			updateBeliefs(ac, obs);
			
			response = "";
		}
		
		out.println(response);
	}
	
	private BeliefPoint[] beliefs;
	
	private int[] getPOMDPActions(int t) {
		int nAgents = cpomdpInstance.getCPOMDPs().length;
		
		if(t == 0) {
			beliefs = new BeliefPoint[nAgents];
			for(int i=0; i<nAgents; i++) {
				beliefs[i] = cpomdpInstance.getCPOMDPs()[i].getInitialBelief();
			}
		}
		
		int[] actions = cpomdpSolution.getActions(t, beliefs);
		
		return actions;
	}
	
	private void updateBeliefs(int[] actions, int[] observations) {
		CPOMDP[] cpomdps = cpomdpInstance.getCPOMDPs();
		for(int i=0; i<cpomdps.length; i++) {
			CPOMDP cpomdp = cpomdps[i];
			BeliefPoint b = beliefs[i];
			int a = actions[i];
			int o = observations[i];
			BeliefPoint bao = cpomdp.updateBelief(b, a, o);
			beliefs[i] = bao;
		}
	}
	
	private double solveXMLMDP(String algName) throws Exception {
		String instanceFile = clientFolder+"/pythonInstance.xml";
		
		cmdpInstance = XMLInstanceManager.readCMDPInstance(instanceFile);
		cpomdpInstance = null;
		
		// select algorithm and run
		CMDPAlgorithm alg = null;
		
		if(algName.equals("cmdp")) {
			alg = new ConstrainedMDP(lpSolver, rnd);
		}
		else if(algName.equals("colgen")) {
			alg = new ColGen(lpSolver, rnd);
		}
		else if(algName.equals("deterministicpreallocation")) {
			alg = new DeterministicPreallocation(lpSolver, rnd);
		}
		else if(algName.startsWith("dynamicrelaxation")) {
			String[] algSplit = algName.split("|");
			double tolerance = Double.parseDouble(algSplit[1]);
			double beta = Double.parseDouble(algSplit[2]);
			alg = new DynamicRelaxation(new ConstrainedMDP(lpSolver, rnd), tolerance, beta, rnd);
		}
		
		// solve
		try {
			alg.setInstance(cmdpInstance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
		}
		
		cmdpSolution = alg.solve();
		cpomdpSolution = null;
		
		// compute expected reward
		double expectedReward = cmdpSolution.getExpectedReward();
		
		return expectedReward;
	}
	
	private double solveXMLPOMDP(String algName) throws Exception {
		String instanceFile = clientFolder+"/pythonInstance.xml";
		
		cpomdpInstance = XMLInstanceManager.readCPOMDPInstance(instanceFile);
		cmdpInstance = null;
		
		CPOMDPAlgorithm alg = null;

		if (algName.equals("cgcp")) {
			alg = new CGCP(lpSolver, rnd);
		} else if (algName.equals("calp")) {
			alg = new FiniteCALP(lpSolver, rnd);
		}

		// solve
		try {
			alg.setInstance(cpomdpInstance);
		} catch (UnsupportedInstanceException e) {
			e.printStackTrace();
		}

		cmdpSolution = null;
		cpomdpSolution = alg.solve();

		// compute expected reward
		double expectedReward = cpomdpSolution.getExpectedReward();
		
		return expectedReward;
	}
	
	private double solveProblem(String domainName, int numAgents, int numDecisions, String algName) throws Exception {
		CMDPInstance cmdpInstance = null;
		CPOMDPInstance cpomdpInstance = null;
		
		cmdpSolution = null;
		cpomdpSolution = null;
		
		double expectedReward = 0.0;
		
		if(domainName.equals("advertising") || domainName.equals("maze") || domainName.equals("tclFixedLimit") || domainName.equals("tclMultiLevel")) {
			// MDP
			if(domainName.equals("advertising")) {
				AdvertisingInstanceGenerator generator = new AdvertisingInstanceGenerator();
				cmdpInstance = generator.getInstance(numAgents, numDecisions);
			}
			else if(domainName.equals("maze")) {
				MazeInstanceGenerator generator = new MazeInstanceGenerator();
				cmdpInstance = generator.getInstance(numAgents, numDecisions);
			}
			else if(domainName.equals("tclFixedLimit")) {
				TCLInstanceGeneratorFixedLimit generator = new TCLInstanceGeneratorFixedLimit();
				cmdpInstance = generator.getInstance(numAgents, numDecisions);
			}
			else if(domainName.equals("tclMultiLevel")) {
				TCLInstanceGeneratorMultiLevel generator = new TCLInstanceGeneratorMultiLevel();
				cmdpInstance = generator.getInstance(numAgents, numDecisions);
			}
		}
		else if(domainName.equals("cbm") || domainName.equals("webad")){
			// POMDP
			if(domainName.equals("cbm")) {
				CBMGenerator generator = new CBMGenerator();
				cpomdpInstance = generator.getInstance(numAgents, numDecisions);
			}
			else if(domainName.equals("webad")) {
				WebAdGenerator generator = new WebAdGenerator();
				cpomdpInstance = generator.getInstance(numAgents, numDecisions);
			}
		}
		
		// select algorithm and run
		if(cmdpInstance != null) {
			// MDP
			
			CMDPAlgorithm alg = null;
			
			if(algName.equals("cmdp")) {
				alg = new ConstrainedMDP(lpSolver, rnd);
			}
			else if(algName.equals("colgen")) {
				alg = new ColGen(lpSolver, rnd);
			}
			else if(algName.equals("deterministicpreallocation")) {
				alg = new DeterministicPreallocation(lpSolver, rnd);
			}
			else if(algName.startsWith("dynamicrelaxation")) {
				String[] algSplit = algName.split("|");
				double tolerance = Double.parseDouble(algSplit[1]);
				double beta = Double.parseDouble(algSplit[2]);
				alg = new DynamicRelaxation(new ConstrainedMDP(lpSolver, rnd), tolerance, beta, rnd);
			}
			
			// solve
			try {
				alg.setInstance(cmdpInstance);
			} catch (UnsupportedInstanceException e) {
				e.printStackTrace();
			}
			
			cmdpSolution = alg.solve();

			expectedReward = cmdpSolution.getExpectedReward();
		}
		else {
			// POMDP
			
			CPOMDPAlgorithm alg = null;
			
			if(algName.equals("cgcp")) {
				alg = new CGCP(lpSolver, rnd);
			}
			else if(algName.equals("calp")) {
				alg = new FiniteCALP(lpSolver, rnd);
			}
			
			// solve
			try {
				alg.setInstance(cpomdpInstance);
			} catch (UnsupportedInstanceException e) {
				e.printStackTrace();
			}
			
			cpomdpSolution = alg.solve();
			
			expectedReward = cpomdpSolution.getExpectedReward();
		}
		
		return expectedReward;
	}
	
	private void dumpDefaultDomain(String domainName, int numAgents, int numDecisions) {
		String dumpFile = clientFolder+"/javaInstance.xml";
		
		if(domainName.equals("advertising")) {
			AdvertisingInstanceGenerator generator = new AdvertisingInstanceGenerator();
			CMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCMDPInstance(instance, dumpFile);
		}
		else if(domainName.equals("cbm")) {
			CBMGenerator generator = new CBMGenerator();
			CPOMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCPOMDPInstance(instance, dumpFile);
		}
		else if(domainName.equals("maze")) {
			MazeInstanceGenerator generator = new MazeInstanceGenerator();
			CMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCMDPInstance(instance, dumpFile);
		}
		else if(domainName.equals("tclFixedLimit")) {
			TCLInstanceGeneratorFixedLimit generator = new TCLInstanceGeneratorFixedLimit();
			CMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCMDPInstance(instance, dumpFile);
		}
		else if(domainName.equals("tclMultiLevel")) {
			TCLInstanceGeneratorMultiLevel generator = new TCLInstanceGeneratorMultiLevel();
			CMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCMDPInstance(instance, dumpFile);
		}
		else if(domainName.equals("webad")) {
			WebAdGenerator generator = new WebAdGenerator();
			CPOMDPInstance instance = generator.getInstance(numAgents, numDecisions);
			XMLInstanceManager.writeCPOMDPInstance(instance, dumpFile);
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 4) {
			System.out.println("The server requires four arguments: port, lpsolver, verbose, clientdir");
			System.out.println();
			System.out.println("1. port is the port where the server waits for a connection");
			System.out.println("2. lpsolver is the name of the LP solver to use (gurobi, lpsolve or simplex)");
			System.out.println("3. verbose indicates whether algorithms should print output (true or false)");
			System.out.println("4. clientdir is the full path of the client directory");
			System.out.println();
			System.out.println("The arguments should be separated by a space.");
			System.exit(0);
		}
		
		// parse params
		int port = Integer.parseInt(args[0]);
		String lpSolverName = args[1];
		boolean verbose = Boolean.parseBoolean(args[2]);
		String clientPath = args[3];
		
		// load lp solver		
		LPSolver lpSolver = null;
		if(lpSolverName.equals("gurobi")) {
			lpSolver = new LPSolverGurobi();
		}
		else if(lpSolverName.equals("lpsolve")) {
			lpSolver = new LPSolverLPSolve();
		}
		else if(lpSolverName.equals("simplex")) {
			lpSolver = new LPSolverSimplex();
		}
		else {
			System.out.println("LP solver should be gurobi, lpsolve or simplex");
			System.exit(0);
		}
		
		// check port number
		if(port < 0) {
			System.out.println("Port should be positive");
		}
		
		// activate verbose output
		if(verbose) {
			ConsoleOutput.enableOutput();
		}
		
		// check if client folder exists
		File f = new File(clientPath);
		if(!(f.exists() && f.isDirectory())) {
			System.out.println("Client path is not a valid directory");
			System.exit(0);
		}
		
		// start server
		Server server = new Server(lpSolver, port, clientPath);
		server.run();
	}
	
}
