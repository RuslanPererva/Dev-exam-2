import java.util.*;

public class Map {
	private List<Region> regionList;
	
	public Map() {
		this.regionList= new ArrayList<>();
	}
	
	public void addRegions (Region ...regions) {
		if (regions == null) {
			throw new IllegalArgumentException("Regions cannot be null");
		}
		for (Region r : regions) {
			if (r == null) {
				throw new IllegalArgumentException("Regions cannot be null");
			}
			else {
			regionList.add(r);
			}
		}
	}
	
	public void traverse (IRegionVisitor v) {
		for (Region r : regionList) {
			r.accept(v);
		}
	}
	
}
