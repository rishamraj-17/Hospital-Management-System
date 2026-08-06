# Contributing to HMS

First off, thank you for considering contributing to the Hospital Management System!

## Development Setup

1. Copy `.env.example` to `.env`
2. Start the database: `docker compose up -d db`
3. Run the application locally: `./mvnw spring-boot:run`

## Pull Request Process

1. Ensure any new features include appropriate tests.
2. Ensure you run `./mvnw clean verify` locally before pushing.
3. Update the README.md with details of changes to the interface, this includes new environment variables, exposed ports, useful file locations and container parameters.
4. Increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
5. You may merge the Pull Request in once you have the sign-off of two other developers, or if you do not have permission to do that, you may request the second reviewer to merge it for you.
