package dev.langchain4j.quarkus.workshop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Person extends PanacheEntity {

    @OneToMany(fetch = FetchType.EAGER)
    List<ReportedTime> reportedTimes;

    String firstName;
    String lastName;

}
