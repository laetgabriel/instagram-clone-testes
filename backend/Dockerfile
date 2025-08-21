FROM maven:3.9.11-amazoncorretto-24-alpine
LABEL maintainer="paulopereira.dev"

EXPOSE 8080

RUN mkdir -p /home/instagram

COPY ./instagram /home/instagram

WORKDIR /home/instagram
