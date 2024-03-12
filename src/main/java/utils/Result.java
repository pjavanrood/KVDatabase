package utils;

public class Result<T> {
    private final boolean success;
    private final T result;
    private final String errorMessage;

    private Result(boolean success, T result, String errorMessage) {
        this.success = success;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public static <T> Result<T> success(T result) {
        return new Result<>(true, result, null);
    }

    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public T get() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
