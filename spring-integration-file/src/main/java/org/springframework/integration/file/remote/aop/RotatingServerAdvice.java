/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.file.remote.aop;

import java.util.List;

import org.springframework.integration.aop.AbstractMessageSourceAdvice;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.remote.AbstractRemoteFileStreamingMessageSource;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.integration.file.remote.synchronizer.AbstractInboundFileSynchronizingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

/**
 * A smart poller advice that rotates across multiple remote servers/directories.
 *
 * @author Gary Russell
 * @author Michael Forstner
 * @author Artem Bilan
 * @author David Turanski
 *
 * @since 5.0.7
 *
 */
public class RotatingServerAdvice extends AbstractMessageSourceAdvice {

	private final RotationPolicy rotationPolicy;

	/**
	 * Create an instance that rotates to the next server/directory if no message is
	 * received.
	 * @param factory the {@link DelegatingSessionFactory}.
	 * @param keyDirectories a list of {@link KeyDirectory}.
	 */
	public RotatingServerAdvice(DelegatingSessionFactory<?> factory, List<KeyDirectory> keyDirectories) {
		this(factory, keyDirectories, false);
	}

	/**
	 * Create an instance that rotates to the next server/directory depending on the fair
	 * argument.
	 * @param factory the {@link DelegatingSessionFactory}.
	 * @param keyDirectories a list of {@link KeyDirectory}.
	 * @param fair true to rotate on every poll, false to rotate when no message is received.
	 */
	public RotatingServerAdvice(DelegatingSessionFactory<?> factory, List<KeyDirectory> keyDirectories, boolean fair) {
		this(new StandardRotationPolicy(factory, keyDirectories, fair));
	}

	/**
	 * Construct an instance that rotates according to the supplied
	 * {@link RotationPolicy}.
	 * @param rotationPolicy the policy.
	 */
	public RotatingServerAdvice(RotationPolicy rotationPolicy) {
		Assert.notNull(rotationPolicy, "'rotationPolicy' cannot be null");
		this.rotationPolicy = rotationPolicy;
	}

	@Override
	public boolean beforeReceive(MessageSource<?> source) {
		this.rotationPolicy.beforeReceive(source);
		return true;
	}

	@Override
	public Message<?> afterReceive(Message<?> result, MessageSource<?> source) {
		this.rotationPolicy.afterReceive(result != null, source);
		return result;
	}

	public static class StandardRotationPolicy extends AbstractStandardRotationPolicy {


		public StandardRotationPolicy(DelegatingSessionFactory<?> factory, List<KeyDirectory> keyDirectories,
				boolean fair) {
			super(factory, keyDirectories, fair);
		}

		@Override
		protected void onRotation(MessageSource<?> source) {
			Assert.isTrue(source instanceof AbstractInboundFileSynchronizingMessageSource
							|| source instanceof AbstractRemoteFileStreamingMessageSource,
					"source must be an AbstractInboundFileSynchronizingMessageSource or a "
							+ "AbstractRemoteFileStreamingMessageSource");

			if (source instanceof AbstractRemoteFileStreamingMessageSource) {
				((AbstractRemoteFileStreamingMessageSource<?>) source).setRemoteDirectory(getCurrent().getDirectory());
			}
			else {
				((AbstractInboundFileSynchronizingMessageSource<?>) source).getSynchronizer()
						.setRemoteDirectory(getCurrent().getDirectory());
			}
		}

	}
}
