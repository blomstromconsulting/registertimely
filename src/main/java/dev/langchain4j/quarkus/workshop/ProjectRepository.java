package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProjectRepository implements PanacheRepository<Project> {

    @Tool("List all projects")
    @Transactional
    public List<Project> listAllProjects() {
        return findAll().list();
    }

    @Tool("Get a project by project name")
    @Transactional
    public Project findProjectByName(String projectName) {
        return find("lower(name) = ?1", projectName.toLowerCase()).firstResultOptional().orElseThrow(() -> new Exceptions.ProjectNotFoundException(projectName));
    }
}
