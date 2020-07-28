# Как понять Spring Framework пока убиваешь дракона

> Чтобы убить дракона надо стать драконом.

И так, ты начинающий Java разработчик и приступил к изучению Spring Framework.

Начнем с базовой части Spring, Spring IoC. Попробуем понять проблему, которую решает это фреймворк.

Для начала коснемся вопроса, что такое DI и IoC. Про смысл этих аббревиатур не писал только ленивый, поэтому я очень 
кратко напомню в чем их смысл.

Есть класс, у класса есть поле, в котором этот класс хочет иметь значение для своей работы. 
Вместо того, чтобы самому разбираться какое значение ему в этом поле нужно, он просит третью сторону 
предоставить ему значение для поля. Это третья сторона и есть контейнер Spring'a, он же основа Spring IoC.

Давай напишем свой контейнер. Что он должен делать? Прежде всего создавать объекты. Такие объекты, создаваемые
при помощи нашего контейнера мы будем называть бинами (`bean`, как в Spring, так как не стоит плодить термины)

```java
public interface Container {

    /**
     * Мы передаем список классов и получаем объекты этих классов. 
     * Что может быть проще?
     */
    List<Object> getBeans(List<Class<?>> wantedTypes);
}
```

Сразу возникает ряд вопросов:

- как создать несколько объектов одного класса?
- как создать объект, если у него нет конструктора по умолчанию?


Первый вопрос можно решить, если мы каждому объекту будем присваивать уникальное имя (как первичный ключ в базах 
данных), которое будем передавать вместе с классом. 

```java
public interface Container {
    
    Map<String, Object> getInstances(Map<String, Class<?>> wantedTypes);
}

public class Example {
    
   public static void main(String[] args) {
       Container container = //...;
       final Map<String, Object> instances = container.getBeans(Map.of(
               "inputDatabase", DataSource.class,
               "outputDatabase", DataSource.class
       ));
        DataSource inputDatabase = instances.get("inputDatabase");
   }
}
```

Правда, тогда нам придется придумывать имена для всех остальных классов и это немного утомительно.

Второй вопрос гораздо сложнее.  Объекты класса без конструктора по умолчанию могут создавать очень 
по-разному: через фабрики, через статические конструкторы, через конструкторы с параметрами. Если в варианте
с конструкторами, имеющими параметры, можно передавать другие объекты, которые создаются тут же 
(а вот и DI!), то что делать с фабриками и статическими конструкторами пока непонятно. 

Простым решение
видится заставить пользователя сообщать нам о тех классах, объекты которых создаются нестандартно. 

Значит, передавать на вход структуру "имя объекта – тип объекта" недостаточно, так как пользователю
необходимо указать, к примеру, статический конструктор. Поэтому придумаем новую структуру:

```java
public class BeanDefinition {
    // имя бина (обязательно)
    private String name;
    // класс бина, в случае если мы создаем объект через конструктор
    private Class<?> beanClass;
    // Ссылка на бин фабрики, которая создает этот бин
    private String factoryBeanName;
    // имя фабричного метода. Если поле factoryBeanName не заполнено, 
    // то будем считать именем статического метода класса, указанного в beanClass
    private String factoryMethodName;
}
```

Теперь немного переосмыслим интерфейс контейнера. Так как все созданные контейнером бины надо где-то хранить,
то будем делать это в самом контейнере. Поэтому переделаем контейнер по канонам ООП и получим примерно следующее.

```java

public interface Container {
    
    // Отдаем все созданные бины
    Map<String, Object> getBeans();

    // Контейнер управляет жизненным циклом, поэтому нам надо 
    // иметь возможность запустить и остановить этот самый цикл
    void start();

    void stop();

    static Container from(List<BeanDefinition> beanDefinitions) {
        return new ContainerImpl(beanDefinitions);
    }
    
    class ContainerImpl implements Container {

        private final Map<String, BeanDefinition> beanDefinitions;

        // Бины будут записаны сюда, после запуска контейнера через метод start()
        private final Map<String, Object> beans = new HashMap<>();

        private ContainerImpl(List<BeanDefinition> beanDefinitions) {
            this.beanDefinitions = beanDefinitions.stream().collect(toMap(BeanDefinition::getName, bd -> bd));
        }

        void start() {}
        
        void stop() {}

        @Override
        public Map<String, Object> getBeans() {
            return Collections.unmodifiableMap(this.beans);
        }
    }
}
```