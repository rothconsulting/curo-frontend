# Curo

## Getting started

1. Run `npm install --save @umb-ag/curo-core` to install the Curo core dependency.
2. Add `CuroCoreModule` to your AppModule imports.
3. Provide `CURO_BASE_PATH` configuration if needed.
   ```typescript
   {
     provide: CURO_BASE_PATH,
     useValue: '/api/curo-api'
   },
   ```

## Development server

Run `npm start` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `npm run build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `npm test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.
