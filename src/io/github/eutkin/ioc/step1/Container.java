package io.github.eutkin.ioc.step1;

import java.util.List;
import java.util.Map;

public interface Container {

    Map<String, Object> getInstances(List<BeanDefinition> definitions);
}
