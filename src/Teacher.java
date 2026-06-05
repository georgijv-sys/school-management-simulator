public class Teacher extends Instructor{

    public Teacher(String name, char gender, int age) {
        super(name, gender, age);
    }

    // returns true if the specialism is 1 or 2
    // meaning that teacher can teach these subject
    // and false is cannot (specialism is not 1 or 2)
    @Override
    public boolean canTeach(Subject subject) {
        return subject.getSpecialism() == 1 || subject.getSpecialism() == 2;
    }
}
