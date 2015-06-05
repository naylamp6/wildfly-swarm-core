package org.wildfly.swarm.runtime.undertow;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.builder.HandlerBuilder;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class StaticHandlerBuilder implements HandlerBuilder {
    @Override
    public String name() {
        return "static-content";
    }

    @Override
    public Map<String, Class<?>> parameters() {
        return new HashMap<>();
    }

    @Override
    public Set<String> requiredParameters() {
        return new HashSet<>();
    }

    @Override
    public String defaultParameter() {
        return "";
    }

    @Override
    public HandlerWrapper build(Map<String, Object> map) {
        return new HandlerWrapper() {
            @Override
            public HttpHandler wrap(HttpHandler next) {
                HttpHandler cur = next;

                Path f = Paths.get(System.getProperty("user.dir"), "target", "classes");
                if (Files.exists(f)) {
                    cur = new ResourceHandler(new FileResourceManager(f.toFile(), 1024), cur);
                }

                f = Paths.get(System.getProperty("user.dir"), "src", "main", "webapp");
                if (Files.exists(f)) {
                    cur = new ResourceHandler(new FileResourceManager(f.toFile(), 1024), cur);
                }

                f = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
                if (Files.exists(f)) {
                    cur = new ResourceHandler(new FileResourceManager(f.toFile(), 1024), cur);
                }

                cur = new ResourceHandler(new ClassPathResourceManager(ClassLoader.getSystemClassLoader()), cur);

                try {
                    Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.wildfly.swarm.bootstrap"));
                    cur = new ResourceHandler(new ClassPathResourceManager(module.getClassLoader()), cur);
                } catch (ModuleLoadException e) {
                }


                return cur;
            }
        };
    }
}
