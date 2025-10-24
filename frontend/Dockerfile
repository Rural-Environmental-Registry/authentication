# Etapa 1: build do Vue
FROM node:18 AS build-stage
WORKDIR /app
COPY . .
ENV CI=true
RUN npm install pnpm -g
RUN yes | pnpm install
RUN pnpm run build


FROM nginx:alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html/rechml/auth
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

#FROM nginx:alpine

#COPY dist/ /usr/share/nginx/htmls

# Redireciona todas as rotas da SPA para index.html

