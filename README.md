# Service Health Tracker

## Run
```bash
docker compose up --build
# http://localhost:8080/swagger-ui.html
```
A **sample admin JWT** is printed on startup. Use:
```
Authorization: Bearer <token>
```

## API
- POST /api/v1/services
- GET /api/v1/services
- GET /api/v1/services/{id} 
- PATCH /api/v1/services/{id}
- DELETE /api/v1/services/{id}
- GET /api/v1/alerts

## Config
- MONGODB_URI (default mongodb://localhost:27017/healthtracker)
- JWT_SIGNING_SECRET (optional, overrides default hardcoded secret)
- health.check.tick-ms 

