package org.matsim.episim;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.matsim.core.config.ReflectiveConfigGroup;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Config option specific to contact tracing and measures performed in {@link org.matsim.episim.model.ProgressionModel}.
 */
public class TracingConfigGroup extends ReflectiveConfigGroup {

	private static final Splitter.MapSplitter SPLITTER = Splitter.on(";").withKeyValueSeparator("=");
	private static final Joiner.MapJoiner JOINER = Joiner.on(";").withKeyValueSeparator("=");

	private static final String PUT_TRACEABLE_PERSONS_IN_QUARANTINE = "putTraceablePersonsInQuarantineAfterDay";
	private static final String TRACING_DAYS_DISTANCE = "tracingDaysDistance";
	private static final String TRACING_PROBABILITY = "tracingProbability";
	private static final String TRACING_DELAY = "tracingDelay";
	private static final String MIN_DURATION = "minDuration";
	private static final String QUARANTINE_HOUSEHOLD = "quarantineHousehold";
	private static final String TRACE_SUSCEPTIBLE = "traceSusceptible";
	private static final String EQUIPMENT_RATE = "equipmentRate";
	private static final String CAPACITY = "tracingCapacity";
	private static final String CAPACITY_TYPE = "capacityType";
	private static final String STRATEGY = "strategy";
	private static final String LOCATION_THRESHOLD = "locationThreshold";
	private static final String GROUPNAME = "episimTracing";

	/**
	 * Amount of persons traceable der day.
	 */
	private final Map<LocalDate, Integer> tracingCapacity = new TreeMap<>();
	/**
	 * Probability of successfully tracing a person.
	 */
	private final Map<LocalDate, Double> tracingProbability = new TreeMap<>();
	/**
	 * Day after which tracing starts and puts persons into quarantine.
	 */
	private int putTraceablePersonsInQuarantineAfterDay = Integer.MAX_VALUE;
	/**
	 * How many days the tracing works back.
	 */
	private int tracingDayDistance = 4;
	/**
	 * Amount of days after the person showing symptoms.
	 */
	private int tracingDelay = 0;
	/**
	 * Probability that a person is equipped with a tracing device.
	 */
	private double equipmentRate = 1.0;

	/**
	 * Minimum duration in seconds for a contact to be relevant for tracing.
	 */
	private double minDuration = 0.0;

	/**
	 * Members of the same household will be put always into quarantine.
	 */
	private boolean quarantineHouseholdMembers = false;

	/**
	 * Trace contacts between two susceptible persons. (Uses a lot more RAM)
	 */
	private boolean traceSusceptible = true;

	/**
	 * Defines if the capacity is either per (infected) person or per contact person.
	 */
	private CapacityType capacityType = CapacityType.PER_PERSON;

	/**
	 * Tracing and containment strategy.
	 */
	private Strategy strategy = Strategy.INDIVIDUAL_ONLY;

	/**
	 * How many infections are required for location based tracing to trigger.
	 */
	private int locationThreshold = 4;

	/**
	 * Default constructor.
	 */
	public TracingConfigGroup() {
		super(GROUPNAME);
	}

	@StringGetter(PUT_TRACEABLE_PERSONS_IN_QUARANTINE)
	public int getPutTraceablePersonsInQuarantineAfterDay() {
		return putTraceablePersonsInQuarantineAfterDay;
	}

	@StringSetter(PUT_TRACEABLE_PERSONS_IN_QUARANTINE)
	public void setPutTraceablePersonsInQuarantineAfterDay(int putTraceablePersonsInQuarantineAfterDay) {
		// yyyy change argument to date.  kai, jun'20
		this.putTraceablePersonsInQuarantineAfterDay = putTraceablePersonsInQuarantineAfterDay;
	}

	@StringGetter(TRACING_DAYS_DISTANCE)
	public int getTracingDayDistance() {
		return tracingDayDistance;
	}

	@StringSetter(TRACING_DAYS_DISTANCE)
	public void setTracingPeriod_days(int tracingDayDistance) {
		this.tracingDayDistance = tracingDayDistance;
	}

	@StringGetter(TRACING_DELAY)
	public int getTracingDelay() {
		return tracingDelay;
	}

	@StringSetter(TRACING_DELAY)
	public void setTracingDelay_days(int tracingDelay) {
		this.tracingDelay = tracingDelay;
	}

	@StringSetter(TRACING_PROBABILITY)
	void setTracingProbability(String capacity) {
		Map<String, String> map = SPLITTER.split(capacity);
		setTracingProbability(map.entrySet().stream().collect(Collectors.toMap(
				e -> LocalDate.parse(e.getKey()), e -> Double.parseDouble(e.getValue())
		)));
	}

	@StringGetter(TRACING_PROBABILITY)
	public String getTracingProbabilityString() {
		return JOINER.join(tracingProbability);
	}

	public void setTracingProbability(Map<LocalDate, Double> tracingProbability) {
		this.tracingProbability.clear();
		this.tracingProbability.putAll(tracingProbability);
	}

	/**
	 * Sets one tracing probability valid throughout whole simulation.
	 */
	public void setTracingProbability(double tracingProbability) {
		setTracingProbability(Map.of(LocalDate.of(1970,1,1), tracingProbability));
	}

	public Map<LocalDate, Double> getTracingProbability() {
		return tracingProbability;
	}

	/**
	 * Sets the tracing capacity for the whole simulation period.
	 *
	 * @param capacity number of persons to trace per day.
	 * @see #setTracingCapacity_pers_per_day(Map)
	 */
	public void setTracingCapacity_pers_per_day(int capacity) {
		setTracingCapacity_pers_per_day(Map.of(LocalDate.of(1970, 1, 1), capacity));
	}

	/**
	 * Sets the tracing capacity for individual days. If a day has no entry the previous will be still valid.
	 *
	 * @param capacity map of dates to changes in capacity.
	 */
	public void setTracingCapacity_pers_per_day(Map<LocalDate, Integer> capacity) {
		tracingCapacity.clear();
		tracingCapacity.putAll(capacity);
	}

	public Map<LocalDate, Integer> getTracingCapacity() {
		return tracingCapacity;
	}

	@StringSetter(CAPACITY)
	void setTracingCapacity(String capacity) {

		Map<String, String> map = SPLITTER.split(capacity);
		setTracingCapacity_pers_per_day(map.entrySet().stream().collect(Collectors.toMap(
				e -> LocalDate.parse(e.getKey()), e -> Integer.parseInt(e.getValue())
		)));
	}

	@StringGetter(CAPACITY)
	String getTracingCapacityString() {
		return JOINER.join(tracingCapacity);
	}

	@StringGetter(EQUIPMENT_RATE)
	public double getEquipmentRate() {
		return equipmentRate;
	}

	@StringSetter(EQUIPMENT_RATE)
	public void setEquipmentRate(double equipmentRate) {
		this.equipmentRate = equipmentRate;
	}

	@StringGetter(MIN_DURATION)
	public double getMinDuration() {
		return minDuration;
	}

	@StringSetter(MIN_DURATION)
	public void setMinContactDuration_sec(double minDuration) {
		this.minDuration = minDuration;
	}

	@StringSetter(QUARANTINE_HOUSEHOLD)
	public void setQuarantineHouseholdMembers(boolean quarantineHouseholdMembers) {
		this.quarantineHouseholdMembers = quarantineHouseholdMembers;
	}

	@StringGetter(QUARANTINE_HOUSEHOLD)
	public boolean getQuarantineHousehold() {
		return quarantineHouseholdMembers;
	}

	@StringGetter(TRACE_SUSCEPTIBLE)
	public boolean getTraceSusceptible() {
		return traceSusceptible;
	}

	@StringSetter(TRACE_SUSCEPTIBLE)
	public void setTraceSusceptible(boolean traceSusceptible) {
		this.traceSusceptible = traceSusceptible;
	}

	@StringGetter(CAPACITY_TYPE)
	public CapacityType getCapacityType() {
		return capacityType;
	}

	@StringSetter(CAPACITY_TYPE)
	public void setCapacityType(CapacityType capacityType) {
		this.capacityType = capacityType;
	}

	@StringGetter(STRATEGY)
	public Strategy getStrategy() {
		return strategy;
	}

	@StringSetter(STRATEGY)
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	@StringGetter(LOCATION_THRESHOLD)
	public int getLocationThreshold() {
		return locationThreshold;
	}

	@StringSetter(LOCATION_THRESHOLD)
	public void setLocationThreshold(int locationThreshold) {
		this.locationThreshold = locationThreshold;
	}

	public enum CapacityType {PER_PERSON, PER_CONTACT_PERSON}

	public enum Strategy {

		/**
		 * Trace contacts of individual persons.
		 */
		INDIVIDUAL_ONLY,

		/**
		 * Trace contacts of all persons that got infected at specific location.
		 */
		LOCATION,
		/**
		 * Trace and test all contacts for persons that have been at specific location.
		 */
		LOCATION_WITH_TESTING
	}

}
