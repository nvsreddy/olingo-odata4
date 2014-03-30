/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.client.core.v4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.client.core.AbstractTest;
import org.apache.olingo.commons.api.domain.ODataLink;
import org.apache.olingo.commons.api.domain.ODataLinkType;
import org.apache.olingo.commons.api.domain.ODataValue;
import org.apache.olingo.commons.api.domain.v4.ODataEntity;
import org.apache.olingo.commons.api.domain.v4.ODataProperty;
import org.apache.olingo.commons.api.format.ODataPubFormat;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDuration;
import org.apache.olingo.commons.core.op.ResourceFactory;
import org.junit.Test;

public class EntityTest extends AbstractTest {

  @Override
  protected ODataClient getClient() {
    return v4Client;
  }

  private void singleton(final ODataPubFormat format) {
    final InputStream input = getClass().getResourceAsStream("VipCustomer." + getSuffix(format));
    final ODataEntity entity = getClient().getBinder().getODataEntity(
            getClient().getDeserializer().toEntry(input, format).getObject());
    assertNotNull(entity);

    assertEquals("#Microsoft.Test.OData.Services.ODataWCFService.Customer", entity.getName());

    final ODataProperty birthday = entity.getProperty("Birthday");
    assertTrue(birthday.hasPrimitiveValue());
    assertEquals(EdmDateTimeOffset.getInstance(), birthday.getPrimitiveValue().getType());

    final ODataProperty timeBetweenLastTwoOrders = entity.getProperty("TimeBetweenLastTwoOrders");
    assertTrue(timeBetweenLastTwoOrders.hasPrimitiveValue());
    assertEquals(EdmDuration.getInstance(), timeBetweenLastTwoOrders.getPrimitiveValue().getType());

    int checked = 0;
    for (ODataLink link : entity.getNavigationLinks()) {
      if ("Parent".equals(link.getName())) {
        checked++;
        assertEquals(ODataLinkType.ENTITY_NAVIGATION, link.getType());
      }
      if ("Orders".equals(link.getName())) {
        checked++;
        if (format == ODataPubFormat.ATOM) {
          assertEquals(ODataLinkType.ENTITY_SET_NAVIGATION, link.getType());
        }
      }
      if ("Company".equals(link.getName())) {
        checked++;
        assertEquals(ODataLinkType.ENTITY_NAVIGATION, link.getType());
      }
    }
    assertEquals(3, checked);

    // operations won't get serialized
    entity.getOperations().clear();
    final ODataEntity written = getClient().getBinder().getODataEntity(getClient().getBinder().
            getEntry(entity, ResourceFactory.entryClassForFormat(format == ODataPubFormat.ATOM)));
    assertEquals(entity, written);
  }

  @Test
  public void atomSingleton() {
    singleton(ODataPubFormat.ATOM);
  }

  @Test
  public void jsonSingleton() {
    singleton(ODataPubFormat.JSON_FULL_METADATA);
  }

  private void withEnums(final ODataPubFormat format) {
    final InputStream input = getClass().getResourceAsStream("Products_5." + getSuffix(format));
    final ODataEntity entity = getClient().getBinder().getODataEntity(
            getClient().getDeserializer().toEntry(input, format).getObject());
    assertNotNull(entity);

    final ODataProperty skinColor = entity.getProperty("SkinColor");
    assertTrue(skinColor.hasEnumValue());
    assertEquals("Microsoft.Test.OData.Services.ODataWCFService.Color", skinColor.getEnumValue().getTypeName());
    assertEquals("Red", skinColor.getEnumValue().getValue());

//    final ODataProperty coverColors = entity.getProperty("CoverColors");
//    assertTrue(coverColors.hasCollectionValue());
//    for (final Iterator<ODataValue> itor = coverColors.getCollectionValue().iterator(); itor.hasNext();) {
//      final ODataValue item = itor.next();
//      assertTrue(item.isEnum());
//    }

    // operations won't get serialized
    entity.getOperations().clear();    
    final ODataEntity written = getClient().getBinder().getODataEntity(getClient().getBinder().
            getEntry(entity, ResourceFactory.entryClassForFormat(format == ODataPubFormat.ATOM)));
    assertEquals(entity, written);
  }

  @Test
  public void atomWithEnums() {
    withEnums(ODataPubFormat.ATOM);
  }

  @Test
  public void jsonWithEnums() {
    withEnums(ODataPubFormat.JSON_FULL_METADATA);
  }
}