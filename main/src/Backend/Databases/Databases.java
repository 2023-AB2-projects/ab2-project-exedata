package Backend.Databases;

import java.util.List;

public class Databases {
    private List<Database> databaseList;

    public Databases(List<Database> databaseList) {
        this.databaseList = databaseList;
    }

    public Databases() {
    }

    public List<Database> getDatabaseList() {
        return databaseList;
    }

    public void setDatabaseList(List<Database> databaseList) {
        this.databaseList = databaseList;
    }

    public void addDatabase(Database database) {
        databaseList.add(database);
    }

    public void dropDatabase(Database database) {
        databaseList.remove(database);
    }

    public void dropDatabase(String databaseName) {
        for (Database i : databaseList) {
            if (i.getName().equals(databaseName)) {
                databaseList.remove(i);
                return;
            }
        }
    }

    public boolean checkDatabaseExists(String databaseName) {
        for (Database i : databaseList) {
            if (i.getName().equals(databaseName)) {
                return true;
            }
        }
        return false;
    }

    public Database getDatabase(String databaseName) {
        for (Database i : databaseList) {
            if (i.getName().equals(databaseName)) {
                return i;
            }
        }
        return null;
    }
}
