package guiRole2;

import java.util.*;
import database.Database;
import entityClasses.SearchTermValidator;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import limits.AIRequestLimiter;

/*******
 * <p> Title: ControllerRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerRole2Home {
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerRole2Home() {
	}
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */
	
	/** A variable to allow the code to access the database **/
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/** Rate-limits curator AI requests per hour (TP3 Aspect 2). */
	private static final AIRequestLimiter aiLimiter = new AIRequestLimiter();
	
	/** The id of the lesson selected in the combo box, or -1 when no lesson is selected **/
	private static int selectedLessonId = -1;
	
	/**********
	 * <p> Method: refreshLessonList() </p>
	 * 
	 * <p> Description: This method reloads the combo box with every lesson in the system and
	 * selects "&lt;Select a Lesson&gt;". </p>
	 * 
	 */
	protected static void refreshLessonList() {
		List<String> lessons = theDatabase.getAllLessonList();
		ViewRole2Home.combobox_SelectLesson.getItems().setAll(lessons);
		ViewRole2Home.combobox_SelectLesson.getSelectionModel().select(0);
	}
	
	/**********
	 * <p> Method: selectLesson() </p>
	 * 
	 * <p> Description: This method is called when the combo box selection changes.  It shows the
	 * selected lesson's core information and sets the check boxes to its current locks. </p>
	 * 
	 */
	protected static void selectLesson() {
		String selected = ViewRole2Home.combobox_SelectLesson.getValue();
		
		// No lesson selected, so clear the fields and disable the check boxes
		if (selected == null || selected.startsWith("<")) {
			selectedLessonId = -1;
			ViewRole2Home.text_Title.setText("");
			ViewRole2Home.text_Description.setText("");
			ViewRole2Home.checkbox_LockLesson.setSelected(false);
			ViewRole2Home.checkbox_LockTitle.setSelected(false);
			ViewRole2Home.checkbox_LockDescription.setSelected(false);
			ViewRole2Home.checkbox_LockLesson.setDisable(true);
			ViewRole2Home.checkbox_LockTitle.setDisable(true);
			ViewRole2Home.checkbox_LockDescription.setDisable(true);
			ViewRole2Home.checkbox_TimeEffortVisible.setSelected(true);
			ViewRole2Home.checkbox_TimeEffortVisible.setDisable(true);
			ViewRole2Home.button_AISearchTerms.setDisable(true);
			ViewRole2Home.button_AIRefineLesson.setDisable(true);
			ViewRole2Home.text_AIOutput.setText("");
			updateAIStatusLabel();
			return;
		}
		
		// The entries have the form "id: title (contributor)", so the id is before the first ':'
		selectedLessonId = Integer.parseInt(selected.substring(0, selected.indexOf(':')));
		if (!theDatabase.getLessonDetailsForCurator(selectedLessonId)) return;
		
		ViewRole2Home.text_Title.setText(theDatabase.getCurrentLessonTitle());
		ViewRole2Home.text_Description.setText(theDatabase.getCurrentLessonDescription());
		
		// Setting a check box from code does not trigger its action, so this does not cause
		// any lock changes
		ViewRole2Home.checkbox_LockLesson.setSelected(theDatabase.getCurrentLessonLocked());
		ViewRole2Home.checkbox_LockTitle.setSelected(theDatabase.getCurrentTitleLocked());
		ViewRole2Home.checkbox_LockDescription.setSelected(
				theDatabase.getCurrentDescriptionLocked());
		ViewRole2Home.checkbox_LockLesson.setDisable(false);
		ViewRole2Home.checkbox_LockTitle.setDisable(false);
		ViewRole2Home.checkbox_LockDescription.setDisable(false);
		ViewRole2Home.checkbox_TimeEffortVisible.setSelected(
				theDatabase.isTimeEffortVisible(selectedLessonId));
		ViewRole2Home.checkbox_TimeEffortVisible.setDisable(false);
		ViewRole2Home.button_AISearchTerms.setDisable(false);
		ViewRole2Home.button_AIRefineLesson.setDisable(false);
		updateAIStatusLabel();
	}
	
	/**********
	 * <p> Method: performLockChange(String field, boolean locked) </p>
	 * 
	 * <p> Description: This method locks or unlocks the entire selected lesson or one of its
	 * fields, then reloads the lesson so the check boxes show what is in the database. </p>
	 * 
	 * @param field		"Lesson", "Title", or "Description"
	 * @param locked	true to lock, false to unlock
	 */
	protected static void performLockChange(String field, boolean locked) {
		if (selectedLessonId < 0) return;
		theDatabase.updateLessonLock(selectedLessonId, field, locked);
		selectLesson();
	}

	/**
	 * Updates whether time and effort stats are visible to the contributor (Elijah's Epic / Aspect 4).
	 */
	protected static void performTimeEffortVisibilityChange(boolean visible) {
		if (selectedLessonId < 0) return;
		theDatabase.updateTimeEffortVisible(selectedLessonId, visible);
		selectLesson();
	}

	/**
	 * Generates validated, rate-limited AI search terms for the selected lesson (Ronald's Epic).
	 */
	protected static void performGenerateAISearchTerms() {
		if (selectedLessonId < 0) return;
		String userName = ViewRole2Home.theUser.getUserName();
		String title = ViewRole2Home.text_Title.getText().trim();
		String desc = ViewRole2Home.text_Description.getText().trim();

		String validationError = SearchTermValidator.checkTerm(title);
		if (!validationError.isEmpty()) {
			showAlert("Invalid Lesson Input", validationError);
			return;
		}

		String limitMsg = aiLimiter.tryRequest(userName, System.currentTimeMillis());
		if (!limitMsg.isEmpty()) {
			showAlert("AI Request Limit Reached", limitMsg);
			updateAIStatusLabel();
			return;
		}

		Set<String> keywords = new LinkedHashSet<String>();
		for (String word : (title + " " + desc).toLowerCase().split("[^a-z0-9]+")) {
			if (word.length() >= 4) keywords.add(word);
			if (keywords.size() >= 10) break;
		}
		ViewRole2Home.text_AIOutput.setText("Suggested Search Terms: " + String.join(", ", keywords));
		updateAIStatusLabel();
	}

	/**
	 * Generates validated, rate-limited AI refinement suggestions for the selected lesson (Hung's Epic).
	 */
	protected static void performAIRefineLesson() {
		if (selectedLessonId < 0) return;
		String userName = ViewRole2Home.theUser.getUserName();
		String title = ViewRole2Home.text_Title.getText().trim();
		String desc = ViewRole2Home.text_Description.getText().trim();

		if (title.isEmpty() || desc.isEmpty()) {
			showAlert("Invalid Lesson Input", "Lesson title and description must not be empty.");
			return;
		}

		String limitMsg = aiLimiter.tryRequest(userName, System.currentTimeMillis());
		if (!limitMsg.isEmpty()) {
			showAlert("AI Request Limit Reached", limitMsg);
			updateAIStatusLabel();
			return;
		}

		String refined = "Refined Lesson Summary:\nTitle: " + title
				+ "\nKey Takeaway: " + desc.replaceAll("\\s+", " ").trim();
		ViewRole2Home.text_AIOutput.setText(refined);
		updateAIStatusLabel();
	}

	private static void updateAIStatusLabel() {
		if (ViewRole2Home.theUser == null) return;
		int left = aiLimiter.getRemaining(ViewRole2Home.theUser.getUserName(), System.currentTimeMillis());
		ViewRole2Home.label_AIStatus.setText("AI requests left this hour: " + left);
	}

	private static void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole2Home.theStage, ViewRole2Home.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole2Home.theStage);
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
}
