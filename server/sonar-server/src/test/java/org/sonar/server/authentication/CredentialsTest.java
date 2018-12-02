/*
 * SonarQube
 * Copyright (C) 2009-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.authentication;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CredentialsTest {

  @Test
  public void password_can_be_empty_but_not_null() {
    Credentials underTest = new Credentials("foo", "");
    assertThat(underTest.getPassword()).isEqualTo("");

    underTest = new Credentials("foo", null);
    assertThat(underTest.getPassword()).isEqualTo("");

    underTest = new Credentials("foo", "   ");
    assertThat(underTest.getPassword()).isEqualTo("   ");

    underTest = new Credentials("foo", "bar");
    assertThat(underTest.getPassword()).isEqualTo("bar");
  }

  @Test
  public void test_equality() {
    assertThat(new Credentials("foo", "bar")).isEqualTo(new Credentials("foo", "bar"));
    assertThat(new Credentials("foo", "bar")).isNotEqualTo(new Credentials("foo", "baaaar"));
    assertThat(new Credentials("foo", "bar")).isNotEqualTo(new Credentials("foooooo", "bar"));
    assertThat(new Credentials("foo", "bar")).isNotEqualTo(new Credentials("foo", null));

    assertThat(new Credentials("foo", "bar").hashCode()).isEqualTo(new Credentials("foo", "bar").hashCode());
  }
}