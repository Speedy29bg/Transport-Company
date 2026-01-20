CREATE TABLE IF NOT EXISTS transport_company (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    salary DECIMAL(15,2) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_company FOREIGN KEY (company_id) REFERENCES transport_company(id)
);

CREATE TABLE IF NOT EXISTS employee_qualification (
    employee_id BIGINT NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    PRIMARY KEY (employee_id, qualification),
    CONSTRAINT fk_employee_qualification_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vehicle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    reg_number VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    seat_count INT,
    max_load_kg INT,
    volume_liters INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_company FOREIGN KEY (company_id) REFERENCES transport_company(id)
);

CREATE TABLE IF NOT EXISTS transport (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    depart_at TIMESTAMP NOT NULL,
    arrive_at TIMESTAMP NOT NULL,
    cargo_kind VARCHAR(50) NOT NULL,
    cargo_weight_kg INT,
    passenger_count INT,
    price DECIMAL(15,2) NOT NULL,
    driver_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transport_company FOREIGN KEY (company_id) REFERENCES transport_company(id),
    CONSTRAINT fk_transport_client FOREIGN KEY (client_id) REFERENCES client(id),
    CONSTRAINT fk_transport_driver FOREIGN KEY (driver_id) REFERENCES employee(id),
    CONSTRAINT fk_transport_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);
