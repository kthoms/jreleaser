/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.tools;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Scoop;
import org.jreleaser.model.tool.spi.ToolProcessingException;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.util.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.Constants.KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR;
import static org.jreleaser.util.Constants.KEY_SCOOP_AUTOUPDATE_URL;
import static org.jreleaser.util.Constants.KEY_SCOOP_BUCKET_REPO_CLONE_URL;
import static org.jreleaser.util.Constants.KEY_SCOOP_BUCKET_REPO_URL;
import static org.jreleaser.util.Constants.KEY_SCOOP_CHECKVER_URL;
import static org.jreleaser.util.Constants.KEY_SCOOP_PACKAGE_NAME;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ScoopToolProcessor extends AbstractRepositoryToolProcessor<Scoop> {
    public ScoopToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(KEY_SCOOP_BUCKET_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));
        props.put(KEY_SCOOP_BUCKET_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getBucket().getOwner(), tool.getBucket().getResolvedName()));

        props.put(KEY_SCOOP_PACKAGE_NAME, tool.getPackageName());
        props.put(KEY_SCOOP_CHECKVER_URL, resolveCheckverUrl(props));
        props.put(KEY_SCOOP_AUTOUPDATE_URL, resolveAutoupdateUrl(props));
        String autoupdateExtracDir = (String) props.get(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME);
        autoupdateExtracDir = autoupdateExtracDir.replace(context.getModel().getProject().getEffectiveVersion(), "$version");
        props.put(KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR, autoupdateExtracDir);
    }

    private Object resolveCheckverUrl(Map<String, Object> props) {
        if (!getTool().getCheckverUrl().contains("{{")) {
            return getTool().getCheckverUrl();
        }
        return applyTemplate(getTool().getCheckverUrl(), props);
    }

    private Object resolveAutoupdateUrl(Map<String, Object> props) {
        if (!getTool().getAutoupdateUrl().contains("{{")) {
            return getTool().getAutoupdateUrl();
        }

        String artifactFile = (String) props.get(KEY_ARTIFACT_FILE);
        String projectVersion = (String) props.get(KEY_PROJECT_VERSION);
        String tagName = (String) props.get(KEY_TAG_NAME);
        artifactFile = artifactFile.replace(projectVersion, "$version");
        tagName = tagName.replace(projectVersion, "$version");

        Map<String, Object> copy = new LinkedHashMap<>(props);
        copy.put(KEY_PROJECT_VERSION, "$version");
        copy.put(KEY_PROJECT_EFFECTIVE_VERSION, "$version");
        copy.put(KEY_TAG_NAME, tagName);
        copy.put(KEY_ARTIFACT_FILE, artifactFile);
        return applyTemplate(getTool().getAutoupdateUrl(), copy);
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "manifest.json".equals(fileName) ?
            outputDirectory.resolve("bucket").resolve(tool.getPackageName().concat(".json")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
