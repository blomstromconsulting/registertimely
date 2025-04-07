package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

@ApplicationScoped
public class ReportedTimeRepository implements PanacheRepository<ReportedTime> {

    @Inject
    PersonRepository personRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ActivityRepository activityRepository;

    private Activity resolveActivity(Project project, String activityName) {
        return activityRepository.getActivityByName(activityName, project.name);
    }

    @Tool("Register a new Reported Time entry for the given project name, activity name, person first name, person last name, date, and duration.")
    @Transactional
    public ReportedTime createReportedTime(String projectName, String activityName, String firstName, String lastName,
                                           LocalDate date, BigDecimal duration) {
        Person person = personRepository.findByName(firstName, lastName);
        Project project = projectRepository.findProjectByName(projectName);
        Activity activity = resolveActivity(project, activityName);
        ReportedTime rt = new ReportedTime();
        rt.activity = activity;
        rt.person = person;
        rt.date = date;
        rt.duration = duration;
        persist(rt);
        return rt;
    }

    @Tool("Deletes a Reported Time entry matching the given project name, activity name, persons first name and last name, date, and duration. Returns true if deleted.")
    @Transactional
    public boolean deleteReportedTime(String projectName, String activityName, String firstName, String lastName,
                                      LocalDate date, BigDecimal duration) {

        Person person = personRepository.findByName(firstName, lastName);
        Project project = projectRepository.findProjectByName(projectName);
        Activity activity = resolveActivity(project, activityName);

        ReportedTime rt = find("activity = ?1 and person = ?2 and date = ?3 and duration = ?4",
                activity, person, date, duration).firstResult();
        if (rt != null) {
            delete(rt);
            return true;
        }
        return false;
    }

    @Tool("Lists all Reported Time entries for a given Person within a specified timespan (from startDate to endDate).")
    @Transactional
    public List<ReportedTime> listReportedTimesForPersonAndTimespan(String firstName, String lastName,
                                                                    LocalDate startDate, LocalDate endDate) {
        Person person = personRepository.findByName(firstName, lastName);
        return list("person = ?1 and date >= ?2 and date <= ?3", person, startDate, endDate);
    }

    @Tool("Lists all Reported Time entries for a given persons with first name and last name during a specified period. " +
            "The period parameter can be one of the following formats: 'THIS_WEEK', 'LAST_WEEK', " +
            "'WEEK_NUMBER:<number>', 'THIS_MONTH', 'LAST_MONTH', or 'MONTH:<number>'.")
    @Transactional
    public List<ReportedTime> listReportedTimesForPersonAndPeriod(String firstName, String lastName, String period) {
        Person person = personRepository.findByName(firstName, lastName);
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        if ("THIS_WEEK".equalsIgnoreCase(period)) {
            startDate = now.with(DayOfWeek.MONDAY);
            endDate = now.with(DayOfWeek.SUNDAY);
        } else if ("LAST_WEEK".equalsIgnoreCase(period)) {
            LocalDate lastWeek = now.minusWeeks(1);
            startDate = lastWeek.with(DayOfWeek.MONDAY);
            endDate = lastWeek.with(DayOfWeek.SUNDAY);
        } else if (period.toUpperCase().startsWith("WEEK_NUMBER:")) {
            try {
                int weekNumber = Integer.parseInt(period.substring("WEEK_NUMBER:".length()));
                WeekFields weekFields = WeekFields.ISO;
                startDate = now.with(weekFields.weekOfYear(), weekNumber)
                        .with(DayOfWeek.MONDAY);
                endDate = startDate.plusDays(6);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid week number format. Use WEEK_NUMBER:<number>.");
            }
        } else if ("THIS_MONTH".equalsIgnoreCase(period)) {
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        } else if ("LAST_MONTH".equalsIgnoreCase(period)) {
            LocalDate lastMonth = now.minusMonths(1);
            startDate = lastMonth.withDayOfMonth(1);
            endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        } else if (period.toUpperCase().startsWith("MONTH:")) {
            try {
                int month = Integer.parseInt(period.substring("MONTH:".length()));
                startDate = LocalDate.of(now.getYear(), month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid month format. Use MONTH:<number>.");
            }
        } else {
            throw new IllegalArgumentException("Invalid period format. Allowed values: THIS_WEEK, LAST_WEEK, WEEK_NUMBER:<number>, THIS_MONTH, LAST_MONTH, or MONTH:<number>.");
        }
        return list("person = ?1 and date >= ?2 and date <= ?3", person, startDate, endDate);
    }

    @Tool("Lists all Reported Time entries for a given person with first name and last name associated with a specific project name.")
    @Transactional
    public List<ReportedTime> listReportedTimesForPersonAndProject(String firstName, String lastName, String projectName) {
        Project project = projectRepository.findProjectByName(projectName);
        Person person = personRepository.findByName(firstName, lastName);
        return list("person = ?1 and activity.project = ?2", person, project);
    }

    @Tool("Lists all Reported Time entries for a given project name within a specified timespan (from startDate to endDate).")
    @Transactional
    public List<ReportedTime> listReportedTimesForProjectAndTimespan(String projectName, LocalDate startDate, LocalDate endDate) {
        Project project = projectRepository.findProjectByName(projectName);
        return list("activity.project = ?1 and date >= ?2 and date <= ?3", project, startDate, endDate);
    }
}

