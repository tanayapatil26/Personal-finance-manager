# Personal Finance Manager

Full-stack finance tracker: **Spring Boot REST API + MySQL + React dashboard**.
Evolved from a Java console application (see `Transaction`, `FileManager`, `Main`) into a layered web app.

## Features
- JWT authentication (register/login); every user sees only their own data (BCrypt-hashed passwords, stateless Spring Security)
- Add, edit, delete income/expense transactions (validated)
- Filter by category, type and date range
- Monthly budget with live "remaining / exceeded" status
- Dashboard: category breakdown (pie), 6-month income vs expense (bar)
- Next-month expense forecast using linear regression (least squares)
- Import `transactions.txt` from the old console app

## Architecture
```
React (Vite, Recharts, Axios)  --HTTP/JSON-->  Spring Boot (Controller -> Service -> Repository)  -->  MySQL
```

## REST API
| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Returns `{token, username}` (all other endpoints need `Authorization: Bearer <token>`) |
| GET | `/api/transactions?category=&type=&from=&to=` | List / filter |
| POST | `/api/transactions` | Create |
| PUT | `/api/transactions/{id}` | Update |
| DELETE | `/api/transactions/{id}` | Delete |
| POST | `/api/transactions/import` | Upload old `transactions.txt` |
| GET | `/api/summary?month=2026-10` | Income, expenses, balance, budget status |
| PUT | `/api/budget` | Set monthly budget `{month, amount}` |
| GET | `/api/analytics/categories?month=&type=` | Totals per category |
| GET | `/api/analytics/monthly?months=6` | Monthly trend |
| GET | `/api/analytics/forecast` | Next-month expense prediction |

## Run with Docker (one command)
```bash
cp .env.example .env     # then edit DB_PASSWORD and JWT_SECRET
docker compose up --build
```
Open http://localhost:3000 (API on :8080). MySQL data persists in a Docker volume.

## Run it manually
> If you ran the earlier version without login, recreate the DB first: `DROP DATABASE finance_db; CREATE DATABASE finance_db;` (tables now have a `user_id` column).

**1. Database**
```sql
CREATE DATABASE finance_db;
```
**2. Backend** (Java 17+, Maven)
```bash
cd backend
# optional: export DB_USER=root DB_PASSWORD=yourpassword
mvn spring-boot:run        # http://localhost:8080
```
**3. Frontend** (Node 18+)
```bash
cd frontend
npm install
npm run dev                # http://localhost:5173
```

## Sample request
```bash
curl -X POST localhost:8080/api/transactions -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount":450.50,"category":"Food","type":"EXPENSE","date":"2026-10-15"}'
```

## Next steps (to push it further)
- Deploy (Render/Railway + Vercel) and add the live link to your CV
- JUnit/MockMvc tests and a GitHub Actions CI pipeline
- Recurring transactions, CSV export, swap the forecast for an ML model
