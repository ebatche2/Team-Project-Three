package entityClasses;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Title: LessonList Class. </p>
 * 
 * <p> Description: A LessonList holds any group of lessons: all of a contributor's lessons, a
 * smaller group such as the result of a search or a filter, or no lessons at all. The same class
 * is used for every group so the rest of the program does not care how the group was made. There
 * is no fixed upper limit on the number of lessons it can hold. </p>
 * 
 * <p> Copyright: Ronald Yalung © 2026 </p>
 * 
 * @author Ronald Yalung
 * 
 * @version 1.00		2026-09-30 Initial version for HW2
 */

public class LessonList {

	/*
	 * An ArrayList is used because it grows as needed, which gives the "no fixed upper limit"
	 * behavior the assignment asks for.
	 */
	private final List<Lesson> lessons = new ArrayList<Lesson>();

	/*******
	 * <p> Method: LessonList() </p>
	 * 
	 * <p> Description: Creates an empty list of lessons. </p>
	 */
	public LessonList() {
	}

	/*******
	 * <p> Method: void add(Lesson lesson) </p>
	 * 
	 * <p> Description: Adds one lesson to the end of the list. </p>
	 * 
	 * @param lesson is the lesson to add
	 */
	public void add(Lesson lesson) {
		lessons.add(lesson);
	}

	/*******
	 * <p> Method: Lesson get(int index) </p>
	 * 
	 * <p> Description: Gets the lesson at a position in the list. The first lesson is at 0. </p>
	 * 
	 * @param index is the position of the lesson
	 * 
	 * @return the lesson at that position
	 */
	public Lesson get(int index) {
		return lessons.get(index);
	}

	/*******
	 * <p> Method: int size() </p>
	 * 
	 * <p> Description: Gets the number of lessons in the list. </p>
	 * 
	 * @return the number of lessons, which is 0 for an empty list
	 */
	public int size() {
		return lessons.size();
	}

	/*******
	 * <p> Method: boolean isEmpty() </p>
	 * 
	 * <p> Description: Checks whether the list has no lessons. </p>
	 * 
	 * @return true if the list is empty, false if it has at least one lesson
	 */
	public boolean isEmpty() {
		return lessons.isEmpty();
	}

	/*******
	 * <p> Method: LessonList filterByCategory(String category) </p>
	 * 
	 * <p> Description: Builds a new list that holds only the lessons with the given category.
	 * The original list is not changed. The new list is empty if no lesson matches. </p>
	 * 
	 * @param category is the category to keep, such as "Design"
	 * 
	 * @return a new LessonList with only the lessons in that category
	 */
	public LessonList filterByCategory(String category) {
		LessonList subset = new LessonList();
		for (Lesson lesson : lessons) {
			if (lesson.getCategory().equals(category)) subset.add(lesson);
		}
		return subset;
	}
}