# Checklist

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 16.1.5.
You should be familiar with the concepts of [Angular](https://angular.io/guide/understanding-angular-overview) to work with this project.

## Preconditions

- Node.js is installed ([v18.16.0](https://nodejs.org/en/blog/release/v18.16.0) was used, but any version higher than 18.10.0 should work)
- Angular CLI is installed via `npm install -g @angular/cli`
- The packages are installed via `npm install` (in project directory)

## Architecture

This code is divided into multiple modules:

- **admin:** This module is responsible for the admin view. It lists the documentations and allows the admin to delete or create new operation templates.
- **documentation:** This module is responsible for the documentation. It holds the documentation editors and loads, updates and deletes documentation entries. It also starts a new documentation, which acts like a session.
- **operation:** This module is responsible for rendering the checklist and all its components. The main rendering loop can be found in the path component, which can be called in other components recursively.
- **circulating:** This module is responsible for the circulating view. It receives and finishes circulating tasks.
- **shared:** This module holds components that are shared between the other modules.

## Commands

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Tipps

- nvm can be used for NodeJS version management
- RxJS is used for reactive programming. A small intro can be found [here](https://www.learnrxjs.io)
- Use [Angular Material](https://material.angular.io/) for the UI components
- ngx-translate is used for translations. The translations are stored in the `src/assets/i18n` folder.
- This app will only fully work when the backend is running at localhost. 
