package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import limits.AIRequestLimiter;

/*******
 * <p> Title: AIRequestLimiterTest Class </p>
 *
 * <p> Description: JUnit tests for the AIRequestLimiter prototype (HW3 Task 5).  The tests are
 * A1 to A11 from "TP3 Testing Details and Rationale.pdf", and together they cover Requirement 1
 * of "TP3 Testing Requirements.pdf": each user can make at most 20 AI requests in any 60-minute
 * period, with a clear message when the limit is reached (CWE-770). </p>
 *
 * <p> These tests were written before the AIRequestLimiter class (test-driven development).
 * Each test passes the time in as a number instead of using the real clock, so the tests run
 * instantly, give the same result every time, and can check exact boundaries like one second
 * before the hour is up. </p>
 *
 * <p> How to read the results: JUnit shows a green check for each test that passed.  A test
 * passes when the limiter returns exactly the expected text: "" means the request was allowed,
 * and any other text is the message the user would see. </p>
 *
 * <p> Copyright: Ronald Yalung © 2026 </p>
 *
 * @author Ronald Yalung
 *
 * @version 1.00	2026-11-07 HW3 Task 5
 */
class AIRequestLimiterTest {

	/** The message shown when the default limit of 20 is reached, without the wait time. */
	static final String LIMIT_REACHED = "You have used 20 of 20 AI requests this hour. Try again in ";

	/*****
	 * <p> Method: long minute(int m) </p>
	 *
	 * <p> Description: Turns minutes after the start of the test into the millisecond time the
	 * limiter uses.  This keeps the tests readable: minute(20) instead of 1200000. </p>
	 *
	 * @param m is the number of minutes
	 *
	 * @return that many minutes in milliseconds
	 */
	static long minute(int m) { return m * 60_000L; }

	/*****
	 * <p> Method: AIRequestLimiter limiterWith20Used(String user) </p>
	 *
	 * <p> Description: Makes a limiter where the user has made 20 requests, one each minute from
	 * minute 0 to minute 19.  This is the setup for A2 to A7. </p>
	 *
	 * @param user is the username making the requests
	 *
	 * @return the limiter, with the user at the limit
	 */
	static AIRequestLimiter limiterWith20Used(String user) {
		AIRequestLimiter limiter = new AIRequestLimiter();
		for (int m = 0; m < 20; m++) limiter.tryRequest(user, minute(m));
		return limiter;
	}


	/*****
	 * <p> Test: A1 (positive) </p>
	 *
	 * <p> Requirement: a user can make AI requests (normal use works). </p>
	 *
	 * <p> Input: Ron's first request at minute 0.  Expected: "" and 19 requests left. </p>
	 */
	@Test
	void A1_firstRequestAllowed() {
		AIRequestLimiter limiter = new AIRequestLimiter();
		assertEquals("", limiter.tryRequest("Ron", minute(0)));
		assertEquals(19, limiter.getRemaining("Ron", minute(0)));
	}

	/*****
	 * <p> Test: A2 (positive, boundary) </p>
	 *
	 * <p> Requirement: up to 20 requests in 60 minutes are allowed. </p>
	 *
	 * <p> Input: 20 requests from minute 0 to 19.  Expected: the 20th returns "" and 0 are
	 * left. </p>
	 */
	@Test
	void A2_twentiethRequestAllowed() {
		AIRequestLimiter limiter = new AIRequestLimiter();
		for (int m = 0; m < 19; m++) limiter.tryRequest("Ron", minute(m));
		assertEquals("", limiter.tryRequest("Ron", minute(19)));		// the 20th request
		assertEquals(0, limiter.getRemaining("Ron", minute(19)));
	}

	/*****
	 * <p> Test: A3 (negative, boundary) </p>
	 *
	 * <p> Requirement: the 21st request in 60 minutes is refused, and the message says how long to
	 * wait. </p>
	 *
	 * <p> Input: a 21st request at minute 20.  Expected: refused, try again in 40 minutes,
	 * because the minute-0 request counts until minute 60. </p>
	 */
	@Test
	void A3_twentyFirstRequestRefused() {
		AIRequestLimiter limiter = limiterWith20Used("Ron");
		assertEquals(LIMIT_REACHED + "40 minutes.", limiter.tryRequest("Ron", minute(20)));
	}

	/*****
	 * <p> Test: A4 (positive) </p>
	 *
	 * <p> Requirement: each user has their own count. </p>
	 *
	 * <p> Input: Ron is at the limit, and Elijah makes his first request at minute 20.
	 * Expected: "" for Elijah. </p>
	 */
	@Test
	void A4_otherUserNotBlocked() {
		AIRequestLimiter limiter = limiterWith20Used("Ron");
		assertEquals("", limiter.tryRequest("Elijah", minute(20)));
	}

	/*****
	 * <p> Test: A5 (positive, time boundary) </p>
	 *
	 * <p> Requirement: a request stops counting once it is 60 minutes old. </p>
	 *
	 * <p> Input: Ron at the limit tries again at exactly minute 60.  Expected: "", because the
	 * minute-0 request is now exactly 60 minutes old. </p>
	 */
	@Test
	void A5_allowedAfterAnHour() {
		AIRequestLimiter limiter = limiterWith20Used("Ron");
		assertEquals("", limiter.tryRequest("Ron", minute(60)));
	}

	/*****
	 * <p> Test: A6 (negative, time boundary) </p>
	 *
	 * <p> Requirement: requests still count until the full 60 minutes have passed, and the wait
	 * is never shown as 0 minutes. </p>
	 *
	 * <p> Input: Ron at the limit tries at minute 59, second 59.  Expected: refused, try again
	 * in 1 minute (1 second rounded up). </p>
	 */
	@Test
	void A6_refusedOneSecondBeforeTheHour() {
		AIRequestLimiter limiter = limiterWith20Used("Ron");
		assertEquals(LIMIT_REACHED + "1 minute.", limiter.tryRequest("Ron", minute(60) - 1000));
	}

	/*****
	 * <p> Test: A7 (positive) </p>
	 *
	 * <p> Requirement: refused requests are not counted. </p>
	 *
	 * <p> Input: Ron at the limit is refused 5 times at minute 30, then tries at minute 60.
	 * Expected: "".  If the refused tries were counted, he would still be blocked. </p>
	 */
	@Test
	void A7_refusedRequestsNotCounted() {
		AIRequestLimiter limiter = limiterWith20Used("Ron");
		for (int i = 0; i < 5; i++) limiter.tryRequest("Ron", minute(30));
		assertEquals("", limiter.tryRequest("Ron", minute(60)));
	}

	/*****
	 * <p> Test: A8 (negative) </p>
	 *
	 * <p> Requirement: a user who isn't logged in can't make AI requests. </p>
	 *
	 * <p> Input: an empty username.  Expected: the not-logged-in message, and 0 requests
	 * left. </p>
	 */
	@Test
	void A8_emptyUserRefused() {
		AIRequestLimiter limiter = new AIRequestLimiter();
		assertEquals(AIRequestLimiter.NOT_LOGGED_IN, limiter.tryRequest("", minute(0)));
		assertEquals(0, limiter.getRemaining("", minute(0)));		// no requests for no user
	}

	/*****
	 * <p> Test: A9 (negative) </p>
	 *
	 * <p> Requirement: a missing user must not crash the program. </p>
	 *
	 * <p> Input: a null username.  Expected: the not-logged-in message and 0 requests left,
	 * with no NullPointerException. </p>
	 */
	@Test
	void A9_nullUserRefused() {
		AIRequestLimiter limiter = new AIRequestLimiter();
		assertEquals(AIRequestLimiter.NOT_LOGGED_IN, limiter.tryRequest(null, minute(0)));
		assertEquals(0, limiter.getRemaining(null, minute(0)));		// and no crash here either
	}

	/*****
	 * <p> Test: A10 (negative) </p>
	 *
	 * <p> Requirement: the limit can't be set to 0 or less by mistake. </p>
	 *
	 * <p> Input: a limit of 0.  Expected: IllegalArgumentException with the message "The AI
	 * request limit must be at least 1." </p>
	 */
	@Test
	void A10_zeroLimitRefused() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new AIRequestLimiter(0));
		assertEquals("The AI request limit must be at least 1.", e.getMessage());
	}

	/*****
	 * <p> Test: A11 (positive, boundary on the setting) </p>
	 *
	 * <p> Requirement: the smallest allowed limit, 1, works. </p>
	 *
	 * <p> Input: a limit of 1 and two requests at minute 0.  Expected: the first returns "" and
	 * the second is refused. </p>
	 */
	@Test
	void A11_limitOfOne() {
		AIRequestLimiter limiter = new AIRequestLimiter(1);
		assertEquals("", limiter.tryRequest("Ron", minute(0)));
		assertEquals("You have used 1 of 1 AI requests this hour. Try again in 60 minutes.",
				limiter.tryRequest("Ron", minute(0)));
	}
}
