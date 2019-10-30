# jVendingMachine
Java code to operate KNW2300 item vending machine. Handles input from keycard swiper and keypad to dispense items to teams.

Dependencies:
-Java (version 11+ recommended)
-JavaFX (must be installed separately after Java 8)
-jSerialComm (included in lib folder)

Usage:
Configure VM_Credit.csv, VM_Users.csv, and VM_Items.csv to match the class roster / team budgets. Then, start jVendingMachine.jar. 

Troubleshooting
-If the GUI fails to launch, run the executable jar from command line with the following syntax:
  "java -jar jVendingMachine.jar"
