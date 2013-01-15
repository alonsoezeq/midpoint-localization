/*
 * Copyright (c) 2013 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2013 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common;

import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.namespace.QName;

/**
 * @author lazyman
 */
@Embeddable
public class RReferenceValue extends RValue<String> {

    private String value;

    public RReferenceValue() {
    }

    public RReferenceValue(String value) {
        this(null, null, value);
    }

    public RReferenceValue(QName name, QName type, String oid) {
        setName(name);
        setType(type);
        setValue(oid);
    }

    @Index(name = "iOid")
    @Column(name = "oidValue")
    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RReferenceValue that = (RReferenceValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
