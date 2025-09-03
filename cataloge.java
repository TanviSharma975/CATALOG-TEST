
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class cataloge {
    
    static class Point {
        int x;
        BigInteger y;
        
        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Convert number from any base (2-36) to decimal
    static BigInteger convertToDecimal(String value, int base) {
        return new BigInteger(value, base);
    }
    
    // Simple JSON parser for this specific format
    static Map<String, String> parseJsonValue(String json, String key) {
        Map<String, String> result = new HashMap<>();
        
        // Find the key section
        Pattern keyPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher keyMatcher = keyPattern.matcher(json);
        
        if (keyMatcher.find()) {
            String content = keyMatcher.group(1);
            
            // Extract base
            Pattern basePattern = Pattern.compile("\"base\"\\s*:\\s*\"([^\"]+)\"");
            Matcher baseMatcher = basePattern.matcher(content);
            if (baseMatcher.find()) {
                result.put("base", baseMatcher.group(1));
            }
            
            // Extract value
            Pattern valuePattern = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
            Matcher valueMatcher = valuePattern.matcher(content);
            if (valueMatcher.find()) {
                result.put("value", valueMatcher.group(1));
            }
        }
        
        return result;
    }
    
    // Extract k value from JSON
    static int parseK(String json) {
        Pattern kPattern = Pattern.compile("\"k\"\\s*:\\s*(\\d+)");
        Matcher kMatcher = kPattern.matcher(json);
        if (kMatcher.find()) {
            return Integer.parseInt(kMatcher.group(1));
        }
        return 0;
    }
    
    // Find secret using Lagrange interpolation at x=0
    static BigInteger findSecret(List<Point> points, int k) {
        // Use first k points
        List<Point> selected = points.subList(0, k);
        
        BigInteger secret = BigInteger.ZERO;
        
        for (int i = 0; i < k; i++) {
            Point pi = selected.get(i);
            
            // Calculate Li(0) = product of (0-xj)/(xi-xj) for all j!=i
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    Point pj = selected.get(j);
                    numerator = numerator.multiply(BigInteger.valueOf(-pj.x));
                    denominator = denominator.multiply(BigInteger.valueOf(pi.x - pj.x));
                }
            }
            
            // Add yi * Li(0) to secret
            BigInteger term = pi.y.multiply(numerator).divide(denominator);
            secret = secret.add(term);
        }
        
        return secret;
    }
    
    static BigInteger solve(String jsonInput) {
        int k = parseK(jsonInput);
        List<Point> points = new ArrayList<>();
        
        // Extract points manually - look for numeric keys
        for (int x = 1; x <= 20; x++) { // Check up to 20 possible x values
            Map<String, String> pointData = parseJsonValue(jsonInput, String.valueOf(x));
            if (!pointData.isEmpty() && pointData.containsKey("base") && pointData.containsKey("value")) {
                int base = Integer.parseInt(pointData.get("base"));
                String value = pointData.get("value");
                BigInteger y = convertToDecimal(value, base);
                points.add(new Point(x, y));
            }
        }
        
        // Sort points by x coordinate
        points.sort(Comparator.comparingInt(p -> p.x));
        
        return findSecret(points, k);
    }
    
    public static void main(String[] args) {
        // Test Case 1
        String test1 = """
        {
            "keys": {"n": 4, "k": 3},
            "1": {"base": "10", "value": "4"},
            "2": {"base": "2", "value": "111"},
            "3": {"base": "10", "value": "12"},
            "6": {"base": "4", "value": "213"}
        }
        """;
        
        BigInteger secret1 = solve(test1);
        System.out.println("Test Case 1 Secret: " + secret1);
        
        // Test Case 2
        String test2 = """
        {
            "keys": {"n": 10, "k": 7},
            "1": {"base": "6", "value": "13444211440455345511"},
            "2": {"base": "15", "value": "aed7015a346d635"},
            "3": {"base": "15", "value": "6aeeb69631c227c"},
            "4": {"base": "16", "value": "e1b5e05623d881f"},
            "5": {"base": "8", "value": "316034514573652620673"},
            "6": {"base": "3", "value": "2122212201122002221120200210011020220200"},
            "7": {"base": "3", "value": "20120221122211000100210021102001201112121"},
            "8": {"base": "6", "value": "20220554335330240002224253"},
            "9": {"base": "12", "value": "45153788322a1255483"},
            "10": {"base": "7", "value": "1101613130313526312514143"}
        }
        """;
        
        BigInteger secret2 = solve(test2);
        System.out.println("Test Case 2 Secret: " + secret2);
    }
}