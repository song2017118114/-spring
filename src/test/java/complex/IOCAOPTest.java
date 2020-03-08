package complex;

import easySpring.down.ioc.xml.XmlBeanFactory;
import easySpring.up.aop.HelloService;
import org.junit.Test;

public class IOCAOPTest {

    @Test
    public void getBean() throws Exception {
        System.out.println("--------- AOP test ----------");
        String location = getClass().getClassLoader().getResource("easy-spring.xml").getFile();
        XmlBeanFactory bf = new XmlBeanFactory(location);
        HelloService helloService = (HelloService) bf.getBean("helloService");
        helloService.sayHelloWorld();
    }
}
