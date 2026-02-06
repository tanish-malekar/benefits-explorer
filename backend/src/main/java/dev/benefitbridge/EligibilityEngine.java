package dev.benefitbridge;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import static dev.benefitbridge.Models.*;
@Component
public class EligibilityEngine {
 public Decision evaluate(Program program, Map<String, JsonNode> attributes) {
  List<Check> checks = program.conditions().stream().map(c -> check(c, attributes.get(c.attribute()))).toList();
  Status status = checks.stream().anyMatch(c -> c.status() == Status.INELIGIBLE) ? Status.INELIGIBLE
   : checks.stream().anyMatch(c -> c.status() == Status.UNKNOWN) ? Status.UNKNOWN : Status.ELIGIBLE;
  return new Decision(program.id(), program.title(), program.description(), program.version(), program.priority(), status, checks);
 }
 private Check check(Condition rule, JsonNode actual) {
  if (actual == null || actual.isNull()) return new Check(rule.attribute(), Status.UNKNOWN, "Missing attribute: " + rule.attribute());
  boolean matches = switch (rule.operator()) {
   case EQ -> rule.value().equals(actual);
   case GTE -> actual.isNumber() && actual.decimalValue().compareTo(rule.value().decimalValue()) >= 0;
   case IN -> { boolean found = false; for (JsonNode allowed : rule.value()) if (allowed.equals(actual)) found = true; yield found; }
  };
  return new Check(rule.attribute(), matches ? Status.ELIGIBLE : Status.INELIGIBLE, rule.explanation());
 }
 public List<Decision> rank(List<Program> programs, Map<String, JsonNode> attributes) {
  return programs.stream().map(p -> evaluate(p, attributes))
   .sorted(Comparator.comparing(Decision::status).thenComparing(Comparator.comparingInt(Decision::priority).reversed()).thenComparing(Decision::title)).toList();
 }
}
