INSERT INTO tbl_users (email, firstname, surname, patronymic, password) VALUES ('xpert14world@gmail.com', 'Mykhailo', 'Diachenko', 'Dmytrovych', '1');


delimiter $$
CREATE FUNCTION glueCarPhotos(c_id INT)
    RETURNS TEXT
BEGIN
    DECLARE gluedData TEXT;
    SELECT GROUP_CONCAT(CONCAT(id, '#', photo) SEPARATOR '?') INTO gluedData
    FROM tbl_cars_photos
    WHERE car_id = c_id;
    RETURN gluedData;
END$$
delimiter ;



SELECT id, brand, model, segment_id, price, city_id, glueCarPhotos(id) FROM tbl_cars;


# Function template for getInformativeUser()

BEGIN
    DECLARE cityId TINYINT;
    SELECT tbl_cars.city_id INTO cityId FROM tbl_invoices
    JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id
    ORDER BY tbl_invoices.ts_created
    LIMIT 1;
    RETURN cityId;
END



# Select query for getInformativeUser()
SELECT id, email,firstname,surname,patronymic,role_id,is_blocked,balance, conf_code, ts_created,
       (SELECT COUNT(id) FROM tbl_invoices WHERE tbl_invoices.client_id = tbl_users.id) AS invoicesAmount,
       getLastInvoiceCity(tbl_users.id) AS lastInvoiceCity
FROM tbl_users WHERE id=?