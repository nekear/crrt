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



SELECT id, brand, model, segment_id, price, city_id, glueCarPhotos(id) FROM tbl_cars