package bean;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-04-16T17:14:37.191+1000")
@StaticMetamodel(Birdie.class)
public class Birdie_ {
	public static volatile SingularAttribute<Birdie, Integer> birdieID;
	public static volatile SingularAttribute<Birdie, Double> period;
	public static volatile SingularAttribute<Birdie, Integer> numObsSeen;
	public static volatile SingularAttribute<Birdie, Integer> avgNumFBsSeenIn;
	public static volatile SingularAttribute<Birdie, Boolean> inBirdiesList;
}
