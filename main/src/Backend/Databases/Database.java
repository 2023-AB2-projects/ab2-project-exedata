package Backend.Databases;

import java.util.List;

public class Database {
    private String name;
    private List<Table> tables;

    public Database(String name, List<Table> tables) {
        this.name = name;
        this.tables = tables;
    }

    public Database() {
    }

    public boolean checkTableExists(String tableName) {
        for (Table i : tables) {
            if (i.getName().equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public Table getTable(String tableName) {
        for (Table i : tables) {
            if (i.getName().equals(tableName)) {
                return i;
            }
        }
        return null;
    }

    public void dropTable(String tableName) {
        tables.removeIf(i -> i.getName().equalsIgnoreCase(tableName));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
