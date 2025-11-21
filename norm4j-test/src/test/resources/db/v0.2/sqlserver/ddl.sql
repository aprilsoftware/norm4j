CREATE TABLE author (id INT IDENTITY(1,1), name NVARCHAR(255) NOT NULL, PRIMARY KEY (id));

CREATE TABLE book (author_id INT NOT NULL, description NVARCHAR(255), id INT IDENTITY(1,1), name NVARCHAR(255) NOT NULL, PRIMARY KEY (id));

CREATE TABLE bookorder (id INT IDENTITY(1,1), orderDate DATETIME NOT NULL, PRIMARY KEY (id));

CREATE TABLE bookorderitem (book_id INT NOT NULL, id INT IDENTITY(1,1), order_id INT NOT NULL, PRIMARY KEY (id));

ALTER TABLE book ADD CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_book FOREIGN KEY (book_id) REFERENCES book (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_bookorder FOREIGN KEY (order_id) REFERENCES bookorder (id);

