CREATE TABLE author (id NUMBER(10), name VARCHAR(255) NOT NULL, PRIMARY KEY (id));

CREATE TABLE book (author_id NUMBER(10) NOT NULL, description VARCHAR(255), id NUMBER(10), name VARCHAR(255) NOT NULL, PRIMARY KEY (id));

CREATE TABLE bookorder (id NUMBER(10), orderDate TIMESTAMP NOT NULL, PRIMARY KEY (id));

CREATE TABLE bookorderitem (book_id NUMBER(10) NOT NULL, id NUMBER(10), order_id NUMBER(10) NOT NULL, PRIMARY KEY (id));

ALTER TABLE book ADD CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_book FOREIGN KEY (book_id) REFERENCES book (id);

ALTER TABLE bookorderitem ADD CONSTRAINT fk_bookorderitem_bookorder FOREIGN KEY (order_id) REFERENCES bookorder (id);

