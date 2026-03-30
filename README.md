# Shop Backend Service

Многомодульный backend-проект под ТЗ: **Ktor + PostgreSQL + Exposed + JWT + Redis + RabbitMQ + Swagger + тесты + Docker + GitHub Actions**.

## Что реализовано

### Пользовательские возможности
- регистрация: `POST /auth/register`
- авторизация: `POST /auth/login`
- просмотр списка товаров: `GET /products`
- просмотр товара: `GET /products/{id}`
- создание заказа: `POST /orders`
- история заказов: `GET /orders`
- отмена заказа: `DELETE /orders/{id}`

### Админские возможности
- создание товара: `POST /products`
- изменение товара: `PUT /products/{id}`
- удаление товара: `DELETE /products/{id}`
- статистика: `GET /stats/orders`

### Технические требования
- **Ktor backend**
- **PostgreSQL**
- **Exposed ORM**
- **JWT авторизация**
- **Redis кэширование**
- **RabbitMQ** (выбран вместо Kafka)
- **Swagger / OpenAPI**
- **Flyway миграции**
- **Unit + Integration + E2E tests**
- **Docker + docker-compose**
- **GitHub Actions CI**

---

## Архитектура

Проект разбит на модули:

```text
shared/   -> DTO, enum'ы, события очереди
app/      -> HTTP API, бизнес-логика, БД, кэш, JWT, Swagger
worker/   -> consumer RabbitMQ, логирование событий, fake-email заглушка
```

Внутри `app` слои разделены на:
- `routes`
- `service`
- `repository`
- `domain`
- `database`
- `plugins`
- `security`
- `cache`
- `messaging`

---

## База данных

Миграция Flyway создаёт таблицы:

- `users`
- `products`
- `orders`
- `order_items`
- `audit_logs`

В проекте есть:
- внешние ключи
- индексы
- связи 1:N:
  - `users -> orders`
  - `orders -> order_items`

---

## Бизнес-логика заказа

При создании заказа:
1. проверяется наличие товаров
2. проверяется stock
3. уменьшается stock
4. создаётся `order`
5. создаются `order_items`
6. пишется запись в `audit_logs`
7. публикуется событие в RabbitMQ
8. заказ кэшируется в Redis

При отмене заказа:
1. проверяется принадлежность заказа пользователю
2. статус меняется на `CANCELLED`
3. stock восстанавливается
4. запись попадает в `audit_logs`
5. публикуется событие
6. кэш заказа обновляется

---

## Swagger / OpenAPI

После запуска доступны:
- OpenAPI: `http://localhost:8080/openapi`
- Swagger UI: `http://localhost:8080/swagger`

---

## Переменные окружения

Скопируй `.env.example` в `.env` и при необходимости измени значения.

Ключевые переменные:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `JWT_SECRET`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

---

## Быстрый запуск через Docker Compose

```bash
cp .env.example .env
docker compose up --build
```

После этого поднимутся:
- PostgreSQL
- Redis
- RabbitMQ
- API app
- worker

### Полезные URL
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger`
- RabbitMQ UI: `http://localhost:15672`

---

## Локальный запуск без Docker

Нужны:
- JDK 21
- Gradle 8.14+
- PostgreSQL
- Redis
- RabbitMQ

Запуск API:
```bash
gradle :app:run
```

Запуск worker:
```bash
gradle :worker:run
```

---

## Тесты

### Unit tests
Примеры:
- `AuthServiceTest`
- `ProductServiceTest`
- `OrderServiceTest`

### Integration tests
Через **Testcontainers**:
- `FlywayMigrationIntegrationTest`
- `OrderRepositoryIntegrationTest`

### E2E tests
На уровне API:
- `AuthAndProductsE2ETest`
- `OrderLifecycleE2ETest`

Запуск:
```bash
gradle test
```

---

## GitHub Actions

Файл workflow:
```text
.github/workflows/ci.yml
```

Что делает pipeline:
- поднимает Java 21
- настраивает Gradle
- запускает `gradle clean test build`
- сохраняет артефакты тестов

---

## Пример сценария проверки

### 1. Логин под админом
`ADMIN_EMAIL` / `ADMIN_PASSWORD` подставляются из env и bootstrap'ятся при старте приложения.

### 2. Создай товар
```http
POST /products
Authorization: Bearer <admin-jwt>
```

### 3. Зарегистрируй пользователя
```http
POST /auth/register
```

### 4. Создай заказ
```http
POST /orders
Authorization: Bearer <user-jwt>
```

### 5. Отмени заказ
```http
DELETE /orders/{id}
Authorization: Bearer <user-jwt>
```

---

## Что ещё можно улучшить

Если захочешь довести проект до прод-уровня, можно добавить:
- refresh token flow
- role-based permission middleware
- optimistic/pessimistic locking для stock
- idempotency keys для order creation
- Prometheus / Micrometer
- real email provider
- отдельные DTO/request validators
- integration tests для Redis и RabbitMQ

---
