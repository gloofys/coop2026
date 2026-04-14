CREATE TABLE payment_schedule_entry (
                                        id BIGSERIAL PRIMARY KEY,
                                        loan_application_id BIGINT NOT NULL,
                                        payment_number INT NOT NULL,
                                        payment_date DATE NOT NULL,
                                        total_payment NUMERIC(12,2) NOT NULL,
                                        principal_payment NUMERIC(12,2) NOT NULL,
                                        interest_payment NUMERIC(12,2) NOT NULL,
                                        remaining_balance NUMERIC(12,2) NOT NULL,

                                        CONSTRAINT fk_payment_schedule_entry_loan_application
                                            FOREIGN KEY (loan_application_id)
                                                REFERENCES loan_application(id)
                                                ON DELETE CASCADE
);