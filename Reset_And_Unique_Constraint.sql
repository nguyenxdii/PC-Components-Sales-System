-- 1. Vô hiệu hóa ràng buộc để xóa sạch không lỗi
EXEC sp_MSforeachtable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL';

-- 2. Xóa sạch dữ liệu cũ
DELETE FROM category_brands; 
DELETE FROM payments; 
DELETE FROM order_details;
DELETE FROM orders; 
DELETE FROM cart_items; 
DELETE FROM comments;
DELETE FROM product_images; 
DELETE FROM products; 
DELETE FROM categories; 
DELETE FROM brands;

-- 3. Reset các cột ID tự tăng về 0 (Sử dụng nháy đơn chuẩn T-SQL)
EXEC sp_MSforeachtable 'IF EXISTS (SELECT * FROM sys.identity_columns WHERE object_id = OBJECT_ID(''?'')) DBCC CHECKIDENT (''?'', RESEED, 0)';

-- 4. Kích hoạt lại ràng buộc
EXEC sp_MSforeachtable 'ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL';

-- 5. THÊM RÀNG BUỘC UNIQUE CHO SLUG (NGĂN LẶP DANH MỤC)
-- Sử dụng nháy đơn chuẩn T-SQL
IF NOT EXISTS (SELECT name FROM sys.indexes WHERE name = 'UC_Category_Slug')
BEGIN
    ALTER TABLE categories ADD CONSTRAINT UC_Category_Slug UNIQUE (slug);
END

PRINT '>>> DATABASE CLEANED & UNIQUE CONSTRAINT ADDED! <<<';
