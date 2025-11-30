use cs3380;


-- DROP TABLES 

DROP TABLE IF EXISTS guest_visit_attractions;
DROP TABLE IF EXISTS guests_book_listings;
DROP TABLE IF EXISTS listings_have_amenities;
DROP TABLE IF EXISTS reviews;

DROP TABLE IF EXISTS crimes;
DROP TABLE IF EXISTS police_stations;
DROP TABLE IF EXISTS attractions;

DROP TABLE IF EXISTS listings;
DROP TABLE IF EXISTS amenities;
DROP TABLE IF EXISTS neighbourhoods;

DROP TABLE IF EXISTS hosts;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS criminals;




-- GUESTS
CREATE TABLE guests (
    guest_id      INT PRIMARY KEY,
    guest_name    NVARCHAR(255) NOT NULL
);

-- HOSTS
CREATE TABLE hosts (
    host_id                INT PRIMARY KEY,
    host_name              NVARCHAR(255) NOT NULL,
    host_identity_verified NVARCHAR(20)  NOT NULL,
    host_since             DATE,
);

-- CRIMINALS
CREATE TABLE criminals (
    criminal_id                  INT PRIMARY KEY,
    criminal_name                NVARCHAR(255) NOT NULL,
    gender                       NVARCHAR(20),
    number_of_prior_convictions INT,
    age_group                   NVARCHAR(50)
);

-- NEIGHBOURHOODS
CREATE TABLE neighbourhoods (
    neighbourhood_id   INT PRIMARY KEY,
    neighbourhood_name NVARCHAR(255) NOT NULL
);

---------------------------------------------------------
-- DEPENDENT TABLES
---------------------------------------------------------

-- LISTINGS (host 1→M listing)
CREATE TABLE listings (
    listing_id            INT PRIMARY KEY,
    name                  NVARCHAR(255) NOT NULL,
    price                 DECIMAL(10,2),
    property_type         NVARCHAR(100),
    review_scores_value   DECIMAL(5,2),
    host_id               INT NOT NULL,
    neighbourhood_id      INT NOT NULL,

    FOREIGN KEY (host_id) 
        REFERENCES hosts(host_id)
        ON DELETE CASCADE,

    FOREIGN KEY (neighbourhood_id)
        REFERENCES neighbourhoods(neighbourhood_id)
        ON DELETE CASCADE
);

-- AMENITIES
CREATE TABLE amenities (
    amenity_id   INT PRIMARY KEY,
    amenity_name NVARCHAR(255) NOT NULL
);

-- M–N LISTINGS x AMENITIES
CREATE TABLE listings_have_amenities (
    listing_id INT NOT NULL,
    amenity_id INT NOT NULL,

    PRIMARY KEY (listing_id, amenity_id),

    FOREIGN KEY (listing_id)
        REFERENCES listings(listing_id)
        ON DELETE CASCADE,

    FOREIGN KEY (amenity_id)
        REFERENCES amenities(amenity_id)
        ON DELETE CASCADE
);

-- REVIEWS
CREATE TABLE reviews (
    review_id  INT PRIMARY KEY,
    listing_id INT NOT NULL,
    guest_id   INT NOT NULL,
    comments   NVARCHAR(MAX),

    FOREIGN KEY (listing_id)
        REFERENCES listings(listing_id)
        ON DELETE CASCADE,

    FOREIGN KEY (guest_id)
        REFERENCES guests(guest_id)
        ON DELETE CASCADE
);

-- ATTRACTIONS
CREATE TABLE attractions (
    attraction_id    INT PRIMARY KEY,
    attraction_name  NVARCHAR(255) NOT NULL,
    neighbourhood_id INT NOT NULL,

    FOREIGN KEY (neighbourhood_id)
        REFERENCES neighbourhoods(neighbourhood_id)
        ON DELETE CASCADE
);

-- M–N: GUEST VISITS ATTRACTIONS
CREATE TABLE guest_visit_attractions (
    guest_id      INT NOT NULL,
    attraction_id INT NOT NULL,
    visit_date DATE,

    PRIMARY KEY (guest_id, attraction_id),

    FOREIGN KEY (guest_id)
        REFERENCES guests(guest_id)
        ON DELETE CASCADE,

    FOREIGN KEY (attraction_id)
        REFERENCES attractions(attraction_id)
        ON DELETE CASCADE
);

-- POLICE STATIONS
CREATE TABLE police_stations (
    police_station_id   INT PRIMARY KEY,
    police_station_name NVARCHAR(255) NOT NULL,
    contact_no          NVARCHAR(50),
    neighbourhood_id    INT NOT NULL,

    FOREIGN KEY (neighbourhood_id)
        REFERENCES neighbourhoods(neighbourhood_id)
        ON DELETE CASCADE
);

-- CRIMES
CREATE TABLE crimes (
    crime_id         INT PRIMARY KEY,
    crime_type       NVARCHAR(255) NOT NULL,
    criminal_id      INT NOT NULL,
    neighbourhood_id INT NOT NULL,

    FOREIGN KEY (criminal_id)
        REFERENCES criminals(criminal_id)
        ON DELETE CASCADE,

    FOREIGN KEY (neighbourhood_id)
        REFERENCES neighbourhoods(neighbourhood_id)
        ON DELETE CASCADE
);

-- M–N: GUESTS BOOK LISTINGS
CREATE TABLE guests_book_listings (
    guest_id     INT NOT NULL,
    listing_id   INT NOT NULL,
    booking_date DATE,

    PRIMARY KEY (guest_id, listing_id),

    FOREIGN KEY (guest_id)
        REFERENCES guests(guest_id)
        ON DELETE CASCADE,

    FOREIGN KEY (listing_id)
        REFERENCES listings(listing_id)
        ON DELETE CASCADE
);
