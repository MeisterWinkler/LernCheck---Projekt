package api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexControllerr {


    public void IndexController(){

    }
    @RequestMapping(path = "/")
    public String index(){
        return "index";
    }
}
