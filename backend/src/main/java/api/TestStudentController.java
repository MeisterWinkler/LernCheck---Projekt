package api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestStudentController {


    public void TestStudentController(){

    }
    @RequestMapping(path = "/student")
    public String index(){
        return "student-join";
    }
}
