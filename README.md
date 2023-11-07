# Jewellery Management Service

This is the backend service for a jewellery management administration system.

## Setup and Running the Project

### Prerequisites

You would need the following tools installed before running the project locally:

- Java 17
- Maven
- IntelliJ IDEA (or any preferred IDE)
- Docker

### Running the project

1. Create .env file in the root folder with database credentials:
   ```
   JMS_DATABASE_NAME=jewellery-management
   JMS_DATABASE_USER=admin
   JMS_DATABASE_PASSWORD=DB-p@s5w0rD
   ```
2. Start DB
    - run `docker-compose up` in a terminal in the root folder
      - This command will start a postgreSQL DB in a docker container with the properties we've entered in the .env file
3. Setup IntelliJ environment variables
    - Run -> Edit Configurations, then under Environment Variables, you should add the following:
   ```
   JMS_DATABASE_NAME=jewellery-management;JMS_DATABASE_USER=admin;JMS_DATABASE_PASSWORD=DB-p@s5w0rD;SECRET_KEY=9dDDE3/Z7EdcCqA35PbruWDfEt0Dxk5cbPGaaudhJ5o=
   ```
   The first 3 parameters are responsible for database connection and should match the ones we set up in
   step 1. The
   last one is a key for JWT token encoding. You can choose to use a different one.

4. Start the app
    - run `mvn clean install` in a terminal to get all the needed dependencies and to build the project
    - Run -> Run -> choose the configuration you set up in step 4
        - The app should be running on localhost:8080
5. Interact with the app

    In this application is implemented admin user. The next few lines describe how this user can be accessed.
   - Send POST to `localhost:8080/login` with JSON body with payload:
     ```json
     {
      "email": "root@gmail.com",
      "password": "p@s5W07d"
     }
     ```
     - The response will contain a token. You'd need to include this token in the Authorization
       header in every
       other request you'd want to send to the service.
         - Authorization header: `Authorization: Bearer <token>`
   - Access any endpoint of the service
     - Example of a `GET /resources` request using curl:
       ```bash
         curl --location 'localhost:8080/resources/quantity/d3515db3-d8a0-4807-bfae-40d53da0405a' \
           --header 'Authorization: Bearer
         eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290QGdtYWlsLmNvbSIsImlhdCI6MTY5NjQ5NjMzMywiZXhwIjoxNjk2NTgyNzMzfQ.WqZMlAvLWkPbqepGrdpwfQY1dG39Jr_69npIWJQb_3U'
       ```