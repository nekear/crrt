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



SELECT * FROM tbl_cars WHERE MATCH(brand, model) AGAINST ('*AM* *e*' IN BOOLEAN MODE)



SELECT tbl_invoices.id AS invoice_id, tbl_invoices.code AS invoice_code,
       tbl_invoices.date_start, tbl_invoices.date_end,
       tbl_invoices.exp_price, tbl_invoices.is_canceled, tbl_invoices.is_rejected,
       getActiveRepairsByInvoiceId(tbl_invoices.id) AS activeRepairs,
       getExpiredRepairsByInvoiceId(tbl_invoices.id) AS expiredRepairs,
       tbl_invoices.driver_id, tbl_drivers.code AS driver_code, driver_u.avatar AS driver_avatar,
       client_u.email AS client_email,
       tbl_cars.brand, tbl_cars.model
FROM tbl_invoices
         LEFT JOIN tbl_drivers ON tbl_invoices.driver_id = tbl_drivers.id
         LEFT JOIN tbl_users AS driver_u ON tbl_drivers.user_id = driver_u.id
         JOIN tbl_users AS client_u ON tbl_invoices.client_id = client_u.id
         JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id
WHERE
    MATCH(tbl_cars.brand, tbl_cars.model) AGAINST ('*Mercedes* *Urus*' IN BOOLEAN MODE) AND
        client_u.email LIKE '%@mail%'