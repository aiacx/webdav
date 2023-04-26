package cc.lqt.servlet;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.commons.codec.binary.Base64;
import lombok.extern.slf4j.Slf4j;
import cc.lqt.config.WebDavConfig;
import cc.lqt.model.WebDavUser;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

@Slf4j
@WebServlet(name = "WebDavServlet", urlPatterns = {WebDavConfig.urlPattern}, loadOnStartup = 1
        , initParams = {@WebInitParam(name = "listings", value = "true"),
        @WebInitParam(name = "readonly", value = "false"),
        @WebInitParam(name = "debug", value = "0"),
})
public class WebDavSupport extends WebdavServlet {
    @Value("${config.davBase}")
    private String davBase;
    @Value("${config.root-password}")
    private String rootPassword;

    @Resource
    private WebDavConfig webDavConfig;

    @Override
    public void init() throws ServletException {
        super.init();
        WebResourceRoot webResourceRoot = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
        DirResourceSet dirResourceSet = new DirResourceSet(webResourceRoot, "/", davBase, "/");
        webResourceRoot.addPreResources(dirResourceSet);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (auth(request, response))
            super.service(request, response);
    }

    private static String getURIUser(String URI) {
        String temp = URI;
        temp = temp.replace("//", "/");
        int index =0;
        //System.out.println("temp="+temp);
        if(!"/".equals(WebDavConfig.urlPatternPre)){
            temp = temp.replace(WebDavConfig.urlPatternPre, "");
        }else {
            index = 1;
        }
        if ("".equals(temp) || "/".equals(temp)) {
            return "";
        } else {
            return temp.split("/")[index];
        }
    }

    private boolean checkPwd(String user, String password) {
        if (WebDavConfig.rootName.equals(user) && password.equals(rootPassword))
            return true;
        for (WebDavUser webDavUser : webDavConfig.getUsers()) {
            if (user.equals(webDavUser.getUsername()) && password.equals(webDavUser.getPassword()))
                return true;
        }
        return false;
    }

    private boolean auth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        String URI = request.getRequestURI();
        String method = request.getMethod();
        if (authorization != null) {
            String base64 = authorization.replaceFirst("Basic\\s+", "");
            String deBase64 = new String(Base64.decodeBase64(base64), Charset.forName("UTF-8"));
            String user = getURIUser(URI);
            String array[] = deBase64.split(":");
            log.info("["+array[0]+"] "+method + " " + URLDecoder.decode(URI, "UTF-8"));
            if (array.length == 2 && checkPwd(array[0], array[1])) {
                if (WebDavConfig.rootName.equals(array[0]))
                    return true;
                if (URI.equals(WebDavConfig.urlPatternPre) || URI.equals(WebDavConfig.urlPatternPre + "/")) {
                    // ("OPTIONS".equals(method) || "GET".equals(method)) &&
                    // 访问根目录，处理网页GET请求
                    String url = WebDavConfig.urlPatternPre + "/" + array[0];
                    url = url.replace("//","/");
                    if ("GET".equals(method))
                        response.sendRedirect(url);
                    // 访问根目录，处理Mac OS OPTIONS请求
                    else if ("OPTIONS".equals(method))
                        response.sendRedirect(url);
                    // 访问根目录，处理FE PROPFIND请求 暂时只能不返回
                    else if ("PROPFIND".equals(method)) {
                        return false;
                    }else
                        return false;
                } else {
                    // 没访问根目录，则判断请求资源是否越权访问其他用户
                    if (array[0].equals(user)) {
                        // 自己的资源则通过
                        return true;
                    } else {
                        // 访问别人的资源，存在资源越权
                        //log.info(array[0]+" 访问越权，不返回数据 "+method+" "+URLDecoder.decode(URI,"UTF-8"));
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);//401
                        return false;
                    }
                }
            } else {
                //密码错误
                log.warn("["+array[0]+"] 登录失败，密码错误");
            }

        } else {
            log.info(method + " " +  URLDecoder.decode(URI,"UTF-8"));
        }
        //密码错误，或没登录，都跳回登录验证
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);//401
        response.setCharacterEncoding("UTF-8");
        response.setHeader("WWW-Authenticate", "Basic realm=\"DAV\"");
        //log.info("登录验证");
        return false;
    }
}