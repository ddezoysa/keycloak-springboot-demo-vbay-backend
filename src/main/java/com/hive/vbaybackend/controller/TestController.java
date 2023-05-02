package com.hive.vbaybackend.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hive.vbaybackend.config.UserData;

@RestController
@RequestMapping("/test")
public class TestController {

  @Autowired
  UserData userData;

  @RequestMapping(value = "/anonymous", method = RequestMethod.GET)
  public ResponseEntity<String> getAnonymous() {
    return ResponseEntity.ok("Hello Anonymous");
  }

  @RequestMapping(value = "/myProducts", method = RequestMethod.GET)
  @RolesAllowed("seller")
  public ResponseEntity<String[]> getMyProducts(@RequestHeader String Authorization) {
    String[] products = { "TV", "Laptop", "Keyboard", "Mouse" };
    return ResponseEntity.ok(products);
  }

  @RequestMapping(value = "/user", method = RequestMethod.GET)
  @RolesAllowed({ "admin", "buyer", "seller" })
  public ResponseEntity<String> getUser(@RequestHeader String Authorization) {
    System.out.println(userData.toString());
    return ResponseEntity.ok("Hello " + userData.getName());
  }
}
