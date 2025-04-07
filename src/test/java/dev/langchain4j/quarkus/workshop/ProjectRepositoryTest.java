package dev.langchain4j.quarkus.workshop;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProjectRepositoryTest {

    @Inject
    ProjectRepository projectRepository;

    @BeforeAll
    @Transactional
    public void setUp() {
        // Create and persist test projects
        Project project1 = new Project();
        project1.name = "TestProject";
        project1.description = "Test Description";
        projectRepository.persist(project1);

        Project project2 = new Project();
        project2.name = "AnotherProject";
        project2.description = "Another Description";
        projectRepository.persist(project2);
    }

    @AfterAll
    @Transactional
    void tearDown() {
        projectRepository.deleteAll();
    }


    @Test
    @Transactional
    public void testListAllProjects() {
        // Act
        List<Project> projects = projectRepository.listAllProjects();

        // Assert using AssertJ
        Assertions.assertThat(projects)
                .isNotNull()
                .hasSize(2)
                .extracting("name")
                .containsExactly("TestProject", "AnotherProject");
    }

    @Test
    @Transactional
    public void testFindProjectByName() {
        // Act
        Project foundProject = projectRepository.findProjectByName("TestProject");

        // Assert using AssertJ
        Assertions.assertThat(foundProject).isNotNull();
        Assertions.assertThat(foundProject.name).isEqualTo("TestProject");
        Assertions.assertThat(foundProject.description).isEqualTo("Test Description");
    }

    @Test
    @Transactional
    public void testFindProjectByNameNotFound() {
        // Assert that a non-existing project name throws the expected exception
        Assertions.assertThatThrownBy(() -> projectRepository.findProjectByName("NonExisting"))
                .isInstanceOf(Exceptions.ProjectNotFoundException.class)
                .hasMessageContaining("NonExisting");
    }
}