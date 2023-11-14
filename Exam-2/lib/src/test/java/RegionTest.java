import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;

class RegionTest {
	private static final Class<?> CITY                     = City                  .class;
	private static final Class<?> COUNT_REGION_VISITOR     = CountRegionVisitor    .class;
	private static final Class<?> IREGION_VISITOR          = IRegionVisitor        .class;
	private static final Class<?> LARGE_CITY               = LargeCity             .class;
	private static final Class<?> MAP                      = Map                   .class;
	private static final Class<?> POPULATION_RANGE_VISITOR = PopulationRangeVisitor.class;
	private static final Class<?> REGION                   = Region                .class;
	private static final Class<?> SMALL_CITY               = SmallCity             .class;
	private static final Class<?> STATE                    = State                 .class;
	private static final Class<?> OBJECT                   = Object                .class;

	@Test
	void testNoStaticNonPrivateFields() {
		Consumer<Class<?>> testFields = c -> Arrays.stream( c.getDeclaredFields() ).filter( f->!f.isSynthetic() ).forEach( f->{
			var mods = f.getModifiers();
			var name = f.getName();
			Truth.assertWithMessage( String.format("field '%s' is not private", name )).that( Modifier.isPrivate( mods )).isTrue();
			Truth.assertWithMessage( String.format("field '%s' is static",      name )).that( Modifier.isStatic ( mods )).isFalse();
		});
		testFields.accept( CITY );
		testFields.accept( COUNT_REGION_VISITOR );
		testFields.accept( IREGION_VISITOR );
		testFields.accept( LARGE_CITY );
		testFields.accept( MAP );
		testFields.accept( POPULATION_RANGE_VISITOR );
		testFields.accept( REGION );
		testFields.accept( SMALL_CITY );
		testFields.accept( STATE );
	}
	@Test
	void testClassesHaveSuper() {
		Consumer<Class<?>> isInterface    = a -> Truth.assertWithMessage( String.format("type '%s' is not an interface", a.getName() ))
				                                      .that( a.isInterface() ).isTrue();
		Consumer<Class<?>> isAbstract     = a -> Truth.assertWithMessage( String.format("class '%s' is not abstract", a.getName() ))
				                                      .that( Modifier.isAbstract( a.getModifiers() )).isTrue();
		BiConsumer<Class<?>,Class<?>> 
		                   isSuperTypeOf  = (a,b) -> Truth.assertWithMessage( String.format( "'%s' is not the supertype of '%s'", a.getSimpleName(), b.getSimpleName() ))
		                                                .that( a.isAssignableFrom( b )).isTrue();
		BiConsumer<Class<?>,Class<?>> 
		                   isSuperClassOf = (a,b) -> Truth.assertWithMessage( String.format( "'%s' is not the superclass of '%s'", a.getSimpleName(), b.getSimpleName() ))
		                                                .that( b.getSuperclass() ).isEqualTo( a );
		isInterface   .accept( IREGION_VISITOR );

		isAbstract    .accept( REGION );
		isAbstract    .accept( CITY   );
		
		isSuperTypeOf .accept( IREGION_VISITOR, COUNT_REGION_VISITOR     );
		isSuperTypeOf .accept( IREGION_VISITOR, POPULATION_RANGE_VISITOR );

		isSuperClassOf.accept( OBJECT, REGION                   );
		isSuperClassOf.accept( REGION, STATE                    );
		isSuperClassOf.accept( REGION, CITY                     );
		isSuperClassOf.accept( OBJECT, MAP                      );
		isSuperClassOf.accept( OBJECT, COUNT_REGION_VISITOR     );
		isSuperClassOf.accept( OBJECT, POPULATION_RANGE_VISITOR );
		isSuperClassOf.accept( CITY,   SMALL_CITY               );
		isSuperClassOf.accept( CITY,   LARGE_CITY               );
	}
	@Test
	void testClassesHaveFields() {
		ObjLongConsumer<Class<?>> hasFields = (c,expected) -> {
			var actual = Arrays.stream ( c.getDeclaredFields() )
				                          .filter ( f->!f.isSynthetic() )
				                          .collect( Collectors.counting() );
			Truth.assertWithMessage( String.format( "Incorrect number of fields in class '%s'", c.getSimpleName() ))
			     .that( actual ).isEqualTo( expected );
		};
		hasFields.accept( REGION,     1L );
		hasFields.accept( STATE,      1L );
		hasFields.accept( CITY,       1L );
		hasFields.accept( SMALL_CITY, 0L );
		hasFields.accept( LARGE_CITY, 0L );
	}
	private static final String CITY_1 = "St. Petersburg";
	private static final String CITY_2 = "Orlando";
	private static final String CITY_3 = "Baton Rouge";
	private static final String CITY_4 = "Winston-Salem";
	private static final String CITY_5 = "San Francisco";

	private static final String STATE_1 = "VA";
	private static final String STATE_2 = "PA";
	private static final String STATE_3 = "NY";
	private static final String STATE_4 = "TX";
	
	private static final String ERROR_NO_CALL = "this method should not get called";

	private static final int    SMALL_LOWER = 0;
	private static final int    SMALL_UPPER = 250_000;
	private static final int    LARGE_LOWER = 250_001;
	private static final int    LARGE_UPPER = Integer.MAX_VALUE;
	@Nested
	class TestRegion {
		@Test
		void testNewRegion() {
			for (String expected : List.of( CITY_1, CITY_2, CITY_3 )) {
				var a = new Region( expected ) {
					@Override
					public void accept(IRegionVisitor visitor) {
						throw new IllegalAccessError( ERROR_NO_CALL );
					}
				};
				Truth.assertThat( a.getName() ).isEqualTo( expected );
			}			
		}
		@Test
		void testBlankOrNullNameThrowsException() {
			for (var name : new String[]{ null,""," ","   ","         " }) {
				var t = assertThrows(
						IllegalArgumentException.class,
						() -> new Region( name ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}
						});
				Truth.assertThat( t.getMessage() )
				     .isEqualTo( "Name cannot be null nor blank" );
			}
		}		
	}
	@Nested
	class TestCity {
		@Test
		void testNewCity() {
			for (var name : List.of( CITY_1, CITY_2, CITY_4 ) ) {
				for (var pop : List.of( SMALL_LOWER, 42, LARGE_UPPER )) {
					var a = new City( name, pop ) {
						@Override
						public void accept(IRegionVisitor visitor) {
							throw new IllegalAccessError( ERROR_NO_CALL );
						}
					};
					Truth.assertThat( a.getName()       ).isEqualTo( name );
					Truth.assertThat( a.getPopulation() ).isEqualTo( pop  );
				}
			}
		}
		@Test
		void testNegativePopulationThrowsException() {
			for (int pop : List.of( -1, -42, Integer.MIN_VALUE )) {
				var t = assertThrows(
						IllegalArgumentException.class,
						() -> new City( CITY_1, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				Truth.assertThat( t.getMessage() )
				     .isEqualTo( String.format( "Population cannot be negative [%d]", pop ));
			}
		}
		@Test
		void testHashcode() {
			int actual;
			int expected;
			var a    = new SmallCity( CITY_1, SMALL_LOWER );
			expected = Objects.hash ( CITY_1, SMALL_LOWER ); 
			actual   = a.hashCode();
			Truth.assertThat( actual ).isEqualTo( expected );
			
			var b    = new LargeCity( CITY_2, LARGE_UPPER );
			expected = Objects.hash ( CITY_2, LARGE_UPPER ); 
			actual   = b.hashCode();
			Truth.assertThat( actual ).isEqualTo( expected );
			
			var c    = new Region( CITY_3 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			expected = Objects.hash ( CITY_3 ); 
			actual   = c.hashCode();
			Truth.assertThat( actual ).isEqualTo( expected );
		}
		@Test
		void testEquals() {
			/* Region */
			var x = new Region( CITY_1 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			Truth.assertThat( x.equals( null )).isFalse(); // null
			Truth.assertThat( x.equals( x    )).isTrue();  // reflexivity
			
			/* City */
			var y = new Region( CITY_2 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					throw new IllegalAccessError( ERROR_NO_CALL );
				}				
			};
			Truth.assertThat( y.equals( null )).isFalse(); // null
			Truth.assertThat( y.equals( y    )).isTrue();  // reflexivity

			/* Small + Large */
			var a = new SmallCity( CITY_1, SMALL_LOWER );
			Truth.assertThat( a.equals( null )).isFalse(); // null
			Truth.assertThat( a.equals( a    )).isTrue();  // reflexivity
			
			var b = new SmallCity( String.valueOf( CITY_1.toCharArray()), SMALL_LOWER ); 
			Truth.assertThat( a.equals( b    )).isTrue();  // symmetry: equal state
			Truth.assertThat( b.equals( a    )).isTrue();  // 

			var c = new SmallCity( CITY_1, SMALL_UPPER ); 
			Truth.assertThat( a.equals( c    )).isFalse(); // symmetry: == name, != population
			Truth.assertThat( c.equals( a    )).isFalse(); // 

			var d = new SmallCity( CITY_2, SMALL_LOWER );
			Truth.assertThat( a.equals( d    )).isFalse(); // symmetry: != name, == population
			Truth.assertThat( d.equals( a    )).isFalse(); // 

			var e = new LargeCity( CITY_3, LARGE_LOWER );
			var f = new LargeCity( CITY_4, LARGE_UPPER );
			Truth.assertThat( e.equals( f    )).isFalse(); // symmetry: != name, != population
			Truth.assertThat( f.equals( e    )).isFalse(); // 

			var g = new LargeCity( CITY_3, LARGE_LOWER ) {};
			Truth.assertThat( e.equals( g    )).isFalse(); // symmetry: subtype

			var h = new LargeCity( CITY_1, LARGE_LOWER );
			var i = new LargeCity( CITY_2, LARGE_LOWER );
			var j = new LargeCity( CITY_1, LARGE_UPPER );
			Truth.assertThat( h.equals( i    )).isFalse(); // transitivity
			Truth.assertThat( i.equals( j    )).isFalse(); // 
		}
	}
	@Nested
	class TestSmallCity {
		@Test
		void testNewSmallCity() {
			for (int expected : List.of( SMALL_LOWER, 100_000, SMALL_UPPER )) {
				var a      = new SmallCity( CITY_2, expected );
				int actual = a.getPopulation();
				Truth.assertThat( actual ).isEqualTo( expected ); 
			}
		}
		@Test
		void testVisitor() {
			var       v = Mockito.spy( new IRegionVisitor() {} );
			SmallCity a = new SmallCity( CITY_1, 100_000 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testPopulationNotInRangeThrowsException() {
			for (int pop : List.of( LARGE_LOWER, LARGE_UPPER )) {
				var t = assertThrows( IllegalArgumentException.class,
						() -> new SmallCity( CITY_2, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				Truth.assertThat( t.getMessage() )
				     .isEqualTo ( String.format( "Population not in range (%d,%d) [%d]", SMALL_LOWER, SMALL_UPPER, pop )); 
			}
		}
	}
	@Nested
	class TestLargeCity {
		@Test
		void testNewLargeCity() {
			for (int expected : List.of( LARGE_LOWER, 500_000, LARGE_UPPER )) {
				var a      = new LargeCity( CITY_2, expected );
				int actual = a.getPopulation();
				Truth.assertThat( actual ).isEqualTo( expected ); 
			}
		}
		@Test
		void testVisitor() {
			var       v = Mockito.spy( new IRegionVisitor() {} );
			LargeCity a = new LargeCity( CITY_1, 500_000 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testPopulationNotInRangeThrowsException() {
			for (int pop : List.of( SMALL_LOWER, SMALL_UPPER )) {
				var t = assertThrows( IllegalArgumentException.class,
						() -> new LargeCity( CITY_2, pop ) {
							@Override
							public void accept(IRegionVisitor visitor) {
								throw new IllegalAccessError( ERROR_NO_CALL );
							}							
						});
				Truth.assertThat( t.getMessage() )
			         .isEqualTo ( String.format( "Population not in range (%d,%d) [%d]", LARGE_LOWER, LARGE_UPPER, pop )); 
			}
		}
	}
	@Nested
	class TestState {
		@Test
		void testState() {
			var expected = STATE_1;
			var a        = new State( expected );
			var actual   = a.getName();
			Truth.assertThat( actual ).isEqualTo( expected ); 
		}
		@Test
		void testAddNullCityThrowsException() {
			State a = new State( STATE_4 );
			var   t = assertThrows( 
					IllegalArgumentException.class,
					() -> a.addCities( null, null ));
			Truth.assertThat( t.getMessage() )
	             .isEqualTo ( "Cities cannot be null" ); 
		}
		@Test
		void testVisitorWithNoCities() {
			var   v = Mockito.mock( IRegionVisitor.class );
			State a = new State( STATE_2 );
			a.accept( v );
			Mockito.verify( v ).visit( a );
		}
		@Test
		void testVisitorWithCities() {
			var        v = Mockito.mock( IRegionVisitor.class );
			State      s = new State    ( STATE_3 );
			SmallCity  a = new SmallCity( CITY_1, SMALL_LOWER );
			SmallCity  b = new SmallCity( CITY_2, SMALL_UPPER );
			LargeCity  c = new LargeCity( CITY_3, LARGE_UPPER );
			s.addCities( a, b, c );
			s.accept( v );
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
		}
	}
	@Nested
	class TestMap {
		@Test
		void testAddNullRegionThrowsException() {
			Map a = new Map();
			var t = assertThrows( 
					IllegalArgumentException.class,
					() -> a.addRegions( null, null ));
			Truth.assertThat( t.getMessage() )
                 .isEqualTo ( "Regions cannot be null" );
		}
		@Test
		void testVisitorWithNoCities() {
			var v = Mockito.mock( IRegionVisitor.class );
			Map a = new Map();
			a.traverse( v );
			Mockito.verifyNoInteractions( v );
		}
		@Test
		void testVisitorWithRegions() {
			var       v = Mockito.mock( IRegionVisitor.class );
			Map       m = new Map();
			State     s = Mockito.spy( new State( STATE_4 ));
			SmallCity a = Mockito.spy( new SmallCity( CITY_1, SMALL_LOWER ) );
			SmallCity b = Mockito.spy( new SmallCity( CITY_2, SMALL_UPPER ));
			LargeCity c = Mockito.spy( new LargeCity( CITY_3, LARGE_UPPER ));
			s.addCities ( b );
			m.addRegions( a, s, c );
			m.traverse  ( v );
			Mockito.verify( a ).accept( v );
			Mockito.verify( b ).accept( v );
			Mockito.verify( c ).accept( v );
			Mockito.verify( s ).accept( v );
			
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
		}
	}
	@Nested
	class TestCountRegionVisitor {
		@Test
		void testNoRegions() {
			var v = Mockito.mock( CountRegionVisitor.class );
			var m = new Map();
			m.traverse( v );
			
			Mockito.verifyNoInteractions( v );
			
			Truth.assertThat( v.getStates() ).isEqualTo( 0 );
			Truth.assertThat( v.getCities() ).isEqualTo( 0 );
		}
		@Test
		void testOneStateNoCities() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State( STATE_1 );
			m.addRegions( s );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getStates() ).isEqualTo( 1 );
			Truth.assertThat( v.getCities() ).isEqualTo( 0 );
		}
		@Test
		void testNoStateOneCity() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var a = new SmallCity( CITY_1, 42_000 );
			m.addRegions( a );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getStates() ).isEqualTo( 0 );
			Truth.assertThat( v.getCities() ).isEqualTo( 1 );
		}
		@Test
		void testStateAndCity() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			s.addCities ( a );
			m.addRegions( s );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getStates() ).isEqualTo( 1 );
			Truth.assertThat( v.getCities() ).isEqualTo( 1 );
		}
		@Test
		void testStatesAndCities() {
			var v = Mockito.spy( new CountRegionVisitor() );
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			var e = new City     ( CITY_5,  77_000 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}
			};
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s, e );
			m.addRegions( z );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verify( v ).visit( e );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getStates() ).isEqualTo( 2 );
			Truth.assertThat( v.getCities() ).isEqualTo( 4 );
		}
	}
	@Nested
	class TestPopulationRangeVisitor {
		@Test
		void testNegativeOrIncorrectRangeThrowsException() {
			Throwable t;
			// negative
			t = assertThrows( 
					IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( Integer.MIN_VALUE, Integer.MAX_VALUE ));
			Truth.assertThat( t.getMessage() ).isEqualTo ( "Range must have positive values" );

			t = assertThrows( 
					IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 42, -1 ));
			Truth.assertThat( t.getMessage() ).isEqualTo ( "Range must have positive values" );

			// lower > upper
			t = assertThrows( 
					IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 42, 41 ));
			Truth.assertThat( t.getMessage() ).isEqualTo ( "Range must be incremental" );

			t = assertThrows( 
					IllegalArgumentException.class,
					() -> new PopulationRangeVisitor( 11, 7 ));
			Truth.assertThat( t.getMessage() ).isEqualTo ( "Range must be incremental" );
		}
		@Test
		void testNoRegions() {
			var v = Mockito.spy( new PopulationRangeVisitor( 11, 41 ));
			var m = new Map();
			m.traverse( v );
			
			Mockito.verifyNoInteractions( v );

			Truth.assertThat( v.getCities() ).isEmpty();
		}
		@Test
		void testOneStateNoCities() {
			var v = Mockito.spy( new PopulationRangeVisitor( 100_000, 500_000 ));
			var m = new Map();
			var s = new State( STATE_1 );
			m.addRegions( s );
			m.traverse  ( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getCities() ).isEmpty();
		}
		@Test
		void testStateAndCityOneInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 42_000, 42_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			var b = new City     ( CITY_2, 42_000 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}				
			};
			s.addCities ( a );
			m.addRegions( s, b );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getCities() ).containsExactly( a );
		}
		@Test
		void testStateAndCityNoneInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 45_000, 50_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var a = new SmallCity( CITY_1, 42_000 );
			s.addCities ( a );
			m.addRegions( s );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( a );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getCities() ).isEmpty();
		}
		@Test
		void testStatesAndCitiesTwoInRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 100_000, 500_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s );
			m.addRegions( z );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getCities() ).containsExactly( b, c );
		}
		@Test
		void testStatesAndCitiesAllRange() {
			var v = Mockito.spy( new PopulationRangeVisitor( 0, 500_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1,  42_000 );
			var b = new SmallCity( CITY_2, 142_000 );
			var c = new LargeCity( CITY_3, 420_000 );
			var d = new SmallCity( CITY_4,   4_200 );
			var e = new Region   ( CITY_5 ) {
				@Override
				public void accept(IRegionVisitor visitor) {
					visitor.visit( this );
				}				
			};
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s );
			m.addRegions( z, e );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verify( v ).visit( e );
			Mockito.verifyNoMoreInteractions( v );
			
			Truth.assertThat( v.getCities() ).containsExactly( a, b, c, d );
		}
		@Test
		void testReturnedListOfCitiesIsCopyOfOriginal() {
			var v = Mockito.spy( new PopulationRangeVisitor( 100_000, 500_000 ));
			var m = new Map();
			var s = new State    ( STATE_1 );
			var z = new State    ( STATE_2 );
			var a = new SmallCity( CITY_1, 123_456 );
			var b = new SmallCity( CITY_2,  99_999 );
			var c = new LargeCity( CITY_3, 500_001 );
			var d = new LargeCity( CITY_4, 456_789 );
			s.addCities ( a, c );
			z.addCities ( d );
			m.addRegions( b, s );
			m.addRegions( z );
			m.traverse( v );
			
			Mockito.verify( v ).visit( s );
			Mockito.verify( v ).visit( z );
			Mockito.verify( v ).visit( a );
			Mockito.verify( v ).visit( b );
			Mockito.verify( v ).visit( c );
			Mockito.verify( v ).visit( d );
			Mockito.verifyNoMoreInteractions( v );
			
			var one = v.getCities();
			Truth.assertThat( one ).containsExactly( a, d );
			var two = v.getCities();
			Truth.assertThat( two ).isNotSameInstanceAs( one );
		}
	}
}