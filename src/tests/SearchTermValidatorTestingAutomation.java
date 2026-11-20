package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entityClasses.SearchTermValidator;

/*******
 * <p> Title: SearchTermValidatorTestingAutomation </p>
 *
 * <p> Description: Automated tests for SearchTermValidator.  Each test calls checkTerm or
 * checkTermList with one input and compares the answer with the expected message.  Every test
 * prints Success or Failure, and main prints the totals.  The 27 test cases are the ones in
 * TP3 Testing Details and Rationale.pdf: TC-1 to TC-17 test checkTerm and TC-18 to TC-27
 * test checkTermList. </p>
 *
 * <p> Aspect: TP3 Aspect 1, staff input is validated (CWE-20). </p>
 */
public class SearchTermValidatorTestingAutomation {

	/** Number of tests that passed so far. */
	static int numPassed = 0;

	/** Number of tests that failed so far. */
	static int numFailed = 0;

	// WHY: the expected messages are typed here on purpose and are NOT taken from
	// SearchTermValidator.  A test must state what the answer should be, independently of
	// the code it is checking.  Otherwise a mistake in the message would make the test agree
	// with the mistake.
	private static final String EMPTY =
			"Search term is empty. Enter a term between 2 and 40 characters.";

	private static final String LIST_EMPTY =
			"Search term list is empty. Enter at least one term.";

	private static String tooShort(int n) {
		return "Search term too short. Minimum 2 characters. Current: " + n;
	}

	private static String tooLong(int n) {
		return "Search term too long. Maximum 40. Current: " + n;
	}

	private static String badChar(String term) {
		return "Search term has an invalid character. Allowed: letters, digits, spaces, "
				+ "hyphens, apostrophes. Entered: " + term;
	}

	private static String noLetterOrDigit(String term) {
		return "Search term must contain at least one letter or digit. Entered: " + term;
	}

	private static String tooManyTerms(int n) {
		return "Too many search terms. Maximum 20. Current: " + n;
	}

	private static String duplicate(String term) {
		return "Duplicate search term: " + term;
	}


	/*****
	 * <p> Method: void check(String testCase, String what, String expected, String actual) </p>
	 *
	 * <p> Description: Compares the message the validator returned with the expected one and
	 * prints the result.  expected.equals(actual) is used so a null result counts as a
	 * failure instead of crashing the test run. </p>
	 *
	 * @param testCase is the test's name, e.g., "TC-1"
	 * @param what says what the test checks
	 * @param expected is the message it should return ("" means valid)
	 * @param actual is the message the validator returned
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
	 * <p> Method: void termTests() </p>
	 *
	 * <p> Description: TC-1 to TC-17, the tests for checkTerm.  TC-1 to TC-6 are positive
	 * tests (the term is valid).  TC-7 to TC-17 are negative tests (the term is rejected). </p>
	 */
	private static void termTests() {

		// TC-1: a typical valid term must be accepted.
		check("TC-1", "valid term \"java\"", "", SearchTermValidator.checkTerm("java"));

		// TC-2:  lower boundary, exactly 2 characters
		check("TC-2", "valid term \"ab\"", "", SearchTermValidator.checkTerm("ab"));

		// TC-3:  upper boundary, exactly 40 characters
		check("TC-3", "valid term \"" + "a".repeat(40) + "\"", "", SearchTermValidator.checkTerm("a".repeat(40)));

		// TC-4:  39 characters
		check("TC-4", "valid term \"" + "a".repeat(39) + "\"", "", SearchTermValidator.checkTerm("a".repeat(39)));

		// TC-5:  spaces around a term that is otherwise fine
		check("TC-5", "valid term \" unit testing \"", "", SearchTermValidator.checkTerm(" unit testing "));

		// TC-6:  apostrophe, hyphen, space and digit together
		check("TC-6", "valid term \"user's guide-2\"", "", SearchTermValidator.checkTerm("user's guide-2"));

		// TC-7: null must be treated as empty, and must not throw.
		check("TC-7", "null term", EMPTY, SearchTermValidator.checkTerm(null));

		// TC-8:  empty string
		check("TC-8", "no text entry", EMPTY, SearchTermValidator.checkTerm(""));

		// TC-9:  only spaces
		check("TC-9", "space in string", EMPTY, SearchTermValidator.checkTerm(" "));

		// TC-10: 1 character
		check("TC-10", "too short", tooShort(1), SearchTermValidator.checkTerm("a"));

		// TC-11: 1 character with spaces around it (length is counted AFTER trimming)
		check("TC-11", "too short", tooShort(1), SearchTermValidator.checkTerm(" a "));

		// TC-12: 41 characters
		check("TC-12", "too long", tooLong(41), SearchTermValidator.checkTerm("a".repeat(41)));

		// TC-13: a valid term with one bad character on the end
		check("TC-13", "bad character", badChar("java!"), SearchTermValidator.checkTerm("java!"));

		// TC-14: markup characters
		check("TC-14", "bad character", badChar("<script>"), SearchTermValidator.checkTerm("<script>"));

		// TC-15: the SQL injection attempt
		check("TC-15", "bad character", badChar("x'; DROP TABLE lessons;--"),
				SearchTermValidator.checkTerm("x'; DROP TABLE lessons;--"));

		// TC-16: only hyphens
		check("TC-16", "no letter or digit", noLetterOrDigit("--"), SearchTermValidator.checkTerm("--"));

		// TC-17: a line break inside the term.  WHY: "\n" is one real line break character,
		// which is not on the allowed list.  (Typing the words "newline" would only test the
		// quote and plus characters, and the test would pass for the wrong reason.)
		check("TC-17", "line break in term", badChar("ab\ncd"), SearchTermValidator.checkTerm("ab\ncd"));
	}


	/*****
	 * <p> Method: List&lt;String&gt; termsList(int n) </p>
	 *
	 * <p> Description: Builds a list of n different valid terms: term1, term2, ... termN.
	 * It is used to build lists that sit right at and just over the size limit. </p>
	 *
	 * @param n is how many terms the list should have
	 *
	 * @return a new list of n different valid terms
	 */
	private static List<String> termsList(int n) {
		List<String> list = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			list.add("term" + i);
		}
		return list;
	}


	/*****
	 * <p> Method: void listTests() </p>
	 *
	 * <p> Description: TC-18 to TC-27, the tests for checkTermList.  TC-18 to TC-20 are
	 * positive tests (the list is valid).  TC-21 to TC-27 are negative tests (the list is
	 * rejected). </p>
	 */
	private static void listTests() {

		// TC-18: a typical valid list
		check("TC-18", "two valid terms", "",
				SearchTermValidator.checkTermList(Arrays.asList("java", "testing")));

		// TC-19: lower list boundary, one term is enough
		check("TC-19", "list with one term", "",
				SearchTermValidator.checkTermList(Arrays.asList("java")));

		// TC-20: upper list boundary, exactly 20 different terms
		check("TC-20", "20 different terms", "", SearchTermValidator.checkTermList(termsList(20)));

		// TC-21: a null list must not throw
		check("TC-21", "null list", LIST_EMPTY, SearchTermValidator.checkTermList(null));

		// TC-22: an empty list
		check("TC-22", "empty list", LIST_EMPTY,
				SearchTermValidator.checkTermList(new ArrayList<String>()));

		// TC-23: one term over the upper boundary
		check("TC-23", "21 terms", tooManyTerms(21), SearchTermValidator.checkTermList(termsList(21)));

		// TC-24: duplicates are not case sensitive
		check("TC-24", "\"java\" and \"JAVA\"", duplicate("JAVA"),
				SearchTermValidator.checkTermList(Arrays.asList("java", "JAVA")));

		// TC-25: duplicates are found after trimming
		check("TC-25", "\"java\" and \" java \"", duplicate("java"),
				SearchTermValidator.checkTermList(Arrays.asList("java", " java ")));

		// TC-26: a bad term inside a list is reported with the term's own message
		check("TC-26", "\"java\" and a 1 character term", tooShort(1),
				SearchTermValidator.checkTermList(Arrays.asList("java", "a")));

		// TC-27: a null item inside a list must not throw
		check("TC-27", "\"java\" and a null item", EMPTY,
				SearchTermValidator.checkTermList(Arrays.asList("java", null)));
	}


	/*****
	 * <p> Method: void main(String[] args) </p>
	 *
	 * <p> Description: Runs every test and prints the totals. </p>
	 *
	 * @param args is not used
	 */
	public static void main(String[] args) {
		termTests();
		listTests();
		System.out.println();
		System.out.println("Passed: " + numPassed + "   Failed: " + numFailed);
	}
}