package tests;

import authorization.LessonAuthorization;

/**
 * <p>Title: LessonAuthorizationTest Class</p>
 * 
 * <p>Description: JUnit-style tests for the LessonAuthorization prototype.
 * Covers the positive and negative cases required by the TP3 Testing Requirements
 * for Aspect 3 and Aspect 4.</p>
 * 
 * <p>Copyright: Hung Ngo © 2026</p>
 * 
 * @author Hung Ngo
 * @version 1.00 2026-11-09
 */
public class LessonAuthorizationTest {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        // Positive: owner + unlocked → deletable
        if (LessonAuthorization.isDeletable("hung", "hung", false, false)) {
            System.out.println("PASS: owner may delete unlocked lesson");
            passed++;
        } else {
            System.out.println("FAIL: owner may delete unlocked lesson");
            failed++;
        }

        // Negative: owner + lesson locked → not deletable
        if (!LessonAuthorization.isDeletable("hung", "hung", true, false)) {
            System.out.println("PASS: locked lesson cannot be deleted");
            passed++;
        } else {
            System.out.println("FAIL: locked lesson cannot be deleted");
            failed++;
        }

        // Negative: non-owner → not deletable
        if (!LessonAuthorization.isDeletable("other", "hung", false, false)) {
            System.out.println("PASS: non-owner cannot delete");
            passed++;
        } else {
            System.out.println("FAIL: non-owner cannot delete");
            failed++;
        }

        // Negative: any field locked → not deletable
        if (!LessonAuthorization.isDeletable("hung", "hung", false, true)) {
            System.out.println("PASS: field-locked lesson cannot be deleted");
            passed++;
        } else {
            System.out.println("FAIL: field-locked lesson cannot be deleted");
            failed++;
        }

        // Field lock: whole-lesson lock forces field locked
        if (LessonAuthorization.isFieldLocked(true, false, true)) {
            System.out.println("PASS: whole-lesson lock forces field locked");
            passed++;
        } else {
            System.out.println("FAIL: whole-lesson lock forces field locked");
            failed++;
        }

        // Field lock: non-owner always sees field locked
        if (LessonAuthorization.isFieldLocked(false, false, false)) {
            System.out.println("PASS: non-owner sees field as locked");
            passed++;
        } else {
            System.out.println("FAIL: non-owner sees field as locked");
            failed++;
        }

        System.out.println("\nResults: " + passed + " passed, " + failed + " failed");
        if (failed == 0) {
            System.out.println("All tests passed – requirements satisfied.");
        }
    }
}
