CREATE INDEX idx_company_name ON transport_company(name);
CREATE INDEX idx_employee_salary ON employee(salary);
CREATE INDEX idx_transport_destination ON transport(destination);
CREATE INDEX idx_transport_company_depart ON transport(company_id, depart_at);
CREATE INDEX idx_vehicle_reg ON vehicle(reg_number);
CREATE INDEX idx_transport_payment ON transport(payment_status);
