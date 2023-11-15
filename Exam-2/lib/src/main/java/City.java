import java.util.*;
public abstract class City extends Region{
	private int population;
	
	public City(String n, int p) {
		super(n);
		if (p<0) {
			throw new IllegalArgumentException("Population cannot be negative [" + p + "]");
		}
		this.population=p;
	}
	
	public int getPopulation() {
		return population;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (super.equals(obj)==false) {
			return false;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		City temp = (City) obj;
		return population == temp.population;
	}
	@Override
	public int hashCode() {
		String tempname = super.getName();
		return Objects.hash(tempname, population);
	}
	
	@Override
	public abstract void accept(IRegionVisitor visitor);
}
