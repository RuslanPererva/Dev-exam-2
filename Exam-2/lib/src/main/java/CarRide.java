
public class CarRide {
	private String from;
	private String to;
	private Integer rate;

public static class Builder {
	
	private String from;
	private String to;
	private Integer rate;
	private boolean Valid=true;

	public Builder from(String s) {
		if (s == null|| s.isBlank()) {
			throw new IllegalArgumentException("from cannot be null");
		}
		else {
			this.from = s;
			return this;
		}
	}
	
	public Builder to(String s) {
		if (s == null || s.isBlank()) {
			throw new IllegalArgumentException("to cannot be null");
		}
		else {
			this.to = s;
			return this;
		}
	}
	
	public Builder rate (int i) {
		if (i < 0) {
			throw new IllegalArgumentException("rate cannot be negative");
		}
		else {
			this.rate = i;
			return this;
		}
	}
	
	public boolean isValid() {
		return rate!=null && to!=null && from!=null;
		
	}
	
	public CarRide build() {
		if (isValid()&& Valid ==true) {
			Valid=false;
			return new CarRide(this);
			
		}
		if (Valid == false) {
			throw new IllegalStateException("CarRide already built");
		}
		String missing = "";
		if (from==null)missing+="from ";
		if (to==null)missing+="to ";
		if (rate==null) missing+="rate ";
		throw new IllegalStateException("missing data:" + missing);
	}
	
}

	private CarRide(Builder b) {
		from=b.from;
		to=b.to;
		rate=b.rate;
	}
	
	@Override
	public String toString() {
		return "CarRide[from="+from+",to="+to+",rate="+rate+"]";
	}
	
}
