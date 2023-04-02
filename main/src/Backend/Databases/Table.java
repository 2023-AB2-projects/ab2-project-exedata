package Backend.Databases;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static Backend.SocketServer.Server.errorClient;

public class Table {
    private String name;
    private List<Attribute> structure;
    private List<String> primaryKey;
    private List<ForeignKey> foreignKeys;
    private List<String> uniqueKeys;
    private List<IndexFile> indexFiles;

    public Table(String name, List<Attribute> structure, List<String> primaryKey, List<ForeignKey> ForeignKeys, List<String> uniqueKeys, List<IndexFile> indexFiles) {
        this.name = name;
        this.structure = structure;
        this.primaryKey = primaryKey;
        this.foreignKeys = ForeignKeys;
        this.uniqueKeys = uniqueKeys;
        this.indexFiles = indexFiles;
    }

    public Table() {
    }

    public boolean checkInsertColumn(String[] column) {
        int numberOfPrimaryKey = primaryKey.size();
        for (String i : column) {
            if (!checkAttributeExists(i)) {
                System.out.println("This column doesn't exists: " + i);
                errorClient.send("This column doesn't exists: " + i);
            }
            if (primaryKey.contains(i)) {
                numberOfPrimaryKey--;
            }
        }
        if (numberOfPrimaryKey != 0) {
            System.out.println("Have a problem with primary keys!");
            errorClient.send("Have a problem with primary keys!");
            return false;
        }
        for (Attribute i : structure) {
            if (i.getIsnull().equalsIgnoreCase("0") && !Arrays.stream(column).toList().contains(i)) {
                System.out.println(i.getName() + " can not be null!");
                errorClient.send(i.getName() + " can not be null!");
                return false;
            }
        }
        return true;
    }

    public Attribute getAttribute(String attributeName){
        for (Attribute i : structure){
            if(i.getName().equalsIgnoreCase(attributeName))
                return i;
        }
        return null;
    }

    public boolean checkAttributeExists(String attributeName) {
        for (Attribute i : structure) {
            if (i.getName().equalsIgnoreCase(attributeName)) {
                return true;
            }
        }
        return false;
    }

    public void dropIndex(String indexName) {
        indexFiles.removeIf(i -> i.getIndexName().equalsIgnoreCase(indexName));
    }

    public void addIndexFile(IndexFile indexFile) {
        indexFiles.add(indexFile);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        foreignKeys.add(foreignKey);
    }

    public void addPrimaryKey(String primaryKeyName) {
        primaryKey.add(primaryKeyName);
    }

    public void addUnique(String uniqueName) {
        uniqueKeys.add(uniqueName);
    }

    public void addAttribute(Attribute attribute) {
        structure.add(attribute);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getStructure() {
        return structure;
    }

    public void setStructure(List<Attribute> structure) {
        this.structure = structure;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<ForeignKey> ForeignKeys) {
        this.foreignKeys = ForeignKeys;
    }

    public List<String> getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(List<String> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public List<IndexFile> getIndexFiles() {
        return indexFiles;
    }

    public void setIndexFiles(List<IndexFile> indexFiles) {
        this.indexFiles = indexFiles;
    }
}
