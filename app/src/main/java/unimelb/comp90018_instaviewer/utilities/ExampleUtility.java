package unimelb.comp90018_instaviewer.utilities;

public class ExampleUtility {
    private static final ExampleUtility ourInstance = new ExampleUtility();

    public static ExampleUtility getInstance() {
        return ourInstance;
    }

    private ExampleUtility() {
    }
}
