-- Mock Graph Users Table
CREATE TABLE IF NOT EXISTS mock_graph_users (
    id VARCHAR(255) PRIMARY KEY,
    display_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    department VARCHAR(255),
    job_title VARCHAR(255),
    office_location VARCHAR(255),
    mobile_phone VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Mock Graph Groups Table
CREATE TABLE IF NOT EXISTS mock_graph_groups (
    id VARCHAR(255) PRIMARY KEY,
    display_name VARCHAR(255),
    description TEXT,
    email VARCHAR(255) UNIQUE
);

-- Group Memberships Table
CREATE TABLE IF NOT EXISTS mock_group_members (
    group_id VARCHAR(255),
    user_id VARCHAR(255),
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES mock_graph_groups(id),
    FOREIGN KEY (user_id) REFERENCES mock_graph_users(id)
);