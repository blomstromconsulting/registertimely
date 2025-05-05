# Report Timely

For extensive AI generated documentation see: https://deepwiki.com/blomstromconsulting/registertimely/1-overview

Report Timely is an AI-powered time registration assistant application built with Quarkus and LangChain4j. It helps users register their work hours by providing an intuitive chat interface that can process multiple activities per day, estimate time allocations, match activities to projects, and distribute hours across appropriate days.

The application uses OpenAI's GPT model to understand user requests and applies Swedish work laws and regulations to ensure compliance with rest period requirements.

## Prerequisites

### Java
- Java 21 or later

### Database
- PostgreSQL with PGVector extension for vector storage

### OpenAI API Key
This application requires an OpenAI API key to function. You need to:
1. Obtain an API key from [OpenAI](https://platform.openai.com/)
2. Set it as an environment variable:
   ```
   export OPENAI_API_KEY=your_api_key_here
   ```

## Development Mode

To start the application in development mode:

1. Ensure Docker Desktop is running (required for the PostgreSQL test container that starts automatically)
2. Make sure your OpenAI API key is set as an environment variable
3. Run the application using Maven:
   ```
   ./mvnw quarkus:dev
   ```

This will start the application in development mode with hot reload enabled. The application will be available at http://localhost:8080.

## Features

- User identification by first and last name
- Time registration for multiple activities
- Estimation of appropriate time allocations
- Matching user activities to available project activities
- Distribution of worked hours across days and activities
- Compliance with Swedish work laws and regulations
- Validation of time entries against compliance rules

## Technology Stack

- Quarkus 3.19.3
- LangChain4j 0.25.0
- OpenAI GPT-4.1
- PostgreSQL with PGVector
- Web Components for UI
