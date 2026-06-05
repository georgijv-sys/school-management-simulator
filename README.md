# School Management Simulator

Object-oriented Java simulation of a training school. The system models students, instructors, subjects, course scheduling, enrolment, course completion, certificate awards, and simulation persistence, with both a Swing dashboard and a console runner.

## Features

- Interactive Swing dashboard for live school operations.
- Day-by-day simulation of arrivals, instructor changes, enrolment, course starts, cancellations, completions, and graduations.
- Course assignment based on instructor specialisms.
- Save/load support for resuming a simulation, including the current day.
- Configuration-file loader with validation and clear error messages.
- Console mode for fixed-length or continuous simulation runs.

## Tech

- Java
- Java Swing
- Object-oriented design: inheritance, abstraction, polymorphism, encapsulation, collections, exceptions, and file I/O
- No external dependencies

## Project Structure

```text
src/
  Administrator.java        Simulation runner, loading, saving, and CLI entry point
  SchoolDashboardApp.java   Swing dashboard entry point
  School.java               Core school state and daily simulation logic
  Course.java               Course lifecycle, enrolment, and completion logic
  Person.java               Base class for people in the school
  Student.java              Student certificates and graduation state
  Instructor.java           Abstract instructor contract
  Teacher.java              Core/lab instructor
  Demonstrator.java         Lab instructor
  OOTrainer.java            Object-oriented programming trainer
  GUITrainer.java           GUI trainer
  Subject.java              Subject metadata
  SimulationEvent.java      Dashboard and console event model
```

## Quick Start

Compile:

```bash
javac -d out src/*.java
```

Run the dashboard:

```bash
java -cp out SchoolDashboardApp
```

Run a fixed-length console simulation from a configuration file:

```bash
java -cp out Administrator school.txt 30
```

Run the default console simulation continuously:

```bash
java -cp out Administrator
```

## Configuration

Example `school.txt`:

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

Subject format:

```text
subject:<description>,<id>,<specialism>,<duration>
```

Specialisms:

```text
1 = Core
2 = Lab
3 = Object-oriented programming
4 = GUI programming
```

## Verification

The project currently compiles with:

```bash
javac -d out src/*.java
```

Basic runtime checks covered:

- Loading a configuration file and running a 3-day console simulation.
- Saving a simulation and loading it back with the correct day count.
