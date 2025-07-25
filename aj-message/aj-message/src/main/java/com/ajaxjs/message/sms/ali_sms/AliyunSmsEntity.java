/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.message.sms.ali_sms;


import lombok.Data;

/**
 * 阿里云短信实体
 *
 * @author sp42 frank@ajaxjs.com
 */
@Data
public class AliyunSmsEntity  {
    /**
     * App ID
     */
    private String accessKeyId;

    /**
     * App 密钥
     */
    private String accessSecret;

    private String phoneNumbers;

    private String signName;

    private String templateCode;

    private String templateParam;
}
