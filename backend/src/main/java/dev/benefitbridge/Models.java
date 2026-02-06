package dev.benefitbridge;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
public final class Models {
 private Models() {}
 public enum Status { ELIGIBLE, UNKNOWN, INELIGIBLE }
 public enum Operator { EQ, GTE, IN }
 public record Condition(String attribute, Operator operator, JsonNode value, String explanation) {}
 public record Program(String id, String title, String description, int version, int priority, List<Condition> conditions) {}
 public record Check(String attribute, Status status, String explanation) {}
 public record Decision(String id, String title, String description, int version, int priority, Status status, List<Check> checks) {}
 public record Profile(Map<String, JsonNode> attributes) {}
}
