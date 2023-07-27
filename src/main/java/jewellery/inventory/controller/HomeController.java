package jewellery.inventory.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "${cors.origins}")
public class HomeController {

  @GetMapping("/home")
  public String home() {
    return "Hello world";
  }
}
