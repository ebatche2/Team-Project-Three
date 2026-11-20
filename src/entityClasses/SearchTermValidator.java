package entityClasses;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/*******
 * <p> Title: SearchTermValidator Class </p>
 *
 * <p> Description: Checks the search terms that a curator types, or that an AI tool
 * suggests, before they are saved with a lesson.  Search terms are text that comes from
 * outside the system, and the AI tool's text cannot be trusted to follow any format.  If a
 * bad term were saved it could be empty, enormous, contain characters used in injection
 * attempts, or repeat another term.  Every check method returns an empty string when the
 * input is valid, and otherwise an error message that names the problem.  This is the same
 * pattern the LessonsLearned check methods use. </p>
 *
 * <p> Aspect: TP3 Aspect 1, staff input is validated (CWE-20). </p>
 *
 * <p> Rules are listed in TP3 Testing Requirements.pdf (R1 to R12). </p>
 */
public class SearchTermValidator {

	/** Smallest number of characters a search term may have, after trimming. */
	public static final int TERM_MIN = 2;

	/** Largest number of characters a search term may have, after trimming. */
	public static final int TERM_MAX = 40;

	/** Largest number of search terms in one list. */
	public static final int LIST_MAX = 20;


	/*****
	 * <p> Method: String checkTerm(String term) </p>
	 *
	 * <p> Description: Checks one search term against rules R1 to R6.  The term is trimmed
	 * first.  It is then rejected if it is empty, shorter than TERM_MIN, longer than
	 * TERM_MAX, contains a character other than a letter, digit, space, hyphen or apostrophe,
	 * or contains no letter or digit at all.  The checks run in that order, so the first
	 * problem found is the one reported. </p>
	 *
	 * @param term the search term to check; it may be null, which is treated as empty
	 *
	 * @return an empty string if the term is valid, otherwise an error message that names
	 *         the problem and shows the length or the term that was entered
	 */
	public static String checkTerm(String term) {

		// WHY: null is handled first because calling trim() on null would crash the program
		// (CWE-476).  A null term becomes "" and is then rejected as empty below.
		String trimmed = term == null ? "" : term.trim();

		// WHY: everything after this point measures the trimmed text, so spaces around a
		// term never count toward its length or make an empty term look valid.
		if (trimmed.length() == 0) {
			return "Search term is empty. Enter a term between 2 and 40 characters.";
		}

		// WHY: the empty check comes before the length checks.  An empty term would
		// otherwise be reported as "too short" and the curator would get a confusing message.
		if (trimmed.length() < TERM_MIN) {
			return "Search term too short. Minimum 2 characters. Current: " + trimmed.length();
		}

		// WHY: a limit on length keeps one entry from using unlimited space (CWE-770) and
		// keeps the term inside what the database column can hold.
		if (trimmed.length() > TERM_MAX) {
			return "Search term too long. Maximum 40. Current: " + trimmed.length();
		}

		// WHY: this lists the characters that are allowed instead of listing the ones that
		// are banned.  A list of banned characters always misses something (quotes,
		// semicolons, markup, control characters), but a list of allowed ones cannot.
		for (char c : trimmed.toCharArray()) {
			if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '\'')) {
				return "Search term has an invalid character. Allowed: letters, digits, spaces, hyphens, apostrophes. Entered: " + term;
			}
		}

		// WHY: the loop above is not enough to reject a term like "--".  Hyphens, spaces and
		// apostrophes are all allowed characters, so a term made only of them passes that loop
		// and would be useless as a search term.  This flag is set to true as soon as one
		// letter or digit is seen.
		boolean hasLetterOrDigit = false;
		for (char c : trimmed.toCharArray()) {
			if (Character.isLetterOrDigit(c)) {
				hasLetterOrDigit = true;
			}
		}
		if (!hasLetterOrDigit) {
			return "Search term must contain at least one letter or digit. Entered: " + term;
		}

		return "";
	}


	/*****
	 * <p> Method: String checkTermList(List&lt;String&gt; terms) </p>
	 *
	 * <p> Description: Checks a whole list of search terms against rules R7 to R10.  The
	 * list is rejected if it is null or empty, or has more than LIST_MAX terms.  Otherwise
	 * every term is checked with checkTerm, and a term that repeats an earlier one is
	 * rejected, ignoring upper and lower case and spaces around the term.  The first problem
	 * found is the one reported. </p>
	 *
	 * @param terms the list of search terms; it may be null, and it may contain null items
	 *
	 * @return an empty string if the whole list is valid, otherwise an error message that
	 *         names the problem
	 */
	public static String checkTermList(List<String> terms) {

		// WHY: a missing list is treated like an empty one, so null never reaches terms.size()
		// and crashes (CWE-476).
		if (terms == null || terms.isEmpty()) {
			return "Search term list is empty. Enter at least one term.";
		}

		// WHY: the size is checked before the loop so an oversized list is rejected right away
		// and never takes the time to be checked term by term (CWE-770).
		if (terms.size() > LIST_MAX) {
			return "Too many search terms. Maximum 20. Current: " + terms.size();
		}

		// WHY: this set remembers the terms that have been seen so far.  A set is used
		// because asking "is this term already in here?" is exactly what it is for.
		Set<String> seen = new HashSet<>();

		for (String term : terms) {

			// WHY: each term gets the same checks as a term typed on its own, so a bad term
			// inside a list (or a null item) is rejected with the same message.
			String message = checkTerm(term);
			if (message.length() > 0) {
				return message;
			}

			// WHY: it is safe to call trim() here because checkTerm returned "", which means
			// the term is not null.
			String trimmed = term.trim();

			// WHY: terms are compared in lower case, so "java" and "JAVA" count as the same
			// term.  Locale.ROOT is used so the result does not change with the computer's
			// language settings.  Set.add returns false when the term was already in the set.
			if (!seen.add(trimmed.toLowerCase(Locale.ROOT))) {
				return "Duplicate search term: " + trimmed;
			}
		}

		return "";
	}
}