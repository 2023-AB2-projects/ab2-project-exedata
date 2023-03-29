package Backend.Databases;

public class Attribute {
    private String name;
    private String type;
    private String isnull;

    public Attribute(String name, String type, String isnull) {
        this.name = name;
        this.type = type;
        this.isnull = isnull;
    }

    public Attribute() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsnull() {
        return isnull;
    }

    public void setIsnull(String isnull) {
        this.isnull = isnull;
    }
}
