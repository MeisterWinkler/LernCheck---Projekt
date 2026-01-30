package api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Controller
public class TestStudentController {


    public void TestStudentController(){

    }
    @RequestMapping(path = "/student")
    public String index(){
        return "student-join";
    }


    @GetMapping("test/String")
    public ResponseEntity<List<String>> testString(){
       List<String> response = Arrays.asList("Test","Test2");
       return new ResponseEntity<List<String>>(response, HttpStatus.OK);
    }
}
