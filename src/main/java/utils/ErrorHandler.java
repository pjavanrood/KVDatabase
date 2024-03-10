package utils;

public class ErrorHandler {
    public static void printException(String className, String message, Exception e) {
        System.err.printf("[%s]: %s\n", className, message);
        System.err.println(e.getMessage());
    }
}
