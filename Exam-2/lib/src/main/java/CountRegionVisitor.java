
public class CountRegionVisitor implements IRegionVisitor{
	private int countState;
	private int countCity;
	
	public CountRegionVisitor() {
		this.countCity=0;
		this.countCity=0;
	}
	@Override
	public void visit (State s) {
		countState++;
	}
	
	@Override
	public void visit (SmallCity c) {
		countCity++;
	}
	
	@Override
	public void visit (LargeCity c) {
		countCity++;
	}
	public int getStates() {
		return countState;
	}
	public int getCities() {
		return countCity;
	}
	
	
}
