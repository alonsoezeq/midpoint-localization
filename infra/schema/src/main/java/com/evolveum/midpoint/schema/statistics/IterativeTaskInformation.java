/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.midpoint.schema.statistics;

import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.xml.ns._public.common.common_3.IterativeTaskInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SynchronizationInformationType;

import javax.xml.namespace.QName;
import java.util.Date;

/**
 * @author Pavol Mederly
 */
public class IterativeTaskInformation {

    protected IterativeTaskInformationType startValue;

    protected String lastSuccessObjectName;
    protected String lastSuccessObjectDisplayName;
    protected QName lastSuccessObjectType;
    protected String lastSuccessObjectOid;
    protected Date lastSuccessEndTimestamp;
    protected long lastSuccessDuration;
    protected long totalSuccessDuration;
    protected int totalSuccessCount;

    protected String lastFailureObjectName;
    protected String lastFailureObjectDisplayName;
    protected QName lastFailureObjectType;
    protected String lastFailureObjectOid;
    protected Date lastFailureEndTimestamp;
    protected long lastFailureDuration;
    protected long totalFailureDuration;
    protected int totalFailureCount;
    protected Throwable lastFailureException;
    protected String lastFailureExceptionMessage;

    protected String currentObjectName;
    protected String currentObjectDisplayName;
    protected QName currentObjectType;
    protected String currentObjectOid;
    protected Date currentObjectStartTimestamp;

    public IterativeTaskInformation() {
        this(null);
    }

    public IterativeTaskInformation(IterativeTaskInformationType value) {
        startValue = value;
    }

    public IterativeTaskInformationType getStartValue() {
        return startValue;
    }

    public IterativeTaskInformationType getDeltaValue() {
        IterativeTaskInformationType rv = toIterativeTaskInformationType();
        rv.setTimestamp(XmlTypeConverter.createXMLGregorianCalendar(new Date()));
        return rv;
    }

    public IterativeTaskInformationType getAggregatedValue() {
        IterativeTaskInformationType delta = toIterativeTaskInformationType();
        IterativeTaskInformationType rv = aggregate(startValue, delta);
        rv.setTimestamp(XmlTypeConverter.createXMLGregorianCalendar(new Date()));
        return rv;
    }

    private IterativeTaskInformationType aggregate(IterativeTaskInformationType startValue, IterativeTaskInformationType delta) {
        if (startValue == null) {
            return delta;
        }
        IterativeTaskInformationType rv = new IterativeTaskInformationType();
        addTo(rv, startValue);
        addTo(rv, delta);
        return rv;
    }

    protected IterativeTaskInformationType toIterativeTaskInformationType() {
        IterativeTaskInformationType rv = new IterativeTaskInformationType();
        toJaxb(rv);
        return rv;
    }

    public void recordOperationEnd(String objectName, String objectDisplayName, QName objectType, String objectOid, long started, Throwable exception) {
        if (exception != null) {
            lastFailureObjectName = objectName;
            lastFailureObjectDisplayName = objectDisplayName;
            lastFailureObjectType = objectType;
            lastFailureObjectOid = objectOid;
            lastFailureEndTimestamp = new Date();
            lastFailureDuration = lastFailureEndTimestamp.getTime() - started;
            lastFailureException = exception;
            lastFailureExceptionMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage();
            totalFailureDuration += lastFailureDuration;
            totalFailureCount++;
        } else {
            lastSuccessObjectName = objectName;
            lastSuccessObjectDisplayName = objectDisplayName;
            lastSuccessObjectType = objectType;
            lastSuccessObjectOid = objectOid;
            lastSuccessEndTimestamp = new Date();
            lastSuccessDuration = lastSuccessEndTimestamp.getTime() - started;
            totalSuccessDuration += lastSuccessDuration;
            totalSuccessCount++;
        }
        currentObjectName = null;
        currentObjectDisplayName = null;
        currentObjectType = null;
        currentObjectOid = null;
        currentObjectStartTimestamp = null;
    }

    public void recordOperationStart(String objectName, String objectDisplayName, QName objectType, String objectOid) {
        currentObjectName = objectName;
        currentObjectDisplayName = objectDisplayName;
        currentObjectType = objectType;
        currentObjectOid = objectOid;
        currentObjectStartTimestamp = new Date();
    }

    public void toJaxb(IterativeTaskInformationType rv) {
        rv.setLastSuccessObjectName(lastSuccessObjectName);
        rv.setLastSuccessObjectDisplayName(lastSuccessObjectDisplayName);
        rv.setLastSuccessObjectType(lastSuccessObjectType);
        rv.setLastSuccessObjectOid(lastSuccessObjectOid);
        rv.setLastSuccessEndTimestamp(XmlTypeConverter.createXMLGregorianCalendar(lastSuccessEndTimestamp));
        rv.setLastSuccessDuration(lastSuccessDuration);
        rv.setTotalSuccessDuration(totalSuccessDuration);
        rv.setTotalSuccessCount(totalSuccessCount);

        rv.setLastFailureObjectName(lastFailureObjectName);
        rv.setLastFailureObjectDisplayName(lastFailureObjectDisplayName);
        rv.setLastFailureObjectType(lastFailureObjectType);
        rv.setLastFailureObjectOid(lastFailureObjectOid);
        rv.setLastFailureEndTimestamp(XmlTypeConverter.createXMLGregorianCalendar(lastFailureEndTimestamp));
        rv.setLastFailureDuration(lastFailureDuration);
        rv.setLastFailureExceptionMessage(lastFailureExceptionMessage);
        rv.setTotalFailureDuration(totalFailureDuration);
        rv.setTotalFailureCount(totalFailureCount);

        rv.setCurrentObjectName(currentObjectName);
        rv.setCurrentObjectDisplayName(currentObjectDisplayName);
        rv.setCurrentObjectType(currentObjectType);
        rv.setCurrentObjectOid(currentObjectOid);
        rv.setCurrentObjectStartTimestamp(XmlTypeConverter.createXMLGregorianCalendar(currentObjectStartTimestamp));
    }

    // sum != null, delta != null
    public static void addTo(IterativeTaskInformationType sum, IterativeTaskInformationType delta) {
        if (delta.getLastSuccessObjectName() != null) {
            sum.setLastSuccessObjectName(delta.getLastSuccessObjectName());
            sum.setLastSuccessObjectDisplayName(delta.getLastSuccessObjectDisplayName());
            sum.setLastSuccessObjectType(delta.getLastSuccessObjectType());
            sum.setLastSuccessObjectOid(delta.getLastSuccessObjectOid());
            sum.setLastSuccessEndTimestamp(delta.getLastSuccessEndTimestamp());
            sum.setLastSuccessDuration(delta.getLastSuccessDuration());
        }
        sum.setTotalSuccessDuration(sum.getTotalSuccessDuration() + delta.getTotalSuccessDuration());
        sum.setTotalSuccessCount(sum.getTotalSuccessCount() + delta.getTotalSuccessCount());

        if (delta.getLastFailureObjectName() != null) {
            sum.setLastFailureObjectName(delta.getLastFailureObjectName());
            sum.setLastFailureObjectDisplayName(delta.getLastFailureObjectDisplayName());
            sum.setLastFailureObjectType(delta.getLastFailureObjectType());
            sum.setLastFailureObjectOid(delta.getLastFailureObjectOid());
            sum.setLastFailureEndTimestamp(delta.getLastFailureEndTimestamp());
            sum.setLastFailureDuration(delta.getLastFailureDuration());
            sum.setLastFailureExceptionMessage(delta.getLastFailureExceptionMessage());
        }
        sum.setTotalFailureDuration(sum.getTotalFailureDuration() + delta.getTotalFailureDuration());
        sum.setTotalFailureCount(sum.getTotalFailureCount() + delta.getTotalFailureCount());

        sum.setCurrentObjectName(delta.getCurrentObjectName());
        sum.setCurrentObjectDisplayName(delta.getCurrentObjectDisplayName());
        sum.setCurrentObjectType(delta.getCurrentObjectType());
        sum.setCurrentObjectOid(delta.getCurrentObjectOid());
        sum.setCurrentObjectStartTimestamp(delta.getCurrentObjectStartTimestamp());
    }
}
