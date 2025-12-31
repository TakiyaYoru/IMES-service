# Docker Setup

## Start PostgreSQL

```bash
docker-compose up -d
```

## Stop PostgreSQL

```bash
docker-compose down
```

## View logs

```bash
docker-compose logs -f postgres
```

## Connect to database

```bash
docker exec -it imes-postgres psql -U imes_user -d imes_db
```
