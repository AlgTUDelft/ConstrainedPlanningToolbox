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

public class ConsoleOutput {
	private static boolean writeOutput = true;
	
	public static void println(String line) {
		if(writeOutput) {
			System.out.println(line);
		}
	}
	
	public static void println() {
		if(writeOutput) {
			System.out.println();
		}
	}
	
	public static void println(int i) {
		if(writeOutput) {
			System.out.println(i);
		}
	}
	
	public static void println(double d) {
		if(writeOutput) {
			System.out.println(d);
		}
	}
	
	public static void print(String line) {
		if(writeOutput) {
			System.out.print(line);
		}
	}
	
	public static void print(int i) {
		if(writeOutput) {
			System.out.print(i);
		}
	}
	
	public static void print(double d) {
		if(writeOutput) {
			System.out.print(d);
		}
	}
	
	public static void enableOutput() {
		writeOutput = true;
	}
	
	public static void disableOutput() {
		writeOutput = false;
	}
}
