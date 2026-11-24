package guiRole1;


/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is where a contributor adds, views, updates, and deletes lessons learned. The lesson
 * work itself is done by ModelLessons, so this class only reads the widgets and shows the results.
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
 * @version 1.02		2026-10-02 Added the lesson add, view, update, and delete actions for HW2
 */

public class ControllerRole1Home {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
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
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
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
	
	/**********
	 * <p> Method: performRefresh() </p>
	 *
	 * <p> Description: Reloads the logged-in contributor's lessons into the list. An empty list
	 * shows the "no lessons" message instead of looking like an error. </p>
	 */
	protected static void performRefresh() {
		String owner = ViewRole1Home.theUser.getUserName();
		ViewRole1Home.currentList = ViewRole1Home.theModel.getMyLessons(owner);
		ViewRole1Home.list_Lessons.getItems().clear();
		ViewRole1Home.text_Details.clear();
		for (int i = 0; i < ViewRole1Home.currentList.size(); i++) {		
			entityClasses.Lesson l = ViewRole1Home.currentList.get(i);
			ViewRole1Home.list_Lessons.getItems().add(l.getId() + ": " + l.getTitle());
		}
		ViewRole1Home.label_ListMessage.setText(
				ViewRole1Home.theModel.getEmptyListMessage(ViewRole1Home.currentList));
	}

	/**********
	 * <p> Method: performSelect() </p>
	 *
	 * <p> Description: Shows the details of the selected lesson. </p>
	 */
	protected static void performSelect() {
		int index = ViewRole1Home.list_Lessons.getSelectionModel().getSelectedIndex();
		if (index < 0) return;
		int id = ViewRole1Home.currentList.get(index).getId();
		entityClasses.Lesson l = ViewRole1Home.theModel.viewLesson(id,
				ViewRole1Home.theUser.getUserName());
		if (l == null) { showMessage(lessonManager.ModelLessons.NOT_FOUND_MESSAGE, true); return; }
		ViewRole1Home.text_Details.setText("Title: " + l.getTitle()
        + "\nCategory: " + l.getCategory()
        + "\nDescription: " + l.getDescription()
        + "\nWhat happened: " + l.getWhatHappened()
        + "\nWhat was learned: " + l.getWhatLearned());
	}

	/**********
	 * <p> Method: performAddLesson() </p>
	 *
	 * <p> Description: Builds a lesson from the form, owned by the logged-in user, and asks the
	 * model to validate and save it. Any message is shown and nothing is saved. </p>
	 */
	protected static void performAddLesson() {
		entityClasses.Lesson l = new entityClasses.Lesson(		
				ViewRole1Home.theUser.getUserName(),
				ViewRole1Home.text_Title.getText(),
				ViewRole1Home.combo_Category.getValue(),
				ViewRole1Home.text_Description.getText(),
				ViewRole1Home.text_WhatHappened.getText(),
				ViewRole1Home.text_WhatLearned.getText());
		String error = ViewRole1Home.theModel.addLesson(l);
		if (error.isEmpty()) {
			showMessage("Lesson added.", false);
			ViewRole1Home.text_Title.clear();
			ViewRole1Home.combo_Category.setValue(null);
			ViewRole1Home.text_Description.clear();
			ViewRole1Home.text_WhatHappened.clear();
			ViewRole1Home.text_WhatLearned.clear();
			performRefresh();
		} else showMessage(error, true);
	}

	/**********
	 * <p> Method: performUpdateLesson() </p>
	 *
	 * <p> Description: Changes one field of the selected lesson. A locked field or a bad value
	 * shows its message and leaves the old value in place. </p>
	 */
	protected static void performUpdateLesson() {
		int index = ViewRole1Home.list_Lessons.getSelectionModel().getSelectedIndex();
		if (index < 0) { showMessage("Select a lesson first.", true); return; }
		String error = ViewRole1Home.theModel.editLesson(
				ViewRole1Home.currentList.get(index).getId(),
				ViewRole1Home.theUser.getUserName(),
				ViewRole1Home.combo_EditField.getValue(),
				ViewRole1Home.text_NewValue.getText());
		if (error.isEmpty()) { showMessage("Lesson updated.", false); performRefresh(); }
		else showMessage(error, true);
	}

	/**********
	 * <p> Method: performDeleteLesson() </p>
	 *
	 * <p> Description: Deletes the selected lesson unless it is locked. </p>
	 */
	protected static void performDeleteLesson() {
		int index = ViewRole1Home.list_Lessons.getSelectionModel().getSelectedIndex();
		if (index < 0) { showMessage("Select a lesson first.", true); return; }
		String error = ViewRole1Home.theModel.deleteLesson(
				ViewRole1Home.currentList.get(index).getId(),
				ViewRole1Home.theUser.getUserName());
		if (error.isEmpty()) { showMessage("Lesson deleted.", false); performRefresh(); }
		else showMessage(error, true);
	}

	/**********
	 * Private helper that shows a message in red for errors and black otherwise.
	 */
	private static void showMessage(String message, boolean isError) {
		ViewRole1Home.label_Message.setTextFill(
				isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.BLACK);
		ViewRole1Home.label_Message.setText(message);
	}
}
