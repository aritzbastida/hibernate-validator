// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.engine.metadata;

import java.util.Set;
import javax.validation.BeanDescriptor;
import javax.validation.PropertyDescriptor;
import javax.validation.Validator;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.hibernate.validation.engine.Order;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class BeanDescriptorImplTest {

	@Test
	public void testIsBeanConstrained() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );

		// constraint via @Valid
		assertFalse( "There should be no direct constraints on the specified bean.", beanDescriptor.hasConstraints() );
		assertTrue( "Bean should be constrainted due to @valid ", beanDescriptor.isBeanConstrained() );

		// constraint hosted on bean itself
		beanDescriptor = validator.getConstraintsForClass( Account.class );
		assertTrue( "There should be direct constraints on the specified bean.", beanDescriptor.hasConstraints() );
		assertTrue( "Bean should be constrainted due to @valid", beanDescriptor.isBeanConstrained() );

		// constraint on bean property
		beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertFalse( "There should be no direct constraints on the specified bean.", beanDescriptor.hasConstraints() );
		assertTrue( "Bean should be constrainted due to @NotNull", beanDescriptor.isBeanConstrained() );
	}

	@Test
	public void testUnconstraintClass() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( UnconstraintEntity.class );
		assertFalse( "There should be no direct constraints on the specified bean.", beanDescriptor.hasConstraints() );
		assertFalse( "Bean should be unconstrainted.", beanDescriptor.isBeanConstrained() );
	}

	@Test
	public void testGetConstraintForExistingConstrainedProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderNumber" );
		assertEquals(
				"There should be one constraint descriptor", 1, propertyDescriptor.getConstraintDescriptors().size()
		);

		beanDescriptor = validator.getConstraintsForClass( Customer.class );
		propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderList" );
		assertEquals(
				"There should be no constraint descriptors", 0, propertyDescriptor.getConstraintDescriptors().size()
		);
		assertTrue( "The property should be cascaded", propertyDescriptor.isCascaded() );
	}

	@Test
	public void testGetConstraintForUnConstrainedProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Customer.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "orderList" );
		assertEquals(
				"There should be no constraint descriptors", 0, propertyDescriptor.getConstraintDescriptors().size()
		);
		assertTrue( "The property should be cascaded", propertyDescriptor.isCascaded() );
	}

	@Test
	public void testGetConstraintsForNonExistingProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertNull( "There should be no descriptor", beanDescriptor.getConstraintsForProperty( "foobar" ) );
	}

	/**
	 * @todo Is this corect or should we get a IllegalArgumentException
	 */
	@Test
	public void testGetConstraintsForNullProperty() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		assertNull( "There should be no descriptor", beanDescriptor.getConstraintsForProperty( null ) );
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedProperties() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertEquals( "There should be only one property", 1, constraintProperties.size() );
		boolean hasOrderNumber = false;
		for ( PropertyDescriptor pd : constraintProperties ) {
			hasOrderNumber |= pd.getPropertyName().equals( "orderNumber" );
		}
		assertTrue( "Wrong property", hasOrderNumber );
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedPropertiesImmutable() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Order.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		try {
			constraintProperties.add( null );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			constraintProperties.remove( constraintProperties.iterator().next() );
			fail( "Set should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}
	}

	/**
	 * HV-95
	 */
	@Test
	public void testGetConstrainedPropertiesForUnconstraintEntity() {
		Validator validator = TestUtil.getValidator();
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( UnconstraintEntity.class );
		Set<PropertyDescriptor> constraintProperties = beanDescriptor.getConstrainedProperties();
		assertEquals( "We should get the empty set.", 0, constraintProperties.size() );
	}
}
