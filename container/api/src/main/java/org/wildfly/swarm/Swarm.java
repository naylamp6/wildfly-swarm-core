/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DeploymentException;
import org.wildfly.swarm.internal.ArtifactManager;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * Default {@code main(...)} if an application does not provide one.
 *
 * <p>This simply constructs a default container, starts it and performs
 * a default deployment.  Typically only useful for barren WAR applications.</p>
 *
 * @author Bob McWhirter
 */
public class Swarm extends Container {

    public static ArtifactManager ARTIFACT_MANAGER;

    public Swarm() throws Exception {
    }

    public Swarm(boolean debugBootstrap) throws Exception {
        super( debugBootstrap );
    }

    @Override
    public Swarm fraction(Supplier<Fraction> supplier) {
        return (Swarm) super.fraction(supplier);
    }

    @Override
    public Swarm fraction(Fraction fraction) {
        return (Swarm) super.fraction(fraction);
    }

    @Override
    public Swarm iface(String name, String expression) {
        return (Swarm) super.iface(name, expression);
    }

    @Override
    public Swarm socketBindingGroup(SocketBindingGroup group) {
        return (Swarm) super.socketBindingGroup(group);
    }

    @Override
    public Swarm start(boolean eagerlyOpen) throws Exception {
        return (Swarm) super.start(eagerlyOpen);
    }

    @Override
    public Swarm stop() throws Exception {
        return (Swarm) super.stop();
    }

    @Override
    public Swarm start() throws Exception {
        return (Swarm) super.start();
    }

    @Override
    public Swarm start(Archive<?> deployment) throws Exception {
        return (Swarm) super.start(deployment);
    }

    @Override
    public Swarm deploy() throws DeploymentException {
        return (Swarm) super.deploy();
    }

    @Override
    public Swarm deploy(Archive<?> deployment) throws DeploymentException {
        return (Swarm) super.deploy(deployment);
    }

    /**
     * Main entry-point.
     *
     * @param args Ignored.
     * @throws Exception if an error occurs.
     */
    public static void main(String... args) throws Exception {
        if (System.getProperty("boot.module.loader") == null) {
            System.setProperty("boot.module.loader", "org.wildfly.swarm.bootstrap.modules.BootModuleLoader");
        }
        Module bootstrap = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("swarm.application"));

        ServiceLoader<ContainerFactory> factory = bootstrap.loadService(ContainerFactory.class);
        Iterator<ContainerFactory> factoryIter = factory.iterator();

        if (!factoryIter.hasNext()) {
            simpleMain(args);
        } else {
            factoryMain(factoryIter.next(), args);
        }
    }

    public static void simpleMain(String... args) throws Exception {
        Container container = new Swarm().start();
        container.deploy();
    }

    public static void factoryMain(ContainerFactory factory, String... args) throws Exception {
        Container container = factory.newContainer(args).start();
        container.deploy();
    }

    public static ArtifactManager artifactManager() throws IOException {
        if (ARTIFACT_MANAGER == null) {
            ARTIFACT_MANAGER = new ArtifactManager();
            ArtifactLookup.INSTANCE.set(ARTIFACT_MANAGER);
        }
        return ARTIFACT_MANAGER;
    }

    public static JavaArchive artifact(String gav) throws Exception {
        return artifactManager().artifact(gav);
    }

    public static JavaArchive artifact(String gav, String asName) throws Exception {
        return artifactManager().artifact(gav, asName);
    }

    public static List<JavaArchive> allArtifacts() throws Exception {
        return artifactManager().allArtifacts();
    }
}
