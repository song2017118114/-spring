package easySpring.up.ioc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class SimpleIOC {
    private Map<String,Object> beanMap = new HashMap<>();

    public SimpleIOC(String location) throws Exception{
        loadBeans(location);
    }

    private void loadBeans(String location) throws Exception{
        //加载XML配置文件
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        // 遍历 <bean> 标签
        for (int i = 0;i < nodes.getLength();i++){
            Node node = nodes.item(i);
            if (node instanceof  Element){
                Element element = (Element)node;
                String id = element.getAttribute("id");
                String className = element.getAttribute("class");

                //加载beanClass
                Class beanClass = null;
                try {
                    beanClass = Class.forName(className);//通过反射加载Class
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                    return;
                }

                //创建bean
                Object bean = beanClass.newInstance();

                // 遍历 <property> 标签
                NodeList propertyNodes = element.getElementsByTagName("property");
                for (int j = 0;j < propertyNodes.getLength();j++){
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode instanceof Element){
                        Element propertyElement = (Element) propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        // 利用反射将 bean 相关字段访问权限设为可访问
                        // getDeclaredField获取所有声明的成员变量，不管是public还是private
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);//针对私有变量而言

                        if (value != null && value.length() > 0){
                            //将属性填充到相关字段中
                            declaredField.set(bean,value);
                        }else{
                            String ref = propertyElement.getAttribute("ref");
                            if (ref == null || ref.length() == 0){
                                throw new IllegalArgumentException("ref config error");
                            }

                            //将引用填充到相应字段中
                            declaredField.set(bean,getBean(ref));
                        }

                        // 将 bean 注册到 bean 容器中
                        registerBean(id, bean);

                    }
                }
            }
        }
    }

    /**
     * 获取bean
     * @param
     * @return
     */
    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (bean == null){
            throw new IllegalArgumentException("there is no bean with name "+ name);
        }
        return bean;
    }

    /**
     * 注册到bean容器中
     * @param id
     * @param bean
     */
    private void registerBean(String id, Object bean) {

        beanMap.put(id,bean);
    }

}
