package io.github.stackinfo.maven.plugins;
/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Prepare pom.xml files in effective-like format for further processing.
 *
 */
@Mojo( name = "prepare", aggregator = true )
public class PrepareMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File targetDirectory;

    @Parameter( defaultValue = "${session.executionRootDirectory}", property = "outputDir", required = true )
    private File rootDirectory;

    @Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    private List<MavenProject> projects;

    public void execute() throws MojoExecutionException
    {
        File outputDirectory = new File(targetDirectory, "stackinfo/poms");

        try {
            createOrCleanDirectory(outputDirectory);
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException e) {
            getLog().error(e);
            return;
        }

        MavenXpp3Writer writer = new MavenXpp3Writer();

        for (MavenProject project: projects) {
            Model model = getMinimalModel(project.getModel());

            try {
                File projectDirectory = model.getProjectDirectory().getCanonicalFile();
                if (!projectDirectory.toString().startsWith(rootDirectory.toString())) {
                    getLog().error("Invalid project directory: " + projectDirectory);
                    continue;
                }

                String relative = rootDirectory.toURI().relativize(projectDirectory.toURI()).getPath();
                File projectOutputDirectory = new File(outputDirectory, relative);
                projectOutputDirectory.mkdirs();

                OutputStream out = new FileOutputStream(new File(projectOutputDirectory, "pom.xml"));
                writer.write(out, model);
            } catch (Exception e) {
                getLog().error(e);
            }
        }
    }

    private Model getMinimalModel(Model model) {

        Model minModel = new Model();

        // basic info
        minModel.setGroupId(model.getGroupId());
        minModel.setArtifactId(model.getArtifactId());
        minModel.setVersion(model.getVersion());
        minModel.setPackaging(model.getPackaging());

        minModel.setDescription(model.getDescription());
        minModel.setName(model.getName());
        minModel.setParent(model.getParent());
        minModel.setModelVersion(model.getModelVersion());
        minModel.setModules(model.getModules());

        // dependencies
        minModel.setDependencies(model.getDependencies());
        minModel.setDependencyManagement(model.getDependencyManagement());

        // licenses
        minModel.setLicenses(model.getLicenses());

        // misc
        minModel.setPomFile(model.getPomFile());

        return minModel;
    }

    private void createOrCleanDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            FileUtils.cleanDirectory(directory);
        } else if (directory.exists()) {
            throw new IOException("Not a directory: " + directory);
        } else {
            if(!directory.mkdirs()) {
                throw new IOException("Unable to create output directory: " + directory);
            }
        }
    }
}
