# ConferenceRent

ConferenceRent is a distributed room reservation application built with Java and RabbitMQ. It demonstrates how independent services can register room availability, process reservation requests, and return responses through asynchronous messaging.

## Features

- Register buildings and publish room availability through periodic heartbeats
- List available buildings
- Request, confirm, and cancel room reservations
- Route messages between client, rental, and building processes with RabbitMQ
- Run multiple rental agents and clients

## Architecture

The application is made up of three process types:

| Process | Responsibility |
| --- | --- |
| Client | Provides a console menu for listing buildings and managing reservations |
| Rental agent | Tracks registered buildings, coordinates reservations, and replies to clients |
| Building agent | Advertises a building and its rooms, and handles room reservation requests |

RabbitMQ exchanges route client requests, building heartbeats, building requests, and replies. The rental agents maintain in-memory registries and share reservation updates while running. See [doc/architecture.md](doc/architecture.md) for the message flow and storage details.

## Technology

- Java 25
- Maven
- RabbitMQ (AMQP)
- Jackson for JSON message serialization
- SLF4J for logging

## Getting started

### Prerequisites

- JDK 25
- Maven
- RabbitMQ running locally, or another RabbitMQ server you can configure

### Configure RabbitMQ

Connection settings are in [`src/main/resources/nl/saxion/paracomp/rabbitmq.properties`](src/main/resources/nl/saxion/paracomp/rabbitmq.properties). The defaults connect to `localhost:5672` with the `guest` user and password.

### Build

```sh
mvn compile
```

### Run

Start each process in a separate terminal from the project root:

```sh
mvn exec:java -Dexec.mainClass=nl.saxion.paracomp.rentalagent.RentalAgent
```

```sh
mvn exec:java -Dexec.mainClass=nl.saxion.paracomp.building.BuildingAgent
```

```sh
mvn exec:java -Dexec.mainClass=nl.saxion.paracomp.client.ClientUI
```

The building agent accepts optional `name`, `address`, and `roomCount` arguments. The client menu lets you list buildings and request, confirm, or cancel reservations. Keep the rental and building processes running while using the client. Press Enter in an agent terminal to stop that process, and choose the client's exit option to quit.

## Project structure

```text
src/main/java/nl/saxion/paracomp/
├── building/       Building agent and message handling
├── client/         Console client and RabbitMQ agent
├── domain/         Reservation, building, room, and message models
└── rentalagent/    Rental agent and reservation coordination
```

## Notes

Building and reservation registries are held in memory. Restarting the rental agents clears reservations, and updates between running rental agents are asynchronous.
