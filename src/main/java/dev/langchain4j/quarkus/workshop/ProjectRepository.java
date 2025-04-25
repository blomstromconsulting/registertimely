package dev.langchain4j.quarkus.workshop;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProjectRepository implements PanacheRepository<Project> {

    @Tool("Retrieves a list of all projects registered in the system. Returns a list of Project objects containing id, name, and description. The list will be empty if no projects are registered.")
    @Transactional
    public List<Project> listAllProjects() {
        return findAll().list();
    }

    @Tool("Retrieves a project by its name. The project name parameter is case-insensitive. Returns the Project object if found. Throws ProjectNotFoundException if no project with the given name exists in the system.")
    @Transactional
    public Project findProjectByName(String projectName) {
        return find("lower(name) = ?1", projectName.toLowerCase()).firstResultOptional().orElseThrow(() -> new Exceptions.ProjectNotFoundException(projectName));
    }
}
