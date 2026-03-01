-- =========================
-- Products
-- =========================
-- ✅ Keep existing products, add more common Indian grocery barcodes
INSERT INTO product (barcode, name, price, stock_quantity) VALUES
                                                               ('8901234567890', 'Milk 500ml',          30,  10),
                                                               ('8909876543210', 'Bread',               25,   5),
                                                               ('8901719101038', 'Parle-G',             10,  12),
                                                               ('8904444444444', 'Butter 100g',         50,   8),
                                                               ('8901058851298', 'Maggie 70g',          15,  10),
                                                               ('8906060900052', 'Amul Cheese Slice',   55,   6),
                                                               ('8901030869556', 'Tata Salt 1kg',       20,  15),
                                                               ('8901063100108', 'Surf Excel 500g',     95,   7),
                                                               ('8901030804388', 'Tata Tea Gold 250g', 110,   9),
                                                               ('8901396040184', 'Colgate 200g',        95,  11),
                                                               ('8901499400016', 'Dettol Soap',         45,  14),
                                                               ('8902080000017', 'Lays Classic 26g',    20,  20),
                                                               ('8906063600027', 'Haldiram Bhujia',     30,  10),
                                                               ('8901719110726', 'Hide & Seek 100g',    30,   8),
                                                               ('8901826100018', 'Bisleri 1L',          20,  25);

-- =========================
-- ✅ NO hardcoded cart rows needed.
-- CartService.scanProduct() now auto-creates an ACTIVE cart for any
-- logged-in user the first time they scan. The old INSERT below caused
-- 404/500 errors because it used 'user1' which doesn't match JWT UUIDs.
--
-- OLD (remove this if it exists in your DB):
-- INSERT INTO cart (user_id, status) VALUES ('user1', 'ACTIVE');
-- =========================

-- If you want to clean up the old user1 data, run:
-- DELETE FROM cart_item WHERE cart_id IN (SELECT id FROM cart WHERE user_id = 'user1');
-- DELETE FROM cart WHERE user_id = 'user1';