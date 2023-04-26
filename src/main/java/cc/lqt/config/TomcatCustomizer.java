package cc.lqt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class TomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory>, Ordered {
    @Value("${config.davBase}")
    private String davBase;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        // 设置Tomcat实例的上传路径
        factory.addContextCustomizers(context -> {
            context.setDocBase(davBase);
        });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
