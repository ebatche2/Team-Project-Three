package tests;

import java.time.Duration;
import java.time.Instant;

import authentication.LoginAttemptLimiter;

/*******
 * <p> Title: LoginAttemptLimiterTestingAutomation Class </p>
 *
 * <p> Description: Automated tests for the failed-attempt limit of TP3 Aspect 5 (Staff log in
 * securely and play the right role).  They implement tests T-20 to T-26 of requirement TR-5 in
 * TP3 Testing Details and Rationale.pdf, plus boundary and robustness tests written with them before
 * the code existed, as test-driven development requires.  The tests follow the team's style: a main method,
 * one line of ***Success*** or ***Failure*** per check, and a count at the end.</p>
 *
 * <p> The limit N and delay D are named constants so the tests do not change when Aspect 2
 * (Ronald) settles the real values.</p>
 *
 * <p> Copyright: Rohan Arora © 2026 </p>
 *
 * @author Rohan Arora
 *
 * @version 1.00	2026-11-09 Initial version for the Task 5 risk reduction prototype
 */
public class LoginAttemptLimiterTestingAutomation {

	/** Number of checks that passed. */
	static int numPassed = 0;

	/** Number of checks that failed. */
	static int numFailed = 0;

	/** The failed-attempt limit N used by these tests. */
	static final int N = 5;

	/** The delay D used by these tests. */
	static final Duration D = Duration.ofSeconds(30);

	/** A fixed starting time, T0, so every test starts from the same moment. */
	static final Instant T0 = Instant.parse("2026-11-09T12:00:00Z");

	/** The clock shared by the limiter under test and the test that moves it. */
	static AdjustableClock clock;

	/** The limiter under test; a fresh one is made for each test case. */
	static LoginAttemptLimiter limiter;

	/*****
	 * <p> Method: LoginAttemptLimiterTestingAutomation() </p>
	 *
	 * <p> Description: Not used; the tests run from main.</p>
	 */
	public LoginAttemptLimiterTestingAutomation() {
	}

	/*****
	 * <p> Method: void main(String[] args) </p>
	 *
	 * <p> Description: Runs every test case and prints the totals.</p>
	 *
	 * @param args not used
	 */
	public static void main(String[] args) {
		testBelowLimit();
		testAtLimit();
		testDelayEnds();
		testSuccessResetsCount();
		testOtherUsernamesUnaffected();
		testUnknownUsername();
		testOnlyAdminNeverLockedOut();
		testRemainingDelay();
		testLimitOfOne();
		testNullUsername();
		testInvalidSettings();
		testDefaultSettings();

		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/*****
	 * <p> Method: void reset() </p>
	 *
	 * <p> Description: Gives each test case a fresh clock at T0 and a fresh limiter, so no test
	 * depends on what an earlier test did.</p>
	 */
	static void reset() {
		clock = new AdjustableClock(T0);
		limiter = new LoginAttemptLimiter(N, D, clock);
	}

	/*****
	 * <p> Method: void fail(String username, int times) </p>
	 *
	 * <p> Description: Records the given number of failed logins for a username.</p>
	 *
	 * @param username the username typed for each failed attempt
	 *
	 * @param times how many failures to record
	 */
	static void fail(String username, int times) {
		for (int i = 0; i < times; i++)
			limiter.recordFailure(username);
	}

	/*****
	 * <p> Method: void testBelowLimit() </p>
	 *
	 * <p> Description: T-20.  N-1 failures, one below the limit, must not cause a delay, and the
	 * correct password then logs in and clears the count.</p>
	 */
	static void testBelowLimit() {
		reset();
		fail("ztRole1", N - 1);

		// One below the limit: the boundary on the allowed side
		check("T-20", "N-1 failures do not delay the username", !limiter.isDelayed("ztRole1"));
		check("T-20", "the failure count is N-1", limiter.getFailedAttempts("ztRole1") == N - 1);

		// The controller calls recordSuccess when the correct password is entered
		limiter.recordSuccess("ztRole1");
		check("T-20", "a successful login clears the count", limiter.getFailedAttempts("ztRole1") == 0);
	}

	/*****
	 * <p> Method: void testAtLimit() </p>
	 *
	 * <p> Description: T-21.  N failures, exactly at the limit, must delay the username straight
	 * away and for the whole of D, so even the correct password is not checked.</p>
	 */
	static void testAtLimit() {
		reset();
		fail("ztRole1", N);

		// Exactly at the limit: the delay starts at once
		check("T-21", "N failures delay the username", limiter.isDelayed("ztRole1"));

		// Still inside the delay one second before it ends
		clock.advance(D.minusSeconds(1));
		check("T-21", "the username is still delayed at D - 1 second", limiter.isDelayed("ztRole1"));
	}

	/*****
	 * <p> Method: void testDelayEnds() </p>
	 *
	 * <p> Description: T-22.  The delay ends exactly at D, after which the username is allowed
	 * again with a fresh set of N attempts.</p>
	 */
	static void testDelayEnds() {
		reset();
		fail("ztRole1", N);

		// The boundary value itself: the delay covers [start, start + D), so at D it is over
		clock.advance(D);
		check("T-22", "the delay is over exactly at D", !limiter.isDelayed("ztRole1"));
		check("T-22", "the count starts over after the delay", limiter.getFailedAttempts("ztRole1") == 0);

		// A fresh set of attempts: N-1 more failures still do not delay
		fail("ztRole1", N - 1);
		check("T-22", "N-1 failures after the delay do not delay again", !limiter.isDelayed("ztRole1"));

		// ...but the Nth does, so the limit keeps working after the first delay
		fail("ztRole1", 1);
		check("T-22", "the Nth failure after the delay delays again", limiter.isDelayed("ztRole1"));
	}

	/*****
	 * <p> Method: void testSuccessResetsCount() </p>
	 *
	 * <p> Description: T-23.  Only consecutive failures count: N-1 failures, a success, and N-1
	 * more failures must not cause a delay even though there were 2N-2 failures in total.</p>
	 */
	static void testSuccessResetsCount() {
		reset();
		fail("ztRole1", N - 1);
		limiter.recordSuccess("ztRole1");
		fail("ztRole1", N - 1);

		// 2N-2 failures in total, but never N in a row
		check("T-23", "failures separated by a success do not add up", !limiter.isDelayed("ztRole1"));
	}

	/*****
	 * <p> Method: void testOtherUsernamesUnaffected() </p>
	 *
	 * <p> Description: T-24.  The delay belongs to one username, so another staff member can
	 * still log in while the first is delayed.</p>
	 */
	static void testOtherUsernamesUnaffected() {
		reset();
		fail("ztRole1", N);

		check("T-24", "ztRole1 is delayed", limiter.isDelayed("ztRole1"));
		check("T-24", "ztRole2 is not delayed", !limiter.isDelayed("ztRole2"));

		// Usernames are case-sensitive in the database, so the limiter keeps them apart too
		check("T-24", "ZTROLE1 is a different username and is not delayed", !limiter.isDelayed("ZTROLE1"));
	}

	/*****
	 * <p> Method: void testUnknownUsername() </p>
	 *
	 * <p> Description: T-25.  A username with no account is limited exactly like a real one, so
	 * the delay message cannot be used to find out which usernames exist.</p>
	 */
	static void testUnknownUsername() {
		reset();

		// The limiter never looks up the account, so it cannot treat the two differently
		fail("ztNobody", N);
		check("T-25", "an unknown username is delayed after N failures", limiter.isDelayed("ztNobody"));
		check("T-25", "its remaining delay is the full D", limiter.remainingDelay("ztNobody").equals(D));
	}

	/*****
	 * <p> Method: void testOnlyAdminNeverLockedOut() </p>
	 *
	 * <p> Description: T-26.  Even if someone keeps trying the only admin's username during the
	 * delay, the delay must not grow, so the admin can always log in once D has passed.</p>
	 */
	static void testOnlyAdminNeverLockedOut() {
		reset();
		fail("ztAdmin", N);

		// Someone keeps guessing during the delay, every 5 seconds
		for (int i = 0; i < 5; i++) {
			clock.advance(Duration.ofSeconds(5));
			limiter.recordFailure("ztAdmin");
		}

		// 25 seconds have passed; the delay still ends 30 seconds after it started
		check("T-26", "failures during the delay do not extend it",
				limiter.remainingDelay("ztAdmin").equals(D.minusSeconds(25)));

		clock.advance(D.minusSeconds(25));
		check("T-26", "the admin is allowed again once D has passed", !limiter.isDelayed("ztAdmin"));
	}

	/*****
	 * <p> Method: void testRemainingDelay() </p>
	 *
	 * <p> Description: Beyond the Task 4.2 list.  The login page needs to tell the user how
	 * long to wait, so remainingDelay must count down and be zero when there is no delay.</p>
	 */
	static void testRemainingDelay() {
		reset();
		check("extra", "no delay before any failure", limiter.remainingDelay("ztRole1").isZero());

		fail("ztRole1", N);
		check("extra", "the remaining delay starts at D", limiter.remainingDelay("ztRole1").equals(D));

		clock.advance(Duration.ofSeconds(10));
		check("extra", "the remaining delay counts down", limiter.remainingDelay("ztRole1").equals(D.minusSeconds(10)));

		clock.advance(D);
		check("extra", "the remaining delay is zero after the delay", limiter.remainingDelay("ztRole1").isZero());
	}

	/*****
	 * <p> Method: void testLimitOfOne() </p>
	 *
	 * <p> Description: Beyond the Task 4.2 list.  The smallest allowed limit is 1, so a single
	 * failure must start the delay.  This is the lower boundary of N.</p>
	 */
	static void testLimitOfOne() {
		clock = new AdjustableClock(T0);
		limiter = new LoginAttemptLimiter(1, D, clock);

		limiter.recordFailure("ztRole1");
		check("extra", "with N = 1, one failure delays the username", limiter.isDelayed("ztRole1"));
	}

	/*****
	 * <p> Method: void testNullUsername() </p>
	 *
	 * <p> Description: Beyond the Task 4.2 list (CWE-476).  The login page can pass an empty or
	 * missing username, and the limiter must treat null like the empty string, never throw.</p>
	 */
	static void testNullUsername() {
		reset();
		Throwable thrown = null;
		try {
			// Every public method, called with null
			fail(null, N);
			limiter.isDelayed(null);
			limiter.remainingDelay(null);
			limiter.getFailedAttempts(null);
			limiter.recordSuccess(null);
		} catch (Throwable t) {
			thrown = t;
		}
		check("extra", "a null username never causes an exception", thrown == null);

		// null and "" are the same username, so failures for one count for the other
		reset();
		fail(null, N - 1);
		fail("", 1);
		check("extra", "null and \"\" share one count", limiter.isDelayed(""));
	}

	/*****
	 * <p> Method: void testInvalidSettings() </p>
	 *
	 * <p> Description: Beyond the Task 4.2 list.  Settings that would switch the limit off or
	 * break it are refused when the limiter is created, rather than failing later at login.</p>
	 */
	static void testInvalidSettings() {
		AdjustableClock c = new AdjustableClock(T0);

		// N = 0 is just below the smallest allowed limit
		check("extra", "a limit of 0 is refused", refused(() -> new LoginAttemptLimiter(0, D, c)));
		check("extra", "a zero delay is refused", refused(() -> new LoginAttemptLimiter(N, Duration.ZERO, c)));
		check("extra", "a negative delay is refused", refused(() -> new LoginAttemptLimiter(N, D.negated(), c)));
		check("extra", "a missing delay is refused", refused(() -> new LoginAttemptLimiter(N, null, c)));
		check("extra", "a missing clock is refused", refused(() -> new LoginAttemptLimiter(N, D, null)));
	}

	/*****
	 * <p> Method: void testDefaultSettings() </p>
	 *
	 * <p> Description: Beyond the Task 4.2 list.  The no-argument constructor must use the
	 * named default settings so the login page can use it before Aspect 2 settles the values.</p>
	 */
	static void testDefaultSettings() {
		LoginAttemptLimiter defaults = new LoginAttemptLimiter();
		check("extra", "the default limit is the named setting",
				defaults.getMaxFailedAttempts() == LoginAttemptLimiter.DEFAULT_MAX_FAILED_ATTEMPTS);
		check("extra", "the default delay is the named setting",
				defaults.getDelay().equals(LoginAttemptLimiter.DEFAULT_DELAY));
	}

	/*****
	 * <p> Method: boolean refused(Runnable action) </p>
	 *
	 * <p> Description: Runs an action and reports whether it threw IllegalArgumentException.</p>
	 *
	 * @param action the action expected to be refused
	 *
	 * @return true if the action threw IllegalArgumentException, otherwise false
	 */
	static boolean refused(Runnable action) {
		try {
			action.run();
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	/*****
	 * <p> Method: void check(String testCase, String what, boolean passed) </p>
	 *
	 * <p> Description: Prints the result of one check and updates the counts.</p>
	 *
	 * @param testCase the test ID from TP3 Testing Details and Rationale.pdf, or "extra"
	 *
	 * @param what a short description of what was checked
	 *
	 * @param passed whether the check passed
	 */
	static void check(String testCase, String what, boolean passed) {
		if (passed) {
			System.out.println("***Success*** " + testCase + ": " + what);
			numPassed++;
		} else {
			System.out.println("***Failure*** " + testCase + ": " + what);
			numFailed++;
		}
	}
}
