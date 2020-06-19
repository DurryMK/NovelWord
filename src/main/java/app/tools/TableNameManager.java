package app.tools;

public class TableNameManager {
    public static String name;

    private TableNameManager() {
    }

    public static String getTableName() {
        return name;
    }

    public static void upDate(String newName) {
        name = newName;
    }
}
