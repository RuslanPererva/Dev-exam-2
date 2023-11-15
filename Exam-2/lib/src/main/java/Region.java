import java.util.*;

public abstract class Region {
	private final String name;

	public Region(String n) {
		if (n == null || n.isBlank()) {
			throw new IllegalArgumentException("Name cannot be null nor blank");
		}
		this.name = n;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		Region temp = (Region) obj;
		return Objects.equals(temp.name, name);
	}
	
	public abstract void accept (IRegionVisitor v);
	
	
	
}
