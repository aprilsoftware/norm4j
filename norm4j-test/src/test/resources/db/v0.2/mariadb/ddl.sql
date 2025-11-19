CREATE TABLE author (id INT AUTO_INCREMENT, name VARCHAR(255) NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB;

CREATE TABLE book (id INT AUTO_INCREMENT, name VARCHAR(255) NOT NULL, author_id INT NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB;

CREATE TABLE bookorder (id INT AUTO_INCREMENT, orderDate DATETIME NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB;

CREATE TABLE bookorderitem (id INT AUTO_INCREMENT, order_id INT NOT NULL, book_id INT NOT NULL, PRIMARY KEY (id)) ENGINE=InnoDB;

ALTER TABLE book ADD CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_book FOREIGN KEY (book_id) REFERENCES book (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_bookorder FOREIGN KEY (order_id) REFERENCES bookorder (id);

