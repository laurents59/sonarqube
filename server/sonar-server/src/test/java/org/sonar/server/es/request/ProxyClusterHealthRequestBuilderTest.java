/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.es.request;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.core.profiling.Profiling;
import org.sonar.server.es.EsTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ProxyClusterHealthRequestBuilderTest {

  @ClassRule
  public static EsTester esTester = new EsTester();

  @Before
  public void setUp() throws Exception {
    esTester.setProfilingLevel(Profiling.Level.NONE);
  }

  @Test
  public void state() {
    ClusterHealthRequestBuilder requestBuilder = esTester.client().prepareHealth();
    ClusterHealthResponse state = requestBuilder.get();
    assertThat(state.getStatus()).isEqualTo(ClusterHealthStatus.GREEN);
  }

  @Test
  public void to_string() {
    assertThat(esTester.client().prepareHealth("rules").toString()).isEqualTo("ES cluster health request on indices 'rules'");
    assertThat(esTester.client().prepareHealth().toString()).isEqualTo("ES cluster health request");
  }

  @Test
  public void with_profiling_full() {
    esTester.setProfilingLevel(Profiling.Level.FULL);

    ClusterHealthRequestBuilder requestBuilder = esTester.client().prepareHealth();
    ClusterHealthResponse state = requestBuilder.get();
    assertThat(state.getStatus()).isEqualTo(ClusterHealthStatus.GREEN);

    // TODO assert profiling
  }

  @Test
  public void get_with_string_timeout_is_not_yet_implemented() throws Exception {
    try {
      esTester.client().prepareHealth().get("1");
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class).hasMessage("Not yet implemented");
    }
  }

  @Test
  public void get_with_time_value_timeout_is_not_yet_implemented() throws Exception {
    try {
      esTester.client().prepareHealth().get(TimeValue.timeValueMinutes(1));
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class).hasMessage("Not yet implemented");
    }
  }

  @Test
  public void execute_should_throw_an_unsupported_operation_exception() throws Exception {
    try {
      esTester.client().prepareHealth().execute();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(UnsupportedOperationException.class).hasMessage("execute() should not be called as it's used for asynchronous");
    }
  }

}
