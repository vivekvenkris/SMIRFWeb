package bean;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2017-05-03T20:07:16.533+1000")
@StaticMetamodel(ObservingSession.class)
public class ObservingSession_ {
	public static volatile SingularAttribute<ObservingSession, Integer> sessionID;
	public static volatile SingularAttribute<ObservingSession, String> startUTC;
	public static volatile SingularAttribute<ObservingSession, Integer> pointingTobs;
	public static volatile SingularAttribute<ObservingSession, Integer> sessionDuration;
	public static volatile SingularAttribute<ObservingSession, Boolean> mdMajor;
	public static volatile SingularAttribute<ObservingSession, Boolean> phaseCalibrateAtStart;
	public static volatile SingularAttribute<ObservingSession, Boolean> fluxCalibrateAtStart;
	public static volatile SingularAttribute<ObservingSession, Boolean> fluxCalibrateWhenever;
	public static volatile SingularAttribute<ObservingSession, Integer> numPlannedPointings;
	public static volatile SingularAttribute<ObservingSession, Integer> numPointingsDone;
	public static volatile SingularAttribute<ObservingSession, String> observer;
}
