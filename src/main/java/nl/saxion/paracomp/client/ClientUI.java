package nl.saxion.paracomp.client;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;

public class ClientUI {

    private final static Logger LOGGER = Logger.getLogger(ClientUI.class.getName());

    // enable next lines to disable logging
//    static {
//        LOGGER.setLevel(java.util.logging.Level.OFF);
//    }

    private final Scanner scanner;
    private final PrintStream out;
    private final PrintStream urgent;
    private final ClientAgent agent;

    // one scanner for keyboard interaction with user
    private ClientUI(InputStream input, PrintStream output, PrintStream error) {
        this(input, output, error, "client1");
    }

    // output for displaying info to user
    private ClientUI(InputStream input, PrintStream output, PrintStream error, String agentName) {
        LOGGER.info(String.format("Starting UI in thread %s",
                                  Thread.currentThread()
                                        .getName()));
        scanner = new Scanner(input);
        out = output;
        urgent = error;
        agent = new ClientAgent(agentName);
    }

    static void main(String[] args) {
        String name = args.length > 0 ?
                args[0] :
                "client1";
        new ClientUI(System.in, System.out, System.err, name).runUI();
    }

    private void runUI() {
        Optional<MenuOption> maybeOption;
        do {
            showMenu();
            maybeOption = tryReadMenuChoice();
            if (maybeOption.isEmpty()) {
                out.printf("Please enter a number between 1 and %d!\n", MenuOption.values().length);
            } else {
                processMenuOption(maybeOption.get());
            }
        } while (maybeOption.isEmpty() || maybeOption.get() != MenuOption.QUIT);
    }

    private void showMenu() {
        for (var option : MenuOption.values()) {
            out.printf("%d. %s\n", option.ordinal() + 1, option.description);
        }
        out.print("Please select an option: ");
    }

    // return empty optional if user did not select valid menu option
    // otherwise return optional with the chosen menu option
    private Optional<MenuOption> tryReadMenuChoice() {
        try {
            // read integer input
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= MenuOption.values().length) {
                // convert one-based menu choice to zero-based array index
                return Optional.of(MenuOption.values()[choice - 1]);
            }
        } catch (NumberFormatException nfe) {
            // ignore exception
        }
        // invalid choice
        return Optional.empty();
    }

    private void processMenuOption(MenuOption option) {
        try {

            switch (option) {
                case LIST_BUILDINGS -> listBuildings();
                case REQUEST_RESERVATION -> requestReservation();
                case CANCEL_RESERVATION -> cancelReservation();
                case CONFIRM_RESERVATION -> confirmReservation();
                case QUIT -> quit();
            }
        } catch (Exception _) {

        }
    }

    private void listBuildings() {
        agent.requestBuildingList();
    }

    private void requestReservation() {
        out.print("Please enter the name of the building you want to reserve: ");
        final String buildingName = scanner.nextLine();

        out.print("Please enter the room number you want to reserve: ");
        final int roomNumber = Integer.parseInt(scanner.nextLine());

        agent.requestReservation(buildingName, roomNumber);
    }

    private void confirmReservation() {
        out.print("Please enter the reservation ID you want to confirm: ");
        final UUID reservationId = UUID.fromString(scanner.nextLine());

        agent.confirmReservation(reservationId);
    }

    private void cancelReservation() {
        out.print("Please enter the reservation ID you want to cancel: ");
        final UUID reservationId = UUID.fromString(scanner.nextLine());

        agent.cancelReservation(reservationId);
    }

    private void quit() {
        agent.close();
    }

    private enum MenuOption {
        // specify menu options
        LIST_BUILDINGS("List buildings"),
        REQUEST_RESERVATION("Request reservation"),
        CANCEL_RESERVATION("Cancel reservation"),
        CONFIRM_RESERVATION("Confirm reservation"),
        QUIT("Exit application");

        // menu description to display in UI
        private final String description;

        MenuOption(String description) {
            this.description = description;
        }
    }
}