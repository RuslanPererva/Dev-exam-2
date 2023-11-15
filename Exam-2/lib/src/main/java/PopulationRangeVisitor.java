import java.util.*;

public class PopulationRangeVisitor implements IRegionVisitor{
	private final int upper;
	private final int lower;
	private List<City> cities;
	
	public PopulationRangeVisitor(int l, int u) {
		if (l<0 || u<0) {
			throw new IllegalArgumentException("Range must have positive values");
		}
		if (l>u) {
			throw new IllegalArgumentException("Range must be incremental");
		}
		this.lower=l;
		this.upper=u;
		this.cities=new ArrayList<>();
	}
	
	public void visit (LargeCity c) {
		if (c.getPopulation() <= upper && c.getPopulation() >= lower) {
			cities.add(c);
		}
	}
	public void visit (SmallCity c) {
		if (c.getPopulation() <= upper && c.getPopulation() >= lower) {
			cities.add(c);
		}
	}
	public List<City> getCities(){
		List<City> temp = new ArrayList<City>();
		for (City c : cities) {
			temp.add(c);
		}
		return temp;
	}
}
