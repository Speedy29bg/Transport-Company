# 🚀 Quick Start Guide - Transport Company Management System

## Стартиране за 5 минути

### Стъпка 1: Database Setup (2 минути)

```sql
-- Влезте в MySQL като root
mysql -u root -p

-- Създайте database и user
CREATE DATABASE transport_company;
CREATE USER 'transport_user'@'localhost' IDENTIFIED BY 'secret';
GRANT ALL PRIVILEGES ON transport_company.* TO 'transport_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Стъпка 2: Стартиране на приложението (1 минута)

```bash
# Влезте в проектната директория
cd "c:\Users\GILIEV\Documents\GitHub\Transport-Company\Transport Company"

# Стартирайте JAR-а (migrations ще се изпълнят автоматично)
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar help
```

### Стъпка 3: Създайте първа компания (30 секунди)

```bash
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company add "Speedy Transport" "Sofia, Bulgaria"
# Output: Company created successfully with ID: 1
```

### Стъпка 4: Добавете клиент (30 секунди)

```bash
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar client add "John Doe" "+359888123456" "john@email.com" -t PERSON
# Output: Client created successfully with ID: 1
```

### Стъпка 5: Добавете шофьор (30 секунди)

```bash
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar employee add 1 "Ivan Petrov" 3000 -r DRIVER -q PASSENGERS_12_PLUS
# Output: Employee created successfully with ID: 1
```

### Стъпка 6: Добавете превозно средство (30 секунди)

```bash
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar vehicle add 1 "CA1234AB" BUS --seats 50
# Output: Vehicle created successfully with ID: 1
```

### Стъпка 7: Създайте транспорт (1 минута)

```bash
# Примерна команда за транспорт (пригодете датите)
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar transport add 1 1 "Sofia" "Plovdiv" "2026-01-20T08:00:00+02:00" "2026-01-20T10:00:00+02:00" PASSENGERS 150.00 1 1 --passengers 45 --status UNPAID
# Output: Transport created successfully with ID: 1
```

### Стъпка 8: Генерирайте отчет (30 секунди)

```bash
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report totals
# Output:
# === TOTALS REPORT ===
# Total Transports: 1
# Total Revenue: 150.00
```

---

## 🎯 Често използвани команди

### Компании
```bash
# Всички компании
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company list

# Сортирани по приход
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company list --sort=revenue

# Вземи по ID
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company get 1

# Редактирай
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company edit 1 -n "New Name" -a "New Address"

# Изтрий
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar company delete 1
```

### Клиенти
```bash
# Всички клиенти
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar client list

# Добави фирма
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar client add "ABC Ltd" "+359888999888" "info@abc.com" -t COMPANY
```

### Служители
```bash
# Всички служители
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar employee list

# Филтрирани по qualification
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar employee list --qualification PASSENGERS_12_PLUS

# Сортирани по заплата
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar employee list --sort=salary
```

### Превозни средства
```bash
# Всички превозни средства за компания
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar vehicle list 1

# Добави камион
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar vehicle add 1 "CA5678CD" TRUCK --load 15000

# Добави цистерна
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar vehicle add 1 "CA9012EF" TANKER --volume 25000
```

### Транспорти
```bash
# Всички транспорти
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar transport list

# Филтрирани по дестинация
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar transport list --destination Plovdiv

# Сортирани по дата
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar transport list --sort=depart
```

### Плащания
```bash
# Маркирай като платено
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar payment set 1 PAID

# Маркирай като неплатено
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar payment set 1 UNPAID
```

### Export/Import
```bash
# Export to CSV
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar export transports --out=transports.csv

# Import from CSV
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar import transports --in=transports.csv
```

### Отчети
```bash
# Общи статистики
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report totals

# Само платени
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report totals --paid-only

# Брой транспорти по шофьор
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report drivers

# Приход за период
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report revenue --from=2026-01-01 --to=2026-01-31

# Приход по шофьор за период
java -jar target/transport-company-1.0.0-SNAPSHOT-shaded.jar report driver-revenue --from=2026-01-01 --to=2026-01-31
```

---

## 🔧 Troubleshooting

### Database Connection Error
```
Error: Communications link failure

Solution:
1. Проверете дали MySQL работи: services.msc → MySQL
2. Проверете application.properties за правилни credentials
3. Проверете дали database-а съществува
```

### JAR Not Found
```
Error: target/transport-company-1.0.0-SNAPSHOT.jar not found

Solution:
1. Build проекта: mvn clean package
2. Проверете в target/ директорията
```

### Invalid Date Format
```
Error: Cannot parse date

Solution:
Използвайте ISO format: 2026-01-20T08:00:00+02:00
                        YYYY-MM-DDTHH:MM:SS+OFFSET
```

---

## 📚 Допълнителни ресурси

- Пълна документация: [README.md](README.md)
- Статус на имплементацията: [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md)
- Requirements mapping: [REQUIREMENTS_TRACEABILITY.md](REQUIREMENTS_TRACEABILITY.md)
- Финален отчет: [COMPLETION_REPORT.md](COMPLETION_REPORT.md)

---

**Готово! Приложението работи! 🎉**
