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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
	private static ConfigFile instance = null;
	
	private Properties properties;
	
	private ConfigFile() {
		properties = new Properties();
		
		try {
			FileInputStream file = new FileInputStream("./CONFIG");
			properties.load(file);
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPropertyFromFile(String property) {
		return properties.getProperty(property);
	}
	
	public static String getStringProperty(String property) {
		if(instance == null) {
			instance = new ConfigFile();
		}
		
		return instance.getPropertyFromFile(property);
    }
	
	public static boolean getBooleanProperty(String property) {
		if(instance == null) {
			instance = new ConfigFile();
		}
		
		return Boolean.parseBoolean(instance.getPropertyFromFile(property));
    }
	
	public static double getDoubleProperty(String property) {
		if(instance == null) {
			instance = new ConfigFile();
		}
		
		return Double.parseDouble(instance.getPropertyFromFile(property));
    }
	
	public static int getIntProperty(String property) {
		if(instance == null) {
			instance = new ConfigFile();
		}
		
		return Integer.parseInt(instance.getPropertyFromFile(property));
    }
}
