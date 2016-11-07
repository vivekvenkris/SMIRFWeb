package bean;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2016-10-31T14:14:18.397+1100")
@StaticMetamodel(TiedBeamSource.class)
public class TiedBeamSource_ {
	public static volatile SingularAttribute<TiedBeamSource, Integer> sourceID;
	public static volatile SingularAttribute<TiedBeamSource, String> sourceName;
	public static volatile SingularAttribute<TiedBeamSource, Boolean> knownPSR;
	public static volatile SingularAttribute<TiedBeamSource, Double> period;
	public static volatile SingularAttribute<TiedBeamSource, Double> dispersionMeasure;
	public static volatile SingularAttribute<TiedBeamSource, Double> acceleration;
	public static volatile SingularAttribute<TiedBeamSource, Double> SNR5;
	public static volatile SingularAttribute<TiedBeamSource, Double> SNR15;
}
