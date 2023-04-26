# WebDav

### 一个WebDav服务端，支持多用户
### 配置文件
#### 1.在application.yml中配置端口号、资源路径、root用户密码
````
server:
  port: 8090
config:
  davBase: /Users/lqt/webdav
  root-password: root
````
#### 2.在webdav-config.xml中配置用户
#### 此文件会在启动后自动生成
````
<config>
    <users>
        <user>
            <username>user</username>
            <password>user</password>
        </user>
    </users>
</config>
````
### 启动
````
java -jar webdav.jar
````
### 网页登录
````
http://localhost:8090
````
### WebDav客户端登录
````
http://localhost:8090/{用户名}
````

### 如何将内置Tomcat替换成AAS？

#### 1.引入Maven依赖
````
<dependency>
    <groupId>com.apusic</groupId>
    <artifactId>aams-spring-boot-starter-all</artifactId>
    <version>2.1.7.RELEASE</version>
    <exclusions>
        <exclusion>
            <groupId>com.dameng</groupId>
            <artifactId>Dm8JdbcDriver18</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-annotation</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </exclusion>
        <exclusion>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-models</artifactId>
        </exclusion>
        <exclusion>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
        </exclusion>
        <exclusion>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

#### 2.替换WebDavSupport.java中引用
##### 将 org.apache.catalina
````
import org.apache.catalina.Globals;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.webresources.DirResourceSet;
````
##### 替换为 com.apusic.ams
````
import com.apusic.ams.Globals;
import com.apusic.ams.WebResourceRoot;
import com.apusic.ams.servlets.WebdavServlet;
import com.apusic.ams.webresources.DirResourceSet;
````
#### 3.配置AAS启动实例的上传目录

##### 创建AASWebServerFactoryCustomizer.java，删除TomcatCustomizer.java
````
import com.apusic.boot.web.embedded.ams.ConfigurableAasWebServerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class AASWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableAasWebServerFactory>, Ordered {
@Value("${config.davBase}")
private String davBase;

    @Override
    public void customize(ConfigurableAasWebServerFactory factory) {
        // 设置上传路径
        factory.addContextCustomizers(context -> {
            context.setDocBase(davBase);
        });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
````
