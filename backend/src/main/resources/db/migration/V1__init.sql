CREATE TABLE loan_application (
                                  id BIGSERIAL PRIMARY KEY,
                                  first_name VARCHAR(32) NOT NULL,
                                  last_name VARCHAR(32) NOT NULL,
                                  personal_code VARCHAR(11) NOT NULL,
                                  loan_period_months INT NOT NULL,
                                  interest_margin NUMERIC(10,3) NOT NULL,
                                  base_interest_rate NUMERIC(10,3) NOT NULL,
                                  loan_amount NUMERIC(12,2) NOT NULL,
                                  status VARCHAR(32) NOT NULL,
                                  rejection_reason VARCHAR(64)
);