package dev.langchain4j.quarkus.workshop;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Entity
public class ReportedTime extends PanacheEntity {

    @ManyToOne
    Activity activity;

    @ManyToOne
    Person person;

    LocalDate date;

    BigDecimal duration;

}
