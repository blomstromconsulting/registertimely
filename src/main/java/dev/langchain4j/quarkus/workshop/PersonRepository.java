package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    @Tool("List all persons that are in the system")
    @Transactional
    public List<Person> listAllPersons() {
        return findAll().list();
    }

    @Tool("Add a person by first name and last name")
    @Transactional
    public void createPerson(String firstName, String lastName) {
        var person = new Person();
        person.firstName = firstName;
        person.lastName = lastName;
        persist(person);
    }

    @Tool("Get a person by first name and last name")
    @Transactional
    public Person findByName(String firstName, String lastName) {
        return find("lower(firstName) = ?1 and lower(lastName) = ?2", firstName.toLowerCase(), lastName.toLowerCase()).firstResultOptional().orElseThrow(() -> new Exceptions.PersonNotFoundException(firstName, lastName));
    }
}
