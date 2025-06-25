-- Initialisation des données de test

-- Insertion d'utilisateurs de test
INSERT INTO users (name, email, phone) VALUES 
('Jean Dupont', 'jean.dupont@email.com', '0123456789'),
('Marie Martin', 'marie.martin@email.com', '0987654321'),
('Pierre Durand', 'pierre.durand@email.com', '0555666777');

-- Insertion de cartes de test
INSERT INTO bank_cards (card_number, expiration_date, cvv, type, user_id) VALUES 
('1234567890123456', '2025-12-31', '123', 'Visa', 1),
('9876543210987654', '2026-06-30', '456', 'Mastercard', 1),
('1111222233334444', '2025-03-15', '789', 'Visa', 2),
('5555666677778888', '2027-01-31', '012', 'American Express', 3); 