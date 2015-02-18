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
package org.sonar.process;

import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * Process inter-communication to :
 * <ul>
 *   <li>share status of child process</li>
 *   <li>stop child process</li>
 * </ul>
 *
 * <p/>
 * It relies on files shared by both processes. Following alternatives were considered but not selected :
 * <ul>
 *   <li>JMX beans over RMI: network issues (mostly because of Java reverse-DNS) + requires to configure and open a new port</li>
 *   <li>simple socket protocol: same drawbacks are RMI connection</li>
 *   <li>java.lang.Process#destroy(): shutdown hooks are not executed on some OS (mostly MSWindows)</li>
 *   <li>execute OS-specific commands (for instance kill on *nix): OS-specific, so hell to support. Moreover how to get identify a process ?</li>
 * </ul>
 */
public class ProcessCommands {

  private final File sharedFile;
  private String processKey;
  private final ConcurrentMap<String, Long> communicationMap;
  private final static ChronicleMapBuilder<String, Long> BUILDER = ChronicleMapBuilder.of(String.class, Long.class);

  public ProcessCommands(File directory, String processKey) {
    this.processKey = processKey;
    if (!directory.isDirectory() || !directory.exists()) {
      throw new IllegalArgumentException("Not a valid directory: " + directory);
    }
    this.sharedFile = new File(directory, "sharedmemory");
    try {
      communicationMap = BUILDER.createPersistedTo(sharedFile);
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Fail to create file %s", sharedFile), e);
    }
  }

  // visible for tests
  ProcessCommands(File sharedFile) {
    this.sharedFile = sharedFile;
    try {
      communicationMap = BUILDER.createPersistedTo(sharedFile);
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Fail to create file %s", sharedFile), e);
    }
  }

  public void prepare() {
  }

  public void endWatch() {
  }

  public boolean isReady() {
    Long result = communicationMap.get(processKey + ".ready");
    return (result != null) && (result == 1L);
  }

  /**
   * To be executed by child process to declare that it's ready
   */
  public void setReady() {
    communicationMap.put(processKey + ".ready", 1L);
  }

  /**
   * To be executed by monitor process to ask for child process termination
   */
  public void askForStop() {
    communicationMap.put(processKey + ".stop", 1L);
  }

  public boolean askedForStop() {
    Long result = communicationMap.get(processKey + ".stop");
    return (result != null) && (result == 1L);
  }
}
