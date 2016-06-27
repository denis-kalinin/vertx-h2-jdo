[![Build Status](https://travis-ci.org/denis-kalinin/vertx-h2-jdo.svg?branch=master)](https://travis-ci.org/denis-kalinin/vertx-h2-jdo)

My customer asked to develop a simple microservice with embedded datastore and RESTful API. The customer had "ostracized" Spring framework (and relatives), for some hidden reasons, so I chose the following tech stack to build the application:
- Vert.x
- Guice
- Jackson
- JDO
- RxJava
- RAML
- H2 database
- Junit
- slf4j + logback
- Gradle
- Handlebars.java