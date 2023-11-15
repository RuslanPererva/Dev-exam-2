import java.util.*;

class Guest {

	private final String name;
	private boolean RSVP;
	
	public Guest(String name) {
		if (name == null || name == "" || name.isBlank()) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		this.name = name;
		this.RSVP = false;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasRSVP() {
		return RSVP;
	}
	
	public void setRSVP (boolean rsvp) {
		this.RSVP = rsvp;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	
	@Override
	public boolean equals (Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		Guest temp = (Guest) obj;
		return Objects.equals(temp.name, name);
	}
	
	@Override
	public String toString() {
		String yesno = "";
		if (RSVP == true) {
			yesno = "yes";
		}
		else {yesno = "no";}
		return "Guest [name=" + name + ",rsvp=" +yesno + "]"; 
	}
	
}
