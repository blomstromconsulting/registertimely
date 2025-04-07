package dev.langchain4j.quarkus.workshop;

public class Exceptions {

    public static class ProjectNotFoundException extends RuntimeException {
        public ProjectNotFoundException(String projectName) {
            super("Project %s not found".formatted(projectName));
        }
    }

    public static class PersonNotFoundException extends RuntimeException {
        public PersonNotFoundException(String firstName, String lastName) {
            super("Person with first name %s and last name %s not found".formatted(firstName, lastName));
        }
    }

    public static class ActivityNotFoundException extends RuntimeException {
        public ActivityNotFoundException(String activityName) {
            super("Activity %s not found".formatted(activityName));
        }
    }
    
}
