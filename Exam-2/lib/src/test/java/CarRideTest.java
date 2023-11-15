import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class CarRideTest {
	private static final Consumer<Class<?>> fieldsNotStatic = 
			c -> Arrays.stream  ( c.getDeclaredFields() )
			           .filter  ( f->!f.isSynthetic() )
			           .forEach ( f->assertFalse( Modifier.isStatic ( f.getModifiers() ), 
			        		                      () -> "field '%s.%s' is static"     .formatted( c.getSimpleName(), f.getName() )));
	private static final Consumer<Class<?>> fieldsPrivate = 
			c -> Arrays.stream  ( c.getDeclaredFields() )
			           .filter  ( f->!f.isSynthetic() )
			           .forEach ( f->assertTrue ( Modifier.isPrivate( f.getModifiers() ), 
			        		                      () -> "field '%s.%s' is not private".formatted( c.getSimpleName(), f.getName() )));
	public static final Consumer<Class<?>> fieldsPublic = 
			c -> Arrays.stream  ( c.getDeclaredFields() )
			           .filter  ( f->!f.isSynthetic() )
			           .forEach ( f->assertTrue ( Modifier.isPublic( f.getModifiers() ), 
					                              () -> "field '%s.%s' is not public".formatted( c.getSimpleName(), f.getName() )));
	public static final Consumer<Class<?>> fieldsFinal = 
			c -> Arrays.stream  ( c.getDeclaredFields() )
			           .filter  ( f->!f.isSynthetic() )
			           .forEach ( f->assertTrue ( Modifier.isFinal( f.getModifiers() ), 
			        		                      () -> "field '%s.%s' is not final" .formatted( c.getSimpleName(), f.getName() )));
	private static final BiConsumer<Class<?>,List<String>> hasPublicMethods = 
			(c,expected) -> {
				var actual = Arrays.stream ( c.getDeclaredMethods() )
                                              .filter ( m -> Modifier.isPublic( m.getModifiers() )) 
                                              .map    ( Method::getName )
										      .filter ( n -> !n.contains("$"))
										      .collect( Collectors.toList() );
				assertThat( actual ).containsExactlyElementsIn( expected );
			};
	private static final Consumer<Class<?>> hasPrivateConstructor = 
			c -> Truth.assertWithMessage( String.format( "'%s' doesn't have a private constructor", c.getSimpleName() ))
	                  .that( Arrays.stream( c.getDeclaredConstructors() )
		               		       .filter( m -> Modifier.isPrivate( m.getModifiers() ))
		               		       .count() )
	                  .isAtLeast( 1 );
	private static final Class<?> CLASS   = CarRide        .class;
	private static final Class<?> BUILDER = CarRide.Builder.class;
	@Nested
	class NonFunctionalTesting {
		@Test
		void testClass() {
			fieldsNotStatic      .accept( CLASS );
			fieldsPrivate        .accept( CLASS );
			hasPrivateConstructor.accept( CLASS );
			hasPublicMethods     .accept( CLASS, List.of( "toString" ));
		}
		@Test
		void testBuilder() {
			fieldsNotStatic.accept( BUILDER );
			fieldsPrivate  .accept( BUILDER );

			assertTrue  ( Modifier.isPublic( BUILDER.getModifiers() ), () -> "class '%s' is not public".formatted( BUILDER.getSimpleName() ));
			assertTrue  ( Modifier.isStatic( BUILDER.getModifiers() ), () -> "class '%s' is not static".formatted( BUILDER.getSimpleName() ));

			Constructor<?>[] con = BUILDER.getDeclaredConstructors();
			assertEquals( 1, con.length, "unexpected number of constructors" );
			assertTrue  ( Modifier.isPublic( con[0].getModifiers() ), () -> String.format( "constructor in class '%s' is not public", BUILDER.getSimpleName() ));

			hasPublicMethods.accept( BUILDER, List.of( "build","from","to","rate","isValid" ));
		}
	}
	@Nested
	class FunctionalTesting {
		@Test
		void testNewBuilder() {
			var builder = new CarRide.Builder();
			
			var valid   = builder.isValid();
			assertThat( valid ).isFalse();
		}
		@Test
		void testBuilderIsValid() {
			var builder = new CarRide.Builder();
			var valid   = builder.isValid();
			assertThat( valid ).isFalse();
			
			var from    = builder.from( "hampton" );
			assertThat( from  ).isSameInstanceAs( builder );
			valid       = builder.isValid();
			assertThat( valid ).isFalse();
			
			var to      = builder.to  ( "norfolk" );
			assertThat( to    ).isSameInstanceAs( builder );
			valid       = builder.isValid();
			assertThat( valid ).isFalse();
						
			var rate    = builder.rate( 42 );
			assertThat( rate  ).isSameInstanceAs( builder );
			valid       = builder.isValid();
			assertThat( valid ).isTrue();
		}
		@Test
		void testNewCarRide() {
			{ // one
			var builder = new CarRide.Builder();
			builder.from( "hampton" );
			builder.to  ( "newport news" );
			builder.rate( 30 );
			
			var car = builder.build();
			var str = car.toString();
			assertThat( str ).isEqualTo( "CarRide[from=hampton,to=newport news,rate=30]" );
			}{// two
			var builder = new CarRide.Builder();
			builder.from( "suffolk" );
			builder.to  ( "chesapeake" );
			builder.rate( 42 );
			
			var car = builder.build();
			assertThat( car ).isNotNull();
			var str = car.toString();
			assertThat( str ).isEqualTo( "CarRide[from=suffolk,to=chesapeake,rate=42]" );
			}
		}
		@Test
		void testCarRideBuiltTwiceThrowsException() {
			var builder = new CarRide.Builder();
			builder.rate( 121 );
			builder.to  ( "williamsburg" );
			builder.from( "virginia beach" );
			
			var car = builder.build();
			assertThat( car ).isNotNull();
			var t = assertThrows( 
					IllegalStateException.class, 
					builder::build );
			assertThat( t.getMessage() ).isEqualTo( "CarRide already built" );
		}
		@Test
		void testSettersWithInvalidParametersThrowExceptions() {
			var builder = new CarRide.Builder();
			// from
			var t = assertThrows( 
					IllegalArgumentException.class, 
					() -> builder.from( null ));
			assertThat( t.getMessage() ).isEqualTo( "from cannot be null" );

			// to
			t = assertThrows( 
					IllegalArgumentException.class, 
					() -> builder.to  ( null ));
			assertThat( t.getMessage() ).isEqualTo( "to cannot be null" );

			// rate
			for (var input : new int[]{ Integer.MIN_VALUE, -1 }) {
				t = assertThrows( 
						IllegalArgumentException.class, 
						() -> builder.rate( input ));
				assertThat( t.getMessage() ).isEqualTo( "rate cannot be negative" );
			}
		}
		@Test
		void testBuildWithMissingValuesThrowExceptions() {
			var builder = new CarRide.Builder();
			// none
			var t = assertThrows( 
					IllegalStateException.class, 
					builder::build );
			var m = t.getMessage();
			assertThat( m ).contains( "missing data" );
			assertThat( m ).contains( "from" );
			assertThat( m ).contains( "to"   );
			assertThat( m ).contains( "rate" );

			// from
			builder.from( "newport news" );
			t = assertThrows( 
					IllegalStateException.class, 
					builder::build );
			m = t.getMessage();
			assertThat( m ).contains      ( "missing data" );
			assertThat( m ).doesNotContain( "from" );
			assertThat( m ).contains      ( "to"   );
			assertThat( m ).contains      ( "rate" );

			// to
			builder.to  ( "yorktown" );
			t = assertThrows( 
					IllegalStateException.class, 
					builder::build );
			m = t.getMessage();
			assertThat( m ).contains      ( "missing data" );
			assertThat( m ).doesNotContain( "from" );
			assertThat( m ).doesNotContain( "to"   );
			assertThat( m ).contains      ( "rate" );

			// rate
			builder.rate( 42 );

			builder.build();
		}
		@Test
		void testConcurrentBuilders() {
			var one   = new CarRide.Builder();
			var two   = new CarRide.Builder();
			var three = new CarRide.Builder();
			
			var uno   = one  .from( "williamsburg" ).to( "poquoson"       ).rate( 15 );
			assertThat( uno ).isSameInstanceAs( one );
			
			var dos   = two  .from( "newport news" ).to( "hampton"        ).rate( 25 );
			assertThat( dos ).isSameInstanceAs( two );
			
			var tres  = three.from( "norfolk"      ).to( "virginia beach" ).rate(  0 );
			assertThat( tres ).isSameInstanceAs( three );
			
			var ride1 = one  .build();
			var ride2 = two  .build();
			var ride3 = three.build();
			
			var str1  = ride1.toString();
			var str2  = ride2.toString();
			var str3  = ride3.toString();

			assertThat( str1 ).isEqualTo( "CarRide[from=williamsburg,to=poquoson,rate=15]" );
			assertThat( str2 ).isEqualTo( "CarRide[from=newport news,to=hampton,rate=25]" );
			assertThat( str3 ).isEqualTo( "CarRide[from=norfolk,to=virginia beach,rate=0]" );
		}
	}
}