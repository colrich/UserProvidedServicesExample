
package io.pivotal.spring.VariableDisplayer;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class DisplayController {

    private Logger LOG = Logger.getLogger(DisplayController.class.getName());

    @Autowired
    private RestTemplate serviceCaller;

    @Autowired
    private GaugeService gauge;

    @Value("${our.custom.message}")
    private String message;


    @RequestMapping("/")
    public VariableInfo getInfo() {
        String envvar = System.getenv("INSTANCE_GUID");
        LOG.log(Level.WARNING, "My variable's content: " + envvar);
        gauge.submit("infoHappened", 1.0f);
        return new VariableInfo("INSTANCE_GUID", envvar);
//        return envvar;
    }

    @RequestMapping("/message") 
    public String getMessage() {
        return message;
    }

    @RequestMapping("/boundServiceCall")
    public VariableInfo callBoundService(@RequestParam("serviceName") String serviceName) {
        String uri = getUPSServiceURL(serviceName);
        LOG.log(Level.WARNING, "bound service uri: " + uri);

        if (uri != null) {
//            ResponseEntity<String> response = serviceCaller.getForEntity(uri, String.class);
//            return response.getBody();
            ResponseEntity<VariableInfo> response = serviceCaller.getForEntity(uri, VariableInfo.class);
            return response.getBody();

        }
        else {
//            return "no bound UPS!";
            return null;
        }
    }

    private String getUPSServiceURL(String serviceName) {
        String vcap = System.getenv("VCAP_SERVICES");
        LOG.log(Level.WARNING, "VCAP_SERVICES content: " + vcap);

        LOG.log(Level.WARNING, "Using GSON to parse the json...");
        JsonElement root = new JsonParser().parse(vcap);
        JsonObject ups = null;
        if (root != null) {
            if (root.getAsJsonObject().has("user-provided")) {
                for (int i = 0; i < root.getAsJsonObject().get("user-provided").getAsJsonArray().size(); i++) {
                    ups = root.getAsJsonObject().get("user-provided").getAsJsonArray().get(i).getAsJsonObject();
                    LOG.log(Level.WARNING, "instance name: " + ups.get("name").getAsString());
                    if (ups.get("name").getAsString().equalsIgnoreCase(serviceName)) break;
                }
            }
            else {
                LOG.log(Level.SEVERE, "ERROR: no redis instance found in VCAP_SERVICES");
            }
        }

        if (ups != null) {
            JsonObject creds = ups.get("credentials").getAsJsonObject();
            return creds.get("uri").getAsString();
        }
        else return null;
    }

}

class VariableInfo {
    public String name;
    public String value;

    public VariableInfo() {}

    public VariableInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}