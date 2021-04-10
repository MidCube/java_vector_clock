/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, C.I. Griffiths
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.cl.fjava.messages;

import java.io.Serializable;
import java.util.Map;

public class ChangeNickMessage extends Message implements Serializable {
  private static final long serialVersionUID = 1L;

  public String name;

  public ChangeNickMessage(String name, String uid, Map<String, Integer> vectorClock) {
    super(uid, vectorClock);
    this.name = name;
  }

  public ChangeNickMessage(String name) {
    this(name, null, null);
  }
}
