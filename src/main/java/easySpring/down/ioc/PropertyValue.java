package easySpring.down.ioc;

public class PropertyValue {

    //这两个字段 用于记录 bean 配置中的标签的属性值
    private final String name;//属性名

    private final Object value;//属性值，可以是一般类型，也可以是引用类型

    public PropertyValue(String name,Object value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

}
