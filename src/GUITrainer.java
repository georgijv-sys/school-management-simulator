public class GUITrainer extends Teacher {

    public GUITrainer(String name, char gender, int age) {
        super(name, gender, age);
    }

    // returns true if the specialism is 1 or 2 or 4
    // meaning that GUITrainer can teach these subject
    // and false is cannot (specialism is not 1 or 2 or 4)
    @Override
    public boolean canTeach(Subject subject) {
        int s = subject.getSpecialism();
        return s == 1 || s == 2 || s == 4;
    }
}