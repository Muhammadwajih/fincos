package pt.uc.dei.fincos.basic;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Map.Entry;

/**
 * A Domain that generates items from a predefined list of items
 * 
 * @author Marcelo R.N. Mendes
 * 
 * @see Domain
 * @see RandomDomain
 * @see SequentialDomain
 *
 */
public class PredefinedListDomain extends Domain {	
	private static final long serialVersionUID = -3816217657821868946L;

	private Random rnd;
	
	private LinkedHashMap<? extends Object, Double> itemSetRanges;
	private LinkedHashMap<Object, Double> itemMix;
	private Object[] items;
	private int index;
	
	/**
	 * Deterministic behavior: All items will be alternately generated in a predictable way, 
	 * all at the same proportion
	 * 
	 * @param items		A list of items
	 */
	public PredefinedListDomain(Object[] items) {		
		this.setItems(items);
		this.index = 0;
	}

	/**
	 * Stochastic behavior: Items will be generated randomly according  to their frequency (approximately)
	 * Frequencies does not need to sum up 1 (or 100), because they are normalized. 
	 * That is, if two items have the same value for the "frequency" parameter, 
	 * they will be generated in (approximately) the same proportion, 
	 * no matter the scale of the "frequency" parameter.
	 * 
	 * @param itemMix		A map item, frequency
	 * @param randomSeed	Seed for random number generation
	 */
	public PredefinedListDomain(LinkedHashMap<Object, Double> itemMix, Long randomSeed) {
		if(randomSeed != null)
			this.rnd = new Random(randomSeed);
		else
			this.rnd = new Random();
		
		this.itemMix = new LinkedHashMap<Object, Double>(itemMix.size());
		this.itemMix.putAll(itemMix);
		this.setItemSetRanges(itemMix);		
	}
	
	public boolean isDeterministic() {
		return (this.items != null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object generateValue() {
		if (this.items != null) {
			synchronized (this) { // Protects state variable "index" from concurrent access
				Object item = this.items[index];
				index = (++index)%this.items.length;
				return item;	
			}			
		}
		else {
			if (this.itemSetRanges != null) {
				double number = rnd.nextDouble();
				Object key="";

				for (Entry e : this.itemSetRanges.entrySet()) {
					if (number < (Double)e.getValue()) {
						key = e.getKey();
						break;
					}
				}
				return key;
			}
			else
				return null;
		}
	}

	private void setItemSetRanges(LinkedHashMap<Object, Double> itemMix) {
		if (itemMix != null) {
			//Normalizes the frequencies, so it is not required that frequencies sum up 1 and
			//Distributes the items in the range [0,1)
			double total = 0;
			
			Iterator<Double> freqIter = itemMix.values().iterator();
			while(freqIter.hasNext()){
				total += (Double)freqIter.next();
			}
		
			Iterator<Entry<Object, Double>> setValueIter = itemMix.entrySet().iterator();
			Entry<Object, Double> e;
			Double value;
			Double previousValue = 0.0;
			while(setValueIter.hasNext()) {
				e = setValueIter.next();
				value = previousValue + (Double)e.getValue()/total;
				previousValue = value;
				itemMix.put(e.getKey(), value);
			}
					
			this.itemSetRanges = itemMix;
		}
		else {
			System.err.println("WARNING: PredefinedListDomain's itemset was set to null.");
		}
	}

	private void setItems(Object[] items) {
		this.items = items;
	}
	
	public Object[] getItems() {
		return items;
	}
	
	public LinkedHashMap<Object, Double> getItemMix() {
		return itemMix;
	}
	
	@Override
	public String toString() {
		return "Predefined list";
	}

	@Override
	public void setRandomSeed(Long seed) {
		if(this.rnd != null)
			this.rnd.setSeed(seed);
	}
}
