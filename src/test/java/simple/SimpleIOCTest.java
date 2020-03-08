package simple;

import easySpring.up.ioc.Car;
import easySpring.up.ioc.Wheel;
import org.junit.Test;

import easySpring.up.ioc.SimpleIOC;


public class SimpleIOCTest {
    @Test
    public void getBean() throws Exception {
        String location = SimpleIOC.class.getClassLoader().getResource("ioc.xml").getFile();
        SimpleIOC bf = new SimpleIOC(location);
        Wheel wheel = (Wheel) bf.getBean("wheel");
        System.out.println(wheel.toString());
        Car car = (Car) bf.getBean("car");
        System.out.println(car.toString());
    }
}
