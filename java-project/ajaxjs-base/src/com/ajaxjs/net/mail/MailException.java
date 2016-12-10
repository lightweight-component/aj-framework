/**
 * Copyright 2015 Frank Cheung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.net.mail;

/**
 * 扩展的一个 mail 异常类
 * 
 * @author frank
 */
public class MailException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param string
	 * @param errCode 出错代码
	 */
	public MailException(String string, int errCode) {
		super(string);
		this.errCode = errCode; 
	}

	/**
	 * 出错代码，卡在哪里了
	 */
	public int errCode;
}
