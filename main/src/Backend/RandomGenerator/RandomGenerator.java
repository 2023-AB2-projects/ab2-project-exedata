package Backend.RandomGenerator;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import Frontend.Connection.ClientConnection;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class RandomGenerator {
    public static void main(String[] args) {
        new RandomGenerator("test", "alma2", 100000);
    }

    private final String databaseName;
    private final String tableName;
    private final int numberOfRows;

    public RandomGenerator(String databaseName, String tableName, int numberOfRows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.numberOfRows = numberOfRows;
        generate();
    }

    private void generate() {
        Databases databases = LoadJSON.load("databases.json");
        assert databases != null;
        Database database = databases.getDatabase(databaseName);
        Table table = database.getTable(tableName);
        List<Attribute> attributeList = table.getStructure();
        List<String> primaryKeyList = table.getPrimaryKey();
        List<String> uniqueList = table.getUniqueKeys();
        ClientConnection clientConnection = new ClientConnection(12000);
        StringBuilder insert;
        StringBuilder values;
        boolean[] uniqueColumn = new boolean[attributeList.size()];
        for (int i = 0; i < attributeList.size(); i++) {
            uniqueColumn[i] = false;
            for (String s : uniqueList) {
                if (attributeList.get(i).getName().equals(s)) {
                    uniqueColumn[i] = true;
                    break;
                }
            }
            for (String s : primaryKeyList) {
                if (attributeList.get(i).getName().equals(s)) {
                    uniqueColumn[i] = true;
                    break;
                }
            }
        }
        Random random = new Random();
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        try {
            clientConnection.send("USE " + databaseName + ";");
            for (int i = 76376; i < numberOfRows; i++) {
                insert = new StringBuilder("INSERT INTO " + tableName + " (");
                values = new StringBuilder();
                for (int j = 0; j < attributeList.size(); j++) {
                    insert.append(attributeList.get(j).getName()).append(", ");
                    String type = attributeList.get(j).getType();
                    if (uniqueColumn[j]) {
                        if (type.equals("INT") || type.equals("FLOAT")) {
                            values.append(i).append(", ");
                        } else {
                            values.append("'").append(i).append("'").append(", ");
                        }
                    } else {
                        switch (type) {
                            case "INT", "FLOAT" -> values.append(random.nextInt(100000)).append(", ");
                            case "VARCHAR" -> {
                                int length = 10;
                                StringBuilder s = new StringBuilder(length);
                                for (int k = 0; k < length; k++) {
                                    int index = random.nextInt(charSet.length());
                                    s.append(charSet.charAt(index));
                                }
                                values.append("'").append(s).append("'").append(", ");
                            }
                            case "BIT" -> values.append(random.nextInt(1)).append(", ");
                            case "DATE", "DATETIME" -> {
                                int minYear = 1000;
                                int maxYear = 2023;
                                int year = random.nextInt(maxYear - minYear + 1) + minYear;
                                int month = random.nextInt(12) + 1;
                                int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
                                int day = random.nextInt(maxDay) + 1;
                                LocalDate randomDate = LocalDate.of(year, month, day);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                String formattedDate = randomDate.format(formatter);
                                values.append("'").append(formattedDate).append("'").append(", ");
                            }
                        }
                    }
                }
                values = new StringBuilder(values.substring(0, values.length() - 2));
                insert = new StringBuilder(insert.substring(0, insert.length() - 2));
                insert.append(") VALUES (");
                insert.append(values).append(");");
                clientConnection.send(insert.toString());
            }
            clientConnection.send("END");
            while (true) {

            }
        } catch (IOException e) {
            ErrorClient.send("Error!");
        }
        clientConnection.disconnect();
    }
}
