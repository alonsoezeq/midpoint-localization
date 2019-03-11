/*
 * Copyright (c) 2010-2019 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.provisioning.impl.async;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;

/**
 *
 */
public class TestAsyncUpdateNoCaching extends TestAsyncUpdate {

	@Override
	protected void tailorResourceObject(PrismObject<ResourceType> object) {
		// nothing to do here ... read capability is present in the XML file
	}

	@Override
	protected int getNumberOfAccountAttributes() {
		return 2;
	}
}
