# ConferenceRent

## Processes

Run one or more rental agents, building agents, and clients. Each process
connects to RabbitMQ using `src/main/resources/nl/saxion/paracomp/rabbitmq.properties`.

Main classes:

- `nl.saxion.paracomp.rentalagent.RentalAgent`
- `nl.saxion.paracomp.building.BuildingAgent`
- `nl.saxion.paracomp.client.ClientUI`

Start a building with optional `name`, `address`, and `roomCount` arguments.
Its building UUID is shown when a client lists buildings.

## Message flow

- `clientExchange` is a **direct** exchange. Clients publish requests with the
  `rent` routing key. All rental agents consume the same `userRentQueue`, so
  RabbitMQ distributes requests between them.
- Buildings announce their details and availability on the
  `buildingRegistryExchange` **fanout** exchange. Rental agents keep those
  details in `RentalRegistry` and expire buildings that stop sending heartbeats.
- The rental agent handles booking, availability checks, confirmation, and
  cancellation. `RentalRegistry` owns the reservation map; a building agent
  only advertises its room capacity and identity.
- Reservation changes are broadcast on `reservationRegistryExchange` so the
  other running rental agents can update their in-memory registries.
- Replies go to the temporary client queue named in the request and use the
  correlation ID to match the request.

The registries are in memory. Restarting all rental agents loses reservations;
updates between running agents are asynchronous.
