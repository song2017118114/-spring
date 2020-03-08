package easySpring.down.ioc;

import java.util.ArrayList;
import java.util.List;

public class PropertyValues {

    private final List<PropertyValue> propertyValueList = new ArrayList<>();

    public void addPropertyValue(PropertyValue pv){
        //在这里可以对参数值pv做一些处理
        this.propertyValueList.add(pv);
    }

    public List<PropertyValue> getPropertyValueList(){
        return this.propertyValueList;
    }
}
