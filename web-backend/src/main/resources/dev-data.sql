-- Insert mock users
INSERT OR IGNORE INTO mock_graph_users (id, display_name, email, department, job_title, office_location, mobile_phone) VALUES
('dev-user-1', 'John Developer', 'john.dev@company.com', 'Engineering', 'Senior Software Engineer', 'Building A, Floor 2', '+1-555-0101'),
('dev-user-2', 'Jane Designer', 'jane.designer@company.com', 'Design', 'UI/UX Designer', 'Building B, Floor 1', '+1-555-0102'),
('dev-user-3', 'Bob Manager', 'bob.manager@company.com', 'Management', 'Project Manager', 'Building C, Floor 3', '+1-555-0103'),
('dev-user-4', 'Alice Tester', 'alice.qa@company.com', 'Quality Assurance', 'QA Engineer', 'Building A, Floor 1', '+1-555-0104'),
('dev-user-5', 'Charlie DevOps', 'charlie.ops@company.com', 'Operations', 'DevOps Engineer', 'Building D, Floor 2', '+1-555-0105');

-- Insert mock groups
INSERT OR IGNORE INTO mock_graph_groups (id, display_name, description, email) VALUES
('dev-group-1', 'Engineering Team', 'Software Development and Engineering', 'eng-team@company.com'),
('dev-group-2', 'Design Department', 'User Experience and Design Team', 'design@company.com'),
('dev-group-3', 'Management', 'Project and Department Management', 'management@company.com'),
('dev-group-4', 'Quality Assurance', 'Software Testing and Quality', 'qa-team@company.com');

-- Assign users to groups
INSERT OR IGNORE INTO mock_group_members (group_id, user_id) VALUES
('dev-group-1', 'dev-user-1'),
('dev-group-1', 'dev-user-5'),
('dev-group-2', 'dev-user-2'),
('dev-group-3', 'dev-user-3'),
('dev-group-4', 'dev-user-4'),
('dev-group-3', 'dev-user-1');