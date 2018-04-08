package bean;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-02-08T16:16:19.457+1100")
@StaticMetamodel(Pointing.class)
public class Pointing_ {
	public static volatile SingularAttribute<Pointing, Integer> pointingID;
	public static volatile SingularAttribute<Pointing, String> pointingName;
	public static volatile SingularAttribute<Pointing, Integer> priority;
	public static volatile SingularAttribute<Pointing, String> type;
	public static volatile SingularAttribute<Pointing, Integer> numObs;
	public static volatile SingularAttribute<Pointing, Integer> leastCadanceInDays;
	public static volatile SingularAttribute<Pointing, String> associatedPulsars;
	public static volatile SingularAttribute<Pointing, Integer> tobs;
}
