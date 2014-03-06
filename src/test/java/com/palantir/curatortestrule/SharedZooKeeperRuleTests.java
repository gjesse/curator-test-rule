/*
 * Copyright 2014 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.curatortestrule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;

/**
 * Tests for {@link SharedZooKeeperRule}.
 *
 * @author juang
 */
public final class SharedZooKeeperRuleTests {

    @Test
    public void testConnectToServer() throws Exception {
        SharedZooKeeperRule rule1 = new SharedZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            CuratorFramework client = rule1.getClient();

            String path = "testpath";
            byte[] data = new byte[] { 1 };
            try {
                client.create().forPath(path, data);
                assertArrayEquals(data, client.getData().forPath(path));
                client.delete().forPath(path);
                assertNull(client.checkExists().forPath(path));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } finally {
            rule1.after();
        }
    }

    @Test
    public void testDoubleBindToSamePort() throws Exception {
        SharedZooKeeperRule rule1 = new SharedZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());
        SharedZooKeeperRule rule2 = new SharedZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            try {
                rule2.before();

                assertEquals(rule1.getCnxnFactory(), rule2.getCnxnFactory());
            } finally {
                rule2.after();
            }
        } finally {
            rule1.after();
        }
    }

    @Test
    public void testBindToDifferentPorts() throws Exception {
        SharedZooKeeperRule rule1 = new SharedZooKeeperRule("namespace1", 9500, new DefaultZooKeeperRuleConfig());
        SharedZooKeeperRule rule2 = new SharedZooKeeperRule("namespace1", 9501, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            try {
                rule2.before();

                assertNotEquals(rule1.getCnxnFactory(), rule2.getCnxnFactory());
            } finally {
                rule2.after();
            }
        } finally {
            rule1.after();
        }
    }

    @Test
    public void testBindToZero() throws Exception {
        SharedZooKeeperRule rule1 = new SharedZooKeeperRule("namespace1", 0, new DefaultZooKeeperRuleConfig());
        SharedZooKeeperRule rule2 = new SharedZooKeeperRule("namespace1", 0, new DefaultZooKeeperRuleConfig());

        try {
            rule1.before();

            try {
                rule2.before();

                assertNotEquals(0, rule1.getCnxnFactory().getLocalPort());
                assertNotEquals(0, rule2.getCnxnFactory().getLocalPort());

                assertEquals(rule1.getCnxnFactory(), rule2.getCnxnFactory());
            } finally {
                rule2.after();
            }
        } finally {
            rule1.after();
        }
    }
}
