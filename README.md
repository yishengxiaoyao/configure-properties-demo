#Spring Boot 自定义相关属性
在我们编写Spring Boot项目的时候，需要对一些值进行配置。但是有的时候我们不知道默认的属性有哪些(出了常用的)，我们可以根据自己的需要进行自定义属性,
也可以根据不同的环境指定不同的配置文件。
## 自定义属性
需要将自定义的属性写入到src/main/resources/application.yml文件中，内容如下:
```
## Top level app properties
app:
  name: ConfigurationPropertiesDemoApp
  description: ${app.name} is a spring boot app that demonstrates how to use external configuration properties
  upload-dir: /uploads
  connect-timeout: 500ms
  read-timeout: 10s
  security:
    username: admin
    password: 123456
    roles: USER,ADMIN,PARTNER   # List Property
    enabled: true
    ## Map Properties (permissions)
    permissions:
      CAN_VIEW_POSTS: true
      CAN_EDIT_POSTS: true
      CAN_DELETE_POSTS: false
      CAN_VIEW_USERS: true
      CAN_EDIT_USERS: true
      CAN_DELETE_USERS: false
```
## 将属性与类进行绑定
如果想要将外部的属性与类类进行绑定，需要在类里面添加@ConfigurationProperties注解,并且指定前缀。绑定类的代码如下:
```
package com.edu.springboot.configurepropertiesdemo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String name;
    private String description;
    private String uploadDir;
    private Duration connectTimeout = Duration.ofMillis(1000);
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(30);
    private final Security security = new Security();
    //省略get/set方法
    public static class Security {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();
        private boolean enabled;
        private Map<String, String> permissions = new HashMap<>();
        //省略get/set方法
    }
}
```
### 数据绑定
>* Type-safe binding:如果想要将属性绑定为list(roles)，需要将类型使用逗号分割,permissions使用map来绑定。
>* Duration Support:默认的单位为毫秒。
>* Naming Convention:在命名的时候使用驼峰法，在转换的时候，可以使用驼峰或者kebab(使用-来连接)。

## 打开配置属性
在打开配置属性时，需要将添加@EnableConfigurationProperties注解，代码如下:
```
package com.edu.springboot.configurepropertiesdemo;
import com.edu.springboot.configurepropertiesdemo.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ConfigurePropertiesDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigurePropertiesDemoApplication.class, args);
    }
}
```
## 注入配置
```
package com.edu.springboot.configurepropertiesdemo.controller;
import com.edu.springboot.configurepropertiesdemo.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
@RestController
public class IndexController {
    @Autowired
    private AppProperties appProperties;
    @GetMapping("/")
    public Map<String, String> getAppDetails() {
        Map<String, String> appDetails = new HashMap<>();
        appDetails.put("name", appProperties.getName());
        appDetails.put("description", appProperties.getDescription());
        return appDetails;
    }
}
```
## 属性验证
可以使用注解方式，来防止某些配置为空，验证某些属性是否符合标准。
```
package com.edu.springboot.configurepropertiesdemo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    @NotNull
    private String name;
    private String description;
    private String uploadDir;
    private Duration connectTimeout = Duration.ofMillis(1000);
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout = Duration.ofSeconds(30);
    @Valid
    private final Security security = new Security();
    //get/set方法省略
    public static class Security {
        private String username;
        private String password;
        @NotEmpty
        private List<String> roles = new ArrayList<>();
        private boolean enabled;
        private Map<String, String> permissions = new HashMap<>();
        //get/set方法省略
    }
}
```
## 指定配置文件
在默认情况下，读取application.yml/application.properties文件,Spring Boot项目中也允许根据环境创建相应的配置文件。
```
application-{profile}.properties
```
指定的配置文件也要放到src/main/resources文件下。

application-dev.yml
```
app:
  name: ConfigurationPropertiesDemoApp-DEVELOPMENT
  security:
    username: dev
```
application-stage.yml
```
app:
  name: ConfigurationPropertiesDemoApp-STAGING
  security:
    username: stage
    password: stage
```
application-prod.yml
```
app:
  name: ConfigurationPropertiesDemoApp-PRODUCTION
  security:
    username: prod
    password: prod
```

## 激活Profile 
### 1.application profiles
```
spring.profiles.active=staging
```
### 2.命令行参数
```
mvn clean package -Dspring.profiles.active=staging
# Running the packaged jar with `spring.profiles.active` argument
java -jar -Dspring.profiles.active=staging target/config-properties-demo-0.0.1-SNAPSHOT.jar
#或者使用
mvn spring-boot:run -Dspring.profiles.active=dev
```
### 3.环境变量
```
export SPRING_PROFILES_ACTIVE=prod
```
## 运行应用程序
```
#运行
mvn spring-boot:run
#获取数据
curl http://localhost:8080
{"name":"ConfigurationPropertiesDemoApp","description":"ConfigurationPropertiesDemoApp is a spring boot app that demonstrates how to use external configuration
```
### 运行特定环境
```
#指定运行环境
mvn spring-boot:run -Dspring.profiles.active=prod
#运行结果
curl http://localhost:8080
{"name":"ConfigurationPropertiesDemoApp-PRODUCTION","description":"ConfigurationPropertiesDemoApp-PRODUCTION is a spring boot app that demonstrates how to use external configuration properties"}
```
也可以在pom.xml文件中添加要激活的环境。

代码放在[github](https://github.com/yishengxiaoyao/configure-properties-demo)


##参考文献

[Spring Boot @ConfigurationProperties: Binding external configurations to POJO classes](https://www.callicoder.com/spring-boot-configuration-properties-example/)

[Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)

[常用属性配置列表](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
