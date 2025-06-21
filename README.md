# Coffee Shop Customer App

A microservices-based solution for a global coffee shop chain, enabling customers to pre-order coffee via iOS/Android apps and shop owners to manage queues and menus on Android. Built with Java Spring Boot, PostgreSQL, and Liquibase, it features secure JWT authentication, geolocation-based shop discovery, queue management, and real-time notifications. Deployable on AWS using Docker Compose for development/staging and EKS for production, with scalable APIs for third-party integration.

## Services
- **Authentication (8085)**: User registration, login, JWT validation.
- **Order (8082)**: Order processing, status, cancellation.
- **Shop (8084)**: Shop configuration, menu, nearby shop discovery.
- **Customer (8080)**: Profile management, order history.
- **Notification (8081)**: SMS/push notifications.
- **Queue (8083)**: Queue management, wait time estimation.

## Setup
1. Clone repo: `git clone <repo-url>`
2. Configure `.env` with database and secrets.
3. Run: `docker-compose up --build -d`
4. Test APIs with Postman/cURL.
