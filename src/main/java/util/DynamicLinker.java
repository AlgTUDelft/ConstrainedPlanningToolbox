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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicLinker {

	public static ClassLoader getGurobiClassLoader() {

		File gurobiJar = findGurobiJar();

		if (gurobiJar == null) {
			throw new IllegalStateException("Unsatisfied link: cannot find Gurobi jar.");
		}

		ClassLoader loader = null;
		try {
			URI uri = gurobiJar.toPath().toUri();
			URL url = new URL(uri.toString());
			loader = URLClassLoader.newInstance(new URL[] { url });
		} catch (MalformedURLException exception) {
			throw new RuntimeException("Somehow path to URL failed.", exception);
		}

		return loader;
	}

	public static boolean canFindGurobiOnPath() {
		return (findGurobiJar() != null);
	}

	private static File findGurobiJar() {
		File gurobiJar = null;
		
		String pathToFile = ConfigFile.getStringProperty("gurobi_jar_file");
		
		File f = new File(pathToFile);
		if(f.exists() && !f.isDirectory()) {
			gurobiJar = f;
		}
		
		return gurobiJar;

//		// The goal file.
//		File gurobiJar = null;
//
//		// Find gurobi lib on the path.
//		String libraryPath = System.getProperty("java.library.path");
//		List<String> libraries = new ArrayList<>(Arrays.asList(libraryPath.split(";")));
//
//		String pathToGurobiDir = null;
//		for (String library : libraries) {
//			if (library.contains("gurobi") && library.endsWith("lib")) {
//				pathToGurobiDir = library;
//			}
//		}
//
//		// If path found, search for library.
//		if (pathToGurobiDir != null) {
//
//			File gurobiLibDir = new File(pathToGurobiDir);
//			File[] gurobiJars = gurobiLibDir.listFiles(new SpecificFileFilter("gurobi.jar"));
//
//			// If library found, store it.
//			if (gurobiJars.length == 1) {
//				gurobiJar = gurobiJars[0];
//			}
//		}
//
//		return gurobiJar;
	}
}
