package lessonManager;

import database.Database;
import entityClasses.Lesson;
import entityClasses.LessonList;
import lessonValidator.LessonValidator;

/*******
 * <p> Title: ModelLessons Class. </p>
 * 
 * <p> Description: This class connects the LessonValidator and the Database class. Every
 * operation on a lesson goes through here, so the same rules are followed no matter who calls it:
 * check that the lesson belongs to the contributor, check that the new value is valid, and only
 * then change the database. The methods that change data return an empty String when they
 * worked, or a helpful message when they did not, and nothing is changed when a message is
 * returned. </p>
 * 
 * <p> Copyright: Ronald Yalung © 2026 </p>
 * 
 * @author Ronald Yalung
 * 
 * @version 1.00		2026-10-01 Initial version for HW2
 */

public class ModelLessons {

	/** The message shown when a lesson does not exist or belongs to someone else. */
	public static final String NOT_FOUND_MESSAGE =
			"Lesson not found or you do not have access to it.";

	/** The message shown when a contributor has not added any lessons. */
	public static final String NO_LESSONS_MESSAGE = "You have not added any lessons yet.";

	/*
	 * The Database object is passed in rather than created here, so the whole application (and
	 * the tests) share one database connection.
	 */
	private final Database database;

	/*******
	 * <p> Method: ModelLessons(Database database) </p>
	 * 
	 * <p> Description: Creates a model that uses the given database. </p>
	 * 
	 * @param database is the connected Database object to use
	 */
	public ModelLessons(Database database) {
		this.database = database;
	}

	/*******
	 * <p> Method: String addLesson(Lesson lesson) </p>
	 * 
	 * <p> Description: Checks every field of a new lesson and, if they are all valid, saves it.
	 * The fields are checked in the order title, category, description, what happened, and what
	 * was learned, and the first problem found is reported. When the lesson is saved, its id is
	 * set. </p>
	 * 
	 * @param lesson is the new lesson to add
	 * 
	 * @return an empty String if the lesson was saved, otherwise a message that says what is
	 * 		wrong
	 */
	// Validation happens here, before the Database is touched, so a bad lesson is never saved
	public String addLesson(Lesson lesson) {
		if (lesson == null) return "No lesson was given.";
		if (lesson.getOwner() == null || lesson.getOwner().trim().isEmpty())
			return "A lesson must have an owner.";
		String error = LessonValidator.validateTitle(lesson.getTitle());
		if (!error.isEmpty()) return error;
		error = LessonValidator.validateCategory(lesson.getCategory());
		if (!error.isEmpty()) return error;
		error = LessonValidator.validateText("Description", lesson.getDescription());
		if (!error.isEmpty()) return error;
		error = LessonValidator.validateText("What happened", lesson.getWhatHappened());
		if (!error.isEmpty()) return error;
		error = LessonValidator.validateText("What was learned", lesson.getWhatLearned());
		if (!error.isEmpty()) return error;
		if (database.createLesson(lesson) < 0)
			return "The lesson could not be saved. Please try again.";
		return "";
	}

	/*******
	 * <p> Method: LessonList getMyLessons(String owner) </p>
	 * 
	 * <p> Description: Gets all of the lessons a contributor has added. The list may be empty. </p>
	 * 
	 * @param owner is the username of the contributor
	 * 
	 * @return a LessonList of only that contributor's lessons
	 */
	public LessonList getMyLessons(String owner) {
		return database.getLessonsByOwner(owner);
	}

	/*******
	 * <p> Method: String getEmptyListMessage(LessonList list) </p>
	 * 
	 * <p> Description: Gives the message to show when a list has no lessons, so an empty list
	 * is explained instead of looking like an error. </p>
	 * 
	 * @param list is the list that was displayed
	 * 
	 * @return the "no lessons" message if the list is empty, otherwise an empty String
	 */
	public String getEmptyListMessage(LessonList list) {
		return list.isEmpty() ? NO_LESSONS_MESSAGE : "";
	}

	/*******
	 * <p> Method: Lesson viewLesson(int id, String requester) </p>
	 * 
	 * <p> Description: Gets one lesson for a contributor to look at, but only if it is theirs.
	 * The lesson says which of its fields are locked. </p>
	 * 
	 * @param id is the id of the lesson
	 * 
	 * @param requester is the username of the contributor asking
	 * 
	 * @return the lesson, or null if it does not exist or belongs to someone else (the caller
	 * 		shows NOT_FOUND_MESSAGE)
	 */
	public Lesson viewLesson(int id, String requester) {
		return database.getLessonForOwner(id, requester);
	}

	/*******
	 * <p> Method: String editLesson(int id, String owner, String field, String newValue) </p>
	 * 
	* <p> Description: Changes one field of a contributor's lesson. The lesson must be theirs and
	 * the new value must be valid. If either fails the old value is kept and a message is
	 * returned. The field names are title, category, description, whatHappened, and
	 * whatLearned. </p>
	 * 
	 * @param id is the id of the lesson
	 * 
	 * @param owner is the username of the contributor making the change
	 * 
	 * @param field is the name of the field to change
	 * 
	 * @param newValue is the new value
	 * 
	 * @return an empty String if the field was changed, otherwise a message that says why not
	 */
	// The lock check comes before the value check, because a locked field cannot change at all
	public String editLesson(int id, String owner, String field, String newValue) {
		Lesson lesson = database.getLessonForOwner(id, owner);
		if (lesson == null) return NOT_FOUND_MESSAGE;
		if (field == null) return "Select the field to change.";

		String error;
		if (field.equals("title")) error = LessonValidator.validateTitle(newValue);
		else if (field.equals("category")) error = LessonValidator.validateCategory(newValue);
		else if (field.equals("description")) error = LessonValidator.validateText("Description", newValue);
		else if (field.equals("whatHappened")) error = LessonValidator.validateText("What happened", newValue);
		else if (field.equals("whatLearned")) error = LessonValidator.validateText("What was learned", newValue);
		else return "Unknown field '" + field + "'.";

		if (!error.isEmpty()) return error;
		if (!database.updateLessonField(id, owner, field, newValue))
			return "The lesson could not be updated. Please try again.";
		return "";
	}
	/*******
	 * <p> Method: String deleteLesson(int id, String owner) </p>
	 * 
	 * <p> Description: Deletes a contributor's lesson. The lesson must be theirs, so a
	 * contributor cannot delete another contributor's lesson. </p>
	 * 
	 * @param id is the id of the lesson
	 * 
	 * @param owner is the username of the contributor deleting the lesson
	 * 
	 * @return an empty String if the lesson was deleted, otherwise a message that says why not
	 */
	public String deleteLesson(int id, String owner) {
		if (database.getLessonForOwner(id, owner) == null) return NOT_FOUND_MESSAGE;
		if (!database.deleteLesson(id, owner))
			return "The lesson could not be deleted. Please try again.";
		return "";
	}
}