package com.simplicity.services.spring;

public class PersonBuilder {
    private Long id;
    private String name;
    private Integer age;

    public static PersonBuilder aDefaultPerson() {
        return new PersonBuilder()
                .withId(100L)
                .withName("Adam Smith")
                .withAge(43);
    }

    public PersonBuilder withAge(Integer age) {
        this.age = age;
        return this;
    }

    public PersonBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PersonBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public PersonRecord build() {
        return new PersonRecord(id, name, age);
    }

}

