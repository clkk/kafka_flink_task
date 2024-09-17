CREATE TABLE calculation_results (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     valueC DOUBLE NOT NULL,
                                     valueD DOUBLE NOT NULL,
                                     statusA INT NOT NULL,
                                     statusB INT NOT NULL,
                                     result DOUBLE NOT NULL,
                                     processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);