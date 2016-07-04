[![Build Status](https://travis-ci.org/denis-kalinin/vertx-h2-jdo.svg?branch=master)](https://travis-ci.org/denis-kalinin/vertx-h2-jdo)

My customer asked to develop a simple microservice with embedded datastore and RESTful API. The customer had "ostracized" Spring framework, for some hidden reasons, so I chose the following tech stack to build the application:
- Java 8
- Vert.x
- Guice
- Jackson
- JDO
- RxJava
- [RAML](http://raml.org) : RESTful API Modeling Language. <small>Fixed by the way [a minor bug](https://github.com/mulesoft/api-console/pull/296) in web api-console while working on this project.</small>
- H2 database
- Junit
- slf4j + logback
- Gradle/Groovy
- Handlebars.java

Continuous Deployment:
	GitHub &#x2192; TravisCI &#x2192; AWS S3 &#x2192; AWS CodeCommit