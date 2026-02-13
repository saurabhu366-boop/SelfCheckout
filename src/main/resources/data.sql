-- =========================
-- Insert demo products
-- =========================
INSERT INTO product (barcode, name, price, stock_quantity)
VALUES
    ('8901234567890', 'Milk 500ml', 30, 10),
    ('8909876543210', 'Bread', 25, 5),
    ('8905555555555', 'Eggs Dozen', 60, 12),
    ('8904444444444', 'Butter 100g', 50, 8),
    ('8901058851298','Maggie 70g',15,10);


-- =========================
-- Insert demo cart
-- =========================
INSERT INTO cart (user_id, status)
VALUES ('user1', 'ACTIVE');


