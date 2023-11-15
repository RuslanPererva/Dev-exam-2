
public interface IRegionVisitor {
    default void visit(Region region) {}
    default void visit(City city) {}
    default void visit(SmallCity smallCity) {}
    default void visit(LargeCity largeCity) {}
    default void visit(State state) {}
}
