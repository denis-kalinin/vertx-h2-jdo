[![Build Status](https://travis-ci.org/denis-kalinin/vertx-h2-jdo.svg?branch=master)](https://travis-ci.org/denis-kalinin/vertx-h2-jdo)

My customer asked to develop a simple <acronym title="microservice">_&mu;Service_</acronym> with embedded database and RESTful API.
This is simple application emulating money accounts and transfers among them.
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

### SDLC &ndash; software development life cycle
GitHub &#x2192; TravisCI &#x2192; AWS S3 &#x2192; AWS CodeCommit

Deployment phase's files for SDLC are stored in `src/deploy/aws/codedeploy` and `.travis.yml`

### Running example
The sample instance is running on AWS at http://test.itranga.com

### Assumptions
Some assumptions was made in the project to facilitate development

1. RESTful API supports `application/json` only&mdash;`application/xml` was discarded for simplicity.
2. API doesn't support security
3. If `Transfer` is made without field `from` then it is considered as incoming transaction from another system and that system is responsible for balancing debit/credit
4. An account may have **negative balance**&mdash;there is no policy.
5. **Direct debit** is allowed, i.e. _negative amount_ in a transfer is valid: `{"from": 12, "to": 1, "amount": -45.42}`

### Binaries and Documentation
[Download binary file](https://s3-eu-west-1.amazonaws.com/vertx-h2-jdo/vertx-h2-jdo.zip) as `zip`-archive. The file also contains sources and javadoc.
[Javadoc API](http://test.itranga.com/javadoc/) is available online.

