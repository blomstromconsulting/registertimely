package dev.langchain4j.quarkus.workshop;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
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
    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriodThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        // Create entries within this week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                monday, BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                monday.plusDays(1), BigDecimal.valueOf(6));

        // Create entry outside this week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                monday.minusDays(7), BigDecimal.valueOf(2));

        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "THIS_WEEK");

        assertThat(results).hasSize(2);
        for (ReportedTime rt : results) {
            assertThat(rt.date).isAfterOrEqualTo(monday);
            assertThat(rt.date).isBeforeOrEqualTo(sunday);
        }
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriodLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

        // Create entries within last week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                lastMonday, BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                lastSunday, BigDecimal.valueOf(6));

        // Create entry outside last week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(2));

        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "LAST_WEEK");

        assertThat(results).hasSize(2);
        for (ReportedTime rt : results) {
            assertThat(rt.date).isAfterOrEqualTo(lastMonday);
            assertThat(rt.date).isBeforeOrEqualTo(lastSunday);
        }
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriodWeekNumber() {
        LocalDate today = LocalDate.now();
        int targetWeek = today.get(WeekFields.ISO.weekOfYear()) + 1; // Next week
        LocalDate nextMonday = today.plusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate nextSunday = nextMonday.plusDays(6);

        // Create entries within next week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                nextMonday, BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                nextSunday, BigDecimal.valueOf(6));

        // Create entry outside next week
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(2));

        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "WEEK_NUMBER:" + targetWeek);

        assertThat(results).hasSize(2);
        for (ReportedTime rt : results) {
            assertThat(rt.date).isAfterOrEqualTo(nextMonday);
            assertThat(rt.date).isBeforeOrEqualTo(nextSunday);
        }
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriodLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate firstDayLastMonth = lastMonth.withDayOfMonth(1);
        LocalDate lastDayLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        // Create entries within last month
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                firstDayLastMonth, BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                lastDayLastMonth, BigDecimal.valueOf(6));

        // Create entry outside last month
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(2));

        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "LAST_MONTH");

        assertThat(results).hasSize(2);
        for (ReportedTime rt : results) {
            assertThat(rt.date).isAfterOrEqualTo(firstDayLastMonth);
            assertThat(rt.date).isBeforeOrEqualTo(lastDayLastMonth);
        }
    }

    @Test
    @Transactional
    public void testListReportedTimesForPersonAndPeriodSpecificMonth() {
        LocalDate today = LocalDate.now();
        int targetMonth = today.getMonthValue() == 12 ? 1 : today.getMonthValue() + 1; // Next month
        LocalDate firstDayTargetMonth = LocalDate.of(today.getYear(), targetMonth, 1);
        LocalDate lastDayTargetMonth = firstDayTargetMonth.withDayOfMonth(firstDayTargetMonth.lengthOfMonth());

        // Create entries within target month
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                firstDayTargetMonth, BigDecimal.valueOf(4));
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                lastDayTargetMonth, BigDecimal.valueOf(6));

        // Create entry outside target month
        reportedTimeRepository.createReportedTime(
                testProject.name, testActivity.name, testPerson.firstName, testPerson.lastName,
                today, BigDecimal.valueOf(2));

        List<ReportedTime> results = reportedTimeRepository
                .listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "MONTH:" + targetMonth);

        assertThat(results).hasSize(2);
        for (ReportedTime rt : results) {
            assertThat(rt.date).isAfterOrEqualTo(firstDayTargetMonth);
            assertThat(rt.date).isBeforeOrEqualTo(lastDayTargetMonth);
        }
    }

    @Test
    @Transactional
    public void testInvalidWeekNumberFormat() {
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "WEEK_NUMBER:invalid")
        );
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid week number format");
    }

    @Test
    @Transactional
    public void testInvalidMonthFormat() {
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.listReportedTimesForPersonAndPeriod(testPerson.firstName, testPerson.lastName, "MONTH:invalid")
        );
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid month format");
    }

    @Test
    @Transactional
    public void testNonExistentPerson() {
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.createReportedTime(
                        testProject.name, testActivity.name, "NonExistent", "Person",
                        LocalDate.now(), BigDecimal.valueOf(8))
        );
        assertThat(thrown)
                .isInstanceOf(Exceptions.PersonNotFoundException.class)
                .hasMessageContaining("Person with first name NonExistent and last name Person not found");
    }

    @Test
    @Transactional
    public void testNonExistentProject() {
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.createReportedTime(
                        "NonExistentProject", testActivity.name, testPerson.firstName, testPerson.lastName,
                        LocalDate.now(), BigDecimal.valueOf(8))
        );
        assertThat(thrown)
                .isInstanceOf(Exceptions.ProjectNotFoundException.class)
                .hasMessageContaining("Project NonExistentProject not found");
    }

    @Test
    @Transactional
    public void testNonExistentActivity() {
        Throwable thrown = catchThrowable(() ->
                reportedTimeRepository.createReportedTime(
                        testProject.name, "NonExistentActivity", testPerson.firstName, testPerson.lastName,
                        LocalDate.now(), BigDecimal.valueOf(8))
        );
        assertThat(thrown)
                .isInstanceOf(Exceptions.ActivityNotFoundException.class)
                .hasMessageContaining("Activity NonExistentActivity not found");
    }
}
