package dev.benefitbridge;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import static dev.benefitbridge.Models.*;
@Component
public class Catalog {
 private final List<Program> programs;
 public Catalog(ObjectMapper mapper) throws IOException {
  try (var stream = new ClassPathResource("programs.json").getInputStream()) {
   programs = List.copyOf(mapper.readValue(stream, new TypeReference<List<Program>>() {}));
  }
  Set<String> ids = new HashSet<>();
  for (var p : programs) {
   if (p.id() == null || p.id().isBlank() || !ids.add(p.id()) || p.version() < 1 || p.conditions() == null || p.conditions().isEmpty())
    throw new IllegalArgumentException("Invalid program definition");
   for (var c : p.conditions()) {
    if (c.attribute() == null || c.attribute().isBlank() || c.operator() == null || c.value() == null || c.value().isNull() || c.explanation() == null)
     throw new IllegalArgumentException("Invalid condition in " + p.id());
    if (c.operator() == Operator.GTE && !c.value().isNumber()) throw new IllegalArgumentException("GTE requires a number");
    if (c.operator() == Operator.IN && (!c.value().isArray() || c.value().isEmpty())) throw new IllegalArgumentException("IN requires nonempty values");
   }
  }
 }
 public List<Program> all() { return programs; }
 public Optional<Program> find(String id) { return programs.stream().filter(p -> p.id().equals(id)).findFirst(); }
}
