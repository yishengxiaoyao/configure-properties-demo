# Spring Boot 配置源数据
Spring Boot jar包包含配置源数据, 该配置源数据提供了所有配置的属性的详细信息。在我们编写application.yml或者application.properties文件时，会起到帮助作用。

如果在相应的类里面添加@ConfigurationProperties注解，在编译时会生成配置源数据文件。

本文参考的是Spring Boot 2.1.3.RELEASE的官方文档。
## 源数据格式
配置源数据是存放在/META-INF/spring-configuration-metadata.json。请看下面的json数据:
```json
{"groups": [
	{
		"name": "server",
		"type": "org.springframework.boot.autoconfigure.web.ServerProperties",
		"sourceType": "org.springframework.boot.autoconfigure.web.ServerProperties"
	},
	{
		"name": "spring.jpa.hibernate",
		"type": "org.springframework.boot.autoconfigure.orm.jpa.JpaProperties$Hibernate",
		"sourceType": "org.springframework.boot.autoconfigure.orm.jpa.JpaProperties",
		"sourceMethod": "getHibernate()"
	}
],"properties": [
	{
		"name": "server.port",
		"type": "java.lang.Integer",
		"sourceType": "org.springframework.boot.autoconfigure.web.ServerProperties"
	},
	{
		"name": "server.address",
		"type": "java.net.InetAddress",
		"sourceType": "org.springframework.boot.autoconfigure.web.ServerProperties"
	},
	{
		  "name": "spring.jpa.hibernate.ddl-auto",
		  "type": "java.lang.String",
		  "description": "DDL mode. This is actually a shortcut for the \"hibernate.hbm2ddl.auto\" property.",
		  "sourceType": "org.springframework.boot.autoconfigure.orm.jpa.JpaProperties$Hibernate"
	}
],"hints": [
	{
		"name": "spring.jpa.hibernate.ddl-auto",
		"values": [
			{
				"value": "none",
				"description": "Disable DDL handling."
			},
			{
				"value": "validate",
				"description": "Validate the schema, make no changes to the database."
			},
			{
				"value": "update",
				"description": "Update the schema if necessary."
			},
			{
				"value": "create",
				"description": "Create the schema and destroy previous data."
			},
			{
				"value": "create-drop",
				"description": "Create and then destroy the schema at the end of the session."
			}
		]
	}
]}
```
1.properties

在每一个配置项(properties标签里面的元素)里面都会有一个具体的值，例如:
```
server.port=9090
server.address=127.0.0.1
```
2.group

分组(group标签里面的元素)项没有具体的值，表示多个属性归为一个组以及相应的配置类。

3.hints

hints这个标签是为了帮助用户进行编写配置文件。相当于这个属性的值为枚举，一一列出来。

### 分组属性
|Name|Type|Purpose|
|----|----|---|
|name|String|分组名称，必须要有|
|type|String|分组数据类型的类名|
|description|String|对这个分组进行简单介绍|
|sourceType|String|从那个类中可以获取这些属性(org.springframework.boot.autoconfigure.web.ServerProperties)|
|sourceMethod|String|从那个方法中可以获取这些属性(getHibernate())|
### 分组属性
|Name|Type|Purpose|
|----|----|----|
|name|String|属性名称，必须要有|
|type|String|属性的类型|
|description|String|对属性的描述|
|sourceType|String|指定属性的来源，或者说是可以在那个类中可以找到这些属性|
|defaultValue|Object|属性的默认值|
|deprecation|Deprecation|表明这个类属性是否已经废弃|

deprecation中包含一下属性:

|Name|Type|Purpose|
|----|----|----|
|level|String|废弃的等级，如果是warning，表示在当前版本还可以使用，最好不要用；如果是error，就是在当前版本不可以使用这个属性|
|reason|String|解释一下为什么废弃这个属性|
|replacement|String|使用那个属性来替代这个已经过时的属性的全程|

如果想要废弃某个属性，可以使用代码实现:
```
@ConfigurationProperties("app.acme")
public class AcmeProperties {

	private String name;

	public String getName() { ... }

	public void setName(String name) { ... }

	@DeprecatedConfigurationProperty(replacement = "app.acme.name")
	@Deprecated
	public String getTarget() {
		return getName();
	}

	@Deprecated
	public void setTarget(String target) {
		setName(target);
	}
}
```
上面代码的意思是将app.acme.target属性变成app.acme.name。

### 提示属性
|Name|Type|Purpose|
|----|----|----|
|name|String|要对那个属性进行提示|
|values|ValueHint[]|ValueHint对象列表|
|providers|ValueProvider[]|ValueProvider对象列表|

values标签值介绍:

|Name|Type|Purpose|
|----|----|----|
|value|Object|提示一些有效值|
|description|String|对属性值的描述|

providers标签值介绍:

|Name|Type|Purpose|
|----|----|----|
|name|String|用于为提示引用的元素提供其他内容帮助的提供程序的名称|
|parameters|json object|提供程序支持的任何其他参数|

## 提供手动提示

为了提高用户的体验，并且帮助用户方便使用属性配置，需要添加附加源数据:1.潜在属性列表描述;2.关联提供者，将明确定义附加到属性。

### 值提示

如果属性的类型是Map,可以对key和value进行提示。例如下面:
```
@ConfigurationProperties("sample")
public class SampleProperties {

	private Map<String,Integer> contexts;
	// getters and setters
}
```
```json
{"hints": [
	{
		"name": "sample.contexts.keys",
		"values": [
			{
				"value": "sample1"
			},
			{
				"value": "sample2"
			}
		]
	}
]}
```
### 值提供者
支持的provider列表如下:

|Name|Description|
|----|----|
|any|允许提供的所有值|
|class-reference|使用target想要应用到那个类|
|handle-as|处理属性|
|logger-name|当项目中可用的包名和类名可以自动生成|
|spring-bean-reference|指定要导入的spring bean|
|spring-profile-name|指定要运行的环境|
#### Any
如果provider设置的话，可以获取指定值的任何一个值。
```json
{"hints": [
	{
		"name": "system.state",
		"values": [
			{
				"value": "on"
			},
			{
				"value": "off"
			}
		],
		"providers": [
			{
				"name": "any"
			}
		]
	}
]}
```
#### Class Reference
class-reference提供下面的参数:

|Parameter|Type|Default value|Description|
|----|----|----|-----|
|target|String|none|用来选择值的类的全名|
|concrete|boolean|true|指定是否仅将具体类视为有效候选者|
下面的代码就是指定server.servlet.jsp.class-name的具体类:
```json
{"hints": [
	{
		"name": "server.servlet.jsp.class-name",
		"providers": [
			{
				"name": "class-reference",
				"parameters": {
					"target": "javax.servlet.http.HttpServlet"
				}
			}
		]
	}
]}
```
#### Handle As
handle-as提供者可以允许你将属性的类型替换为更高级别的类型。

提供的转换类型如下:
* 任意java.lang.Enum:列出属性可能的值。
* java.nio.charset.Charset:指定编码格式
* java.util.Locale:指定时区
* org.springframework.util.MimeType:支持的content-type
* org.springframework.core.io.Resource:支持Spring资源的自动完成

指定liquibase的修改日志存放位置，使用org.springframework.core.io.Resource将值传递过去。
```json
{"hints": [
	{
		"name": "spring.liquibase.change-log",
		"providers": [
			{
				"name": "handle-as",
				"parameters": {
					"target": "org.springframework.core.io.Resource"
				}
			}
		]
	}
]}
```
#### Logger Name
logger-name提供者自动补全loggername和[loggergroup](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html#boot-features-custom-log-groups)。
下面的代码端就是介绍logger.level属性。Keys是logger名称，values可以自定义:
```json
{"hints": [
	{
		"name": "logging.level.keys",
		"values": [
			{
				"value": "root",
				"description": "Root logger used to assign the default logging level."
			},
			{
				"value": "sql",
				"description": "SQL logging group including Hibernate SQL logger."
			},
			{
				"value": "web",
				"description": "Web logging group including codecs."
			}
		],
		"providers": [
			{
				"name": "logger-name"
			}
		]
	},
	{
		"name": "logging.level.values",
		"values": [
			{
				"value": "trace"
			},
			{
				"value": "debug"
			},
			{
				"value": "info"
			},
			{
				"value": "warn"
			},
			{
				"value": "error"
			},
			{
				"value": "fatal"
			},
			{
				"value": "off"
			}
		],
		"providers": [
			{
				"name": "any"
			}
		]
	}
]}
```
#### Spring Bean Reference
spring-bean-reference提供程序自动完成在当前项目中的配置中定义的bean。

下面的代码段表示spring.jmx.server属性使用MBeanServer bean的名称:
```json
{"hints": [
	{
		"name": "spring.jmx.server",
		"providers": [
			{
				"name": "spring-bean-reference",
				"parameters": {
					"target": "javax.management.MBeanServer"
				}
			}
		]
	}
]}
```
*在使用这个provider时候，需要使用ApplicationContext来获取相应的类*

#### Spring Profile Name
激活在当前项目中定义的profile,代码段如下:
```json
{"hints": [
	{
		"name": "spring.profiles.active",
		"providers": [
			{
				"name": "spring-profile-name"
			}
		]
	}
]}
```
## 使用注解处理器来生成自己的源数据
如果想要生成自定义的源数据，可以添加相应的依赖包:
```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-configuration-processor</artifactId>
	<optional>true</optional>
</dependency>
```
然后在相应的配置类里面添加@ConfigurationProperties注解。

对项目进行打包，解压相应的jar包，在META-INF中，可以看到spring-configuration-metadata.json文件

关于如何自定义配置，请参考后续文章。

##参考文章
[Configuration Metadata](https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html)