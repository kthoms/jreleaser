/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.model;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Sdkman extends AbstractAnnouncer {
    public static final String NAME = "sdkman";

    private String consumerKey;
    private String consumerToken;
    private String candidate;
    private boolean major = true;

    public Sdkman() {
        super(NAME);
    }

    void setAll(Sdkman sdkman) {
        super.setAll(sdkman);
        this.consumerKey = sdkman.consumerKey;
        this.consumerToken = sdkman.consumerToken;
        this.candidate = sdkman.candidate;
        this.major = sdkman.major;
    }

    public String getResolvedConsumerKey() {
        if (isNotBlank(consumerKey)) {
            return consumerKey;
        }
        return System.getenv("SDKMAN_CONSUMER_KEY");
    }

    public String getResolvedConsumerToken() {
        if (isNotBlank(consumerToken)) {
            return consumerToken;
        }
        return System.getenv("SDKMAN_CONSUMER_TOKEN");
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerToken() {
        return consumerToken;
    }

    public void setConsumerToken(String consumerToken) {
        this.consumerToken = consumerToken;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public boolean isMajor() {
        return major;
    }

    public void setMajor(boolean major) {
        this.major = major;
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? "************" : "**unset**");
        props.put("consumerToken", isNotBlank(getResolvedConsumerToken()) ? "************" : "**unset**");
        props.put("candidate", candidate);
        props.put("major", major);
    }
}