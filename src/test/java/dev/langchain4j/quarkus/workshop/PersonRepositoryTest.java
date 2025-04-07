package dev.langchain4j.quarkus.workshop;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonRepositoryTest {

    @Inject
    PersonRepository personRepository;

    @AfterAll
    @Transactional
    void tearDown() {
        personRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateAndFindPerson() {
        // given
        String firstName = "John";
        String lastName = "Doe";
        personRepository.createPerson(firstName, lastName);

        // when
        Person person = personRepository.findByName(firstName, lastName);

        // then
        assertThat(person).isNotNull();
        assertThat(person.firstName).isEqualTo(firstName);
        assertThat(person.lastName).isEqualTo(lastName);
    }

    @Test
    @Transactional
    public void testListAllPersons() {
        // given
        String firstName1 = "Alice";
        String lastName1 = "Smith";
        String firstName2 = "Bob";
        String lastName2 = "Jones";
        personRepository.createPerson(firstName1, lastName1);
        personRepository.createPerson(firstName2, lastName2);

        // when
        List<Person> persons = personRepository.listAllPersons();

        // then
        assertThat(persons).isNotEmpty();
        assertThat(persons)
                .extracting("firstName", "lastName")
                .contains(tuple(firstName1, lastName1), tuple(firstName2, lastName2));
    }
}