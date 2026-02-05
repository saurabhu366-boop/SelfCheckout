-- =========================
-- Insert demo products
-- =========================
INSERT INTO product (barcode, name, price, stock_quantity)
VALUES
    ('8901234567890', 'Milk 500ml', 30, 10),
    ('8909876543210', 'Bread', 25, 5),
    ('8905555555555', 'Eggs Dozen', 60, 12),
    ('8904444444444', 'Butter 100g', 50, 8),
    ('8903333333333', 'Apple 1kg', 120, 6);

-- =========================
-- Insert demo cart
-- =========================
INSERT INTO cart (user_id, status)
VALUES ('user1', 'ACTIVE');

-- =========================
-- Insert cart items
-- =========================
INSERT INTO cart_item (cart_id, product_id, quantity)
VALUES
    (1, 1, 2),   -- Milk x2
    (1, 2, 1),   -- Bread x1
    (1, 3, 1);   -- Eggs x1
