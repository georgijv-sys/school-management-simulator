public class Demonstrator extends Instructor {

    public Demonstrator(String name, char gender, int age) {
        super(name, gender, age);
    }

    // returns true if the specialism is  2
    // meaning that demonstrator can teach these subject
    // and false is cannot (specialism is not 2)
    @Override
    public boolean canTeach(Subject subject) {
        return subject.getSpecialism() == 2;
    }
}