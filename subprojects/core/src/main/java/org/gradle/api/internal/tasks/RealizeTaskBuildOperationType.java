/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.api.internal.tasks;

import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.taskfactory.InternalTaskInstanceId;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationType;
import org.gradle.internal.scan.UsedByScanPlugin;

/**
 * Represents a creation request for a task. Actual task may be realized later.
 *
 * @since 4.9
 */
public final class RealizeTaskBuildOperationType implements BuildOperationType<RealizeTaskBuildOperationType.Details, RealizeTaskBuildOperationType.Result> {

    @UsedByScanPlugin
    public interface Details {
        String getBuildPath();

        String getTaskPath();

        /**
         * An ID for the task, that disambiguates it from other tasks with the same path.
         *
         * Due to a bug in Gradle, two tasks with the same path can be executed.
         * This is very problematic for build scans.
         * As such, scans need to be able to differentiate between different tasks with the same path.
         * The combination of the path and ID does this.
         *
         * In later versions of Gradle, executing two tasks with the same path will be prevented
         * and this value can be noop-ed.
         */
        long getTaskId();

        boolean isReplacement();

        boolean isEager();
    }

    @UsedByScanPlugin
    public interface Result {
    }

    private RealizeTaskBuildOperationType() {
    }

    static final class RealizeDetailsImpl implements RealizeTaskBuildOperationType.Details {
        private final InternalTaskInstanceId id;
        private final String buildPath;
        private final String taskPath;
        private final boolean replacement;
        private final boolean eager;

        RealizeDetailsImpl(InternalTaskInstanceId id, ProjectInternal project, String taskName, boolean replacement, boolean eager) {
            this.id = id;
            this.buildPath = project.getGradle().getIdentityPath().toString();
            this.taskPath = project.projectPath(taskName).toString();
            this.replacement = replacement;
            this.eager = eager;
        }

        @Override
        public String getBuildPath() {
            return buildPath;
        }

        @Override
        public String getTaskPath() {
            return taskPath;
        }

        @Override
        public long getTaskId() {
            return id.getId();
        }

        @Override
        public boolean isReplacement() {
            return replacement;
        }

        @Override
        public boolean isEager() {
            return eager;
        }

        public BuildOperationDescriptor.Builder descriptor() {
            return BuildOperationDescriptor.displayName("Realize task " + taskPath)
                .details(this);
        }
    }

    private static final class RealizeResultImpl implements RealizeTaskBuildOperationType.Result {
    }

    static final Result REALIZE_RESULT_INSTANCE = new RealizeResultImpl();
}
