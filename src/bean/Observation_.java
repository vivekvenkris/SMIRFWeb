package bean;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-04-15T18:48:01.760+1000")
@StaticMetamodel(Observation.class)
public class Observation_ {
	public static volatile SingularAttribute<Observation, Integer> observationID;
	public static volatile SingularAttribute<Observation, String> sourceName;
	public static volatile SingularAttribute<Observation, String> utc;
	public static volatile SingularAttribute<Observation, Integer> tobs;
	public static volatile SingularAttribute<Observation, ObservingSession> observingSession;
	public static volatile SingularAttribute<Observation, String> tiedBeamSources;
	public static volatile SingularAttribute<Observation, String> observationType;
	public static volatile SingularAttribute<Observation, Boolean> complete;
	public static volatile SingularAttribute<Observation, Integer> managementStatus;
}
