FROM node:18.20.5 AS build
WORKDIR /app
COPY package*.json .
RUN npm install
ARG PROFILE=local
# temporary workaround to prevent parsing of unwanted files by tailwind
RUN find node_modules -path '*/needle/test/files/*' -delete
COPY . .
RUN if [ "$PROFILE" = "prod" ]; then \
      echo "Building for prod..."; \
      npm run build:prod; \
    elif [ "$PROFILE" = "local" ]; then \
      echo "Building for local..."; \
      npm run build:local; \
    else \
      echo "Building for dev..."; \
      npm run build:dev; \
    fi
  
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build app/dist/latte-client /usr/share/nginx/html
EXPOSE 80

