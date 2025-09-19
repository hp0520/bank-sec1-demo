package com.example.bank;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

@RestController
public class BankController {

  @Autowired
  JdbcTemplate jdbc;

  @PostConstruct
  public void init() {
    jdbc.execute("CREATE TABLE IF NOT EXISTS users(id INT PRIMARY KEY, username VARCHAR(64), balance DECIMAL(10,2));");
    jdbc.update("MERGE INTO users KEY(id) VALUES(1,'alice',1000.00)");
    jdbc.update("MERGE INTO users KEY(id) VALUES(2,'bob',500.00)");
  }

  // clean version (safe echo) — you can make it risky later
  @GetMapping("/echo")
  public String echo(@RequestParam String msg) {
    // return untrusted input encoded (simple demo)
    return "You said: " + msg.replace("<","&lt;").replace(">","&gt;");
  }

  // clean version (parameterized)
  @GetMapping("/user")
  public List<Map<String,Object>> getUser(@RequestParam int id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    return jdbc.queryForList(sql, id);
  }

  @GetMapping("/transfer")
  public String transfer(@RequestParam int from, @RequestParam int to, @RequestParam double amount,
                         @RequestParam(defaultValue="${API_KEY:not_used}") String apiKey) {
    // dummy check to keep the endpoint simple; replace with proper auth later
    if (apiKey == null || apiKey.equals("not_used")) {
      return "Unauthorized";
    }
    jdbc.update("UPDATE users SET balance = balance - ? WHERE id = ?", amount, from);
    jdbc.update("UPDATE users SET balance = balance + ? WHERE id = ?", amount, to);
    return "OK";
  }

  @GetMapping("/users")
  public List<Map<String,Object>> users() {
    return jdbc.queryForList("SELECT * FROM users");
  }
}
