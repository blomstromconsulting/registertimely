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

    @Tool("Creates and persists a new time entry in the system. Parameters: projectName (case-insensitive), activityName (case-insensitive, must exist in the project), firstName and lastName of the person (case-insensitive), date (in ISO format YYYY-MM-DD), and duration (decimal number of hours). Returns the created ReportedTime object. Throws ProjectNotFoundException if project doesn't exist, ActivityNotFoundException if activity doesn't exist, or PersonNotFoundException if person doesn't exist.")
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

    @Tool("Deletes a specific time entry from the system. Parameters: projectName (case-insensitive), activityName (case-insensitive), firstName and lastName of the person (case-insensitive), date (in ISO format YYYY-MM-DD), and duration (decimal number of hours). Returns true if an entry was found and deleted, false if no matching entry was found. Throws ProjectNotFoundException if project doesn't exist, ActivityNotFoundException if activity doesn't exist, or PersonNotFoundException if person doesn't exist.")
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

    @Tool("Retrieves all time entries for a specific person within a date range. Parameters: firstName and lastName of the person (case-insensitive), startDate (inclusive, in ISO format YYYY-MM-DD), and endDate (inclusive, in ISO format YYYY-MM-DD). Returns a list of ReportedTime objects within the specified date range. The list will be empty if no entries are found. Throws PersonNotFoundException if the person doesn't exist.")
    @Transactional
    public List<ReportedTime> listReportedTimesForPersonAndTimespan(String firstName, String lastName,
                                                                    LocalDate startDate, LocalDate endDate) {
        Person person = personRepository.findByName(firstName, lastName);
        return list("person = ?1 and date >= ?2 and date <= ?3", person, startDate, endDate);
    }

    @Tool("Retrieves all time entries for a specific person during a predefined period. Parameters: firstName and lastName of the person (case-insensitive), and period (a string specifying the time period). Valid period values are: 'THIS_WEEK' (current week from Monday to Sunday), 'LAST_WEEK' (previous week), 'WEEK_NUMBER:n' (specific week number in current year), 'THIS_MONTH' (current month), 'LAST_MONTH' (previous month), or 'MONTH:n' (specific month number 1-12 in current year). Returns a list of ReportedTime objects within the calculated date range. The list will be empty if no entries are found. Throws PersonNotFoundException if the person doesn't exist, or IllegalArgumentException if the period format is invalid.")
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

    @Tool("Retrieves all time entries for a specific person on a specific project. Parameters: firstName and lastName of the person (case-insensitive), and projectName (case-insensitive). Returns a list of ReportedTime objects for the specified person and project. The list will be empty if no entries are found. Throws PersonNotFoundException if the person doesn't exist or ProjectNotFoundException if the project doesn't exist.")
    @Transactional
    public List<ReportedTime> listReportedTimesForPersonAndProject(String firstName, String lastName, String projectName) {
        Project project = projectRepository.findProjectByName(projectName);
        Person person = personRepository.findByName(firstName, lastName);
        return list("person = ?1 and activity.project = ?2", person, project);
    }

    @Tool("Retrieves all time entries for a specific project within a date range. Parameters: projectName (case-insensitive), startDate (inclusive, in ISO format YYYY-MM-DD), and endDate (inclusive, in ISO format YYYY-MM-DD). Returns a list of ReportedTime objects for the specified project within the date range. The list will be empty if no entries are found. Throws ProjectNotFoundException if the project doesn't exist.")
    @Transactional
    public List<ReportedTime> listReportedTimesForProjectAndTimespan(String projectName, LocalDate startDate, LocalDate endDate) {
        Project project = projectRepository.findProjectByName(projectName);
        return list("activity.project = ?1 and date >= ?2 and date <= ?3", project, startDate, endDate);
    }
}
