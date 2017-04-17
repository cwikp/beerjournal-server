package com.beerjournal.breweriana.persistence.category;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@EqualsAndHashCode(exclude = "id")
public class Category {
    @Id
    private final String id;
    private final String name;

    @PersistenceConstructor
    Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Category of(String name) {
        return new Category(null, name);
    }
}
