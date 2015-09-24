/*
 * Copyright 2015 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */

package com.unboundid.scim2.server.utils;

import com.unboundid.scim2.common.Path;
import com.unboundid.scim2.common.types.AttributeDefinition;

import java.util.Set;



/**
 * A resource trimmer implementing the SCIM standard for returning attributes.
 */
public class ScimResourceTrimmer extends ResourceTrimmer
{
  private final ResourceTypeDefinition resourceType;
  private final Set<Path> requestAttributes;
  private final Set<Path> queryAttributes;
  private final boolean excluded;



  /**
   * Create a new SCIMResourceTrimmer.
   * @param resourceType       The resource type definition for resources to
   *                           trim.
   * @param requestAttributes  The attributes in the request object or null for
   *                           other requests.
   * @param queryAttributes    The attributes from the 'attributes' or
   *                           'excludedAttributes' query parameter.
   * @param excluded           {@code true} if the queryAttributes came from
   *                           the excludedAttributes query parameter.
   */
  public ScimResourceTrimmer(final ResourceTypeDefinition resourceType,
                             final Set<Path> requestAttributes,
                             final Set<Path> queryAttributes,
                             final boolean excluded)
  {
    this.resourceType      = resourceType;
    this.requestAttributes = requestAttributes;
    this.queryAttributes   = queryAttributes;
    this.excluded          = excluded;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldReturn(final Path path)
  {
    AttributeDefinition attributeDefinition =
        resourceType.getAttributeDefinition(path);
    AttributeDefinition.Returned returned = attributeDefinition == null ?
        AttributeDefinition.Returned.DEFAULT :
        attributeDefinition.getReturned();

    switch(returned)
    {
      case ALWAYS:
        return true;
      case NEVER:
        return false;
      case REQUEST:
        // Return only if it was one of the request attributes or if there are
        // no request attributes, then only if it was one of the override query
        // attributes.
        return requestAttributes.contains(path) ||
               (requestAttributes.isEmpty() && !excluded &&
                queryAttributes.contains(path));
      default:
        // Return if it is not one of the excluded query attributes and no
        // override query attributes are provided. If override query attributes
        // are provided, only return if it is one of them.
        if(excluded)
        {
          return !queryAttributes.contains(path);
        }
        else
        {
          return queryAttributes.isEmpty() ||
              queryAttributes.contains(path);
        }
    }
  }
}