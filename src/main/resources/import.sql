-- ============================================
-- Inserting Persons
-- ============================================
INSERT INTO Person (id, firstName, lastName) VALUES (1, 'Alice', 'Smith');
INSERT INTO Person (id, firstName, lastName) VALUES (2, 'Bob', 'Johnson');
INSERT INTO Person (id, firstName, lastName) VALUES (3, 'Carol', 'Williams');
INSERT INTO Person (id, firstName, lastName) VALUES (4, 'David', 'Brown');
INSERT INTO Person (id, firstName, lastName) VALUES (5, 'Eva', 'Davis');

-- Restart the person sequence so that the next id is 6
ALTER SEQUENCE person_seq RESTART WITH 6;

-- ============================================
-- Inserting Projects
-- ============================================
INSERT INTO Project (id, name, description) VALUES (1, 'Project Apollo', 'Development of the Apollo platform.');
INSERT INTO Project (id, name, description) VALUES (2, 'Project Orion', 'Upgrade of the Orion system.');
INSERT INTO Project (id, name, description) VALUES (3, 'Project Zeus', 'Innovation project for Zeus features.');

-- Optionally, restart the project sequence (if using a similar sequence mechanism)
ALTER SEQUENCE project_seq RESTART WITH 4;

-- ============================================
-- Inserting Activities
-- ============================================
-- Activity "Maintenance" for Project Apollo (id=1)
INSERT INTO Activity (id, project_id, name, description) VALUES (1, 1, 'Maintenance', 'Routine maintenance tasks for Project Apollo.');

-- Activity "Activation Feature" for Project Orion (id=2)
INSERT INTO Activity (id, project_id, name, description) VALUES (2, 2, 'Activation Feature', 'Implementation of activation features in Project Orion.');

-- Activity "History Feature" for Project Zeus (id=3)
INSERT INTO Activity (id, project_id, name, description) VALUES (3, 3, 'History Feature', 'Development of history tracking feature in Project Zeus.');

-- Activity "Smaller Improvements" for Project Apollo (id=1)
INSERT INTO Activity (id, project_id, name, description) VALUES (4, 1, 'Smaller Improvements', 'Minor improvements and bug fixes in Project Apollo.');

-- Restart the activity sequence so that the next id is 5
ALTER SEQUENCE activity_seq RESTART WITH 5;

-- ============================================
-- Inserting Reported Time Entries (for February 2025)
-- ============================================
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (1, 1, 1, '2025-02-05', 3.5);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (2, 2, 2, '2025-02-10', 4.0);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (3, 3, 3, '2025-02-15', 2.0);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (4, 4, 4, '2025-02-20', 1.5);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (5, 2, 5, '2025-02-25', 5.0);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (6, 1, 3, '2025-02-12', 2.5);
INSERT INTO ReportedTime (id, activity_id, person_id, date, duration) VALUES (7, 4, 1, '2025-02-18', 3.0);

-- Restart the reported time sequence so that the next id is 8
ALTER SEQUENCE reportedtime_seq RESTART WITH 8;
