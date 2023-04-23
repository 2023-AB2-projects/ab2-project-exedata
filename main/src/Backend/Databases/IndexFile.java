package Backend.Databases;

import java.util.List;

public class IndexFile {
    private String indexName;
    private List<String> indexAttributes;
    private String isUnique;

    public IndexFile(String indexName, List<String> indexAttributes, String isUnique) {
        this.indexName = indexName;
        this.indexAttributes = indexAttributes;
        this.isUnique = isUnique;
    }

    public IndexFile() {
    }

    public boolean equalsIndexAttributes(String[] attributeNames){
        for(String i : attributeNames){
            if(!indexAttributes.contains(i))
                return false;
        }
        return true;
    }

    public String getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(String isUnique) {
        this.isUnique = isUnique;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public List<String> getIndexAttributes() {
        return indexAttributes;
    }

    public void setIndexAttributes(List<String> indexAttributes) {
        this.indexAttributes = indexAttributes;
    }
}
