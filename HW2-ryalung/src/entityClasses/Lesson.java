package entityClasses;

/*******
 * <p> Title: Lesson Class. </p>
 * 
 * <p> Description: A Lesson object holds the core information for one lesson learned, along with
 * its owner (the contributor who added it).
 * This class only holds data. Checking that the data is valid is done by a separate validator
 * class, and saving it is done by the Database class. </p>
 * 
 * <p> Copyright: Ronald Yalung © 2026 </p>
 * 
 * @author Ronald Yalung
 * 
 * @version 1.00		2026-09-27 Initial version for HW2
 */

public class Lesson {

	/*
	 * These are the private attributes of a lesson.  The id is assigned by the database when the
	 * lesson is saved, so it is 0 until then.  
	 */
	private int id = 0;
	private String owner;
	private String title;
	private String category;
	private String description;
	private String whatHappened;
	private String whatLearned;

	/*******
	 * <p> Method: Lesson(String owner, String title, String category, String description,
	 * String whatHappened, String whatLearned) </p>
	 * 
	 * <p> Description: Creates a new lesson that has not been saved yet, so its id is 0. </p>
	 * 
	 * @param owner is the username of the contributor who is adding the lesson
	 * 
	 * @param title is the short title of the lesson
	 * 
	 * @param category is the category of the lesson, such as Requirements or Testing
	 * 
	 * @param description is a description of the lesson
	 * 
	 * @param whatHappened describes what happened
	 * 
	 * @param whatLearned describes what was learned
	 */
	public Lesson(String owner, String title, String category, String description,
			String whatHappened, String whatLearned) {
		this.owner = owner;
		this.title = title;
		this.category = category;
		this.description = description;
		this.whatHappened = whatHappened;
		this.whatLearned = whatLearned;
	}

	/*******
	 * <p> Method: int getId() </p>
	 * 
	 * <p> Description: Gets the id the database gave this lesson. </p>
	 * 
	 * @return the id of the lesson, or 0 if it has not been saved yet
	 */
	public int getId() { return id; }

	/*******
	 * <p> Method: void setId(int id) </p>
	 * 
	 * <p> Description: Sets the id of the lesson. The Database class calls this after the lesson
	 * is saved. </p>
	 * 
	 * @param id is the id assigned by the database
	 */
	public void setId(int id) { this.id = id; }

	/*******
	 * <p> Method: String getOwner() </p>
	 * 
	 * <p> Description: Gets the username of the contributor who added the lesson. </p>
	 * 
	 * @return the owner's username
	 */
	public String getOwner() { return owner; }

	/*******
	 * <p> Method: String getTitle() </p>
	 * 
	 * <p> Description: Gets the title of the lesson. </p>
	 * 
	 * @return the title
	 */
	public String getTitle() { return title; }

	/*******
	 * <p> Method: void setTitle(String title) </p>
	 * 
	 * <p> Description: Sets the title of the lesson. </p>
	 * 
	 * @param title is the new title
	 */
	public void setTitle(String title) { this.title = title; }

	/*******
	 * <p> Method: String getCategory() </p>
	 * 
	 * <p> Description: Gets the category of the lesson. </p>
	 * 
	 * @return the category
	 */
	public String getCategory() { return category; }

	/*******
	 * <p> Method: void setCategory(String category) </p>
	 * 
	 * <p> Description: Sets the category of the lesson. </p>
	 * 
	 * @param category is the new category
	 */
	public void setCategory(String category) { this.category = category; }

	/*******
	 * <p> Method: String getDescription() </p>
	 * 
	 * <p> Description: Gets the description of the lesson. </p>
	 * 
	 * @return the description
	 */
	public String getDescription() { return description; }

	/*******
	 * <p> Method: void setDescription(String description) </p>
	 * 
	 * <p> Description: Sets the description of the lesson. </p>
	 * 
	 * @param description is the new description
	 */
	public void setDescription(String description) { this.description = description; }

	/*******
	 * <p> Method: String getWhatHappened() </p>
	 * 
	 * <p> Description: Gets the text describing what happened. </p>
	 * 
	 * @return what happened
	 */
	public String getWhatHappened() { return whatHappened; }

	/*******
	 * <p> Method: void setWhatHappened(String whatHappened) </p>
	 * 
	 * <p> Description: Sets the text describing what happened. </p>
	 * 
	 * @param whatHappened is the new text
	 */
	public void setWhatHappened(String whatHappened) { this.whatHappened = whatHappened; }

	/*******
	 * <p> Method: String getWhatLearned() </p>
	 * 
	 * <p> Description: Gets the text describing what was learned. </p>
	 * 
	 * @return what was learned
	 */
	public String getWhatLearned() { return whatLearned; }

	/*******
	 * <p> Method: void setWhatLearned(String whatLearned) </p>
	 * 
	 * <p> Description: Sets the text describing what was learned. </p>
	 * 
	 * @param whatLearned is the new text
	 */
	public void setWhatLearned(String whatLearned) { this.whatLearned = whatLearned; }

}