public class SimulationEvent {

    public enum Type {
        STUDENT_JOINED,
        STUDENT_LEFT,
        INSTRUCTOR_JOINED,
        INSTRUCTOR_LEFT,
        COURSE_CREATED,
        COURSE_STARTED,
        COURSE_CANCELLED,
        COURSE_COMPLETED,
        INSTRUCTOR_ASSIGNED,
        STUDENT_ENROLLED,
        SAVE,
        LOAD
    }

    private int day;
    private Type type;
    private String message;

    public SimulationEvent(int day, Type type, String message) {
        this.day = day;
        this.type = type;
        this.message = message;
    }

    public int getDay() {
        return day;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return "Day " + day + " - " + message;
    }
}
