/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.core.service.spring;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openwms.core.domain.system.usermanagement.Role;
import org.openwms.core.service.RoleService;
import org.openwms.core.service.exception.ServiceRuntimeException;
import org.openwms.core.test.AbstractJpaSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * A RoleServiceTest.
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 */
@ContextConfiguration("classpath:/org/openwms/core/service/spring/Test-context.xml")
public class RoleServiceTest extends AbstractJpaSpringContextTests {

    @Autowired
    private RoleService srv;

    /**
     * Setting up some test data.
     */
    @Before
    public void onBefore() {
        entityManager.persist(new Role("ROLE_ADMIN"));
        entityManager.persist(new Role("ROLE_USER"));
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Test to remove a Role that does not exist.
     */
    @Test
    public final void testRemoveWithNotKnownEntity() {
        try {
            srv.remove((long) 4711);
            assertEquals("Expect to have 2 roles", 2, entityManager.createNamedQuery(Role.NQ_FIND_ALL).getResultList()
                    .size());
        } catch (Exception ex) {
            fail("Unexpected exception occurred:" + ex.getMessage());
        }
    }

    /**
     * Test to remove roles and call with a null argument.
     */
    @Test
    public final void testRemoveWithNull() {
        try {
            srv.remove(null);
            fail("Expected to catch an IllegalArgumentException when calling remove() with null");
        } catch (ServiceRuntimeException sre) {
            LOGGER.debug("OK: ServiceRuntimeException when calling remove with null argument");
            if (!sre.getMessage().equals("Roles to be removed must not be null")) {
                fail("IllegalArgumentException expected as root exception");
            }
        }
    }

    /**
     * Test to remove roles and call with a null argument.
     */
    @Test
    public final void testRemove() {
        try {
            Role persistedRole = (Role) entityManager.createNamedQuery(Role.NQ_FIND_BY_UNIQUE_QUERY)
                    .setParameter(1, "ROLE_ADMIN").getSingleResult();
            srv.remove(persistedRole.getId());
            assertEquals("Expect to have 1 Role left", 1, entityManager.createNamedQuery(Role.NQ_FIND_ALL)
                    .getResultList().size());
        } catch (Exception ex) {
            fail("Unexpected exception occurred:" + ex.getMessage());
        }
    }

    /**
     * Test to call save with null argument.
     */
    @Test
    public final void testSaveWithNull() {
        try {
            srv.save(null);
            fail("Should throw an exception when calling with null");
        } catch (ServiceRuntimeException sre) {
            LOGGER.debug("OK: Exception when try to call save with null argument:" + sre.getMessage());
        }
    }

    /**
     * Test to save a transient role.
     */
    @Test
    public final void testSaveTransient() {
        Role role = null;
        try {
            role = srv.save(new Role("ROLE_ANONYMOUS"));
        } catch (Exception e) {
            fail("Exception thrown during saving a role");
        }
        assertNotNull("Expected to return a role", role);
        assertFalse("Expect the role as persisted", role.isNew());
    }

    /**
     * Test to save a detached role.
     */
    @Test
    public final void testSaveDetached() {
        Role role = findRole("ROLE_ADMIN");
        Role roleSaved = null;
        role.setDescription("Test description");
        try {
            roleSaved = srv.save(role);
        } catch (Exception e) {
            fail("Exception thrown during saving a role");
        }
        assertNotNull("Expected to return a role", roleSaved);
        assertFalse("Expect the role as persisted", roleSaved.isNew());
        assertEquals("Expected that description was saved", "Test description", roleSaved.getDescription());
    }

    /**
     * Test findAll.
     */
    @Test
    public final void testFindAll() {
        assertEquals("2 Roles are expected", 2, srv.findAll().size());
    }

    private Role findRole(String roleName) {
        return (Role) entityManager.createNamedQuery(Role.NQ_FIND_BY_UNIQUE_QUERY).setParameter(1, roleName)
                .getSingleResult();
    }
}