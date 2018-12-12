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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import model.AlphaVector;

public class OutputFileWriter {
	/**
	 * Get the string of the floating point number d with 17 decimal digits
	 * @param d floating point number
	 * @return string corresponding to d
	 */
	public static String doubleToString(double d) {
		// 17 is the maximum number of decimals that can be represented in IEEE 754 double floating point (varies from 15 to 17)
		return String.format("%.17f", d);
	}
	
	/**
	 * Write a value function to a file
	 * @param vectors vector set representing the value function
	 * @param outputFile output file where values should be written
	 */
	public static void dumpValueFunction(ArrayList<AlphaVector> vectors, String outputFile) {
		try {
			Writer output = new BufferedWriter(new FileWriter(outputFile));
			
			for(AlphaVector a : vectors) {
				output.write(a.getAction()+"\n");
				//assert a.getAction() != -1;
				
				for(int i=0; i<a.size(); i++) {
					output.write(doubleToString(a.getEntry(i)) + " ");
				}
				
				output.write("\n\n");
			}
			
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a standard LP (U,w) to a file in the directory specified
	 * @param U vector set
	 * @param w vector
	 * @param directory directory where file should be written
	 * @param index index of the LP, which is used in the filename
	 */
	public static void dumpLP(ArrayList<AlphaVector> U, AlphaVector w, String directory, int index) {		
		try {
			Writer output = new BufferedWriter(new FileWriter(directory+"/lp"+index));
			
			output.write("nVectors="+U.size()+"\n");
			output.write("nStates="+w.size()+"\n");
			
			// write w
			output.write("w ");
			for(int i=0; i<w.size(); i++) {
				output.write(doubleToString(w.getEntry(i))+" ");
			}
			output.write("\n");
			
			// write vectors u
			for(AlphaVector u : U) {
				for(int i=0; i<u.size(); i++) {
					output.write(doubleToString(u.getEntry(i))+" ");
				}
				output.write("\n");
			}
			
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Append line to a file
	 * @param filename file where line should be added
	 * @param line line to be added
	 */
	public static void appendLine(String filename, String line) {
		try {
			Writer output = new BufferedWriter(new FileWriter(filename,true));
			output.append(line+"\n");			
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clear file
	 * @param filename
	 */
	public static void clearFile(String filename) {
		try {
			PrintWriter writer = new PrintWriter(filename);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Rename file
	 * @param filename original filename
	 * @param newName new filename
	 */
	public static void renameFile(String filename, String newName) {
		File file = new File(filename);
		File newFile = new File(newName);
		file.renameTo(newFile);
	}
}
