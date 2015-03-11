/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.pipe.timing;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import org.dswarm.init.Monitoring;
import org.dswarm.persistence.model.internal.gdm.GDMModel;

@In(GDMModel.class)
@Out(GDMModel.class)
@Description("Benchmarks the execution time of the downstream modules.")
public final class ObjectTimer extends TimerBased<ObjectReceiver<GDMModel>>
		implements ObjectPipe<GDMModel, ObjectReceiver<GDMModel>> {

	@Inject
	private ObjectTimer(
			@Monitoring final MetricRegistry registry,
			@Assisted final String prefix) {
		super(registry, prefix);
	}

	@Override
	public void process(final GDMModel obj) {
		final TimingContext context = startMeasurement("process");
		try {
			getReceiver().process(obj);
		} finally {
			context.stop();
		}
	}
}
