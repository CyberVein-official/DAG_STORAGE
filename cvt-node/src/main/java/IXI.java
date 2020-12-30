import com.cvt.iri.service.CallableRequest;
import com.cvt.iri.service.dto.AbstractResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jmx.mbeanserver.Util.cast;
import static java.nio.file.StandardWatchEventKinds.*;

public class IXI {

    private static final Logger log = LoggerFactory.getLogger(IXI.class);
    private static final int MAX_TREE_DEPTH = 2;

    private final Gson gson = new GsonBuilder().create();
    private final ScriptEngine scriptEngine = (new ScriptEngineManager()).getEngineByName("JavaScript");
    private final Map<String, Map<String, CallableRequest<AbstractResponse>>> ixiAPI = new HashMap<>();
    private final Map<String, Map<String, Runnable>> ixiLifetime = new HashMap<>();
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();
    private final Map<Path, Long> loadedLastTime = new HashMap<>();

    private WatchService watcher;
    private Thread dirWatchThread;
    private Path rootPath;

    private boolean shutdown = false;
    private final Cvt cvt;

    public IXI() {
        cvt = null;
    }

    public IXI(Cvt cvt) {
        this.cvt = cvt;
    }

    public void init(String rootDir) throws Exception {
        if(rootDir.length() > 0) {
            watcher = FileSystems.getDefault().newWatchService();
            this.rootPath = Paths.get(rootDir);
            if(this.rootPath.toFile().exists() || this.rootPath.toFile().mkdir()) {
                registerRecursive(this.rootPath);
                dirWatchThread = (new Thread(this::processWatchEvents));
                dirWatchThread.start();
            }
        }
    }

    private void registerRecursive(final Path root) throws IOException {
        Files.walkFileTree(root, EnumSet.allOf(FileVisitOption.class), MAX_TREE_DEPTH, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path modulePath, BasicFileAttributes attrs) throws IOException {
                watch(modulePath);
                if (modulePath != rootPath) {
                    loadModule(modulePath);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processWatchEvents() {
        while(!shutdown) {
            WatchKey key = null;
            try {
                key = watcher.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error("Watcher interrupted: ", e);
            }
            if (key == null) {
                continue;
            }
            WatchKey finalKey = key;
            key.pollEvents().forEach(watchEvent -> {
                WatchEvent<Path> pathEvent = cast(watchEvent);
                IxiEvent ixiEvent = IxiEvent.fromName(watchEvent.kind().name());
                Path watchedPath = watchKeys.get(finalKey);
                if (watchedPath != null) {
                    handleModulePathEvent(watchedPath, ixiEvent, watchedPath.resolve(pathEvent.context()));
                }
            });
            key.reset();
        }
    }








}
