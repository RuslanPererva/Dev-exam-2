import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class GalaTest {
    private static final Class<?> GALA   = Gala       .class;
    private static final Class<?> ADD    = Gala.Add   .class;
    private static final Class<?> DELETE = Gala.Delete.class;
    private static final Class<?> RSVP   = Gala.RSVP  .class;
    
	@Test
	void testFieldsArePrivateNonStatic() {
		Consumer<Class<?>> allFieldsPrivate = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' must be private", name ))
					.that( Modifier.isPrivate( mod ))
					.isTrue();
				});
		Consumer<Class<?>> noFieldsStatic  = c -> Arrays.stream (c.getDeclaredFields())
				.filter (f->!f.isSynthetic())
				.forEach(f->{
					int mod  = f.getModifiers();
					var name = f.getName();
					Truth.assertWithMessage( String.format( "field '%s' cannot be static",  name ))
					.that( Modifier.isStatic( mod ))
					.isFalse();
				});
		allFieldsPrivate.accept( GALA   );
		allFieldsPrivate.accept( ADD    );
		allFieldsPrivate.accept( DELETE );
		allFieldsPrivate.accept( RSVP   );

		noFieldsStatic  .accept( GALA   );
		noFieldsStatic  .accept( ADD    );
		noFieldsStatic  .accept( DELETE );
		noFieldsStatic  .accept( RSVP   );
	}
	@Test
	void testNewGala() {
		var gala = new Gala();
		var a    = new Guest("Brad Pitt");
		
		Truth.assertThat( gala.hasGuest( a )).isFalse();

		Truth.assertThat( gala.getGuests()  ).isEmpty();
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).isEmpty();

		Truth.assertThat( gala.undoSize() ).isEqualTo( 0 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testOneGuest() {
		var gala = new Gala();
		var a    = new Guest("Carlos Santana");

		gala.execute( gala.new Add( a ));

		Truth.assertThat( gala.hasGuest( a )).isTrue();

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).containsExactly( a );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 1 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testOneRSPV() {
		var gala = new Gala();
		var a    = new Guest("Ric Ocasek");
		var b    = new Guest("John Bonham");

		gala.execute( gala.new Add ( a ));
		gala.execute( gala.new RSVP( a, true ));

		Truth.assertThat( gala.hasGuest( a )).isTrue();
		Truth.assertThat( gala.hasGuest( b )).isFalse();

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).containsExactly( a );
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testOneDelete() {
		var gala = new Gala();
		var a    = new Guest("Eddie Van Halen");

		// add
		gala.execute( gala.new Add( a ));

		Truth.assertThat( gala.hasGuest( a )).isTrue();

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).containsExactly( a );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 1 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		

		// delete
		gala.execute( gala.new Delete( a ));

		Truth.assertThat( gala.hasGuest( a )).isFalse();

		Truth.assertThat( gala.getGuests()  ).isEmpty();
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).isEmpty();

		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testRedoUndoOneGuest() {
		var gala = new Gala();
		var a    = new Guest("Paul McCartney");

		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new RSVP  ( a, true ));
		gala.execute( gala.new Delete( a ));

		Truth.assertThat( gala.hasGuest( a )).isFalse();

		Truth.assertThat( gala.getGuests()  ).isEmpty();
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 3 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		

		// undo (from deleted to RSVPed)
		gala.undo();

		Truth.assertThat( gala.hasGuest( a )).isTrue();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 1 );		

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).containsExactly( a );

		// undo (from RSVPed to invited)
		gala.undo();

		Truth.assertThat( gala.hasGuest( a )).isTrue();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 1 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 2 );		

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).containsExactly( a );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();

		// undo (from invited to not)
		gala.undo();

		Truth.assertThat( gala.hasGuest( a )).isFalse();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 0 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 3 );		

		Truth.assertThat( gala.getGuests()  ).isEmpty();
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).isEmpty();

		// redo (from not to invited, to RSVPed)
		gala.redo();
		gala.redo();

		Truth.assertThat( gala.hasGuest( a )).isTrue();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 1 );		

		Truth.assertThat( gala.getGuests()  ).containsExactly( a );
		Truth.assertThat( gala.getPending() ).isEmpty();
		Truth.assertThat( gala.getRSVP()    ).containsExactly( a );
	}
	@Test
	void testRedoUndoExecute() {
		var gala = new Gala();
		var a    = new Guest("Shakira");
		var b    = new Guest("Maradona");
		var c    = new Guest("Pele");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));

		Truth.assertThat( gala.hasGuest( a )).isTrue();
		Truth.assertThat( gala.hasGuest( b )).isTrue();
		Truth.assertThat( gala.hasGuest( c )).isFalse();
		
		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b );
		Truth.assertThat( gala.getPending() ).containsExactly( a, b );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		

		gala.undo();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 1 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 1 );		

		gala.execute( gala.new Add( c ));

		Truth.assertThat( gala.getGuests()  ).containsExactly( a, c );
		Truth.assertThat( gala.getPending() ).containsExactly( a, c );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testUndoRedoSeveralGuests() {
		var gala = new Gala();
		var a    = new Guest("Cristian Castro");
		var b    = new Guest("Paul McCartney");
		var c    = new Guest("Ricardo Arjona");
		var d    = new Guest("David Grohl");

		// add a, b(RSPV), c
		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new Add   ( b ));
		gala.execute( gala.new RSVP  ( b, true ));
		gala.execute( gala.new Add   ( c ));
		gala.execute( gala.new RSVP  ( c, true ));
		gala.execute( gala.new Add   ( d ));
		gala.execute( gala.new RSVP  ( c, false ));

		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b, c, d );
		Truth.assertThat( gala.getPending() ).containsExactly( a,    c, d );
		Truth.assertThat( gala.getRSVP()    ).containsExactly(    b       );
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 7 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		

		// undo 3 times
		gala.undo();
		gala.undo();
		gala.undo();
		
		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b, c );
		Truth.assertThat( gala.getPending() ).containsExactly( a,    c );
		Truth.assertThat( gala.getRSVP()    ).containsExactly(    b    );
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 4 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 3 );		

		// undo 2 times
		gala.undo();
		gala.undo();
		
		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b );
		Truth.assertThat( gala.getPending() ).containsExactly( a, b );
		Truth.assertThat( gala.getRSVP()    ).isEmpty();
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 2 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 5 );		

		// execute command
		gala.execute( gala.new RSVP( a, true ));
		
		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b );
		Truth.assertThat( gala.getPending() ).containsExactly(    b );
		Truth.assertThat( gala.getRSVP()    ).containsExactly( a    );
		
		Truth.assertThat( gala.undoSize() ).isEqualTo( 3 );
		Truth.assertThat( gala.redoSize() ).isEqualTo( 0 );		
	}
	@Test
	void testGetListsReturnCopies() {
		var gala = new Gala();
		var a    = new Guest("Jeff Porcaro");
		var b    = new Guest("John Bonham");
		var c    = new Guest("David Garibaldi");

		// add a, b(RSPV), c
		gala.execute( gala.new Add ( a ));
		gala.execute( gala.new Add ( b ));
		gala.execute( gala.new RSVP( a, true ));
		gala.execute( gala.new Add ( c ));
		gala.execute( gala.new RSVP( c, true ));

		gala.getGuests() .clear();
		gala.getPending().clear();
		gala.getRSVP()   .clear();

		Truth.assertThat( gala.getGuests()  ).containsExactly( a, b, c );
		Truth.assertThat( gala.getPending() ).containsExactly(    b    );
		Truth.assertThat( gala.getRSVP()    ).containsExactly( a,    c );
	}
	@Test
	void testExceptionAddExisting() {
		var gala = new Gala();
		var a    = new Guest("Geddy Lee");
		var b    = new Guest("Neil Peart");
		var c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		gala.execute( gala.new Add( c ));
		
		var add = gala.new Add ( b );
		var t   = assertThrows( 
				IllegalStateException.class,
				() -> gala.execute( add ));
		Truth.assertThat( t.getMessage() ).isEqualTo( "guest exists already" );
	}
	@Test
	void testExceptionDeleteNonExisting() {
		var gala = new Gala();
		var a    = new Guest("Geddy Lee");
		var b    = new Guest("Neil Peart");
		var c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		
		var del = gala.new Delete( c );
		var t   = assertThrows( 
				IllegalStateException.class,
				() -> gala.execute( del ));
		Truth.assertThat( t.getMessage() ).isEqualTo( "guest doesn't exist" );
	}
	@Test
	void testExceptionRSVPNonExisting() {
		var gala = new Gala();
		var a    = new Guest("Geddy Lee");
		var b    = new Guest("Neil Peart");
		var c    = new Guest("Alex Lifeson");

		gala.execute( gala.new Add   ( a ));
		gala.execute( gala.new Add   ( b ));
		gala.execute( gala.new Delete( a ));
		gala.execute( gala.new Add   ( c ));

		var       rsvp = gala.new RSVP( a, true );
		Throwable t    = assertThrows( IllegalStateException.class,
				() -> gala.execute( rsvp ));
		Truth.assertThat( t.getMessage() ).isEqualTo( "guest doesn't exist" );
	}
	@Test
	void testExceptionUndo() {
		var gala = new Gala();
		var a    = new Guest("Billy Gibbons");
		var b    = new Guest("Frank Beard");
		var c    = new Guest("Dusty Hill");

		gala.execute( gala.new Add( a ));
		gala.execute( gala.new Add( b ));
		gala.execute( gala.new Add( c ));
		
		gala.undo();
		gala.undo();
		gala.undo();

		var t = assertThrows( 
				IndexOutOfBoundsException.class,
				gala::undo );
		Truth.assertThat( t.getMessage() )
		     .isEqualTo ( "Index -1 out of bounds for length 0" );
	}
	@Test
	void testExceptionRedo() {
		var gala = new Gala();
		var t    = assertThrows( 
				IndexOutOfBoundsException.class,
				gala::redo );
		Truth.assertThat( t.getMessage() )
		     .isEqualTo ( "Index -1 out of bounds for length 0" );
	}
}