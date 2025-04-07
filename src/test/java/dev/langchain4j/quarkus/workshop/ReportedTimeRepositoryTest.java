package dev.langchain4j.quarkus.workshop;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@QuarkusTest
public class ReportedTimeRepositoryTest {

    @Inject
    ReportedTimeRepository reportedTimeRepository;

    @Inject
    PersonRepository personRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ActivityRepository activityRepository;

    private Person testPerson;
    private Project testProject;
    private Activity testActivity;

    @BeforeEach
    @Transactional
    public void setup() {
        // Create and persist test Person
        testPerson = new Person();
        testPerson.firstName = "Mick";
        testPerson.lastName = "Doe";
        personRepository.persist(testPerson);

        // Create and persist test Project
        testProject = new Project();
        testProject.name = "TestProjectTwo";
        testProject.description = "Test Project Description";
        projectRepository.persist(testProject);

        // Create and persist test Activity linked to the Project
        testActivity = new Activity();
        testActivity.name = "TestActivity";
        testActivity.description = "Test Activity Description";
        testActivity.project = testProject;
        activityRepository.persist(testActivity);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        reportedTimeRepository.deleteAll();
        activityRepository.deleteAll();
        projectRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateReportedTime() {
        LocalDate date = LocalDate.now();
        BigDecimal duration = BigDecimal.valueOf(8);

        ReportedTime rt = reportedTimeRepository.createReportedTime(
                testProject.name,
                testActivity.name,
                testPerson.firstName,
                testPerson.lastName,
                date,
                duration
        );

        assertThat(rt).isNotNull();
        assertThat(rt.activity.id).isEqualTo(testActivity.id);
        assertThat(rt.person.id).isEqualTo(testPerson.id);
        assertThat(rt.date).isEqualTo(date);
        assertThat(rt.duration).isEqualByComparingTo(duration);
    }

    @Test
    @Transactional
    public void testDeleteReportedTime() {
        LocalDate date = LocalDate.now();
        BigDecimal duration = BigDecimal.valueOf(4);

        // First, create a reported time entry.
        ReportedTime rt = reportedTimeRepository.createReportedTime(
                testProject.name,
                testActivity.name,
                testPerson.firstName,
                testPerson.lastName,
                date,
                duration
        );

        // Delete the entry.
        boolean deleted = reportedTimeRepository.deleteReportedTime(
                testProject.name,
                testActivity.name,
                testPerson.firstName,
                testPerson.lastName,
                date,
                duration
        );
        assertThat(deleted).isTrue();

        // Verify that the reported time is no longer in the repository.
        List<ReportedTime> found = reportedTimeRepository.list("id", rt.id);
        assertThat(found).isEmpty();
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndTimespan() {
        LocalDate today = LocalDate.now();
        // Create two entries within the timespan and one outside.
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.minusDays(1), BigDecimal.valueOf(5));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(3));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.plusDays(10), BigDecimal.valueOf(2));

        // List entries from yesterday to today.
        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndTimespan(testPerson.firstName, testPerson.lastName,
                        today.minusDays(1), today);
        assertThat(results).hasSize(2);
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriod() {
        LocalDate today = LocalDate.now();
        // Create entries assumed to be in "THIS_MONTH".
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.minusDays(2), BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.minusDays(1), BigDecimal.valueOf(6));
        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "THIS_MONTH");
        assertThat(results).isNotEmpty();

        // Test invalid period
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "INVALID_PERIOD")
        );
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid period format");
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndProject() {
        LocalDate date = LocalDate.now();
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                date, BigDecimal.valueOf(7));
        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndProject(testPerson.firstName, testPerson.lastName, testProject.name);
        assertThat(results).hasSize(1);
        ReportedTime rt = results.getFirst();
        assertThat(rt.activity.project.id).isEqualTo(testProject.id);
        assertThat(rt.person.id).isEqualTo(testPerson.id);
    }

    @Test
    @Transactional
    public void testListReportedTimesForProjectAndTimespan() {
        LocalDate today = LocalDate.now();
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(3));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.plusDays(1), BigDecimal.valueOf(5));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today.plusDays(10), BigDecimal.valueOf(2));
        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForProjectAndTimespan(testProject.name, today, today.plusDays(1));
        assertThat(results).hasSize(2);
    }
}
