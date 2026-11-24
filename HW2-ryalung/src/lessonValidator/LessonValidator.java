package lessonValidator;

/*******
 * <p> Title: LessonValidator Class. </p>
 * 
 * <p> Description: This class checks the values a contributor types in for a lesson learned.
 * Every method returns an empty String when the value is fine, or a helpful error message when it
 * is not. The message says which field is wrong and what the rule is. The limits are kept in
 * constants so the messages and the checks always agree. </p>
 * 
 * <p> Copyright: Ronald Yalung © 2026 </p>
 * 
 * @author Ronald Yalung
 * 
 * @version 1.00		2026-09-29 Initial version for HW2
 */

public class LessonValidator {

	/*
	 * These are the limits from the User Stories and Test Cases documents.  Changing a limit here
	 * changes both the check and the message that is shown.
	 */

	/** The longest title allowed, in characters. */
	public static final int MAX_TITLE = 100;

	/** The longest text allowed in the description, what happened, and similar fields. */
	public static final int MAX_TEXT = 500;

	/** The only categories a lesson may have. */
	public static final String[] CATEGORIES =
		{"Requirements", "Design", "Coding", "Testing", "Process"};

	// Punctuation allowed in a title in addition to letters, numbers, and spaces
	private static final String TITLE_PUNCTUATION = ".,!?-:'()";

	/*******
	 * <p> Method: LessonValidator() </p>
	 * 
	 * <p> Description: This class only has static methods, so it is never instantiated. </p>
	 */
	private LessonValidator() {
	}

	/*******
	 * <p> Method: String validateTitle(String title) </p>
	 * 
	 * <p> Description: Checks that a title is not blank, is not too long, and uses only letters,
	 * numbers, spaces, and basic punctuation. </p>
	 * 
	 * @param title is the title to check
	 * 
	 * @return an empty String if the title is valid, otherwise an error message
	 */
	public static String validateTitle(String title) {
		if (title == null || title.trim().isEmpty())
			return "Title cannot be empty. Enter 1 to " + MAX_TITLE + " characters.";
		if (title.length() > MAX_TITLE)
			return "Title is too long (" + title.length() + " characters). The maximum is "
					+ MAX_TITLE + ".";
		for (char c : title.toCharArray()) {
			if (!Character.isLetterOrDigit(c) && c != ' ' && TITLE_PUNCTUATION.indexOf(c) < 0)
				return "Title has an invalid character '" + c
						+ "'. Use letters, numbers, spaces, and basic punctuation only.";
		}
		return "";
	}

	/*******
	 * <p> Method: String validateCategory(String category) </p>
	 * 
	 * <p> Description: Checks that the category is one of the allowed categories. </p>
	 * 
	 * @param category is the category to check
	 * 
	 * @return an empty String if the category is valid, otherwise an error message
	 */
	public static String validateCategory(String category) {
		String allowed = String.join(", ", CATEGORIES);
		if (category == null || category.trim().isEmpty())
			return "Category cannot be empty. Choose one of: " + allowed + ".";
		for (String c : CATEGORIES) {
			if (c.equals(category)) return "";
		}
		return "Category '" + category + "' is not valid. Choose one of: " + allowed + ".";
	}

	/*******
	 * <p> Method: String validateText(String label, String value) </p>
	 * 
	 * <p> Description: Checks one of the long text fields (description, what happened, or what
	 * was learned). The text must not be blank and must not be longer than the limit. </p>
	 * 
	 * @param label is the name of the field, used in the message (for example "Description")
	 * 
	 * @param value is the text to check
	 * 
	 * @return an empty String if the text is valid, otherwise an error message
	 */
	public static String validateText(String label, String value) {
		if (value == null || value.trim().isEmpty())
			return label + " cannot be empty.";
		if (value.length() > MAX_TEXT)
			return label + " is too long (" + value.length() + " characters). The maximum is "
					+ MAX_TEXT + ".";
		return "";
	}

	

}