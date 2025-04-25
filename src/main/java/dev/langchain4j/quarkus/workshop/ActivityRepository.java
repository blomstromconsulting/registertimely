package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ActivityRepository implements PanacheRepository<Activity> {

    @Inject
    ProjectRepository projectRepository;

    @Tool("Retrieves a list of all available activities in the system. Returns a list of Activity objects containing id, name, description, and associated project.")
    @Transactional
    public List<Activity> listAllActivities() {
        return findAll().list();
    }

    @Tool("Retrieves a specific activity by its name and the associated project name. Both parameters are case-insensitive. Returns the Activity object if found. Throws ActivityNotFoundException if no activity with the given name exists in the specified project.")
    @Transactional
    public Activity getActivityByName(String activityName, String projectName) {
        return find("lower(name)=?1 and lower(project.name)=?2",
                activityName.toLowerCase(), projectName.toLowerCase()).firstResultOptional().orElseThrow(
                () -> new Exceptions.ActivityNotFoundException(activityName));
    }

    @Tool("Retrieves all activities associated with a specific project identified by its name. The project name is case-insensitive. Returns a list of Activity objects for the specified project. Throws ProjectNotFoundException if no project with the given name exists.")
    @Transactional
    public List<Activity> listAllActivitiesByProjectName(String projectName) {
        var project = projectRepository.findProjectByName(projectName);
        return list("project.id=?1", project.id);
    }
}
