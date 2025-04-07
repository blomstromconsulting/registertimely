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

    @Tool("List all available activities")
    @Transactional
    public List<Activity> listAllActivities() {
        return findAll().list();
    }

    @Tool("Get activity by activity name and project name")
    @Transactional
    public Activity getActivityByName(String activityName, String projectName) {
        return find("lower(name)=?1 and lower(project.name)=?2",
                activityName.toLowerCase(), projectName.toLowerCase()).firstResultOptional().orElseThrow(
                () -> new Exceptions.ActivityNotFoundException(activityName));
    }

    @Tool("List all activities for a project name")
    @Transactional
    public List<Activity> listAllActivitiesByProjectName(String projectName) {
        var project = projectRepository.findProjectByName(projectName);
        return list("project.id=?1", project.id);
    }
}
