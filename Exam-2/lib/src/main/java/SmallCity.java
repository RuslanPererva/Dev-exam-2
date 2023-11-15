class SmallCity extends City {
	
	public SmallCity (String n, int p) {
		super(n, p);
		if (p > 250000) {
			throw new IllegalArgumentException ("Population not in range (0,250000) [" + p + "]");
		}
	}

	@Override
	public void accept(IRegionVisitor visitor) {
		visitor.visit(this);
	}

}
