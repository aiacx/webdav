package cc.lqt.config;

import cc.lqt.model.WebDavUser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

@Slf4j
@Data
@Configuration
public class WebDavConfig {
    @Value("${config.davBase}")
    private String davBase;
    public static final String urlPattern = "/*";
    public static final String urlPatternPre = "/";
    public static final String rootName = "root";
    private static final String configStr = "<config>\n" +
            "    <users>\n" +
            "        <user>\n" +
            "            <username>user</username>\n" +
            "            <password>user</password>\n" +
            "        </user>\n" +
            "    </users>\n" +
            "</config>";
    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    public List<WebDavUser> users;

    @Bean
    public void init() {
        String xmlStr = "";
        String fileName = "webdav-config.xml";
        File file = new File(fileName);
        log.info("正在读取配置文件webdav-config.xml...");
        if (!file.exists()) {
            // 文件不存在，则创建config.xml
            Path path = Paths.get(fileName);
            //使用newBufferedWriter创建文件并写文件
            //使用try-with-resource方法关闭流，不用手动关闭
            log.error("未找到webdav-config.xml，重新创建...");
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(configStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                //process the line
                xmlStr += line;
                //System.out.println(line);
            }
            XmlMapper xmlMapper = new XmlMapper();
            WebDavConfig config = xmlMapper.readValue(xmlStr, WebDavConfig.class);
            this.users = config.getUsers();
            mkdirs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mkdirs() {
        File rootFile = new File(davBase + File.separator + rootName);
        if (!rootFile.exists()) {
            log.info("创建管理员目录" + rootFile.getAbsolutePath());
            rootFile.mkdirs();
        }
        for (WebDavUser webDavUser : this.users) {
            File file = new File(davBase + File.separator + webDavUser.getUsername());
            if (!file.exists()) {
                log.info("创建用户目录" + file.getAbsolutePath());
                file.mkdirs();
            }
        }
    }
}
