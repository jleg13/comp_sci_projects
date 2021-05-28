-- Database for store

CREATE TABLE customers
(
    customer_id INTEGER PRIMARY KEY,
    last_name CHAR(50) NOT NULL,
    first_name CHAR(50) NOT NULL,
    address CHAR(200),
    city CHAR(50),
    state CHAR(3) NOT NULL CHECK (state IN ('NSW', 'VIC', 'QLD', 'ACT', 'TAS', 'NT', 'SA', 'WA')),
    postcode CHAR(8)
);

CREATE TABLE movies
(
    movie_id INTEGER PRIMARY KEY,
    movie_title CHAR(100) NOT NULL,
    director_last_name CHAR(50) NOT NULL,
    director_first_name CHAR(50) NOT NULL,
    genre CHAR(20) NOT NULL CHECK (genre IN ('Action', 'Adventure', 'Comedy', 'Romance', 'Science Fiction', 'Documentary', 'Drama', 'Horror')),
    media_type CHAR(20) CHECK (media_type IN ('DVD', 'Blu-Ray', '')),
    release_date date,
    studio_name CHAR(50),
    retail_price REAL NOT NULL CHECK (retail_price > 0),
    current_stock INTEGER NOT NULL CHECK (current_stock >= 0)
);

CREATE TABLE shipments
(
    shipment_id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    movie_id INTEGER NOT NULL,
    shipment_date date,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
