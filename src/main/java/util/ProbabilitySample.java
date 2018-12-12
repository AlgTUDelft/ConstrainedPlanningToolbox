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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProbabilitySample {
	private List<Item> items;
	private Random rnd;
	private double probabilitySum = 0.0;
	
	public ProbabilitySample(Random rnd) {
		items = new ArrayList<Item>();
		this.rnd = rnd;
	}
	
	public void addItem(int item, double probability) {
		assert probability >= 0.0 && probability <= 1.0 : "PROB: "+probability;		
		if(probability > 0.0) {
			items.add(new Item(item,probability));
			probabilitySum += probability;
		}
	}
	
	public int sampleItem() {
		assert Math.abs(probabilitySum-1.0) < 0.001 : "No valid probability distribution: "+probabilitySum;
		assert items.size() > 0 : "No items added";
		
		double cumulative = 0.0;
		double randomNumber = rnd.nextDouble();
		int retItem = items.get(items.size()-1).item;
		
		for(Item item : items) {
			cumulative += item.probability;
			
			if(randomNumber <= cumulative) {
				retItem = item.item;
				break;
			}
		}
		
		return retItem;
	}
	
	private class Item {
		public int item;
		public double probability;
		
		public Item(int item, double probability) {
			this.item = item;
			this.probability = probability;
		}
	}
	
	public static int sampleItemInline(int[] items, double[] probabilities, Random rnd) {
		assert items.length > 0 && probabilities.length > 0 && items.length==probabilities.length;
		
		if (items.length == 1) {
			return items[0];
		} else {
			final double randomNumber = rnd.nextDouble();
	
			int index = 0;
			double cumulative = 0;
			
			while ((cumulative += probabilities[index]) < randomNumber) index++;
			
			return items[index];
		}
	}

	public static <T> T sampleObjectInline(List<T> items, List<Double> probabilities, Random rnd) {
		if (items.size() == 1) {
			return items.get(0);
		} else {
			final double randomNumber = rnd.nextDouble();

			int index = 0;
			double cumulative = 0;
			
			while ((cumulative += probabilities.get(index)) < randomNumber) index++;
			
			return items.get(index);
		}
	}
}
