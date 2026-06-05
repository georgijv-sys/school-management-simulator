import java.util.ArrayList;

public class Student extends Person {

    private ArrayList<Integer> certificates = new ArrayList<>();

    public Student(String name, char gender, int age) {
        super(name, gender, age);
    }

    // gets a certificate when graduating from the course
    // the certificate is added to the arraylist that contains all the certificates
    public void graduate(Subject subject) {
        certificates.add(subject.getID());
    }

    public ArrayList<Integer> getCertificates() {
        return certificates;
    }

    //checks if a student has a certificate for this subject
    public boolean hasCertificate(Subject subject) {
        return certificates.contains(subject.getID());
    }
}