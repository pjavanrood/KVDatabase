package utils;

import org.json.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String METHOD = "method";
    private static final String[] VALID_METHODS = new String[] {"put", "get"};

    public static Map<String, String> parseMessageString(String request) {
        JSONObject jsonObject = new JSONObject(request);
        String method = null;
        String key = null;
        String value = null;
        Map<String, String> result = new HashMap<>();
        try {
            method = jsonObject.get(METHOD).toString();
            result.put(METHOD, method);
            key = jsonObject.get(KEY).toString();
            result.put(KEY, key);
            if ( method.equals("put") ) {
                value = jsonObject.get(VALUE).toString();
                result.put(VALUE, value);
            }
        } catch ( Exception e ) {
            if (method == null) {
                ErrorHandler.printException("RequestParser", "method not found", e);
                result.put("error", "method not found");
            }
            else if (key == null) {
                ErrorHandler.printException("[RequestParser]", "key not found", e);
                result.put("error", "key not found");
            }
            else if ( !Arrays.asList(VALID_METHODS).contains(method) ) {
                ErrorHandler.printException("[RequestParser]", "invalid method", e);
                result.put("error", "invalid method");
            }
            else {
                ErrorHandler.printException("[RequestParser]", "value not found", e);
                result.put("error", "value not found");
            }
        }
        return result;
    }

    public static String parseMessageMap(Map<String, String> responseMap) {
        JSONObject responseJSON = new JSONObject(responseMap);
        return responseJSON.toString();
    }
}
