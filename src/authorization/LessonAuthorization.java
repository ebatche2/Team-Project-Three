package authorization;

/**
 * <p>Title: LessonAuthorization Class</p>
 * 
 * <p>Description: Small risk-reduction prototype that enforces role and lock
 * rules so each staff role can only reach its own functions (Aspect 3) and
 * permissions are enforced the same way everywhere (Aspect 4). Built to satisfy
 * the access-control requirements assigned to Hung.</p>
 * 
 * <p>Copyright: Hung Ngo © 2026</p>
 * 
 * @author Hung Ngo
 * @version 1.00 2026-11-09 Initial prototype for HW3 Task 5
 */
public class LessonAuthorization {

    /**
     * Returns true only when the requester is the owner and the lesson is not locked.
     * This is the single decision point that blocks contributors from deleting locked lessons.
     * 
     * @param requesterUsername the username of the person trying to delete
     * @param ownerUsername     the username that created the lesson
     * @param isLessonLocked    true if the whole lesson is locked
     * @param isAnyFieldLocked  true if any individual field is locked
     * @return true if the requester may delete the lesson
     */
    public static boolean isDeletable(String requesterUsername, String ownerUsername,
                                      boolean isLessonLocked, boolean isAnyFieldLocked) {
        // Why: a locked lesson or any locked field must never be deletable by the contributor
        // (C-2 and Aspect 4). Checking both flags in one place guarantees the rule is applied
        // the same way no matter which screen calls this method.
        if (isLessonLocked || isAnyFieldLocked) {
            return false;
        }
        // Why: only the owner may delete an unlocked lesson (Aspect 3). A null or different
        // username is treated as unauthorized so a missing check cannot be bypassed.
        if (requesterUsername == null || !requesterUsername.equals(ownerUsername)) {
            return false;
        }
        return true;
    }

    /**
     * Returns true when a field must be read-only for the current user.
     * 
     * @param isLessonLocked true if the whole lesson is locked
     * @param isFieldLocked  true if this specific field is locked
     * @param isOwner        true if the requester owns the lesson
     * @return true if the field must be treated as locked
     */
    public static boolean isFieldLocked(boolean isLessonLocked, boolean isFieldLocked, boolean isOwner) {
        // Why: a whole-lesson lock forces every field to be locked, matching the existing
        // TP2 LessonsLearned behavior and Aspect 4 (same rule everywhere).
        if (isLessonLocked) {
            return true;
        }
        // Why: even if the field itself is not locked, a non-owner must never edit it
        // (Aspect 3 – each role reaches only its own functions).
        if (!isOwner) {
            return true;
        }
        return isFieldLocked;
    }
}
