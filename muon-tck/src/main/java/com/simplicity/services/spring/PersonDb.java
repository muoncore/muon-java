package com.simplicity.services.spring;

import java.util.HashMap;
import java.util.Map;

import static com.simplicity.services.spring.PersonBuilder.aDefaultPerson;

public class PersonDb {
    public static Map<Long, PersonRecord> PERSON_DB = new HashMap<Long, PersonRecord>() {{
        put(1L, aDefaultPerson()
                .withId(1L)
                .withName("John Smith")
                .withAge(34)
                .build());
        put(2L, aDefaultPerson()
                .withId(2L)
                .withName("John Scott")
                .withAge(23)
                .build());
        put(3L, aDefaultPerson()
                .withId(3L)
                .withName("Adam Williams")
                .withAge(55)
                .build());
        put(4L, aDefaultPerson()
                .withId(4L)
                .withName("Martin Hansson")
                .withAge(40)
                .build());
        put(5L, aDefaultPerson()
                .withId(5L)
                .withName("Adam Morton")
                .withAge(30)
                .build());
        put(6L, aDefaultPerson()
                .withId(6L)
                .withName("Hans Harper")
                .withAge(22)
                .build());
    }};
}
