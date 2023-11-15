import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class State extends Region{
	private List<City> cities;
	
	public State (String n) {
		super (n);
		this.cities= new ArrayList<>();
	}
	
	public void addCities(City ...cities) {
		if (cities == null) {
			throw new IllegalArgumentException("Cities cannot be null");
		}
		else {
			for (City c : cities) {
				if (c == null) {
					throw new IllegalArgumentException("Cities cannot be null");
				}
				else {
				this.cities.add(c);
				}
			}
		}
	}
	
	public List<City> getCities(){
		List<City> temp = new ArrayList<>();
		for (City c : cities) {
			temp.add(c);
		}
		return temp;
	}
    @Override
    public void accept(IRegionVisitor visitor) {
    	visitor.visit(this);
    	for (City c : cities) {
    		c.accept(visitor);
    	}
        
    }
}
