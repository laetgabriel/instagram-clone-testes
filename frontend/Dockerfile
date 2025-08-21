FROM node:24-alpine3.21
LABEL maintainer="paulopereira.dev"

WORKDIR /home/instagram

COPY ./instagram/package.json ./package.json
COPY ./instagram/package-lock.json ./package-lock.json

RUN npm install

COPY ./instagram .

EXPOSE 3000

CMD ["npm", "run", "dev"]
