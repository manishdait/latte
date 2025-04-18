FROM node:18.20.5 AS build
WORKDIR /app
COPY package*.json .
RUN npm install
# temporary workaround to prevent parsing of unwanted files by tailwind
RUN find node_modules -path '*/needle/test/files/*' -delete
COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build app/dist/latte-client /usr/share/nginx/html
EXPOSE 80

