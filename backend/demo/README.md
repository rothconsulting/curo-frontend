# Demo process

#### Purpose:
We add a demo process for testing our library (backend/library).

#### What does it:
You can start a new technic suggestion for your software architect. 
The software architect can then approve or discard the suggestion.
You can see the logs in the Terminal where you start the backend.

#### How to start:
In the Terminal start the backend (Camunda):
```commandline
cd backend
./gradlew bootRun
```
To see the process in Camunda go on http://localhost:8080 and log in with the default credentials.

To see the SwaggerUI go on http://localhost:8080/swagger-ui.html#/ and log in with the default credentials.

In another Terminal window Start the frontend (Curo):
```commandline
cd frontend
npm start
```
To see Curo go on http://localhost:8080.
