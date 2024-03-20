/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/14/2024
 * @version: 0.1
 */
-- Create the new database for the program
CREATE DATABASE IF NOT EXISTS universal_gift_registry;

--Use the new database
USE universal_gift_registry;

/**Create a new user specifically for the new database that is used within the program
 *to update the information contained within the tables as the program operates
*/
CREATE USER IF NOT EXISTS 'ugradmin'@'localhost' IDENTIFIED BY 'ugrpassword101*';
GRANT ALL PRIVILEGES ON universal_gift_registry.* TO 'ugradmin'@'localhost';
FLUSH PRIVILEGES;

--Build the table for the User entities
CREATE TABLE IF NOT EXISTS User
(
  Email VARCHAR(255) NOT NULL,
  Street_Address VARCHAR(255) NOT NULL,
  City VARCHAR(15) NOT NULL,
  State CHAR(2) NOT NULL,
  Zip CHAR(5) NOT NULL,
  First_Name VARCHAR(255) NOT NULL,
  Last_Name VARCHAR(255) NOT NULL,
  Password  VARCHAR(16) NOT NULL,
  PRIMARY KEY (Email)
);

--Build the table for the List entities
CREATE TABLE IF NOT EXISTS List
(
  Occasion CHAR(4) NOT NULL,
  ListName VARCHAR(25) NOT NULL,
  ListID INT NOT NULL AUTO_INCREMENT,
  Email VARCHAR(50) NOT NULL,
  PRIMARY KEY (ListID),
  FOREIGN KEY (Email) REFERENCES User(Email)
);

--Build the table for the Item entities
CREATE TABLE IF NOT EXISTS Item
(
  ItemURL VARCHAR(2048) NOT NULL,
  ItemImage VARCHAR(255),
  ItemName VARCHAR(2048) NOT NULL,
  ItemSize VARCHAR(50),
  ItemColor VARCHAR(50),
  ItemPrice NUMERIC(7,2) NOT NULL,
  ItemID INT NOT NULL AUTO_INCREMENT,
  Purchased Boolean NOT NULL,
  Email VARCHAR(50) NOT NULL,
  ListID INT NOT NULL,
  PRIMARY KEY (ItemID),
  FOREIGN KEY (Email) REFERENCES User(Email),
  FOREIGN KEY (ListID) REFERENCES List(ListID)
);