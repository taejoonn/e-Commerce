CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;

CREATE TABLE IF NOT EXISTS BillingInfo (
    Id INT                          NOT NULL AUTO_INCREMENT,
    CardNumber BIGINT                  NOT NULL,
    PhoneNumber BIGINT                 NOT NULL,
    Address CHAR(40)                NOT NULL,
    CardType CHAR(10)               NOT NULL,
    CardExpiryDate CHAR(8)          NOT NULL,
    PRIMARY KEY (Id),
    CHECK (Id > 0),
    CHECK (CardNumber > 1000000000000000 AND CardNumber < 9999999999999999),
    CHECK (PhoneNumber > 1000000000 AND PhoneNumber < 10000000000)
);

CREATE TABLE IF NOT EXISTS Seller (
    Id INT                          NOT NULL AUTO_INCREMENT,
    BillingInfoId INT               NOT NULL,
    CompanyName CHAR(20)            NOT NULL,
    PRIMARY KEY (Id),
    UNIQUE (BillingInfoId),
    FOREIGN KEY (BillingInfoId) REFERENCES BillingInfo (Id)
        ON UPDATE CASCADE,
    CHECK (Id > 0)
);

CREATE TABLE IF NOT EXISTS Item (
    Id INT				NOT NULL AUTO_INCREMENT,
    SellerId INT 			NOT NULL,
    Price DECIMAL			NOT NULL,
    ItemName CHAR(20)                   NOT NULL,
    Description CHAR(255),
    PRIMARY KEY (Id),
    FOREIGN KEY (SellerId) REFERENCES Seller (Id)
        ON UPDATE CASCADE,
    CHECK (Id > 0 AND SellerId > 0 AND Price > 0)
);

CREATE TABLE IF NOT EXISTS Inventory (
    ItemId INT  			NOT NULL, 
    SellerId INT 			NOT NULL,
    Quantity INT			DEFAULT 0,
    Price DECIMAL			NOT NULL,
    PRIMARY KEY (ItemId),
    CHECK (ItemId > 0 AND SellerId > 0 AND Quantity >= 0 AND Price > 0),
    FOREIGN KEY (SellerId) REFERENCES Seller (Id)
        ON UPDATE CASCADE,
    FOREIGN KEY (ItemId) REFERENCES Item(Id)
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Customers (
    Id INT			NOT NULL AUTO_INCREMENT,
    BillingInfoId INT		NOT NULL,
    LastName CHAR(20)		NOT NULL,
    FirstName CHAR(20)		NOT NULL,
    Email CHAR(40)		NOT NULL,
    PRIMARY KEY (Id),
    UNIQUE (BillingInfoId),
    FOREIGN KEY (BillingInfoId) REFERENCES BillingInfo (Id)
        ON UPDATE CASCADE,
    CHECK (Id > 0 AND BillingInfoId > 0)
);

CREATE TABLE IF NOT EXISTS ShoppingCart (
    CustomerId INT			NOT NULL,
    ItemId INT 				NOT NULL,
    SellerId INT                        NOT NULL,
    Quantity INT 			DEFAULT 1,
    Price Decimal 			NOT NULL,
    TotalPrice INT 			NOT NULL,
    PRIMARY KEY (CustomerId, ItemId),
    FOREIGN KEY (CustomerId) REFERENCES Customers (Id)
        ON UPDATE CASCADE,
    FOREIGN KEY (ItemId) REFERENCES Item (Id)
        ON UPDATE CASCADE,
    FOREIGN KEY (SellerId) REFERENCES Seller (Id)
        ON UPDATE CASCADE,
    CHECK (CustomerId > 0 AND ItemId > 0 AND SellerId > 0 AND Quantity > 0 AND Price > 0 AND TotalPrice > 0),
    CHECK (TotalPrice >= Price), 
    CHECK ( (SELECT Quantity FROM Inventory WHERE ItemId = Inventory (ItemId) AND SellerId = Inventory (SellerId)) >= Quantity)
);

CREATE TABLE IF NOT EXISTS Orders (
    Id INT			NOT NULL AUTO_INCREMENT,
    CustomerId INT		NOT NULL,
    BillingInfoId INT		NOT NULL,
    OrderDate DATE		NOT NULL,
    PRIMARY KEY (Id),
    FOREIGN KEY (CustomerId) REFERENCES Customers (Id)
        ON UPDATE CASCADE,
    FOREIGN KEY (BillingInfoId) REFERENCES BillingInfo (Id)
        ON UPDATE CASCADE,
    CHECK (Id > 0 AND CustomerId > 0 AND BillingInfoId > 0)
);

CREATE TABLE IF NOT EXISTS Ordered (
    OrderId INT                 NOT NULL,
    ItemId INT                  NOT NULL,
    SellerId INT                NOT NULL,
    Quantity INT                NOT NULL,
    Price DECIMAL               NOT NULL,
    PRIMARY KEY (OrderId, ItemId, SellerId),
    FOREIGN KEY (OrderId) REFERENCES Orders (Id),
    FOREIGN KEY (ItemId) REFERENCES Item (Id),
    FOREIGN KEY (SellerId) REFERENCES Seller (Id)
);

CREATE TABLE IF NOT EXISTS User (
    SellerId INT,
    CustomerId INT,
    Username CHAR(20)		NOT NULL,
    Password CHAR(20)		NOT NULL,
    PRIMARY KEY (Username),
    CHECK (CustomerId > 0),
    CHECK (CustomerId != SellerId)
);