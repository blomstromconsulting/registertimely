package dev.langchain4j.quarkus.workshop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Activity extends PanacheEntity {

    @ManyToOne(optional = false)
    Project project;

    @OneToMany(fetch = FetchType.EAGER)
    List<ReportedTime> reportedTimes;

    @Column(unique = true)
    String name;
    String description;

}
