package tests;

import entityClasses.User;

/*******
 * <p> Title: UserTestingAutomation </p>
 *
 * <p> Description: Automated tests for the TP2 User class (HW3 Task 2.4).  Section 1 holds
 * the behavior tests (UT-1 to UT-4): they check that the constructor, the setters, the getters
 * and getNumRoles do what the Javadoc says, and they should all pass.  Section 2 holds the
 * weakness tests (W-1 to W-5): each one states what a secure User class SHOULD do, and each
 * one currently fails.  Every failure is a defect that is listed in TP2 Test Designs.pdf.
 * Together the tests call every method in User, so the coverage run should show 100%. </p>
 *
 * <p> Defects covered: CWE-20 Improper Input Validation, CWE-476 NULL Pointer Dereference,
 * A04 Cryptographic Failures, and A01 Broken Access Control. </p>
 */
public class UserTestingAutomation {

	/** Number of tests that passed so far. */
	static int numPassed = 0;

	/** Number of tests that failed so far. */
	static int numFailed = 0;

	// Sample account values used by the behavior tests.
	private static final String USER = "ebatche2";
	private static final String PASS = "Secret#123";
	private static final String FIRST = "Elijah";
	private static final String MIDDLE = "J";
	private static final String LAST = "Batchelor";
	private static final String PREFERRED = "Eli";
	private static final String EMAIL = "ebatche2@asu.edu";


	/*****
	 * <p> Method: void check(String testCase, String what, String expected, String actual) </p>
	 *
	 * <p> Description: Compares the expected and actual text and prints Success or Failure. </p>
	 *
	 * @param testCase is the test's name, e.g., "UT-1a"
	 * @param what says what the test checks
	 * @param expected is the value the test should get
	 * @param actual is the value the test got
	 */
	private static void check(String testCase, String what, String expected, String actual) {
		if (expected.equals(actual)) {
			System.out.println("***Success*** " + testCase + ": " + what);
			numPassed++;
		} else {
			System.out.println("***Failure*** " + testCase + ": " + what);
			System.out.println("   Expected: " + expected);
			System.out.println("   Actual:   " + actual);
			numFailed++;
		}
	}


	/*****
	 * <p> Method: boolean rejected(Runnable action) </p>
	 *
	 * <p> Description: Runs an action and reports whether it was refused by throwing an
	 * exception.  WHY: User has no way to return an error message, so a secure User would
	 * have to refuse bad input by throwing. </p>
	 *
	 * @param action is the code to run
	 *
	 * @return true if the action threw a RuntimeException, false if it ran normally
	 */
	private static boolean rejected(Runnable action) {
		try {
			action.run();
			return false;
		} catch (RuntimeException e) {
			return true;
		}
	}


	/*****
	 * <p> Method: User sample() </p>
	 *
	 * <p> Description: Builds a user with the sample values and no roles. </p>
	 *
	 * @return a new User
	 */
	private static User sample() {
		return new User(USER, PASS, FIRST, MIDDLE, LAST, PREFERRED, EMAIL, false, false, false);
	}


	/*****
	 * <p> Method: String roleCount(boolean admin, boolean r1, boolean r2) </p>
	 *
	 * <p> Description: Builds a user with the given roles and returns getNumRoles as text. </p>
	 *
	 * @param admin is the Admin role
	 * @param r1 is role1
	 * @param r2 is role2
	 *
	 * @return the number of roles as a String
	 */
	private static String roleCount(boolean admin, boolean r1, boolean r2) {
		User u = new User(USER, PASS, FIRST, MIDDLE, LAST, PREFERRED, EMAIL, admin, r1, r2);
		return "" + u.getNumRoles();
	}


	/*****
	 * <p> Method: void behaviorTests() </p>
	 *
	 * <p> Description: UT-1 to UT-4.  Each checks one promise the User class makes. </p>
	 */
	private static void behaviorTests() {

		// UT-1: the constructor stores all ten values and every getter returns them
		User u = new User(USER, PASS, FIRST, MIDDLE, LAST, PREFERRED, EMAIL, true, true, false);
		check("UT-1a", "constructor stores userName", USER, u.getUserName());
		check("UT-1b", "constructor stores password", PASS, u.getPassword());
		check("UT-1c", "constructor stores first name", FIRST, u.getFirstName());
		check("UT-1d", "constructor stores middle name", MIDDLE, u.getMiddleName());
		check("UT-1e", "constructor stores last name", LAST, u.getLastName());
		check("UT-1f", "constructor stores preferred name", PREFERRED, u.getPreferredFirstName());
		check("UT-1g", "constructor stores email", EMAIL, u.getEmailAddress());
		check("UT-1h", "constructor stores Admin role (true)", "true", "" + u.getAdminRole());
		check("UT-1i", "constructor stores role1 (true)", "true", "" + u.getNewRole1());
		check("UT-1j", "constructor stores role2 (false)", "false", "" + u.getNewRole2());

		// UT-2: each text setter replaces the stored value
		User s = sample();
		s.setUserName("newName");
		check("UT-2a", "setUserName", "newName", s.getUserName());
		s.setPassword("NewPass#9");
		check("UT-2b", "setPassword", "NewPass#9", s.getPassword());
		s.setFirstName("Ann");
		check("UT-2c", "setFirstName", "Ann", s.getFirstName());
		s.setMiddleName("B");
		check("UT-2d", "setMiddleName", "B", s.getMiddleName());
		s.setLastName("Lee");
		check("UT-2e", "setLastName", "Lee", s.getLastName());
		s.setPreferredFirstName("Annie");
		check("UT-2f", "setPreferredFirstName", "Annie", s.getPreferredFirstName());
		s.setEmailAddress("ann@asu.edu");
		check("UT-2g", "setEmailAddress", "ann@asu.edu", s.getEmailAddress());

		// UT-3: each role setter changes only its own role
		User r = sample();
		r.setAdminRole(true);
		check("UT-3a", "setAdminRole(true) sets Admin", "true", "" + r.getAdminRole());
		check("UT-3b", "setAdminRole(true) leaves role1 alone", "false", "" + r.getNewRole1());
		r.setRole1User(true);
		check("UT-3c", "setRole1User(true) sets role1", "true", "" + r.getNewRole1());
		r.setRole2User(true);
		check("UT-3d", "setRole2User(true) sets role2", "true", "" + r.getNewRole2());
		r.setAdminRole(false);
		check("UT-3e", "setAdminRole(false) clears Admin", "false", "" + r.getAdminRole());

		// UT-4: getNumRoles for all eight combinations.  Boundary values are 0 roles (none)
		// and 3 roles (all), and the three single-role and three double-role cases make each
		// "if" in getNumRoles take both its true and its false path.
		check("UT-4a", "no roles = 0 (lower boundary)", "0", roleCount(false, false, false));
		check("UT-4b", "Admin only = 1", "1", roleCount(true, false, false));
		check("UT-4c", "role1 only = 1", "1", roleCount(false, true, false));
		check("UT-4d", "role2 only = 1", "1", roleCount(false, false, true));
		check("UT-4e", "Admin and role1 = 2", "2", roleCount(true, true, false));
		check("UT-4f", "Admin and role2 = 2", "2", roleCount(true, false, true));
		check("UT-4g", "role1 and role2 = 2", "2", roleCount(false, true, true));
		check("UT-4h", "all three roles = 3 (upper boundary)", "3", roleCount(true, true, true));
	}


	/*****
	 * <p> Method: void weaknessTests() </p>
	 *
	 * <p> Description: W-1 to W-5.  Each test says what a secure User class should do.  They
	 * currently fail, and each failure is one defect in TP2 Test Designs.pdf. </p>
	 */
	private static void weaknessTests() {

		// W-1 (CWE-20): a User must not be created without a user name
		check("W-1", "CWE-20: constructor refuses a null userName", "rejected",
				rejected(() -> new User(null, PASS, FIRST, MIDDLE, LAST, PREFERRED, EMAIL,
						false, false, false)) ? "rejected" : "accepted");

		// W-2 (CWE-20): the password must not be set to an empty string
		User a = sample();
		check("W-2", "CWE-20: setPassword refuses an empty password", "rejected",
				rejected(() -> a.setPassword("")) ? "rejected" : "accepted");

		// W-3 (CWE-476): a default User has null fields that crash callers
		check("W-3", "CWE-476: default User has a userName that is not null", "not null",
				new User().getUserName() == null ? "null" : "not null");

		// W-4 (A04): the password is kept and returned as plain text
		User b = sample();
		check("W-4", "A04: getPassword does not return the plain-text password", "not plain text",
				PASS.equals(b.getPassword()) ? "plain text" : "not plain text");

		// W-5 (A01 / CWE-862): any code can make a user an Admin
		User c = sample();
		check("W-5", "A01: setAdminRole(true) needs authorization", "rejected",
				rejected(() -> c.setAdminRole(true)) ? "rejected" : "accepted");
	}


	/*****
	 * <p> Method: void main(String[] args) </p>
	 *
	 * <p> Description: Runs every test and prints the totals. </p>
	 *
	 * @param args is not used
	 */
	public static void main(String[] args) {
		System.out.println("--- Behavior tests ---");
		behaviorTests();
		System.out.println();
		System.out.println("--- Weakness tests (each failure is a defect) ---");
		weaknessTests();
		System.out.println();
		System.out.println("Passed: " + numPassed + "   Failed: " + numFailed);
	}
}