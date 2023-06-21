CREATE TABLE IF NOT EXISTS TODOS (
    id INT generated by default as identity default on null,
    todo_id       VARCHAR(80) NOT NULL,
    user_id INT NOT NULL,
    content     VARCHAR(80)      NOT NULL,
    completed BOOLEAN NOT NULL,
    created_at DATE NOT NULL,
    updated_at DATE NOT NULL
    );
