package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

    @Tool("Retrieves a list of all persons registered in the system. Returns a list of Person objects containing id, firstName, and lastName. The list will be empty if no persons are registered.")
    @Transactional
    public List<Person> listAllPersons() {
        return findAll().list();
    }

    @Tool("Creates and persists a new person in the system with the specified first name and last name. Both parameters are required. This method does not check for duplicates, so multiple persons with the same name can be created. Returns void.")
    @Transactional
    public void createPerson(String firstName, String lastName) {
        var person = new Person();
        person.firstName = firstName;
        person.lastName = lastName;
        persist(person);
    }

    @Tool("Retrieves a person by their first name and last name. Both parameters are case-insensitive. Returns the Person object if found. Throws PersonNotFoundException if no person with the given first name and last name exists in the system.")
    @Transactional
    public Person findByName(String firstName, String lastName) {
        return find("lower(firstName) = ?1 and lower(lastName) = ?2", firstName.toLowerCase(), lastName.toLowerCase()).firstResultOptional().orElseThrow(() -> new Exceptions.PersonNotFoundException(firstName, lastName));
    }
}
