package cc.lqt.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class WebDavUser {
    @JacksonXmlProperty(localName = "username")
    public String username;
    @JacksonXmlProperty(localName = "password")
    public String password;
}
