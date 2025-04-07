package dev.langchain4j.quarkus.workshop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Project extends PanacheEntity {

    @Column(unique = true)
    String name;
    String description;

}
