package dev.benefitbridge;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static dev.benefitbridge.Models.*;

/** Local single-member demo. No production authentication or verified attributes. */
@RestController
@RequestMapping("/api")
public class BenefitsController {
 private static final String MEMBER = "demo-member";
 private final JdbcTemplate db;
 private final ObjectMapper mapper;
 private final Catalog catalog;
 private final EligibilityEngine engine;
 public BenefitsController(JdbcTemplate db, ObjectMapper mapper, Catalog catalog, EligibilityEngine engine) {
  this.db=db; this.mapper=mapper; this.catalog=catalog; this.engine=engine;
 }
 private Map<String,JsonNode> attributes(boolean lock) {
  String json = db.queryForObject("SELECT attributes::text FROM member_profile WHERE id=?" + (lock ? " FOR UPDATE" : ""), String.class, MEMBER);
  try { return mapper.readValue(json, new TypeReference<Map<String,JsonNode>>() {}); }
  catch (Exception e) { throw new IllegalStateException("Stored profile is invalid", e); }
 }
 private String encode(Object value) {
  try { return mapper.writeValueAsString(value); }
  catch (Exception e) { throw new IllegalArgumentException("Cannot serialize profile", e); }
 }
 @GetMapping("/profile") public Profile profile() { return new Profile(attributes(false)); }
 @PutMapping("/profile") public Profile save(@RequestBody Profile profile) {
  if (profile.attributes() == null || profile.attributes().size() > 30) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide up to 30 attributes");
  for (var entry : profile.attributes().entrySet()) {
   if (!entry.getKey().matches("[a-zA-Z][a-zA-Z0-9_]{0,49}") || entry.getValue() == null || entry.getValue().isContainerNode())
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attributes must be scalar values or null");
  }
  JsonNode age = profile.attributes().get("age");
  if (age != null && !age.isNull() && (!age.isIntegralNumber() || !age.canConvertToInt() || age.asInt() < 0 || age.asInt() > 130))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Age must be an integer from 0 to 130");
  for (String key : List.of("student", "disability")) {
   JsonNode v = profile.attributes().get(key);
   if (v != null && !v.isNull() && !v.isBoolean()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " must be boolean");
  }
  JsonNode residency = profile.attributes().get("residency");
  if (residency != null && !residency.isNull() && (!residency.isTextual() || !residency.asText().matches("[A-Z]{2}")))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Residency must be a two-letter state code");
  db.update("UPDATE member_profile SET attributes=?::jsonb, updated_at=now() WHERE id=?", encode(profile.attributes()), MEMBER);
  return profile;
 }
 @GetMapping("/programs") public List<Decision> discover() { return engine.rank(catalog.all(), attributes(false)); }
 @GetMapping("/gaps") public List<String> gaps() {
  var attrs=attributes(false);
  return catalog.all().stream().flatMap(p -> p.conditions().stream()).map(Condition::attribute).distinct()
   .filter(key -> !attrs.containsKey(key) || attrs.get(key).isNull()).sorted().toList();
 }
 @GetMapping("/enrollments") public List<Map<String,Object>> enrollments() {
  return db.queryForList("SELECT id, program_id, rule_version, created_at FROM enrollment WHERE member_id=? ORDER BY created_at DESC", MEMBER);
 }
 @PostMapping("/programs/{id}/enroll") @Transactional
 public Map<String,Object> enroll(@PathVariable String id) {
  Program program=catalog.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Program not found"));
  var attrs=attributes(true); // Lock the profile until the policy decision and insert commit.
  var decision=engine.evaluate(program,attrs);
  if (decision.status() != Status.ELIGIBLE) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Enrollment requires all conditions to pass");
  db.update("INSERT INTO enrollment(id,member_id,program_id,rule_version,attribute_snapshot) VALUES (?,?,?, ?,?::jsonb) ON CONFLICT(member_id,program_id) DO NOTHING",
   UUID.randomUUID(),MEMBER,id,program.version(),encode(attrs));
  return Map.of("programId",id,"status","ENROLLED","ruleVersion",program.version());
 }
}
