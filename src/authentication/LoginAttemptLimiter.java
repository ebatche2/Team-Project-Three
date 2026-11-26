package authentication;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/*******
 * <p> Title: LoginAttemptLimiter Class </p>
 *
 * <p> Description: Limits repeated failed logins for TP3 Aspect 5 (Staff log in securely and
 * play the right role, A07 Authentication Failures).  After N failed logins in a row for one
 * username, that username is delayed for D: the login page must refuse it, without checking the
 * password, until the delay is over.  This implements objective O2.2 of Initial ConOps.pdf and
 * requirement TR-5 of TP3 Testing Requirements.pdf.</p>
 *
 * <p> The rules are:</p>
 * <ul>
 * <li>Only consecutive failures count; a successful login clears the count.</li>
 * <li>The Nth failure starts the delay.  The delay covers the half-open interval
 * [start, start + D), so the username is allowed again exactly at start + D.</li>
 * <li>Failures recorded during a delay are ignored, so the delay can never be extended.  This
 * guarantees that the only admin can always log in once D has passed (scenario S8).</li>
 * <li>When a delay ends, the username starts again with a fresh set of N attempts.</li>
 * <li>Every username is treated the same whether or not an account exists, so the delay cannot
 * be used to discover which usernames are real.  Usernames are compared exactly, as the
 * database does, and null is treated as the empty string.</li>
 * </ul>
 *
 * <p> Time is read from a java.time.Clock passed to the constructor, so tests can control it.
 * The limit and delay are named settings because Aspect 2 (Ronald) will set the final values.</p>
 *
 * <p> The counts are kept in memory, which suits the single-user desktop application.  Whether
 * they should survive an application restart is an open question recorded in Which Aspect and
 * Why.pdf.</p>
 *
 * <p> Copyright: Rohan Arora © 2026 </p>
 *
 * @author Rohan Arora
 *
 * @version 1.00	2026-11-09 Risk reduction prototype for HW3 Task 5
 */
public class LoginAttemptLimiter {

	/** The limit N used by the no-argument constructor until Aspect 2 settles the value. */
	public static final int DEFAULT_MAX_FAILED_ATTEMPTS = 5;

	/** The delay D used by the no-argument constructor until Aspect 2 settles the value. */
	public static final Duration DEFAULT_DELAY = Duration.ofSeconds(30);

	/** The number of consecutive failures, N, that starts a delay. */
	private final int maxFailedAttempts;

	/** How long a delay lasts, D. */
	private final Duration delay;

	/** The source of the current time. */
	private final Clock clock;

	/** What is known about each username that has failed at least once and not yet succeeded. */
	private final Map<String, AttemptRecord> records = new HashMap<String, AttemptRecord>();

	/*******
	 * <p> Title: AttemptRecord Class </p>
	 *
	 * <p> Description: The state kept for one username: how many failures in a row, and when
	 * its delay ends if it is delayed.</p>
	 */
	private static class AttemptRecord {

		/** Consecutive failures since the last success or the end of the last delay. */
		int failedAttempts = 0;

		/** When the current delay ends, or null if the username is not delayed. */
		Instant delayedUntil = null;

		/*****
		 * <p> Method: AttemptRecord() </p>
		 *
		 * <p> Description: Creates a record with no failures and no delay.</p>
		 */
		AttemptRecord() {
		}
	}

	/*****
	 * <p> Method: LoginAttemptLimiter() </p>
	 *
	 * <p> Description: Creates a limiter with the default limit and delay, using the system
	 * clock.  This is the constructor the login page uses.</p>
	 */
	public LoginAttemptLimiter() {
		this(DEFAULT_MAX_FAILED_ATTEMPTS, DEFAULT_DELAY, Clock.systemUTC());
	}

	/*****
	 * <p> Method: LoginAttemptLimiter(int maxFailedAttempts, Duration delay, Clock clock) </p>
	 *
	 * <p> Description: Creates a limiter with the given settings.  Settings that would switch
	 * the limit off or make it unusable are refused here, so a mistake shows up when the
	 * application starts rather than at a login.</p>
	 *
	 * @param maxFailedAttempts the number of consecutive failures, N, that starts a delay;
	 * must be at least 1
	 *
	 * @param delay how long a delay lasts, D; must be longer than zero
	 *
	 * @param clock the source of the current time; tests pass a clock they can move forward
	 *
	 * @throws IllegalArgumentException if N is less than 1, D is missing or not positive,
	 * or the clock is missing
	 */
	public LoginAttemptLimiter(int maxFailedAttempts, Duration delay, Clock clock) {
		// N = 0 would delay every username before its first attempt
		if (maxFailedAttempts < 1)
			throw new IllegalArgumentException("The failed-attempt limit must be at least 1");

		// A zero or negative delay would mean the limit never takes effect
		if (delay == null || delay.isZero() || delay.isNegative())
			throw new IllegalArgumentException("The delay must be longer than zero");

		if (clock == null)
			throw new IllegalArgumentException("A clock is required");

		this.maxFailedAttempts = maxFailedAttempts;
		this.delay = delay;
		this.clock = clock;
	}

	/*****
	 * <p> Method: void recordFailure(String username) </p>
	 *
	 * <p> Description: Records one failed login for a username.  The Nth consecutive failure
	 * starts a delay of D.  A failure recorded while the username is already delayed is
	 * ignored, so the delay cannot be extended by repeated guessing.</p>
	 *
	 * @param username the username that was typed; null is treated as the empty string
	 */
	public void recordFailure(String username) {
		AttemptRecord record = currentRecord(key(username));

		// During a delay the password is not even checked, so this attempt does not count
		if (record.delayedUntil != null) return;

		record.failedAttempts++;

		// The Nth failure in a row starts the delay from now
		if (record.failedAttempts >= maxFailedAttempts)
			record.delayedUntil = clock.instant().plus(delay);
	}

	/*****
	 * <p> Method: void recordSuccess(String username) </p>
	 *
	 * <p> Description: Records a successful login, which clears the username's count.  The
	 * login page only calls this when the username was not delayed.</p>
	 *
	 * @param username the username that logged in; null is treated as the empty string
	 */
	public void recordSuccess(String username) {
		// Removing the record resets the count, and stops the map from growing for real users
		records.remove(key(username));
	}

	/*****
	 * <p> Method: boolean isDelayed(String username) </p>
	 *
	 * <p> Description: Tells the login page whether a username must be refused right now
	 * without checking its password.</p>
	 *
	 * @param username the username that was typed; null is treated as the empty string
	 *
	 * @return true if the username is inside a delay, otherwise false
	 */
	public boolean isDelayed(String username) {
		return !remainingDelay(username).isZero();
	}

	/*****
	 * <p> Method: Duration remainingDelay(String username) </p>
	 *
	 * <p> Description: Returns how long a username must still wait, so the login page can tell
	 * the user when to try again.</p>
	 *
	 * @param username the username that was typed; null is treated as the empty string
	 *
	 * @return the time left in the current delay, or Duration.ZERO if the username is not
	 * delayed
	 */
	public Duration remainingDelay(String username) {
		AttemptRecord record = records.get(key(username));

		// No record, no delay, or a delay that has ended all mean nothing to wait for
		if (record == null || record.delayedUntil == null || delayHasEnded(record)) return Duration.ZERO;

		return Duration.between(clock.instant(), record.delayedUntil);
	}

	/*****
	 * <p> Method: int getFailedAttempts(String username) </p>
	 *
	 * <p> Description: Returns the number of consecutive failures currently counted for a
	 * username.  After a delay has ended this is 0, because the username starts again.</p>
	 *
	 * @param username the username that was typed; null is treated as the empty string
	 *
	 * @return the number of consecutive failures being counted
	 */
	public int getFailedAttempts(String username) {
		AttemptRecord record = records.get(key(username));
		if (record == null) return 0;

		// A delay that has run out no longer counts against the username
		if (delayHasEnded(record)) return 0;

		return record.failedAttempts;
	}

	/*****
	 * <p> Method: int getMaxFailedAttempts() </p>
	 *
	 * <p> Description: Returns the limit this limiter was created with.</p>
	 *
	 * @return the number of consecutive failures, N, that starts a delay
	 */
	public int getMaxFailedAttempts() {
		return maxFailedAttempts;
	}

	/*****
	 * <p> Method: Duration getDelay() </p>
	 *
	 * <p> Description: Returns the delay this limiter was created with.</p>
	 *
	 * @return how long a delay lasts, D
	 */
	public Duration getDelay() {
		return delay;
	}

	/*****
	 * <p> Method: AttemptRecord currentRecord(String key) </p>
	 *
	 * <p> Description: Returns the record for a username, creating it if needed.  If the
	 * username's delay has ended, the record is replaced by a fresh one, which is how a
	 * username gets a new set of N attempts after a delay.</p>
	 *
	 * @param key the username, already converted by key()
	 *
	 * @return the record to update for this username
	 */
	private AttemptRecord currentRecord(String key) {
		AttemptRecord record = records.get(key);

		// A record whose delay has run out is stale, so start over with a fresh one
		if (record == null || delayHasEnded(record)) {
			record = new AttemptRecord();
			records.put(key, record);
		}
		return record;
	}

	/*****
	 * <p> Method: boolean delayHasEnded(AttemptRecord record) </p>
	 *
	 * <p> Description: Tells whether a record had a delay that is now over.  This is the one
	 * place the end of a delay is decided, so every method agrees on the boundary.</p>
	 *
	 * @param record the record to check; must not be null
	 *
	 * @return true if the record was delayed and the delay has ended, otherwise false
	 */
	private boolean delayHasEnded(AttemptRecord record) {
		// The interval is [start, start + D), so the delay is over at exactly delayedUntil
		return record.delayedUntil != null && !clock.instant().isBefore(record.delayedUntil);
	}

	/*****
	 * <p> Method: String key(String username) </p>
	 *
	 * <p> Description: Converts a username to the key used in the map.  Null becomes the empty
	 * string so a missing username is counted like an empty one and never causes a
	 * NullPointerException (CWE-476).  Usernames are otherwise kept exactly as typed, because
	 * the database compares them exactly.</p>
	 *
	 * @param username the username that was typed
	 *
	 * @return the key for this username
	 */
	private static String key(String username) {
		return username == null ? "" : username;
	}
}
