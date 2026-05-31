# Checklist

This project contains the front- and backend of the collaborative checklist application.
The backend is written in Java with SpringBoot and the frontend is written in TypeScript with Angular.

The whole app is dockerized and can be started with a single command.
But the two parts can also be started locally for development (look at other READMEs in according directories).
To start the whole app with docker, of course [docker](https://www.docker.com/get-started/) must be installed and
running.
Also, the backend part must be built as a Jar (see README of backend).
If those preconditions are met, the app can be started with the following command: ```docker-compose up```
After that the frontend can be found at localhost:80.

The BPMN for the Cochlea-Operation has been modified and expanded by additional files.
Those files are located in the Cochlea-Modell.zip in this directory. All other files in the subprojects are for testing
purposes.

The two directories frontend and backend can be opened as separate projects in an IDE. 