package easySpring.down.ioc.xml;

import easySpring.down.ioc.BeanDefinition;
import easySpring.down.ioc.BeanDefinitionReader;
import easySpring.down.ioc.BeanReference;
import easySpring.down.ioc.PropertyValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取xml配置文件
 */
public class XmlBeanDefinitionReader implements BeanDefinitionReader {

    //将 <id, BeanDefinition> 键值对缓存在 Map 中
    private Map<String, BeanDefinition> registry;

    public XmlBeanDefinitionReader() {
        registry = new HashMap<>();
    }


    public Map<String, BeanDefinition> getRegistry() {
        return registry;
    }

    @Override
    public void loadBeanDefinitions(String location) throws Exception {
        InputStream inputStream = new FileInputStream(location);
        //解析器工厂
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //解析器
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        //获取xml的全部节点
        Document doc = docBuilder.parse(inputStream);
        //根节点，标签
        Element root = doc.getDocumentElement();
        parseBeanDefinitions(root);
    }

    private void parseBeanDefinitions(Element root) {
        //NodeList提供对节点的有序集合抽象
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                parseBeanDefinition(ele);
            }
        }
    }

    /**
     * 单个节点转换成BeanDefinition对象
     * @param ele
     */
    private void parseBeanDefinition(Element ele) {
        String name = ele.getAttribute("id");
        String className = ele.getAttribute("class");
        //封装id，class到beanDefinition中去
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(className);
        //注入属性
        processProperty(ele, beanDefinition);
        registry.put(name, beanDefinition);
    }

    /**
     * 注入属性
     * @param ele
     * @param beanDefinition
     */
    private void processProperty(Element ele, BeanDefinition beanDefinition) {
        NodeList propertyNodes = ele.getElementsByTagName("property");
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node propertyNode = propertyNodes.item(i);
            if (propertyNode instanceof Element) {
                Element propertyElement = (Element) propertyNode;
                String name = propertyElement.getAttribute("name");
                String value = propertyElement.getAttribute("value");
                if (value != null && value.length() > 0) {
                    //注入属性，name-value
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
                } else {
                    //注入引用，name-beanReference对象
                    String ref = propertyElement.getAttribute("ref");
                    if (ref == null || ref.length() == 0) {
                        throw new IllegalArgumentException("ref config error");
                    }
                    BeanReference beanReference = new BeanReference(ref);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
                }
            }
        }
    }
}
