/*
 * Copyright 2025 April Software
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.norm4j.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.norm4j.dialects.MariaDBDialect;
import org.norm4j.dialects.OracleDialect;
import org.norm4j.dialects.PostgreSQLDialect;
import org.norm4j.dialects.SQLServerDialect;
import org.norm4j.metadata.MetadataManager;

@Mojo(name = "generate-ddl", threadSafe = true)
public class GenerateDdlMojo extends AbstractMojo {
    @Parameter
    private List<String> packages;

    @Parameter
    private List<String> dialects;

    @Parameter(defaultValue = "${project.basedir}/src/main/resources/db")
    private File outputDirectory;

    @Parameter(defaultValue = "${norm4j.schema.version}")
    private String version;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    public GenerateDdlMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("norm4j:generate-ddl starting");

        URLClassLoader projectClassLoader = null;
        ClassLoader originalTcl = Thread.currentThread().getContextClassLoader();

        try {
            projectClassLoader = buildProjectClassLoader();
            Thread.currentThread().setContextClassLoader(projectClassLoader);

            for (String dialect : dialects) {
                MetadataManager metadataManager;

                if ("mariadb".equalsIgnoreCase(dialect)) {
                    metadataManager = new MetadataManager(new MariaDBDialect());
                } else if ("oracle".equalsIgnoreCase(dialect)) {
                    metadataManager = new MetadataManager(new OracleDialect());
                } else if ("postgresql".equalsIgnoreCase(dialect)) {
                    metadataManager = new MetadataManager(new PostgreSQLDialect());
                } else if ("sqlserver".equalsIgnoreCase(dialect)) {
                    metadataManager = new MetadataManager(new SQLServerDialect());
                } else {
                    throw new RuntimeException("Unknown dialect.");
                }

                if (packages == null || packages.isEmpty()) {
                    getLog().warn("No <packages> configured for norm4j-maven-plugin; no tables will be discovered.");
                } else {
                    for (String pkg : packages) {
                        getLog().info("Registering package: " + pkg);
                        metadataManager.registerPackageFromClassPath(pkg);
                    }
                }

                Path out = outputDirectory.toPath()
                        .resolve(version)
                        .resolve(dialect)
                        .resolve("ddl.sql");

                getLog().info("Generating DDL to: " + out);

                metadataManager.createDdlAsResource(out.toString());
            }

            getLog().info("norm4j:generate-ddl finished successfully");
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate norm4j DDL", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalTcl);
            if (projectClassLoader != null) {
                try {
                    projectClassLoader.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private URLClassLoader buildProjectClassLoader()
            throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> elements = new ArrayList<>();

        elements.addAll((List<String>) (List<?>) project.getCompileClasspathElements());

        elements.addAll((List<String>) (List<?>) project.getTestClasspathElements());

        List<URL> urls = new ArrayList<>(elements.size());
        for (String element : elements) {
            File file = new File(element);
            urls.add(file.toURI().toURL());
            getLog().debug("Adding to plugin classpath: " + file);
        }

        return new URLClassLoader(
                urls.toArray(new URL[0]),
                getClass().getClassLoader());
    }
}
