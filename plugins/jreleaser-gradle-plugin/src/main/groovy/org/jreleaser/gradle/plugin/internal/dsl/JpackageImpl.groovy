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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Jpackage
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.10.0
 */
@CompileStatic
class JpackageImpl extends AbstractJavaAssembler implements Jpackage {
    String name
    final Property<String> jlink
    final JavaImpl java
    final PlatformImpl platform

    private final ApplicationPackageImpl applicationPackage
    private final LauncherImpl launcher
    private final LinuxImpl linux
    private final WindowsImpl windows
    private final OsxImpl osx
    final NamedDomainObjectContainer<ArtifactImpl> runtimeImages

    @Inject
    JpackageImpl(ObjectFactory objects) {
        super(objects)

        jlink = objects.property(String).convention(Providers.notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        applicationPackage = objects.newInstance(ApplicationPackageImpl, objects)
        launcher = objects.newInstance(LauncherImpl, objects)
        linux = objects.newInstance(LinuxImpl, objects)
        windows = objects.newInstance(WindowsImpl, objects)
        osx = objects.newInstance(OsxImpl, objects)

        runtimeImages = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            jlink.present ||
            java.isSet() ||
            platform.isSet() ||
            applicationPackage.isSet() ||
            launcher.isSet() ||
            linux.isSet() ||
            windows.isSet() ||
            osx.isSet() ||
            !runtimeImages.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void runtimeImage(Action<? super Artifact> action) {
        action.execute(runtimeImages.maybeCreate("runtimeImage-${runtimeImages.size()}".toString()))
    }

    @Override
    void applicationPackage(Action<? super ApplicationPackage> action) {
        action.execute(applicationPackage)
    }

    @Override
    void launcher(Action<? super Launcher> action) {
        action.execute(launcher)
    }

    @Override
    void linux(Action<? super Linux> action) {
        action.execute(linux)
    }

    @Override
    void windows(Action<? super Windows> action) {
        action.execute(windows)
    }

    @Override
    void osx(Action<? super Osx> action) {
        action.execute(osx)
    }

    @Override
    void runtimeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, runtimeImages.maybeCreate("runtimeImage-${runtimeImages.size()}".toString()))
    }

    @Override
    void applicationPackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ApplicationPackage) Closure<Void> action) {
        ConfigureUtil.configure(action, applicationPackage)
    }

    @Override
    void launcher(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Launcher) Closure<Void> action) {
        ConfigureUtil.configure(action, launcher)
    }

    @Override
    void linux(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Linux) Closure<Void> action) {
        ConfigureUtil.configure(action, linux)
    }

    @Override
    void windows(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Windows) Closure<Void> action) {
        ConfigureUtil.configure(action, windows)
    }

    @Override
    void osx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Osx) Closure<Void> action) {
        ConfigureUtil.configure(action, osx)
    }

    org.jreleaser.model.Jpackage toModel() {
        org.jreleaser.model.Jpackage jpackage = new org.jreleaser.model.Jpackage()
        jpackage.name = name
        fillProperties(jpackage)
        if (java.isSet()) jpackage.java = java.toModel()
        if (platform.isSet()) jpackage.platform = platform.toModel()
        if (applicationPackage.isSet()) jpackage.applicationPackage = applicationPackage.toModel()
        if (launcher.isSet()) jpackage.launcher = launcher.toModel()
        if (linux.isSet()) jpackage.linux = linux.toModel()
        if (windows.isSet()) jpackage.windows = windows.toModel()
        if (osx.isSet()) jpackage.osx = osx.toModel()
        if (jlink.present) jpackage.jlink = jlink.get()
        for (ArtifactImpl artifact : runtimeImages) {
            jpackage.addRuntimeImage(artifact.toModel())
        }

        jpackage
    }

    @CompileStatic
    static class ApplicationPackageImpl implements ApplicationPackage {
        final ListProperty<String> fileAssociations
        final Property<String> appVersion
        final Property<String> vendor
        final Property<String> copyright
        final Property<String> licenseFile
        final Property<String> resourceDir

        @Inject
        ApplicationPackageImpl(ObjectFactory objects) {
            appVersion = objects.property(String).convention(Providers.notDefined())
            vendor = objects.property(String).convention(Providers.notDefined())
            copyright = objects.property(String).convention(Providers.notDefined())
            licenseFile = objects.property(String).convention(Providers.notDefined())
            resourceDir = objects.property(String).convention(Providers.notDefined())
            fileAssociations = objects.listProperty(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            appVersion.present ||
                vendor.present ||
                copyright.present ||
                licenseFile.present ||
                resourceDir.present ||
                fileAssociations.present
        }

        org.jreleaser.model.Jpackage.ApplicationPackage toModel() {
            org.jreleaser.model.Jpackage.ApplicationPackage a = new org.jreleaser.model.Jpackage.ApplicationPackage()
            a.appVersion = appVersion.orNull
            a.vendor = vendor.orNull
            a.copyright = copyright.orNull
            a.licenseFile = licenseFile.orNull
            a.resourceDir = resourceDir.orNull
            a.fileAssociations = (List<String>) fileAssociations.getOrElse([] as List<String>)
            a
        }
    }


    @CompileStatic
    static class LauncherImpl implements Launcher {
        final ListProperty<String> arguments
        final ListProperty<String> javaOptions
        final ListProperty<String> launchers

        @Inject
        LauncherImpl(ObjectFactory objects) {
            arguments = objects.listProperty(String).convention(Providers.notDefined())
            javaOptions = objects.listProperty(String).convention(Providers.notDefined())
            launchers = objects.listProperty(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            arguments.present ||
                javaOptions.present ||
                launchers.present
        }

        org.jreleaser.model.Jpackage.Launcher toModel() {
            org.jreleaser.model.Jpackage.Launcher a = new org.jreleaser.model.Jpackage.Launcher()
            a.arguments = (List<String>) arguments.getOrElse([] as List<String>)
            a.javaOptions = (List<String>) javaOptions.getOrElse([] as List<String>)
            a.launchers = (List<String>) launchers.getOrElse([] as List<String>)
            a
        }
    }

    @CompileStatic
    private static abstract class AbstractPlatformPackager implements PlatformPackager {
        final RegularFileProperty icon
        final ListProperty<String> types
        final Property<String> installDir

        @Inject
        AbstractPlatformPackager(ObjectFactory objects) {
            icon = objects.fileProperty().convention(Providers.notDefined())
            types = objects.listProperty(String).convention(Providers.notDefined())
            installDir = objects.property(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            icon.present ||
                types.present ||
                jdk.isSet() ||
                installDir.present
        }

        protected abstract ArtifactImpl getJdk()

        @Override
        void jdk(Action<? super Artifact> action) {
            action.execute(jdk)
        }

        @Override
        void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
            ConfigureUtil.configure(action, jdk)
        }

        void fillProperties(org.jreleaser.model.Jpackage.PlatformPackager p) {
            p.icon = icon.orNull
            p.types = (List<String>) types.getOrElse([] as List<String>)
            p.installDir = installDir.orNull
            if (jdk.isSet()) p.jdk = jdk.toModel()
        }
    }

    @CompileStatic
    static class LinuxImpl extends AbstractPlatformPackager implements Linux {
        final ListProperty<String> packageDeps
        final Property<String> packageName
        final Property<String> maintainer
        final Property<String> menuGroup
        final Property<String> license
        final Property<String> appRelease
        final Property<String> appCategory
        final Property<Boolean> shortcut
        private final ArtifactImpl jdk

        @Inject
        LinuxImpl(ObjectFactory objects) {
            super(objects)
            packageDeps = objects.listProperty(String).convention(Providers.notDefined())
            packageName = objects.property(String).convention(Providers.notDefined())
            maintainer = objects.property(String).convention(Providers.notDefined())
            menuGroup = objects.property(String).convention(Providers.notDefined())
            license = objects.property(String).convention(Providers.notDefined())
            appRelease = objects.property(String).convention(Providers.notDefined())
            appCategory = objects.property(String).convention(Providers.notDefined())
            shortcut = objects.property(Boolean).convention(Providers.notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                packageDeps.present ||
                packageName.present ||
                maintainer.present ||
                menuGroup.present ||
                license.present ||
                appRelease.present ||
                appCategory.present ||
                shortcut.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.Jpackage.Linux toModel() {
            org.jreleaser.model.Jpackage.Linux a = new org.jreleaser.model.Jpackage.Linux()
            fillProperties(a)
            a.packageName = packageName.orNull
            a.maintainer = maintainer.orNull
            a.menuGroup = menuGroup.orNull
            a.license = license.orNull
            a.appRelease = appRelease.orNull
            a.appCategory = appCategory.orNull
            a.shortcut = shortcut.orNull
            a.packageDeps = (List<String>) packageDeps.getOrElse([] as List<String>)
            a
        }
    }

    @CompileStatic
    static class WindowsImpl extends AbstractPlatformPackager implements Windows {
        final Property<Boolean> console
        final Property<Boolean> dirChooser
        final Property<Boolean> menu
        final Property<Boolean> perUserInstall
        final Property<Boolean> shortcut
        final Property<String> menuGroup
        final Property<String> upgradeUuid
        private final ArtifactImpl jdk

        @Inject
        WindowsImpl(ObjectFactory objects) {
            super(objects)
            console = objects.property(Boolean).convention(Providers.notDefined())
            dirChooser = objects.property(Boolean).convention(Providers.notDefined())
            menu = objects.property(Boolean).convention(Providers.notDefined())
            perUserInstall = objects.property(Boolean).convention(Providers.notDefined())
            shortcut = objects.property(Boolean).convention(Providers.notDefined())
            menuGroup = objects.property(String).convention(Providers.notDefined())
            upgradeUuid = objects.property(String).convention(Providers.notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                console.present ||
                dirChooser.present ||
                menu.present ||
                perUserInstall.present ||
                shortcut.present ||
                menuGroup.present ||
                upgradeUuid.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.Jpackage.Windows toModel() {
            org.jreleaser.model.Jpackage.Windows a = new org.jreleaser.model.Jpackage.Windows()
            fillProperties(a)
            a.console = console.orNull
            a.dirChooser = dirChooser.orNull
            a.menu = menu.orNull
            a.perUserInstall = perUserInstall.orNull
            a.shortcut = shortcut.orNull
            a.menuGroup = menuGroup.orNull
            a.upgradeUuid = upgradeUuid.orNull
            if (jdk.isSet()) a.jdk = jdk.toModel()
            a
        }
    }

    @CompileStatic
    static class OsxImpl extends AbstractPlatformPackager implements Osx {
        final Property<String> packageIdentifier
        final Property<String> packageName
        final Property<String> packageSigningPrefix
        final Property<String> signingKeychain
        final Property<String> signingKeyUsername
        final Property<Boolean> sign
        private final ArtifactImpl jdk

        @Inject
        OsxImpl(ObjectFactory objects) {
            super(objects)
            packageIdentifier = objects.property(String).convention(Providers.notDefined())
            packageName = objects.property(String).convention(Providers.notDefined())
            packageSigningPrefix = objects.property(String).convention(Providers.notDefined())
            signingKeychain = objects.property(String).convention(Providers.notDefined())
            signingKeyUsername = objects.property(String).convention(Providers.notDefined())
            sign = objects.property(Boolean).convention(Providers.notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                packageIdentifier.present ||
                packageName.present ||
                packageSigningPrefix.present ||
                signingKeychain.present ||
                signingKeyUsername.present ||
                sign.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.Jpackage.Osx toModel() {
            org.jreleaser.model.Jpackage.Osx a = new org.jreleaser.model.Jpackage.Osx()
            fillProperties(a)
            a.packageIdentifier = packageIdentifier.orNull
            a.packageName = packageName.orNull
            a.packageSigningPrefix = packageSigningPrefix.orNull
            a.signingKeychain = signingKeychain.orNull
            a.signingKeyUsername = signingKeyUsername.orNull
            a.sign = sign.orNull
            a
        }
    }
}
