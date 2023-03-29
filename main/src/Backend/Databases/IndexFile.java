package Backend.Databases;

import java.util.List;

public class IndexFile {
    private String indexName;
    private String fileName;
    private List<String> indexAttributes;

    public IndexFile(String indexName, String fileName, List<String> indexAttributes) {
        this.indexName = indexName;
        this.fileName=fileName;
        this.indexAttributes = indexAttributes;
    }

    public IndexFile() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
