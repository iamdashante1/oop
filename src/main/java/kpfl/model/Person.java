package kpfl.model;

import kpfl.exception.ValidationException;

/**
 * PLACEHOLDER for Member 2's Person. Replace with the real file; keep these signatures
 * (constructor, getId(), getName(), getRole()) so LeagueCommissioner still compiles.
 */
public abstract class Person {
    private final String id;
    private String name;
    private int age;
    private String gender;

    protected Person(String id, String name, int age, String gender) {
        if (id == null || id.trim().isEmpty()) throw new ValidationException("Person ID is required.");
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Name is required.");
        if (age <= 0) throw new ValidationException("Age must be positive.");
        this.id = id.trim();
        this.name = name.trim();
        this.age = age;
        this.gender = gender;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " " + id + " - " + name;
    }
}
