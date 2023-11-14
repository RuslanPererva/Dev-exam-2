import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class GuestTest {
    private static final Class<?> GUEST = Guest.class;

	@Test
	void testAllFieldsPrivateNonStatic() {
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
		allFieldsPrivate.accept( GUEST );
		noFieldsStatic  .accept( GUEST );
	}
	@Test
	void testNameNullOrBlankThrowsException() {
		for(String name : new String[] { null, ""," ","   ","     ","                           "}) {
			var e = assertThrows( 
					IllegalArgumentException.class, 
					() -> new Guest( name ));
			Truth.assertThat( e.getMessage() )
			.isEqualTo( "name cannot be null or blank" );
		}
	}
	@Test
	void testNewObject() {
		var a = new Guest( "Helen" );
		Truth.assertThat( a.getName() ).isEqualTo( "Helen" );  
		Truth.assertThat( a.hasRSVP() ).isFalse();
	}
	@Test
	void testRSVP() {
		var a = new Guest( "Bob" );
		Truth.assertThat( a.hasRSVP() ).isFalse();
		a.setRSVP(true);
		Truth.assertThat( a.hasRSVP() ).isTrue();
		a.setRSVP(false);
		Truth.assertThat( a.hasRSVP() ).isFalse();
	}

	@Nested
	class TestEquals {
		@Test
		void testToNull() {
			var one = new Guest( "Evan" );
			Truth.assertThat( one.equals( null )).isFalse();
		}
		@Test
		void testReflectivity() {
			var one = new Guest( "Foo" );
			Truth.assertThat( one.equals( one )).isTrue();
		}
		@Test
		void testSymmetryEqualState() {
			var one = new Guest( "Bar" );
			var two = new Guest( new String( new char[]{'B','a','r'}));
			Truth.assertThat( one.equals( two )).isTrue();
		}
		@Test
		void testSymmetryDifferentState() {
			var one = new Guest( "Ximena" );
			var two = new Guest( "Yoyo"   );
			Truth.assertThat( one.equals( two )).isFalse();
		}
		@Test
		void testSymmetryDifferentType() {
			var one = new Guest( "Jude" );
			Truth.assertThat( one.equals( "Jude" )).isFalse();
			Truth.assertThat( one.equals( 42     )).isFalse();
		}
		@Test
		void testSymmetrySubclass() {
			var one = new Guest( "Kelvin" );
			var two = new Guest( "Kelvin" ) { };
			Truth.assertThat( one.equals( two )).isFalse();
		}
	}
	
	@Nested
	class TestHashCode {
		@Test
		void testNewObject() {
			for (String name : List.of( "Lola", "Manuel", "Neville", "Oscar" )) {
				int expected  = Objects.hash ( name );
				int actual    = new Guest( name ).hashCode();
				Truth.assertThat( actual ).isEqualTo( expected );
			}
		}
	}
	
	@Nested
	class TestToString {
		@Test
		void test0() {
			var expected = "Guest [name=Pedro,rsvp=no]";
			var actual   = new Guest( "Pedro" ).toString();
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		void test1() {
			Guest a     = new Guest( "Quincy" );
			a.setRSVP(true);
			var expected = "Guest [name=Quincy,rsvp=yes]";
			var actual   = a.toString();
			Truth.assertThat( actual ).isEqualTo( expected );
		}
	}
}