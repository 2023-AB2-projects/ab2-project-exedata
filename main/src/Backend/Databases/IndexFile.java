package Backend.Databases;

import java.util.List;

public class IndexFile {
    private String indexName;
    private List<String> indexAttributes;

    public IndexFile(String indexName, List<String> indexAttributes) {
        this.indexName = indexName;
        this.indexAttributes = indexAttributes;
    }

    public IndexFile() {
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
