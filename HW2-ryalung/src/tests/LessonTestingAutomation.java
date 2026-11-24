package tests;

import database.Database;
import entityClasses.Lesson;
import entityClasses.LessonList;
import lessonManager.ModelLessons;

/*******
 * <p> Title: LessonTestingAutomation Class. </p>
 *
 * <p> Description: A Java demonstration for semi-automated tests of the lesson CRUD code
 * 		(ModelLessons, LessonValidator, LessonList, and the Database lesson methods), following
 * 		the same pattern as the TP1 PasswordEvaluator and UserNameRecognizer testing classes.
 * 		Each group of checks is labeled with its test case number from HW2 Test Cases.pdf. </p>
 *
 * <p> Copyright: Ronald Yalung © 2026 </p>
 *
 * @author Ronald Yalung
 *
 * @version 1.00	2026-10-02	Initial test suite for HW2, covering the 12 cases documented in
 * 		HW2 Test Cases.pdf.
 */
public class LessonTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	static Database db = new Database();
	static ModelLessons model;
	static final String CATS = "Requirements, Design, Coding, Testing, Process";

	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________");
		System.out.println("\nLesson Testing Automation");

		db.connectToDatabase();
		model = new ModelLessons(db);

		/************** Start of the test cases **************/

		// TC-1: a good lesson is saved, read back, and still there after reopening
		db.deleteAllLessons();
		Lesson good = new Lesson("role1", "Password rules were unclear", "Requirements",
				"Rules were not written down", "We guessed the rules", "Write rules first");
		performTestCase(1, "Add a good lesson", model.addLesson(good), "");
		performCheck(1, "The lesson got an id", "true", "" + (good.getId() > 0));
		Lesson back = model.viewLesson(good.getId(), "role1");
		performCheck(1, "Owner read back", "role1", back.getOwner());
		performCheck(1, "Title read back", "Password rules were unclear", back.getTitle());
		performCheck(1, "Category read back", "Requirements", back.getCategory());
		performCheck(1, "Description read back", "Rules were not written down", back.getDescription());
		performCheck(1, "What happened read back", "We guessed the rules", back.getWhatHappened());
		performCheck(1, "What was learned read back", "Write rules first", back.getWhatLearned());
		reopen();
		Lesson again = model.getMyLessons("role1").get(0);
		performCheck(1, "Lesson count after reopening", "1", "" + model.getMyLessons("role1").size());
		performCheck(1, "Owner after reopening", "role1", again.getOwner());
		performCheck(1, "Title after reopening", "Password rules were unclear", again.getTitle());
		performCheck(1, "Category after reopening", "Requirements", again.getCategory());
		performCheck(1, "Description after reopening", "Rules were not written down", again.getDescription());
		performCheck(1, "What happened after reopening", "We guessed the rules", again.getWhatHappened());
		performCheck(1, "What was learned after reopening", "Write rules first", again.getWhatLearned());

		// TC-2: title rules (blank, 101 characters, invalid character, and exactly 100 accepted)
		db.deleteAllLessons();
		performTestCase(2, "Empty title", add("role1", "", "Design"),
				"Title cannot be empty. Enter 1 to 100 characters.");
		performTestCase(2, "Title of only spaces", add("role1", "   ", "Design"),
				"Title cannot be empty. Enter 1 to 100 characters.");
		performTestCase(2, "Title of 101 characters", add("role1", "x".repeat(101), "Design"),
				"Title is too long (101 characters). The maximum is 100.");
		performTestCase(2, "Title \"Rework <script>\"", add("role1", "Rework <script>", "Design"),
				"Title has an invalid character '<'. Use letters, numbers, spaces, and basic "
				+ "punctuation only.");
		performCheck(2, "Nothing was saved by the bad titles", "0",
				"" + model.getMyLessons("role1").size());
		performTestCase(2, "Title of exactly 100 characters", add("role1", "x".repeat(100), "Design"), "");

		// TC-3: a category that is not allowed, and a blank category
		performTestCase(3, "Category \"Cooking\"", add("role1", "Good title", "Cooking"),
				"Category 'Cooking' is not valid. Choose one of: " + CATS + ".");
		performTestCase(3, "Blank category", add("role1", "Good title", ""),
				"Category cannot be empty. Choose one of: " + CATS + ".");

		// TC-4: text field rules (each bad value is tried by itself, the other fields are good)
		performTestCase(4, "Blank description",
				model.addLesson(new Lesson("role1", "T", "Design", "", "h", "l")),
				"Description cannot be empty.");
		performTestCase(4, "Description of 501 characters",
				model.addLesson(new Lesson("role1", "T", "Design", "d".repeat(501), "h", "l")),
				"Description is too long (501 characters). The maximum is 500.");
		performTestCase(4, "Blank what happened",
				model.addLesson(new Lesson("role1", "T", "Design", "d", "", "l")),
				"What happened cannot be empty.");
		performTestCase(4, "Blank what was learned",
				model.addLesson(new Lesson("role1", "T", "Design", "d", "h", "")),
				"What was learned cannot be empty.");

		// TC-5: a contributor who has not added any lessons
		db.deleteAllLessons();
		LessonList none = model.getMyLessons("role1");
		performCheck(5, "Size of an empty list", "0", "" + none.size());
		performCheck(5, "Message for an empty list", "You have not added any lessons yet.",
				model.getEmptyListMessage(none));

		// TC-6: list sizes for role1 (1, 3, and 100 lessons, there is no upper limit)
		add("role1", "Lesson 1", "Coding");
		performCheck(6, "One lesson", "1", "" + model.getMyLessons("role1").size());
		add("role1", "Lesson 2", "Coding");
		add("role1", "Lesson 3", "Coding");
		performCheck(6, "Three lessons", "3", "" + model.getMyLessons("role1").size());
		for (int i = 4; i <= 100; i++) add("role1", "Lesson " + i, "Coding");
		performCheck(6, "One hundred lessons", "100", "" + model.getMyLessons("role1").size());

		// TC-7: each contributor sees only their own lessons
		db.deleteAllLessons();
		add("role1", "Role1 lesson", "Design");
		add("role2", "Role2 lesson", "Testing");
		performCheck(7, "role1 lesson count", "1", "" + model.getMyLessons("role1").size());
		performCheck(7, "role1 sees its own title", "Role1 lesson",
				model.getMyLessons("role1").get(0).getTitle());
		performCheck(7, "role2 lesson count", "1", "" + model.getMyLessons("role2").size());
		performCheck(7, "role2 sees its own title", "Role2 lesson",
				model.getMyLessons("role2").get(0).getTitle());

		// TC-8: changing the description is saved and still there after reopening
		db.deleteAllLessons();
		add("role1", "Update me", "Design");
		int id = model.getMyLessons("role1").get(0).getId();
		performTestCase(8, "Change the description",
				model.editLesson(id, "role1", "description", "New description"), "");
		performCheck(8, "Description read back", "New description",
				model.viewLesson(id, "role1").getDescription());
		reopen();
		Lesson changed = model.viewLesson(id, "role1");
		performCheck(8, "Description after reopening", "New description", changed.getDescription());
		performCheck(8, "Title is unchanged", "Update me", changed.getTitle());
		performCheck(8, "Category is unchanged", "Design", changed.getCategory());
		performCheck(8, "What happened is unchanged", "happened", changed.getWhatHappened());
		performCheck(8, "What was learned is unchanged", "learned", changed.getWhatLearned());

		// TC-9: bad new values are rejected and the old values stay, even after reopening
		performTestCase(9, "Change the title to blank", model.editLesson(id, "role1", "title", ""),
				"Title cannot be empty. Enter 1 to 100 characters.");
		performTestCase(9, "Change the category to \"Cooking\"",
				model.editLesson(id, "role1", "category", "Cooking"),
				"Category 'Cooking' is not valid. Choose one of: " + CATS + ".");
		reopen();
		Lesson same = model.viewLesson(id, "role1");
		performCheck(9, "Old title after reopening", "Update me", same.getTitle());
		performCheck(9, "Old category after reopening", "Design", same.getCategory());

		// TC-10: deleting a lesson removes it, and it stays gone after reopening
		performTestCase(10, "Delete the lesson", model.deleteLesson(id, "role1"), "");
		performCheck(10, "Lesson count before reopening", "0", "" + model.getMyLessons("role1").size());
		reopen();
		performCheck(10, "Lesson count after reopening", "0", "" + model.getMyLessons("role1").size());

		// TC-11: someone else's lesson and a missing lesson give the not-found message
		add("role2", "Role2 only", "Design");
		int role2Id = model.getMyLessons("role2").get(0).getId();
		performCheck(11, "role1 opens role2's lesson (null means not found)", "true",
				"" + (model.viewLesson(role2Id, "role1") == null));
		performTestCase(11, "role1 changes role2's lesson",
				model.editLesson(role2Id, "role1", "title", "Changed"), ModelLessons.NOT_FOUND_MESSAGE);
		performTestCase(11, "role1 deletes role2's lesson",
				model.deleteLesson(role2Id, "role1"), ModelLessons.NOT_FOUND_MESSAGE);
		performTestCase(11, "role1 deletes lesson 99999",
				model.deleteLesson(99999, "role1"), ModelLessons.NOT_FOUND_MESSAGE);
		performCheck(11, "role2's lesson is still there", "1", "" + model.getMyLessons("role2").size());
		performCheck(11, "role2's title is unchanged", "Role2 only",
				model.getMyLessons("role2").get(0).getTitle());

		// TC-12: a LessonList can hold all, some, or none of the lessons
		db.deleteAllLessons();
		add("role1", "A", "Design");
		add("role1", "B", "Design");
		add("role1", "C", "Coding");
		LessonList all = model.getMyLessons("role1");
		performCheck(12, "All lessons", "3", "" + all.size());
		performCheck(12, "Only the Design lessons", "2", "" + all.filterByCategory("Design").size());
		performCheck(12, "A category with no lessons", "0", "" + all.filterByCategory("Testing").size());
		performCheck(12, "The empty list is empty", "true", "" + all.filterByCategory("Testing").isEmpty());

		/************** End of the test cases **************/

		db.deleteAllLessons();
		db.closeConnection();

		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	// Adds a lesson with good values for everything except the title and category
	private static String add(String owner, String title, String category) {
		return model.addLesson(new Lesson(owner, title, category, "desc", "happened", "learned"));
	}

	// "Reopen the app" means closing and reconnecting the database
	private static void reopen() throws Exception {
		db.closeConnection();
		db.connectToDatabase();
	}

	/*
	 * For an operation that returns a message. An empty expectedMessage means the operation
	 * should be accepted. Otherwise it should be rejected with exactly that message.
	 */
	private static void performTestCase(int testCase, String description, String resultText,
			String expectedMessage) {

		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: " + description);
		System.out.println("______________");

		boolean expectedPass = expectedMessage.isEmpty();

		System.out.println();

		if (resultText.length() > 0) {
			if (expectedPass) {
				System.out.println("***Failure*** <" + description + "> was rejected." +
						"\nBut it was supposed to be accepted, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			} else if (resultText.equals(expectedMessage)) {
				System.out.println("***Success*** <" + description + "> was rejected." +
						"\nIt was supposed to be rejected with this message, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			} else {
				System.out.println("***Failure*** <" + description + "> was rejected," +
						"\nbut the message was not the expected one!\n");
				System.out.println("Expected message: " + expectedMessage);
				System.out.println("Actual message:   " + resultText);
				numFailed++;
			}
		} else {
			if (expectedPass) {
				System.out.println("***Success*** <" + description + "> was accepted, so this is a pass!");
				numPassed++;
			} else {
				System.out.println("***Failure*** <" + description + "> was accepted" +
						"\nBut it was supposed to be rejected, so this is a failure!");
				numFailed++;
			}
		}
	}

	// For a value that is read back from the program, such as a count, a title, or a flag
	private static void performCheck(int testCase, String description, String expected,
			String actual) {

		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Check: " + description);
		System.out.println("______________");

		System.out.println();

		if (expected.equals(actual)) {
			System.out.println("***Success*** The value was <" + actual + ">, as expected, so this is a pass!");
			numPassed++;
		} else {
			System.out.println("***Failure*** The value was <" + actual + ">" +
					"\nBut it was supposed to be <" + expected + ">, so this is a failure!");
			numFailed++;
		}
	}
}