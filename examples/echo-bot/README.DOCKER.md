# EchoBot — Docker packaging

Docker-файлы для сборки и запуска примера **echo-bot**.

## Файлы

- `Dockerfile` — multistage-сборка: Gradle build → slim JRE runtime image.
- `docker-compose.yml` — convenience compose-файл для сборки и запуска.
- `.dockerignore` — исключает артефакты сборки и секреты.

## Сборка и запуск

### 1. Сборка образа

```bash
# Из корня репозитория:
docker build -f examples/echo-bot/Dockerfile -t yuchat/echo-bot:latest .

# Или из папки примера:
cd examples/echo-bot
docker build -f Dockerfile -t yuchat/echo-bot:latest ../..
```

### 2. Запуск контейнера

```bash
docker run --rm \
  -e YUCHAT_BOT_TOKEN="$YUCHAT_BOT_TOKEN" \
  -e YUCHAT_BASE_URL="$YUCHAT_BASE_URL" \
  yuchat/echo-bot:latest
```

### 3. Через docker-compose

```bash
cd examples/echo-bot
YUCHAT_BOT_TOKEN=... YUCHAT_BASE_URL=https://your-host:8443 docker-compose up --build
```

## Примечания

- Образ ожидает, что Gradle `installDist` создаст `/app/bin/echo-bot` и дистрибутив в `/app`.
- Для production используйте docker secrets или передавайте переменные окружения с хоста.
- Контекст сборки — корень репозитория, чтобы Gradle мог видеть все модули проекта.
