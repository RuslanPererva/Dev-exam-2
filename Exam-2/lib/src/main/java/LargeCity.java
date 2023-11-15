
public class LargeCity extends City {
		
		public LargeCity (String n, int p) {
			super(n, p);
			if (p <= 250000) {
				throw new IllegalArgumentException ("Population not in range (250001," + Integer.MAX_VALUE +") [" + p + "]");
			}
		}

		@Override
		public void accept(IRegionVisitor visitor) {
			visitor.visit(this);
		}

}
