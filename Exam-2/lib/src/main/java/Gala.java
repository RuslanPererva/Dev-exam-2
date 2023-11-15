import java.util.*;
class Gala {
	private final Stack<Command> undo;
	private final Stack<Command> redo;
	private final List<Guest> guestList;
	
	public Gala() {
		this.undo=new Stack<>();
		this.redo=new Stack<>();
		this.guestList=new ArrayList<>();
	}
	
	public void execute (Command c) {
		c.execute();
		undo.push(c);
		redo.clear();
	}
	
	public void undo() {
		if (undo.isEmpty() != true) {
		Command temp = undo.pop();
		temp.undo();
		redo.push(temp);
	}
		else {
			throw new IndexOutOfBoundsException("Index -1 out of bounds for length 0");
		}
	}
	public void redo() {
		if (redo.isEmpty() != true) {
		Command temp = redo.pop();
		temp.execute();
		undo.push(temp);
	}
		else {
			throw new IndexOutOfBoundsException("Index -1 out of bounds for length 0");
		}
	}
	
	public int undoSize() {
		return this.undo.getSize();
	}
	public int redoSize() {
		return this.redo.getSize();
	}
	
	public boolean hasGuest (Guest g) {
		return guestList.contains(g);
	}
	public List<Guest> getGuests(){
		List<Guest> temp = new ArrayList<>();
		for (Guest g:guestList) {
			temp.add(g);
		}
		return temp;
	}
	
	public List<Guest> getPending(){
		List<Guest> temp = new ArrayList<>();
		for (Guest g:guestList) {
			if (g.hasRSVP()==false) {
			temp.add(g);
			}
		}
		return temp;
	}
	
	public List<Guest> getRSVP(){
		List<Guest> temp = new ArrayList<>();
		for (Guest g:guestList) {
			if (g.hasRSVP()) {
			temp.add(g);
			}
		}
		return temp;
	}
	
	
	final class Add implements Command{
		private Guest guest;
		
		public Add (Guest g) {
			this.guest=g;
		}

		@Override
		public Command execute() {
			if (guestList.contains(guest)) {
				throw new IllegalStateException("guest exists already");
			}
			else {
				guestList.add(guest);
			}
			return this;
		}

		@Override
		public Command undo() {
			if (guestList.contains(guest)) {
				guestList.remove(guest);
			}
			return this;
		}
		
	}
	
	final class Delete implements Command{
private Guest guest;
		
		public Delete (Guest g) {
			this.guest=g;
		}

		@Override
		public Command execute() {
			if (guestList.contains(guest)==false) {
				throw new IllegalStateException("guest doesn't exist");
			}
			else {
				guestList.remove(guest);
			}
			return this;
		}

		@Override
		public Command undo() {
			if (guestList.contains(guest)==false) {
				guestList.add(guest);
			}
			return this;
		}
	}
	final class RSVP implements Command {
		private Guest guest;
		private boolean isRSVP;
		
		public RSVP (Guest g, boolean RSVP) {
			this.guest=g;
			this.isRSVP=RSVP;
		}

		@Override
		public Command execute() {
			if (guestList.contains(guest)==false) {
				throw new IllegalStateException("guest doesn't exist");
			}
			guest.setRSVP(isRSVP);
			return this;
		}

		@Override
		public Command undo() {
			guest.setRSVP(!isRSVP);
			return this;
		}
		
	}
}
