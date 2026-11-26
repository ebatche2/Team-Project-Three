package limits;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/*******
 * <p> Title: AIRequestLimiter Class </p>
 *
 * <p> Description: This class limits how many AI requests each user can make.  It is a
 * prototype for TP3 Aspect #2, "Staff actions have limits" (CWE-770 Allocation of Resources
 * Without Limits).  Every AI feature in TP3 (curator AI search terms, curator AI editing, and the
 * contributor AI writing tools) asks this class before sending a request, so one user can't use up
 * the AI tools for everyone, and the free-tier limits of the AI services (LanguageTool and
 * Grammarly, from our AICoaching.pdf) aren't reached. </p>
 *
 * <p> Each user can make at most {@link #DEFAULT_MAX_REQUESTS} requests in any 60-minute
 * period (Initial ConOps.pdf, objective O1).  A request stops counting once it is 60 minutes old.
 * When the limit is reached, the user gets a message saying how many minutes until they can try
 * again (objective O6). </p>
 *
 * <p> Like the PasswordEvaluator class, tryRequest returns an empty String when the request is
 * allowed, or the message the GUI should show. </p>
 *
 * <p> Copyright: Ronald Yalung © 2026 </p>
 *
 * @author Ronald Yalung
 *
 * @version 1.00	2026-11-07 HW3 Task 5 prototype
 */
public class AIRequestLimiter {

	/** The number of AI requests each user can make per hour unless another limit is given.
	 *  Proposed in Initial ConOps.pdf (O1); to be confirmed with the staff. */
	public static final int DEFAULT_MAX_REQUESTS = 20;

	/** How long a request counts toward the limit: 60 minutes, in milliseconds. */
	public static final long WINDOW_MILLIS = 60 * 60 * 1000L;

	/** The message returned when there is no logged-in user (TP3 Aspect #5 handles login). */
	public static final String NOT_LOGGED_IN = "Error: You must be logged in to use AI tools.";

	/** The most requests one user can make in WINDOW_MILLIS. */
	private final int maxRequests;

	/** For each username, the times of the requests that still count, oldest first. */
	private final Map<String, Deque<Long>> requestTimes = new HashMap<String, Deque<Long>>();


	/*****
	 * <p> Method: AIRequestLimiter() </p>
	 *
	 * <p> Description: Creates a limiter with the default limit of 20 requests per hour. </p>
	 */
	public AIRequestLimiter() {
		this(DEFAULT_MAX_REQUESTS);
	}


	/*****
	 * <p> Method: AIRequestLimiter(int maxRequests) </p>
	 *
	 * <p> Description: Creates a limiter with a different limit, for example if the staff decide
	 * 20 is too many or too few. </p>
	 *
	 * @param maxRequests is the most requests one user can make per hour; it must be at least 1
	 *
	 * @throws IllegalArgumentException if maxRequests is less than 1
	 */
	public AIRequestLimiter(int maxRequests) {
		// A limit of 0 or less would quietly turn off AI for every user, which is a setup
		// mistake (A02), so it is refused right away instead of being found later by users.
		if (maxRequests < 1)
			throw new IllegalArgumentException("The AI request limit must be at least 1.");
		this.maxRequests = maxRequests;
	}


	/*****
	 * <p> Method: String tryRequest(String userName, long nowMillis) </p>
	 *
	 * <p> Description: Asks to make one AI request.  If the user is under the limit, the request
	 * is recorded and "" is returned, so the AI feature can go ahead.  If they are at the limit,
	 * nothing is recorded and a message is returned. </p>
	 *
	 * @param userName is the logged-in user making the request
	 *
	 * @param nowMillis is the current time in milliseconds (System.currentTimeMillis() in the
	 * app; a fixed time in the tests)
	 *
	 * @return "" if the request is allowed, else the message to show the user
	 */
	public String tryRequest(String userName, long nowMillis) {
		// Checked first so a null username can't cause a NullPointerException below (A9)
		if (userName == null || userName.trim().isEmpty()) return NOT_LOGGED_IN;

		Deque<Long> times = timesFor(userName);
		removeExpired(times, nowMillis);

		if (times.size() >= maxRequests) {
			// The refused request is NOT added to the list.  If it were, a user who keeps
			// clicking while blocked would push their wait time further and further back (A7).
			return "You have used " + maxRequests + " of " + maxRequests
					+ " AI requests this hour. Try again in " + minutesUntilFree(times, nowMillis) + ".";
		}

		times.addLast(nowMillis);
		return "";
	}


	/*****
	 * <p> Method: int getRemaining(String userName, long nowMillis) </p>
	 *
	 * <p> Description: Tells how many requests the user has left right now, so a page can show
	 * something like "AI requests left this hour: 12". </p>
	 *
	 * @param userName is the logged-in user
	 *
	 * @param nowMillis is the current time in milliseconds
	 *
	 * @return the number of requests left, or 0 if there is no logged-in user
	 */
	public int getRemaining(String userName, long nowMillis) {
		if (userName == null || userName.trim().isEmpty()) return 0;
		Deque<Long> times = timesFor(userName);
		removeExpired(times, nowMillis);
		return maxRequests - times.size();
	}


	/*****
	 * <p> Method: Deque&lt;Long&gt; timesFor(String userName) </p>
	 *
	 * <p> Description: Gets the request times for one user, creating an empty list the first time
	 * the user makes a request.  Keeping a separate list per user is what keeps one user from
	 * blocking everyone else (A4). </p>
	 *
	 * @param userName is the user
	 *
	 * @return that user's request times, oldest first
	 */
	private Deque<Long> timesFor(String userName) {
		Deque<Long> times = requestTimes.get(userName);
		if (times == null) {
			times = new ArrayDeque<Long>();
			requestTimes.put(userName, times);
		}
		return times;
	}


	/*****
	 * <p> Method: void removeExpired(Deque&lt;Long&gt; times, long nowMillis) </p>
	 *
	 * <p> Description: Removes the requests that are 60 minutes old or older, since they no
	 * longer count. </p>
	 *
	 * @param times is one user's request times, oldest first
	 *
	 * @param nowMillis is the current time in milliseconds
	 */
	private void removeExpired(Deque<Long> times, long nowMillis) {
		// The times are added in order, so only the front of the list can be expired.  That's
		// why this stops at the first request that still counts instead of checking them all.
		// ">=" makes a request exactly 60 minutes old expire, so the user can try again right
		// at the hour (A5), not one millisecond later.
		while (!times.isEmpty() && nowMillis - times.peekFirst() >= WINDOW_MILLIS) {
			times.removeFirst();
		}
	}


	/*****
	 * <p> Method: String minutesUntilFree(Deque&lt;Long&gt; times, long nowMillis) </p>
	 *
	 * <p> Description: Works out how long until the user's oldest request expires, as text for
	 * the message, for example "40 minutes" or "1 minute". </p>
	 *
	 * @param times is one user's request times, oldest first (not empty)
	 *
	 * @param nowMillis is the current time in milliseconds
	 *
	 * @return the wait time as text
	 */
	private String minutesUntilFree(Deque<Long> times, long nowMillis) {
		long millisLeft = times.peekFirst() + WINDOW_MILLIS - nowMillis;
		// Rounded UP to whole minutes.  Rounding down would tell a user with 30 seconds left to
		// "try again in 0 minutes", and their next try would still be refused (A6).
		long minutes = (millisLeft + 59_999) / 60_000;
		return minutes == 1 ? "1 minute" : minutes + " minutes";
	}
}