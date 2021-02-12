package org.dita.dost.invoker;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.bson.Document;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.valueOf;
import static java.lang.System.*;
import static java.nio.file.Paths.get;

public class PerformanceTracker implements BuildListener {

    private final Map<String, Long> timeStamp = new HashMap<>();

    private final Map<String, Long> runtimes = new HashMap<>();

    @Override
    public void buildStarted(BuildEvent event) {
        timeStamp.put("build", currentTimeMillis());
    }

    @Override
    public void buildFinished(BuildEvent event) {
        try {
            if (getProperty("skipPerformanceTracking") != null && "true".equalsIgnoreCase(getProperty("skipPerformanceTracking").trim())) {
                return;
            }

            Optional<MongoCookie> mongoCookie = loadCookie();
            if (!mongoCookie.isPresent()) {
                return;
            }

            Document document = buildDocument(event);
            insert(document, mongoCookie.get());
        } catch (Exception e) {
            // never fail a dita-ot build if performance tracking encounters an error
        }
    }

    Optional<MongoCookie> loadCookie() {
        try {
            File cookieFile = get(getClass().getResource("/.mongodb").toURI()).toFile();
            if (cookieFile.exists()) {
                Yaml yaml = new Yaml();
                try (FileInputStream inputStream = new FileInputStream(cookieFile)) {
                    return Optional.ofNullable(yaml.loadAs(inputStream, MongoCookie.class));
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return Optional.empty();
    }

    private void insert(Document document, MongoCookie cookie) {
        MongoClientOptions options = MongoClientOptions.builder().retryWrites(false).build();
        MongoCredential credential = MongoCredential.createCredential(cookie.getUser(), cookie.getDatabase(), cookie.getPassword().toCharArray());
        try (MongoClient mongoClient = new MongoClient(new ServerAddress(cookie.getHost(), valueOf(cookie.getPort())), credential, options)) {
            MongoDatabase database = mongoClient.getDatabase(cookie.getDatabase());
            MongoCollection<Document> collection = database.getCollection("runtimes");
            collection.insertOne(document);
        } catch (Exception e) {
            // don't abort dita-build if runtime information cannot be stored in the db
        }
    }

    private Document buildDocument(BuildEvent event) {
        Project project = event.getProject();
        Document document = new Document();
        document.append("started", timeStamp.get("build"));
        document.append("finished", currentTimeMillis());
        document.append("runtime", currentTimeMillis() - timeStamp.get("build"));
        document.append("document", grepFilename(project.getUserProperty("args.input")));
        document.append("transtype", project.getUserProperty("transtype"));
        document.append("dita-basedir", project.getProperty("dita.dir"));
        document.append("dita-version", grepVersion(project.getProperty("dita.dir")));
        document.append("java-version", project.getProperty("java.version"));
        if (getenv().containsKey("HOSTNAME")) {
            document.append("hostname", getenv().get("HOSTNAME"));
        } else {
            document.append("hostname", getenv().getOrDefault("COMPUTERNAME", "unknown"));
        }
        document.append("runtimes", fixKeys(runtimes));
        return document;
    }

    private String grepFilename(String fullPath) {
        return get(fullPath).getFileName().toString();
    }

    String grepVersion(String basedir) {
        Iterator<Path> pathIterator = get(basedir).iterator();
        while (pathIterator.hasNext()) {
            Path path = pathIterator.next();
            if (path.toString().startsWith("dita-ot-")) {
                return path.toString().replaceAll("^.*?(?=\\d)", "");
            }
        }
        return "";
    }

    /**
     * Currently, dots are not fully supported in map keys by the mongodb server (4.4) and its Java driver (3.12.7).
     * Therefore replacing them with underscore.
     */
    Map<String, Long> fixKeys(Map<String, Long> runtimes) {
        Map<String, Long> result = new HashMap<>();
        runtimes.forEach((key, value) -> result.put(key.replace(".", "_"), value));
        return result;
    }

    @Override
    public void targetStarted(BuildEvent event) {
        timeStamp.put(event.getTarget().getName(), currentTimeMillis());
    }

    @Override
    public void targetFinished(BuildEvent event) {
        runtimes.put(event.getTarget().getName(), currentTimeMillis() - timeStamp.get(event.getTarget().getName()));
    }

    @Override
    public void taskStarted(BuildEvent event) {
        // not needed
    }

    @Override
    public void taskFinished(BuildEvent event) {
        // not needed
    }

    @Override
    public void messageLogged(BuildEvent event) {
        // not needed
    }

}
