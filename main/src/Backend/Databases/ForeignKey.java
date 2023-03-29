package Backend.Databases;

public class ForeignKey {
    private String name;
    private String refTable;
    private String refAttribute;

    public ForeignKey(String name, String refTable, String refAttribute) {
        this.name = name;
        this.refTable = refTable;
        this.refAttribute = refAttribute;
    }

    public ForeignKey() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }

    public String getRefAttribute() {
        return refAttribute;
    }

    public void setRefAttribute(String refAttribute) {
        this.refAttribute = refAttribute;
    }
}