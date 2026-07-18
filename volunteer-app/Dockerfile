# 管理端（Vue Admin）Docker 构建
# 注意：本 Dockerfile 仅用于构建前端管理端，不包含微信小程序代码
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
