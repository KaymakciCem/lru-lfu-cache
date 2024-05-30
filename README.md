# LRU and LFU assignment
___
### Spring Boot Application

#### 
- This project provides an API where users can add, remove or get cache items. 
- You can initialize the cache.strategy key inside of the application.yml file. 
- Available values for cache.strategy are LRU and LFU.

### Tech Stack
- Java 17
- Spring Boot
- Restful API
- Docker
- k8s deployment file (configurations added for the k8s cluster. the required image is on dockerhub)

### Prerequisites

---
- Maven
- Docker

### Build & Run

first go to the terminal and open up the project directory. "~/java-engineer-distributed-systems-challenge"

### build

mvn clean install

### Run tests

mvn test

### Docker

to run the project
 - docker-compose up -d --build

to stop the project
 - docker-compose down

### explicitly building docker images
 - docker build -t caching-systems-service .

### API DOCUMENTATION (Swagger)

- After project runs you will be able to reach the url below where you can see the API doc.
- http://localhost:4040/swagger-ui/index.html

### Metrics

- Some metrics are enabled on the actuator api. We can observe the system on production.
- http://localhost:4040/actuator

### Prometheus
http://localhost:4040/actuator/prometheus