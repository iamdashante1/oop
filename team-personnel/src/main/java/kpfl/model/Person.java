package kpfl.model;

import kpfl.exception.ValidationException;

/** Shared details for players, coaches, officials, spectators and the commissioner. */
public abstract class Person {
    // An ID stays fixed because other parts of the app use it to identify this person.
    private final String id;
    private String name;
    private int age;
    private String gender;


    // Check the shared details when any kind of person is created.
    protected Person(String id, String name, int age, String gender) {
        if (id == null || id.trim().isEmpty())
            throw new ValidationException("Person ID is required.");
        this.id = id.trim();
        setName(name);
        // Set age here directly; Player and Coach check their own age rules next.
        if (age <= 0) throw new ValidationException("Age must be positive.");
        this.age = age;
        setGender(gender);
    }

    // getters mutators
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }

    public void setName(String name) {
        // Trim extra spaces so searches and printed names stay consistent.
        if (name == null || name.trim().isEmpty())
            throw new ValidationException("Name is required.");
        this.name = name.trim();
    }

    public void setAge(int age) {
        if (age <= 0) throw new ValidationException("Age must be positive.");
        this.age = age;
    }

    public void setGender(String gender) {
        if (gender == null || gender.trim().isEmpty())
            throw new ValidationException("Gender is required.");
        this.gender = gender.trim();
    }

    // Each kind of person supplies its own role, for example "Player" or "Coach".
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " " + id + " - " + name;
    }
}
