package tests;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/*******
 * <p> Title: AdjustableClock Class </p>
 *
 * <p> Description: A clock for tests that stands still until a test moves it forward.  The
 * failed-attempt delay in TP3 Aspect 5 is measured in time, so the tests need to place the clock
 * exactly one second before, at, and after the end of a delay without waiting in real time.
 * This class satisfies the "controllable clock" that TP3 Testing Requirements.pdf (section 2.3)
 * asks the implementation to support.</p>
 *
 * <p> Copyright: Rohan Arora © 2026 </p>
 *
 * @author Rohan Arora
 *
 * @version 1.00	2026-11-09 Initial version for the Task 5 risk reduction prototype
 */
public class AdjustableClock extends Clock {

	/** The instant this clock reports until a test moves it. */
	private Instant now;

	/** The time zone reported by this clock; the limiter only uses instants, so UTC is enough. */
	private final ZoneId zone;

	/*****
	 * <p> Method: AdjustableClock(Instant start) </p>
	 *
	 * <p> Description: Creates a clock that reports the given instant in UTC.</p>
	 *
	 * @param start the instant the clock reports until it is advanced
	 */
	public AdjustableClock(Instant start) {
		this(start, ZoneOffset.UTC);
	}

	/*****
	 * <p> Method: AdjustableClock(Instant start, ZoneId zone) </p>
	 *
	 * <p> Description: Creates a clock that reports the given instant in the given zone.  Used
	 * by withZone so the copy keeps the same instant.</p>
	 *
	 * @param start the instant the clock reports until it is advanced
	 *
	 * @param zone the time zone this clock reports
	 */
	private AdjustableClock(Instant start, ZoneId zone) {
		this.now = start;
		this.zone = zone;
	}

	/*****
	 * <p> Method: void advance(Duration amount) </p>
	 *
	 * <p> Description: Moves the clock forward, which is how a test simulates time passing.</p>
	 *
	 * @param amount how far to move the clock; must not be negative
	 */
	public void advance(Duration amount) {
		// Time never runs backwards in the real system, so a test should not be able to do it either
		if (amount.isNegative())
			throw new IllegalArgumentException("A clock can only be moved forward");
		now = now.plus(amount);
	}

	/*****
	 * <p> Method: Instant instant() </p>
	 *
	 * <p> Description: Returns the instant this clock is currently set to.</p>
	 *
	 * @return the current instant of this clock
	 */
	@Override
	public Instant instant() {
		return now;
	}

	/*****
	 * <p> Method: ZoneId getZone() </p>
	 *
	 * <p> Description: Returns the time zone of this clock.</p>
	 *
	 * @return the time zone of this clock
	 */
	@Override
	public ZoneId getZone() {
		return zone;
	}

	/*****
	 * <p> Method: Clock withZone(ZoneId newZone) </p>
	 *
	 * <p> Description: Returns a copy of this clock in another time zone, as java.time.Clock
	 * requires.  The copy starts at the same instant but is advanced separately.</p>
	 *
	 * @param newZone the time zone for the copy
	 *
	 * @return a new clock at the same instant in the given zone
	 */
	@Override
	public Clock withZone(ZoneId newZone) {
		return new AdjustableClock(now, newZone);
	}
}
