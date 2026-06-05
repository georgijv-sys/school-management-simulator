# School Management Simulator

Object-oriented Java school management simulation system featuring course scheduling, enrolment management, instructor assignment, simulation persistence, and a Swing dashboard.

## Highlights

- Live school operations dashboard with courses, students, instructors, subjects, event log, and trend chart.
- Day-by-day simulation of student arrivals, instructor hiring/leaving, enrolment, course starts, completions, cancellations, and certificate awards.
- Object-oriented model using inheritance, abstract classes, polymorphism, encapsulation, collections, exceptions, and file I/O.
- Save/load support for pausing and resuming simulations.
- Console mode is still available for the original coursework-style simulation output.

## Run

Compile:

```bash
javac -d out src/*.java
```

Run the dashboard:

```bash
java -cp out SchoolDashboardApp
```

Run the console simulator:

```bash
java -cp out Administrator
```

Run from a configuration file for a fixed number of days:

```bash
java -cp out Administrator school.txt 30
```

## Configuration Format

```text
school:Java Training School
subject:Basics,1,1,5
subject:Lab 1,2,2,2
student:Alice,F,20
Teacher:Dr Smith,F,35
Demonstrator:Alex,M,28
OOTrainer:Casey,F,31
GUITrainer:Morgan,M,33
```
