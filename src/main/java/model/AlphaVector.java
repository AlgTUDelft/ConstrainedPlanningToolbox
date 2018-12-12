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
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlphaVector implements Comparable<AlphaVector>, Serializable {
	private static final long serialVersionUID = 1L;

	private double[] entries;
	
	private int originU;
	private int originW;
	private int action = -1;
	private int index = -1;
	private BeliefPoint belief = null;
	
	public AlphaVector(double[] entries) {
		this.entries = entries;
	}
	
	/**
	 * Get entry i of the vector
	 * @param i index
	 * @return entry i
	 */
	public double getEntry(int i) {
		assert i < entries.length;
		return entries[i];
	}
	
	/**
	 * Get all entries of the vector
	 * @return vector entries
	 */
	public double[] getEntries() {
		return entries;
	}
	
	/**
	 * Get minimum of the entries in this vector
	 * @return max value
	 */
	public double getMinValue() {
		double minValue = Double.POSITIVE_INFINITY;
		
		for(int i=0; i<entries.length; i++) {
			if(entries[i] < minValue) {
				minValue = entries[i];
			}
		}
		
		return minValue;
	}
	
	/**
	 * Get number of entries in the vector
	 * @return number of entries
	 */
	public int size() {
		return entries.length;
	}
	
	/**
	 * Get vector u from U and w from W that were used to compute this vector when computing a cross sum
	 * @param u index of u
	 * @param w index of w
	 */
	public void setOrigin(int u, int w) {
		originU = u;
		originW = w;
	}
	
	/**
	 * Get vector u from U that was used to create this vector
	 * @return index of u
	 */
	public int getOriginU() {
		return originU;
	}
	
	/**
	 * Get vector w from W that was used to create this vector
	 * @return index of w
	 */
	public int getOriginW() {
		return originW;
	}
	
	/**
	 * Get action associated with this vector
	 * @return action
	 */
	public int getAction() {
		return action;
	}
	
	/**
	 * Set action associated with this vector
	 * @param a action
	 */
	public void setAction(int a) {
		action = a;
	}
	
	/**
	 * Set the index of this vector. It is used in RBIP to define collection indices using original vector indices
	 * @param i index
	 */
	public void setIndex(int i) {
		index = i;
	}
	
	/**
	 * Get the index of this vector
	 * @return index
	 */
	public int getIndex() {
		return index;
	}
	
	public BeliefPoint getBeliefPoint() {
		return belief;
	}
	
	public boolean hasBeliefPoint() {
		return (belief != null);
	}
	
	public void setBeliefPoint(BeliefPoint b) {
		this.belief = b;
	}
	
	/**
	 * Compute dot product of this vector and elements of v
	 * @param v vector v
	 * @return dot product
	 */
	public double getDotProduct(double[] v) {
		assert entries != null;
		assert v != null;
		assert entries.length == v.length;
		double dp = 0.0;
		
		for(int i=0; i<entries.length; i++) {
			dp += entries[i] * v[i];
		}
		
		return dp;
	}
	
	/**
	 * Returns true iff entries in other vectors are identical
	 * @param otherVector vector to compare with
	 * @return true iff identical
	 */
	public boolean equals(AlphaVector otherVector) {
		if(otherVector.size() != size()) {
			return false;
		}
		else {
			boolean retValue = true;
			
			for(int i=0; i<size()&&retValue; i++) {
				// unlikely to be equal here
				retValue = retValue && (entries[i]==otherVector.getEntry(i));
			}
			
			return retValue;
		}
	}
	
	/**
	 * Get string representation of this vector
	 */
	public String toString() {
		String ret = "<AlphaVector(";
		
		for(int i=0; i<entries.length; i++) {
			ret += entries[i]+" ";
		}
		
		return ret+")>";
	}
	
	/**
	 * Compute sum of two vectors
	 * @param v1 vector 1
	 * @param v2 vector 2
	 * @return sum of the vectors
	 */
	public static AlphaVector sumVectors(AlphaVector v1, AlphaVector v2) {
		assert v1.size() == v2.size();

		double[] newEntries = new double[v1.size()];

		for (int s = 0; s < newEntries.length; s++) {
			newEntries[s] = v1.getEntry(s) + v2.getEntry(s);
		}

		assert v1.getAction() == v2.getAction();
		int action = v1.getAction();

		AlphaVector newVector = new AlphaVector(newEntries);
		newVector.setAction(action);

		return newVector;
	}
	
	/**
	 * Check whether first vector is lexicographically greater than the second
	 * @param v1 first vector
	 * @param v2 second vector
	 * @return true iff v1 is lexicographically greater than v2
	 */
	public static boolean lexGreater(AlphaVector v1, AlphaVector v2) {
		assert v1.size() == v2.size();
		
		for(int i=0; i<v1.size(); i++) {
			double v1Entry = v1.getEntry(i);
			double v2Entry = v2.getEntry(i);
			
			if(v1Entry != v2Entry) {
				if(v1Entry > v2Entry) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Get index of the best vector in U at belief point b
	 * @param b belief b
	 * @param U vector set U
	 * @return index of the best vector in U at b
	 */
	public static int getBestVectorIndex(double[] b, List<AlphaVector> U) {
		double max = Double.NEGATIVE_INFINITY;
		int wIndex = -1;
		AlphaVector w = null;
		
		for(int i=0; i<U.size(); i++) {
			AlphaVector u = U.get(i);
			double product = u.getDotProduct(b);
			
			if(product > max) {
				wIndex = i;
				w = u;
				max = product;
			}
			else if(product == max && lexGreater(u, w)) {
				wIndex = i;
				w = u;
			}
		}
		
		return wIndex;
	}
	
	/**
	 * Get index of the best vector in U at belief point b
	 * @param b belief b
	 * @param U vector set U
	 * @param skip vector encoding which alphavectors should not be considered
	 * @return index of the best vector in U at b
	 */
	public static int getBestVectorIndex(double[] b, ArrayList<AlphaVector> U, boolean[] skip) {
		double max = Double.NEGATIVE_INFINITY;
		int wIndex = -1;
		AlphaVector w = null;
		
		for(int i=0; i<U.size(); i++) {
			if(skip[i]) {
				continue;
			}
			
			AlphaVector u = U.get(i);
			double product = u.getDotProduct(b);
			
			if(product > max) {
				wIndex = i;
				w = u;
				max = product;
			}
			else if(product == max && lexGreater(u, w)) {
				wIndex = i;
				w = u;
			}
		}
		
		return wIndex;
	}
	
	/**
	 * Get indices of the best vectors in U at belief point b
	 * @param b belief b
	 * @param U vector set U
	 * @return indices of the best vectors in U at b
	 */
	public static List<Integer> getBestVectorIndices(double[] b, ArrayList<AlphaVector> U) {
		double max = Double.NEGATIVE_INFINITY;
		ArrayList<Integer> retList = new ArrayList<Integer>();
		
		for(int i=0; i<U.size(); i++) {
			AlphaVector u = U.get(i);
			double product = u.getDotProduct(b);
			
			if(Math.abs(product-max) <= 0.000000000001) {
				retList.add(i);
			}
			else if(product > max){
				retList.clear();
				retList.add(i);
				max = product;
			}
		}
		
		return retList;
	}
	
	/**
	 * Get value of belief b in vector set U
	 * @param b belief b
	 * @param U vector set U
	 * @return the value
	 */
	public static double getValue(double[] b, List<AlphaVector> U) {
		double max = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i<U.size(); i++) {
			AlphaVector u = U.get(i);
			double product = u.getDotProduct(b);
			
			if(product > max){
				max = product;
			}
		}
		
		return max;
	}
	
	@Override
	public int compareTo(AlphaVector other) {
		return AlphaVector.lexGreater(this, other) ? -1 : 0;
	}
}
