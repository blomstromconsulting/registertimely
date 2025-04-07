package dev.langchain4j.quarkus.workshop;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRepositoryTest {

    @Inject
    ActivityRepository activityRepository;

    @Inject
    ProjectRepository projectRepository;

    @BeforeAll
    @Transactional
    void setup() {

        // Create and persist a sample project
        Project project = new Project();
        project.name = "SampleProject";
        projectRepository.persist(project);

        // Create and persist two sample activities associated with the project
        Activity activity1 = new Activity();
        activity1.name = "ActivityOne";
        activity1.project = project;
        activity1.persist();

        Activity activity2 = new Activity();
        activity2.name = "ActivityTwo";
        activity2.project = project;
        activity2.persist();
    }

    @AfterAll
    @Transactional
    void tearDown() {
        activityRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void testListAllActivities() {
        List<Activity> activities = activityRepository.listAllActivities();

        assertThat(activities).hasSize(2);
    }

    @Test
    void testGetActivityByName() {
        // This should retrieve the activity with name "ActivityOne" for project "SampleProject"
        Activity activity = activityRepository.getActivityByName("ActivityOne", "SampleProject");

        assertThat(activity).hasFieldOrPropertyWithValue("name", "ActivityOne");
    }

    @Test
    void testListAllActivitiesByProjectName() {
        List<Activity> activities = activityRepository.listAllActivitiesByProjectName("SampleProject");

        assertThat(activities).hasSize(2);
    }
}