[![Build Status](https://travis-ci.org/denis-kalinin/vertx-h2-jdo.svg?branch=master)](https://travis-ci.org/denis-kalinin/vertx-h2-jdo)

My customer asked to develop a simple <acronym title="microservice">_&mu;Service_</acronym> with embedded datastore and RESTful API. 
The customer had "ostracized" Spring framework for some reasons, so I chose the following tech stack to build the application:
### Tech stack
- Java 8
- Vert.x
- Guice
- Jackson
- JDO : Datanucleus
- RxJava
- [RAML](http://raml.org) : _RESTful API Modeling Language_
- RAML console : _The RAML Console allows browsing of API documentation and in-browser testing of API methods._
<br />_(I fixed [a minor bug](https://github.com/mulesoft/api-console/pull/296) while working on this project)_
- H2 database
- Junit
- slf4j + logback
- Gradle & Groovy
- Handlebars.java

### SDLC &mdash; software development life cycle
GitHub &#x2192; TravisCI &#x2192; AWS S3 &#x2192; AWS CodeCommit

Deployment phase's files for SDLC are stored in `src/deploy/aws/codedeploy` and `.travis.yml`

### Assumptions
Some assumptions was made in the project to facilitate development

1. RESTful API supports `application/json` only &mdash; `application/xml` was discarded for simplicity.
2. API doesn't support security
3. If `Transfer` is made without field `from` then it is considered as incoming transaction from another system and that system is responsible for balancing debit/credit
4. **Direct debit** is allowed, i.e. negative amount in a transfer is valid:
```json
{
  "from": 12,
  "to": 1,
  "amount": -45.42
}
```
5. An account may have negative balance
