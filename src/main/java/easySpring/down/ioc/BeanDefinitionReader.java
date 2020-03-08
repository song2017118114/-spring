package easySpring.down.ioc;

import java.io.FileNotFoundException;

/**
 * 加载和解析配置文件的接口
 */
public interface BeanDefinitionReader {

    void loadBeanDefinitions(String location) throws FileNotFoundException,Exception;
}
