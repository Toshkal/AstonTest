-- Создаем таблицу для хранения банковских счетов
CREATE TABLE IF NOT EXISTS Accounts (
    Id UUID PRIMARY KEY,
    Account_Number VARCHAR(10) NOT NULL ,
    Name VARCHAR(255) NOT NULL,
    Pin_Code VARCHAR(60) NOT NULL,
    Balance DECIMAL(10, 2) DEFAULT 0.00
    );

-- Создаем таблицу для хранения операций по счетам (если нужно)
CREATE TABLE IF NOT EXISTS Transactions (
    TransactionID UUID PRIMARY KEY,
    Account_From_Number VARCHAR(20) NOT NULL,
    Account_To_Number VARCHAR(10),
    Amount DECIMAL(10, 2) NOT NULL,
    Transaction_Type VARCHAR(255) NOT NULL,
    Transaction_Date TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
    );